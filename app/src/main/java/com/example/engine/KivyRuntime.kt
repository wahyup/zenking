package com.example.engine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class KivyRuntime {
    private val interpreter = PythonInterpreter()
    private val _uiState = MutableStateFlow<KivyUiState>(KivyUiState.Idle)
    val uiState: StateFlow<KivyUiState> = _uiState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _fps = MutableStateFlow(60f)
    val fps: StateFlow<Float> = _fps.asStateFlow()

    private val _renderDurationMs = MutableStateFlow(0L)
    val renderDurationMs: StateFlow<Long> = _renderDurationMs.asStateFlow()

    private var clockJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    var currentPythonCode: String = ""
    var currentKvCode: String = ""

    fun executeCode(pythonCode: String, kvCode: String = "") {
        currentPythonCode = pythonCode
        currentKvCode = kvCode
        stopClock()

        _uiState.value = KivyUiState.Loading

        val start = System.currentTimeMillis()
        val result = interpreter.run(pythonCode, kvCode)
        val duration = System.currentTimeMillis() - start
        _renderDurationMs.value = duration

        _logs.value = result.logs

        if (result.isSuccess && result.rootWidget != null) {
            _uiState.value = KivyUiState.Running(
                appTitle = result.appTitle,
                rootWidget = result.rootWidget,
                executionTimeMs = duration
            )
            startClock()
        } else if (!result.isSuccess) {
            _uiState.value = KivyUiState.Error(
                errorMessage = result.error ?: "Unknown execution error",
                logs = result.logs
            )
        } else {
            // Success without UI (e.g. pure python script)
            _uiState.value = KivyUiState.ScriptCompleted(
                logs = result.logs,
                executionTimeMs = duration
            )
        }
    }

    private fun startClock() {
        clockJob?.cancel()
        if (interpreter.scheduledTasks.isEmpty()) return

        clockJob = coroutineScope.launch {
            var lastTime = System.nanoTime()
            while (isActive) {
                val now = System.nanoTime()
                val dt = (now - lastTime) / 1_000_000_000f
                lastTime = now

                if (dt > 0) {
                    _fps.value = (1f / dt).coerceIn(10f, 60f)
                }

                val tasksToRun = interpreter.scheduledTasks.filter { it.isActive }
                for (task in tasksToRun) {
                    task.timeUntilNextRun -= dt
                    if (task.timeUntilNextRun <= 0) {
                        try {
                            task.callback(dt)
                        } catch (e: Exception) {
                            println("[Clock Error] ${e.message}")
                        }
                        if (task.isOnce) {
                            task.isActive = false
                        } else {
                            task.timeUntilNextRun = task.intervalSeconds
                        }
                    }
                }
                interpreter.scheduledTasks.removeAll { !it.isActive }

                // Trigger recomposition by notifying UI state if still running
                val current = _uiState.value
                if (current is KivyUiState.Running) {
                    _uiState.value = current.copy(recomposeTrigger = current.recomposeTrigger + 1)
                }

                delay(16) // ~60fps
            }
        }
    }

    fun stopClock() {
        clockJob?.cancel()
        clockJob = null
    }

    fun triggerEvent(widget: KivyWidgetNode, eventName: String, args: List<Any?> = emptyList()) {
        // Direct event handler
        widget.eventHandlers[eventName]?.invoke(args)

        // KV Script handler
        widget.kvEventScripts[eventName]?.let { script ->
            executeKvScript(script, widget)
        }

        // Screen navigation
        if (widget.type == "Screen" || (widget.parent != null && widget.parent?.type == "ScreenManager")) {
            // Handled
        }

        // Notify state update
        val current = _uiState.value
        if (current is KivyUiState.Running) {
            _uiState.value = current.copy(recomposeTrigger = current.recomposeTrigger + 1)
        }
    }

    private fun executeKvScript(script: String, sourceWidget: KivyWidgetNode) {
        val trimmed = script.trim()
        val app = interpreter.runningAppInstance
        val root = interpreter.rootWidget

        try {
            if (trimmed.startsWith("root.") && trimmed.contains("(") && trimmed.endsWith(")")) {
                val methodName = trimmed.removePrefix("root.").substringBefore("(").trim()
                if (app != null) {
                    app.klass.methods[methodName]?.invoke(app, emptyList())
                }
            } else if (trimmed.startsWith("app.") && trimmed.contains("(") && trimmed.endsWith(")")) {
                val methodName = trimmed.removePrefix("app.").substringBefore("(").trim()
                if (app != null) {
                    app.klass.methods[methodName]?.invoke(app, emptyList())
                }
            } else if (trimmed.contains("=")) {
                val parts = trimmed.split("=", limit = 2)
                val lhs = parts[0].trim()
                val rhs = parts[1].trim()

                var targetWidget = sourceWidget
                var targetProp = lhs

                if (lhs.startsWith("root.ids.")) {
                    val idAndProp = lhs.removePrefix("root.ids.").split(".")
                    val widgetId = idAndProp[0]
                    targetProp = idAndProp.getOrElse(1) { "text" }
                    root?.findWidgetById(widgetId)?.let { targetWidget = it }
                }

                val rhsVal: Any? = when {
                    rhs == "self.value" -> sourceWidget.getFloat("value")
                    rhs == "self.text" -> sourceWidget.getString("text")
                    rhs == "self.active" -> sourceWidget.getBoolean("active")
                    else -> rhs.removeSurrounding("'", "'").removeSurrounding("\"", "\"")
                }

                targetWidget.properties[targetProp] = rhsVal
            }
        } catch (e: Exception) {
            println("[KV Action Error] ${e.message}")
        }
    }

    fun executeReplExpression(expression: String): String {
        return try {
            val result = interpreter.evaluateExpression(expression, interpreter.globalEnv, interpreter.runningAppInstance)
            val out = result?.toString() ?: "None"
            _logs.value = _logs.value + ">>> $expression" + out
            out
        } catch (e: Exception) {
            val err = "Error: ${e.message}"
            _logs.value = _logs.value + ">>> $expression" + err
            err
        }
    }

    fun navigateScreen(screenManager: KivyWidgetNode, targetScreenName: String) {
        screenManager.properties["current"] = targetScreenName
        val current = _uiState.value
        if (current is KivyUiState.Running) {
            _uiState.value = current.copy(recomposeTrigger = current.recomposeTrigger + 1)
        }
    }
}

sealed class KivyUiState {
    data object Idle : KivyUiState()
    data object Loading : KivyUiState()
    data class Running(
        val appTitle: String,
        val rootWidget: KivyWidgetNode,
        val executionTimeMs: Long,
        val recomposeTrigger: Long = 0
    ) : KivyUiState()
    data class ScriptCompleted(
        val logs: List<String>,
        val executionTimeMs: Long
    ) : KivyUiState()
    data class Error(
        val errorMessage: String,
        val logs: List<String>
    ) : KivyUiState()
}
