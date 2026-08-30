package com.example.engine

import android.util.Log
import java.util.Random
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class PythonEnvironment(val parent: PythonEnvironment? = null) {
    val variables = mutableMapOf<String, Any?>()

    fun get(name: String): Any? {
        if (variables.containsKey(name)) return variables[name]
        return parent?.get(name)
    }

    fun set(name: String, value: Any?) {
        variables[name] = value
    }

    fun setGlobal(name: String, value: Any?) {
        if (parent != null) {
            parent.setGlobal(name, value)
        } else {
            variables[name] = value
        }
    }
}

class PythonClass(
    val name: String,
    val parentClassName: String?,
    val methods: MutableMap<String, PythonMethod> = mutableMapOf(),
    val classEnv: PythonEnvironment
) {
    fun instantiate(interpreter: PythonInterpreter, args: List<Any?> = emptyList(), kwargs: Map<String, Any?> = emptyMap()): PythonInstance {
        val instance = PythonInstance(this, interpreter)
        // If parent is a Kivy widget, create backing node
        if (isKivyWidget()) {
            val node = KivyWidgetNode(type = name)
            instance.backingWidget = node
            instance.fields["ids"] = node.ids
        }
        // Call __init__ if defined
        methods["__init__"]?.invoke(instance, args, kwargs)
        return instance
    }

    fun isKivyWidget(): Boolean {
        if (parentClassName in listOf("Widget", "BoxLayout", "GridLayout", "FloatLayout", "Screen", "ScreenManager", "Label", "Button")) {
            return true
        }
        if (name in listOf("BoxLayout", "GridLayout", "FloatLayout", "Screen", "ScreenManager", "Label", "Button", "Slider", "Switch", "ProgressBar", "Widget")) {
            return true
        }
        return false
    }
}

class PythonInstance(
    val klass: PythonClass,
    val interpreter: PythonInterpreter
) {
    val fields = mutableMapOf<String, Any?>()
    var backingWidget: KivyWidgetNode? = null

    fun getAttribute(attr: String): Any? {
        if (fields.containsKey(attr)) return fields[attr]
        if (attr == "ids" && backingWidget != null) return backingWidget!!.ids
        if (attr == "title" && fields.containsKey("title")) return fields["title"]
        if (attr == "manager") return fields["manager"]
        if (attr == "current" && fields.containsKey("current")) return fields["current"]

        if (backingWidget != null) {
            when (attr) {
                "text" -> return backingWidget!!.getString("text")
                "value" -> return backingWidget!!.getFloat("value")
                "active" -> return backingWidget!!.getBoolean("active")
                "score" -> return fields["score"] ?: 0
                "ball_x" -> return fields["ball_x"] ?: 0f
                "ball_y" -> return fields["ball_y"] ?: 0f
                "canvas" -> return backingWidget!!.canvas
            }
        }

        // Look up methods in class hierarchy
        klass.methods[attr]?.let { method ->
            return { args: List<Any?>, kwargs: Map<String, Any?> ->
                method.invoke(this, args, kwargs)
            }
        }

        return null
    }

    fun setAttribute(attr: String, value: Any?) {
        fields[attr] = value
        if (backingWidget != null) {
            when (attr) {
                "text" -> backingWidget!!.properties["text"] = value?.toString() ?: ""
                "value" -> {
                    val num = (value as? Number)?.toFloat() ?: (value?.toString()?.toFloatOrNull() ?: 0f)
                    backingWidget!!.properties["value"] = num
                }
                "active" -> {
                    val bool = when (value) {
                        is Boolean -> value
                        is Number -> value.toInt() != 0
                        else -> value?.toString()?.lowercase() == "true"
                    }
                    backingWidget!!.properties["active"] = bool
                }
                "color" -> {
                    if (value is KivyColor) backingWidget!!.properties["color"] = value
                    else if (value is List<*>) backingWidget!!.properties["color"] = KivyColor.fromList(value)
                }
                "background_color" -> {
                    if (value is KivyColor) backingWidget!!.properties["background_color"] = value
                    else if (value is List<*>) backingWidget!!.properties["background_color"] = KivyColor.fromList(value)
                }
            }
        }
    }
}

class PythonMethod(
    val name: String,
    val paramNames: List<String>,
    val bodyLines: List<String>,
    val declaringInterpreter: PythonInterpreter
) {
    fun invoke(instance: PythonInstance, args: List<Any?>, kwargs: Map<String, Any?> = emptyMap()): Any? {
        val localEnv = PythonEnvironment(declaringInterpreter.globalEnv)
        localEnv.set("self", instance)

        var argIdx = 0
        for (i in 1 until paramNames.size) {
            val param = paramNames[i]
            if (kwargs.containsKey(param)) {
                localEnv.set(param, kwargs[param])
            } else if (argIdx < args.size) {
                localEnv.set(param, args[argIdx++])
            }
        }

        return declaringInterpreter.executeBlock(bodyLines, localEnv, instance)
    }
}

class PythonInterpreter {
    val globalEnv = PythonEnvironment()
    val classes = mutableMapOf<String, PythonClass>()
    val stdoutLogs = mutableListOf<String>()
    val scheduledTasks = mutableListOf<ClockTask>()
    private val random = Random()
    private var taskIdCounter = 0L

    var runningAppInstance: PythonInstance? = null
    var rootWidget: KivyWidgetNode? = null
    var kvRules: Map<String, KvRule> = emptyMap()

    init {
        setupBuiltins()
    }

    private fun setupBuiltins() {
        // Built-in functions
        globalEnv.set("print", { args: List<Any?> ->
            val output = args.joinToString(" ") { it?.toString() ?: "None" }
            stdoutLogs.add(output)
            println("[Python STDOUT] $output")
        })

        globalEnv.set("len", { args: List<Any?> ->
            when (val item = args.getOrNull(0)) {
                is String -> item.length
                is List<*> -> item.size
                is Map<*, *> -> item.size
                is Collection<*> -> item.size
                else -> 0
            }
        })

        globalEnv.set("str", { args: List<Any?> -> args.getOrNull(0)?.toString() ?: "" })
        globalEnv.set("int", { args: List<Any?> ->
            when (val v = args.getOrNull(0)) {
                is Number -> v.toInt()
                is String -> v.toDoubleOrNull()?.toInt() ?: 0
                is Boolean -> if (v) 1 else 0
                else -> 0
            }
        })
        globalEnv.set("float", { args: List<Any?> ->
            when (val v = args.getOrNull(0)) {
                is Number -> v.toFloat()
                is String -> v.toFloatOrNull() ?: 0f
                else -> 0f
            }
        })
        globalEnv.set("abs", { args: List<Any?> ->
            when (val v = args.getOrNull(0)) {
                is Number -> abs(v.toDouble())
                else -> 0.0
            }
        })
        globalEnv.set("round", { args: List<Any?> ->
            when (val v = args.getOrNull(0)) {
                is Number -> v.toDouble().roundToInt()
                else -> 0
            }
        })

        // Math module
        val mathModule = mapOf<String, Any?>(
            "sin" to { args: List<Any?> -> sin((args.getOrNull(0) as? Number)?.toDouble() ?: 0.0) },
            "cos" to { args: List<Any?> -> cos((args.getOrNull(0) as? Number)?.toDouble() ?: 0.0) },
            "sqrt" to { args: List<Any?> -> sqrt((args.getOrNull(0) as? Number)?.toDouble() ?: 0.0) },
            "floor" to { args: List<Any?> -> floor((args.getOrNull(0) as? Number)?.toDouble() ?: 0.0) },
            "ceil" to { args: List<Any?> -> ceil((args.getOrNull(0) as? Number)?.toDouble() ?: 0.0) },
            "pi" to Math.PI,
            "pow" to { args: List<Any?> ->
                val base = (args.getOrNull(0) as? Number)?.toDouble() ?: 0.0
                val exp = (args.getOrNull(1) as? Number)?.toDouble() ?: 1.0
                base.pow(exp)
            }
        )
        globalEnv.set("math", mathModule)

        // Random module
        val randomModule = mapOf<String, Any?>(
            "choice" to { args: List<Any?> ->
                val list = args.getOrNull(0) as? List<*> ?: emptyList<Any>()
                if (list.isNotEmpty()) list[random.nextInt(list.size)] else null
            },
            "randint" to { args: List<Any?> ->
                val min = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                val max = (args.getOrNull(1) as? Number)?.toInt() ?: 100
                if (max > min) random.nextInt(max - min + 1) + min else min
            },
            "random" to { _: List<Any?> -> random.nextDouble() }
        )
        globalEnv.set("random", randomModule)

        // Clock module
        val clockModule = mapOf<String, Any?>(
            "schedule_interval" to { args: List<Any?> ->
                val callback = args.getOrNull(0)
                val interval = (args.getOrNull(1) as? Number)?.toFloat() ?: (1f / 60f)
                val taskId = ++taskIdCounter
                val task = ClockTask(
                    id = taskId,
                    callback = { dt ->
                        invokeCallable(callback, listOf(dt))
                    },
                    intervalSeconds = interval
                )
                scheduledTasks.add(task)
                taskId
            },
            "schedule_once" to { args: List<Any?> ->
                val callback = args.getOrNull(0)
                val delay = (args.getOrNull(1) as? Number)?.toFloat() ?: 0f
                val taskId = ++taskIdCounter
                val task = ClockTask(
                    id = taskId,
                    callback = { dt -> invokeCallable(callback, listOf(dt)) },
                    intervalSeconds = delay,
                    isOnce = true
                )
                scheduledTasks.add(task)
                taskId
            },
            "unschedule" to { args: List<Any?> ->
                val id = (args.getOrNull(0) as? Number)?.toLong()
                scheduledTasks.removeAll { it.id == id }
            }
        )
        globalEnv.set("Clock", clockModule)
    }

    fun invokeCallable(callable: Any?, args: List<Any?>): Any? {
        return when (callable) {
            is Function1<*, *> -> (callable as? Function1<List<Any?>, Any?>)?.invoke(args)
            is Function2<*, *, *> -> (callable as? Function2<Any?, Any?, Any?>)?.invoke(args.getOrNull(0), args.getOrNull(1))
            else -> null
        }
    }

    fun run(pythonCode: String, kvCode: String = ""): ExecutionResult {
        stdoutLogs.clear()
        scheduledTasks.clear()
        runningAppInstance = null
        rootWidget = null

        val startTime = System.currentTimeMillis()

        if (kvCode.isNotBlank()) {
            try {
                kvRules = KvParser.parse(kvCode)
                stdoutLogs.add("📦 Loaded ${kvRules.size} Kivy (.kv) rule(s)")
            } catch (e: Exception) {
                stdoutLogs.add("⚠️ KV Parse warning: ${e.message}")
            }
        }

        try {
            parseAndExecutePython(pythonCode)
            val duration = System.currentTimeMillis() - startTime
            val appTitle = (runningAppInstance?.fields?.get("title") as? String) ?: "Kivy Python 3 App"

            return ExecutionResult(
                isSuccess = true,
                logs = stdoutLogs.toList(),
                rootWidget = rootWidget,
                executionTimeMs = duration,
                appTitle = appTitle
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            val errorMsg = "Traceback (most recent call last):\n  ${e.message}"
            stdoutLogs.add(errorMsg)
            Log.e("PythonInterpreter", "Execution error", e)
            return ExecutionResult(
                isSuccess = false,
                logs = stdoutLogs.toList(),
                error = errorMsg,
                rootWidget = rootWidget,
                executionTimeMs = duration
            )
        }
    }

    private fun parseAndExecutePython(code: String) {
        val lines = code.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                i++
                continue
            }

            // Class definition
            if (trimmed.startsWith("class ")) {
                val classDef = trimmed.removePrefix("class ").trim()
                val className: String
                val parentName: String?

                if (classDef.contains("(") && classDef.contains(")")) {
                    className = classDef.substring(0, classDef.indexOf("(")).trim()
                    parentName = classDef.substring(classDef.indexOf("(") + 1, classDef.indexOf(")")).trim()
                } else {
                    className = classDef.removeSuffix(":").trim()
                    parentName = null
                }

                // Collect class body
                val classLines = mutableListOf<String>()
                val classIndent = line.takeWhile { it == ' ' || it == '\t' }.length
                i++

                while (i < lines.size) {
                    val bodyLine = lines[i]
                    if (bodyLine.trim().isEmpty()) {
                        classLines.add(bodyLine)
                        i++
                        continue
                    }
                    val bodyIndent = bodyLine.takeWhile { it == ' ' || it == '\t' }.length
                    if (bodyIndent > classIndent) {
                        classLines.add(bodyLine)
                        i++
                    } else {
                        break
                    }
                }

                // Parse methods within class
                val pClass = PythonClass(className, parentName, classEnv = PythonEnvironment(globalEnv))
                parseClassMethods(pClass, classLines)
                classes[className] = pClass
                globalEnv.set(className, pClass)
                continue
            }

            // Top-level instantiation / run: e.g. DashboardApp().run() or if __name__ == '__main__': ...
            if (trimmed.contains(".run()") || trimmed.contains("().run()")) {
                val appClassName = trimmed.substringBefore("().run()").substringBefore(".run()").trim().substringAfterLast(" ")
                val appClass = classes[appClassName]
                if (appClass != null) {
                    val appInst = appClass.instantiate(this)
                    runningAppInstance = appInst
                    val buildMethod = appClass.methods["build"]
                    if (buildMethod != null) {
                        val returned = buildMethod.invoke(appInst, emptyList())
                        if (returned is KivyWidgetNode) {
                            rootWidget = returned
                        } else if (returned is PythonInstance && returned.backingWidget != null) {
                            rootWidget = returned.backingWidget
                        }
                    }
                }
                i++
                continue
            }

            // Standalone top-level expression or assignment
            executeSingleStatement(trimmed, globalEnv, null)
            i++
        }
    }

    private fun parseClassMethods(pClass: PythonClass, lines: List<String>) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()
            if (trimmed.startsWith("def ")) {
                val defStr = trimmed.removePrefix("def ").trim()
                val methodName = defStr.substringBefore("(").trim()
                val paramsStr = defStr.substringAfter("(").substringBefore(")")
                val params = paramsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                val methodLines = mutableListOf<String>()
                val methodIndent = line.takeWhile { it == ' ' || it == '\t' }.length
                i++

                while (i < lines.size) {
                    val bodyLine = lines[i]
                    if (bodyLine.trim().isEmpty()) {
                        methodLines.add(bodyLine)
                        i++
                        continue
                    }
                    val bodyIndent = bodyLine.takeWhile { it == ' ' || it == '\t' }.length
                    if (bodyIndent > methodIndent) {
                        methodLines.add(bodyLine)
                        i++
                    } else {
                        break
                    }
                }

                pClass.methods[methodName] = PythonMethod(methodName, params, methodLines, this)
                continue
            }
            i++
        }
    }

    fun executeBlock(lines: List<String>, env: PythonEnvironment, self: PythonInstance?): Any? {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                i++
                continue
            }

            // Return statement
            if (trimmed.startsWith("return")) {
                val expr = trimmed.removePrefix("return").trim()
                return if (expr.isNotEmpty()) evaluateExpression(expr, env, self) else null
            }

            // If statement
            if (trimmed.startsWith("if ")) {
                val condExpr = trimmed.removePrefix("if ").removeSuffix(":").trim()
                val condVal = evaluateCondition(condExpr, env, self)

                val ifLines = mutableListOf<String>()
                val ifIndent = line.takeWhile { it == ' ' || it == '\t' }.length
                i++

                while (i < lines.size) {
                    val bLine = lines[i]
                    if (bLine.trim().isEmpty()) {
                        ifLines.add(bLine)
                        i++
                        continue
                    }
                    val bIndent = bLine.takeWhile { it == ' ' || it == '\t' }.length
                    if (bIndent > ifIndent) {
                        ifLines.add(bLine)
                        i++
                    } else {
                        break
                    }
                }

                if (condVal) {
                    val res = executeBlock(ifLines, env, self)
                    if (res != null) return res
                }
                continue
            }

            executeSingleStatement(trimmed, env, self)
            i++
        }
        return null
    }

    private fun executeSingleStatement(stmt: String, env: PythonEnvironment, self: PythonInstance?) {
        // Special case: widget canvas block 'with self.canvas:'
        if (stmt.startsWith("with self.canvas:") || stmt.startsWith("with self.canvas")) {
            return
        }

        // Print statement
        if (stmt.startsWith("print(") && stmt.endsWith(")")) {
            val inner = stmt.removeSurrounding("print(", ")").trim()
            val evaluated = evaluateExpression(inner, env, self)
            val logStr = evaluated?.toString() ?: "None"
            stdoutLogs.add(logStr)
            return
        }

        // Assignment: a = b or self.a = b or a += b
        if (stmt.contains("=") && !stmt.startsWith("==") && !stmt.contains("==") && !stmt.contains("<=") && !stmt.contains(">=")) {
            if (stmt.contains("+=")) {
                val parts = stmt.split("+=")
                val target = parts[0].trim()
                val current = evaluateExpression(target, env, self)
                val add = evaluateExpression(parts[1].trim(), env, self)
                val result = when {
                    current is Number && add is Number -> current.toDouble() + add.toDouble()
                    current is String || add is String -> "$current$add"
                    else -> add
                }
                assignValue(target, result, env, self)
                return
            }

            if (stmt.contains("-=")) {
                val parts = stmt.split("-=")
                val target = parts[0].trim()
                val current = (evaluateExpression(target, env, self) as? Number)?.toDouble() ?: 0.0
                val sub = (evaluateExpression(parts[1].trim(), env, self) as? Number)?.toDouble() ?: 0.0
                assignValue(target, current - sub, env, self)
                return
            }

            val parts = stmt.split("=", limit = 2)
            val target = parts[0].trim()
            val valueExpr = parts[1].trim()
            val value = evaluateExpression(valueExpr, env, self)
            assignValue(target, value, env, self)
            return
        }

        // Method / Function invocation: e.g. root.add_widget(header) or btn.bind(...) or self.game.jump()
        evaluateExpression(stmt, env, self)
    }

    private fun assignValue(target: String, value: Any?, env: PythonEnvironment, self: PythonInstance?) {
        if (target.startsWith("self.")) {
            val attr = target.removePrefix("self.").trim()
            self?.setAttribute(attr, value)
        } else if (target.contains(".")) {
            val parts = target.split(".", limit = 2)
            val obj = evaluateExpression(parts[0].trim(), env, self)
            val propName = parts[1].trim()
            if (obj is PythonInstance) {
                obj.setAttribute(propName, value)
            } else if (obj is KivyWidgetNode) {
                when (propName) {
                    "text" -> obj.properties["text"] = value?.toString() ?: ""
                    "value" -> obj.properties["value"] = (value as? Number)?.toFloat() ?: 0f
                    "active" -> obj.properties["active"] = (value == true || value?.toString()?.lowercase() == "true")
                    else -> obj.properties[propName] = value
                }
            }
        } else {
            env.set(target, value)
        }
    }

    fun evaluateExpression(expr: String, env: PythonEnvironment, self: PythonInstance?): Any? {
        val trimmed = expr.trim()
        if (trimmed.isEmpty()) return null

        // Literal primitives
        if (trimmed == "True") return true
        if (trimmed == "False") return false
        if (trimmed == "None") return null
        if (trimmed.toIntOrNull() != null) return trimmed.toInt()
        if (trimmed.toDoubleOrNull() != null) return trimmed.toDouble()

        // String literal: "..." or '...'
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length - 1).replace("\\n", "\n")
        }

        // f-string: f"Score: {self.score}"
        if (trimmed.startsWith("f\"") || trimmed.startsWith("f'")) {
            val template = trimmed.substring(2, trimmed.length - 1)
            return evaluateFString(template, env, self)
        }

        // List literal: [a, b, c]
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            val inner = trimmed.substring(1, trimmed.length - 1).trim()
            if (inner.isEmpty()) return mutableListOf<Any?>()
            val items = splitTopLevel(inner, ',')
            return items.map { evaluateExpression(it, env, self) }.toMutableList()
        }

        // Tuple literal: (a, b)
        if (trimmed.startsWith("(") && trimmed.endsWith(")") && !trimmed.contains(" ") && trimmed.contains(",")) {
            val inner = trimmed.substring(1, trimmed.length - 1).trim()
            val items = splitTopLevel(inner, ',')
            return items.map { evaluateExpression(it, env, self) }
        }

        // Dict literal: {'a': 1}
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            val inner = trimmed.substring(1, trimmed.length - 1).trim()
            val map = mutableMapOf<String, Any?>()
            if (inner.isNotEmpty()) {
                val pairs = splitTopLevel(inner, ',')
                for (pair in pairs) {
                    val kv = pair.split(":", limit = 2)
                    if (kv.size == 2) {
                        val k = evaluateExpression(kv[0], env, self)?.toString() ?: ""
                        val v = evaluateExpression(kv[1], env, self)
                        map[k] = v
                    }
                }
            }
            return map
        }

        // String concatenation with '+'
        if (trimmed.contains(" + ") && !trimmed.startsWith("eval(")) {
            val parts = trimmed.split(" + ")
            val evaluated = parts.map { evaluateExpression(it, env, self)?.toString() ?: "" }
            return evaluated.joinToString("")
        }

        // eval() invocation
        if (trimmed.startsWith("eval(") && trimmed.endsWith(")")) {
            val inner = trimmed.removeSurrounding("eval(", ")").trim()
            val exprToEval = evaluateExpression(inner, env, self)?.toString() ?: ""
            return evaluateMathExpression(exprToEval)
        }

        // Instantiation or Function Call: e.g. Label(text="...", font_size=16) or Clock.schedule_interval(...)
        if (trimmed.contains("(") && trimmed.endsWith(")")) {
            val calleeName = trimmed.substring(0, trimmed.indexOf("(")).trim()
            val argsStr = trimmed.substring(trimmed.indexOf("(") + 1, trimmed.length - 1).trim()
            return callFunctionOrConstructor(calleeName, argsStr, env, self)
        }

        // Attribute access: self.prop or root.prop or self.ids.name_input
        if (trimmed.contains(".")) {
            val parts = trimmed.split(".")
            var current: Any? = when (parts[0]) {
                "self" -> self
                "root" -> self?.backingWidget ?: self
                else -> env.get(parts[0])
            }

            for (p in 1 until parts.size) {
                val prop = parts[p]
                current = when (current) {
                    is PythonInstance -> current.getAttribute(prop)
                    is KivyWidgetNode -> {
                        when (prop) {
                            "text" -> current.getString("text")
                            "value" -> current.getFloat("value")
                            "active" -> current.getBoolean("active")
                            "ids" -> current.ids
                            else -> current.properties[prop] ?: current.findWidgetById(prop)
                        }
                    }
                    is Map<*, *> -> (current as Map<String, Any?>)[prop]
                    else -> null
                }
            }
            return current
        }

        // Variable lookup
        if (trimmed == "self") return self
        return env.get(trimmed)
    }

    private fun evaluateFString(template: String, env: PythonEnvironment, self: PythonInstance?): String {
        val result = StringBuilder()
        var i = 0
        while (i < template.length) {
            if (template[i] == '{') {
                val end = template.indexOf('}', i)
                if (end != -1) {
                    val expr = template.substring(i + 1, end).trim()
                    val evalVal = evaluateExpression(expr, env, self)
                    result.append(evalVal?.toString() ?: "None")
                    i = end + 1
                    continue
                }
            }
            result.append(template[i])
            i++
        }
        return result.toString().replace("\\n", "\n")
    }

    private fun evaluateCondition(expr: String, env: PythonEnvironment, self: PythonInstance?): Boolean {
        val trimmed = expr.trim()
        if (trimmed.startsWith("not ")) {
            return !evaluateCondition(trimmed.removePrefix("not ").trim(), env, self)
        }
        if (trimmed.contains("==")) {
            val parts = trimmed.split("==")
            val left = evaluateExpression(parts[0].trim(), env, self)
            val right = evaluateExpression(parts[1].trim(), env, self)
            return left == right || left.toString() == right.toString()
        }
        if (trimmed.contains("!=")) {
            val parts = trimmed.split("!=")
            val left = evaluateExpression(parts[0].trim(), env, self)
            val right = evaluateExpression(parts[1].trim(), env, self)
            return left != right && left.toString() != right.toString()
        }
        if (trimmed.contains("<=")) {
            val parts = trimmed.split("<=")
            val left = (evaluateExpression(parts[0].trim(), env, self) as? Number)?.toDouble() ?: 0.0
            val right = (evaluateExpression(parts[1].trim(), env, self) as? Number)?.toDouble() ?: 0.0
            return left <= right
        }
        if (trimmed.contains(">=")) {
            val parts = trimmed.split(">=")
            val left = (evaluateExpression(parts[0].trim(), env, self) as? Number)?.toDouble() ?: 0.0
            val right = (evaluateExpression(parts[1].trim(), env, self) as? Number)?.toDouble() ?: 0.0
            return left >= right
        }
        if (trimmed.contains("<")) {
            val parts = trimmed.split("<")
            val left = (evaluateExpression(parts[0].trim(), env, self) as? Number)?.toDouble() ?: 0.0
            val right = (evaluateExpression(parts[1].trim(), env, self) as? Number)?.toDouble() ?: 0.0
            return left < right
        }
        if (trimmed.contains(">")) {
            val parts = trimmed.split(">")
            val left = (evaluateExpression(parts[0].trim(), env, self) as? Number)?.toDouble() ?: 0.0
            val right = (evaluateExpression(parts[1].trim(), env, self) as? Number)?.toDouble() ?: 0.0
            return left > right
        }

        val v = evaluateExpression(trimmed, env, self)
        return when (v) {
            is Boolean -> v
            is Number -> v.toDouble() != 0.0
            is String -> v.isNotEmpty()
            null -> false
            else -> true
        }
    }

    private fun callFunctionOrConstructor(name: String, argsStr: String, env: PythonEnvironment, self: PythonInstance?): Any? {
        val (posArgs, kwArgs) = parseArguments(argsStr, env, self)

        // Built-in Kivy Widget Constructors
        if (name in listOf("BoxLayout", "GridLayout", "FloatLayout", "AnchorLayout", "Button", "Label", "TextInput", "Slider", "Switch", "ProgressBar", "Image", "ScreenManager", "Screen", "Widget")) {
            val node = KivyWidgetNode(type = name)
            kwArgs.forEach { (k, v) ->
                when (k) {
                    "text" -> node.properties["text"] = v?.toString() ?: ""
                    "font_size" -> node.properties["font_size"] = (v as? Number)?.toInt() ?: 16
                    "orientation" -> node.properties["orientation"] = v?.toString() ?: "horizontal"
                    "spacing" -> node.properties["spacing"] = (v as? Number)?.toInt() ?: 8
                    "padding" -> node.properties["padding"] = (v as? Number)?.toInt() ?: 12
                    "cols" -> node.properties["cols"] = (v as? Number)?.toInt() ?: 2
                    "rows" -> node.properties["rows"] = (v as? Number)?.toInt() ?: 2
                    "active" -> node.properties["active"] = (v == true || v?.toString()?.lowercase() == "true")
                    "min" -> node.properties["min"] = (v as? Number)?.toFloat() ?: 0f
                    "max" -> node.properties["max"] = (v as? Number)?.toFloat() ?: 100f
                    "value" -> node.properties["value"] = (v as? Number)?.toFloat() ?: 0f
                    "step" -> node.properties["step"] = (v as? Number)?.toFloat() ?: 1f
                    "size_hint" -> node.properties["size_hint"] = v
                    "color" -> {
                        if (v is List<*>) node.properties["color"] = KivyColor.fromList(v)
                        else if (v is KivyColor) node.properties["color"] = v
                    }
                    "background_color" -> {
                        if (v is List<*>) node.properties["background_color"] = KivyColor.fromList(v)
                        else if (v is KivyColor) node.properties["background_color"] = v
                    }
                    "name" -> node.properties["name"] = v?.toString() ?: ""
                    else -> node.properties[k] = v
                }
            }

            // Check if there are KV rules for this type
            kvRules[name]?.let { rule ->
                KvParser.applyRuleToWidget(node, rule)
            }

            return node
        }

        // Canvas Graphics Instructions
        if (name == "Color") {
            val col = if (posArgs.size >= 3) {
                KivyColor(
                    (posArgs[0] as? Number)?.toFloat() ?: 1f,
                    (posArgs[1] as? Number)?.toFloat() ?: 1f,
                    (posArgs[2] as? Number)?.toFloat() ?: 1f,
                    (posArgs.getOrNull(3) as? Number)?.toFloat() ?: 1f
                )
            } else KivyColor(1f, 1f, 1f, 1f)
            self?.backingWidget?.canvas?.add(KivyCanvasInstruction.SetColor(col))
            return col
        }

        if (name == "Ellipse") {
            val pos = kwArgs["pos"] as? List<*>
            val size = kwArgs["size"] as? List<*>
            val x = (pos?.getOrNull(0) as? Number)?.toFloat() ?: 0f
            val y = (pos?.getOrNull(1) as? Number)?.toFloat() ?: 0f
            val w = (size?.getOrNull(0) as? Number)?.toFloat() ?: 40f
            val h = (size?.getOrNull(1) as? Number)?.toFloat() ?: 40f
            val inst = KivyCanvasInstruction.DrawEllipse(x, y, w, h)
            self?.backingWidget?.canvas?.add(inst)
            return inst
        }

        if (name == "Rectangle") {
            val pos = kwArgs["pos"] as? List<*>
            val size = kwArgs["size"] as? List<*>
            val x = (pos?.getOrNull(0) as? Number)?.toFloat() ?: 0f
            val y = (pos?.getOrNull(1) as? Number)?.toFloat() ?: 0f
            val w = (size?.getOrNull(0) as? Number)?.toFloat() ?: 100f
            val h = (size?.getOrNull(1) as? Number)?.toFloat() ?: 20f
            val inst = KivyCanvasInstruction.DrawRectangle(x, y, w, h)
            self?.backingWidget?.canvas?.add(inst)
            return inst
        }

        // Custom Defined Class instantiation
        classes[name]?.let { customClass ->
            val inst = customClass.instantiate(this, posArgs, kwArgs)
            kvRules[name]?.let { rule ->
                if (inst.backingWidget != null) {
                    KvParser.applyRuleToWidget(inst.backingWidget!!, rule)
                }
            }
            return inst
        }

        // Method calls on objects: e.g. root.add_widget(header), btn.bind(on_press=...)
        if (name.contains(".")) {
            val parts = name.split(".")
            val objName = parts[0]
            val methodName = parts[1]

            val targetObj = when (objName) {
                "self" -> self
                "root" -> self?.backingWidget ?: self
                "Clock" -> globalEnv.get("Clock")
                else -> env.get(objName)
            }

            if (targetObj is KivyWidgetNode) {
                if (methodName == "add_widget") {
                    val child = posArgs.getOrNull(0)
                    if (child is KivyWidgetNode) {
                        targetObj.addWidget(child)
                    } else if (child is PythonInstance && child.backingWidget != null) {
                        targetObj.addWidget(child.backingWidget!!)
                    }
                    return null
                }
                if (methodName == "bind") {
                    kwArgs.forEach { (eventKey, handler) ->
                        targetObj.eventHandlers[eventKey] = { args ->
                            invokeCallable(handler, args)
                        }
                    }
                    return null
                }
            }

            if (targetObj is PythonInstance) {
                targetObj.klass.methods[methodName]?.let { method ->
                    return method.invoke(targetObj, posArgs, kwArgs)
                }
            }

            if (targetObj is Map<*, *>) {
                val func = (targetObj as Map<String, Any?>)[methodName]
                if (func is Function1<*, *>) {
                    return (func as Function1<List<Any?>, Any?>).invoke(posArgs)
                }
            }
        }

        // Global functions (print, len, etc.)
        val globalFunc = env.get(name)
        if (globalFunc is Function1<*, *>) {
            return (globalFunc as Function1<List<Any?>, Any?>).invoke(posArgs)
        }

        return null
    }

    private fun parseArguments(argsStr: String, env: PythonEnvironment, self: PythonInstance?): Pair<List<Any?>, Map<String, Any?>> {
        if (argsStr.isEmpty()) return Pair(emptyList(), emptyMap())

        val posArgs = mutableListOf<Any?>()
        val kwArgs = mutableMapOf<String, Any?>()

        val tokens = splitTopLevel(argsStr, ',')
        for (token in tokens) {
            val trimmed = token.trim()
            if (trimmed.contains("=") && !trimmed.startsWith("==") && !trimmed.contains("==")) {
                val kv = trimmed.split("=", limit = 2)
                val k = kv[0].trim()
                val v = evaluateExpression(kv[1].trim(), env, self)
                kwArgs[k] = v
            } else {
                posArgs.add(evaluateExpression(trimmed, env, self))
            }
        }

        return Pair(posArgs, kwArgs)
    }

    private fun splitTopLevel(str: String, delimiter: Char): List<String> {
        val results = mutableListOf<String>()
        var depth = 0
        val current = StringBuilder()

        for (ch in str) {
            when (ch) {
                '(', '[', '{' -> depth++
                ')', ']', '}' -> depth--
            }

            if (ch == delimiter && depth == 0) {
                results.add(current.toString())
                current.clear()
            } else {
                current.append(ch)
            }
        }
        if (current.isNotEmpty()) {
            results.add(current.toString())
        }
        return results
    }

    private fun evaluateMathExpression(expr: String): Any {
        try {
            val clean = expr.replace(" ", "")
            // Handle basic expressions
            return SimpleMathEvaluator.eval(clean)
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }
}

object SimpleMathEvaluator {
    fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < str.length) str[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm()
                        eat('-'.code) -> x -= parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor()
                        eat('/'.code) -> x /= parseFactor()
                        eat('%'.code) -> x %= parseFactor()
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                    while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                    x = str.substring(startPos, pos).toDouble()
                } else if (ch in 'a'.code..'z'.code) {
                    while (ch in 'a'.code..'z'.code) nextChar()
                    val func = str.substring(startPos, pos)
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> sqrt(x)
                        "sin" -> sin(Math.toRadians(x))
                        "cos" -> cos(Math.toRadians(x))
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                if (eat('^'.code)) x = x.pow(parseFactor())
                return x
            }
        }.parse()
    }
}
