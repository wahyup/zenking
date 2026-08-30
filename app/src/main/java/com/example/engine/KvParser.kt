package com.example.engine

import java.util.Stack

class KvRule(
    val name: String,
    val parentType: String = "Widget",
    val rootNode: KvNode
)

data class KvNode(
    val type: String,
    val id: String = "",
    val properties: MutableMap<String, String> = mutableMapOf(),
    val eventHandlers: MutableMap<String, String> = mutableMapOf(),
    val children: MutableList<KvNode> = mutableListOf(),
    val indentLevel: Int = 0
)

object KvParser {

    fun parse(kvContent: String): Map<String, KvRule> {
        val rules = mutableMapOf<String, KvRule>()
        val lines = kvContent.lines()
        
        var currentRuleName: String? = null
        var currentParentType = "Widget"
        val nodeStack = Stack<KvNode>()
        var rootNodeOfRule: KvNode? = null

        for (rawLine in lines) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

            val indent = rawLine.takeWhile { it == ' ' || it == '\t' }.length

            // Check for rule definition: <WidgetName>: or <WidgetName@Parent>:
            if (trimmed.startsWith("<") && trimmed.contains(">:") || (trimmed.endsWith(":") && !trimmed.contains(" ") && currentRuleName == null)) {
                // Save previous rule if exists
                if (currentRuleName != null && rootNodeOfRule != null) {
                    rules[currentRuleName] = KvRule(currentRuleName, currentParentType, rootNodeOfRule)
                }

                val header = trimmed.removePrefix("<").removeSuffix(":").removeSuffix(">")
                if (header.contains("@")) {
                    val parts = header.split("@")
                    currentRuleName = parts[0].trim()
                    currentParentType = parts[1].trim()
                } else {
                    currentRuleName = header.trim()
                    currentParentType = "Widget"
                }

                rootNodeOfRule = KvNode(type = currentParentType, indentLevel = indent)
                nodeStack.clear()
                nodeStack.push(rootNodeOfRule)
                continue
            }

            // Check if this is a widget instantiation inside a rule or root
            if (trimmed.endsWith(":") && !trimmed.contains("on_") && !trimmed.startsWith("canvas")) {
                val widgetType = trimmed.removeSuffix(":").trim()
                val newNode = KvNode(type = widgetType, indentLevel = indent)

                while (nodeStack.isNotEmpty() && nodeStack.peek().indentLevel >= indent && nodeStack.size > 1) {
                    nodeStack.pop()
                }

                if (nodeStack.isNotEmpty()) {
                    nodeStack.peek().children.add(newNode)
                } else {
                    rootNodeOfRule = newNode
                }
                nodeStack.push(newNode)
                continue
            }

            // Check for property or event assignment: key: value
            if (trimmed.contains(":")) {
                val colonIdx = trimmed.indexOf(":")
                val key = trimmed.substring(0, colonIdx).trim()
                val value = trimmed.substring(colonIdx + 1).trim().removeSurrounding("'", "'").removeSurrounding("\"", "\"")

                if (nodeStack.isNotEmpty()) {
                    val targetNode = nodeStack.peek()
                    if (key == "id") {
                        // Special id assignment
                        val idVal = value.trim()
                        // Recreate node with ID or update
                        targetNode.properties["id"] = idVal
                    } else if (key.startsWith("on_")) {
                        targetNode.eventHandlers[key] = value
                    } else {
                        targetNode.properties[key] = value
                    }
                }
            }
        }

        if (currentRuleName != null && rootNodeOfRule != null) {
            rules[currentRuleName] = KvRule(currentRuleName, currentParentType, rootNodeOfRule)
        }

        return rules
    }

    fun applyRuleToWidget(widget: KivyWidgetNode, rule: KvRule, rootWidget: KivyWidgetNode = widget) {
        applyNodeToWidget(widget, rule.rootNode, rootWidget)
    }

    private fun applyNodeToWidget(widget: KivyWidgetNode, node: KvNode, rootWidget: KivyWidgetNode) {
        // Copy properties
        node.properties.forEach { (k, v) ->
            when (k) {
                "text" -> widget.properties["text"] = v
                "font_size" -> widget.properties["font_size"] = v.toIntOrNull() ?: 16
                "orientation" -> widget.properties["orientation"] = v
                "spacing" -> widget.properties["spacing"] = v.toIntOrNull() ?: 8
                "padding" -> widget.properties["padding"] = v.toIntOrNull() ?: 12
                "cols" -> widget.properties["cols"] = v.toIntOrNull() ?: 2
                "rows" -> widget.properties["rows"] = v.toIntOrNull() ?: 2
                "active" -> widget.properties["active"] = (v.lowercase() == "true")
                "min" -> widget.properties["min"] = v.toFloatOrNull() ?: 0f
                "max" -> widget.properties["max"] = v.toFloatOrNull() ?: 100f
                "value" -> widget.properties["value"] = v.toFloatOrNull() ?: 0f
                "step" -> widget.properties["step"] = v.toFloatOrNull() ?: 1f
                "hint_text" -> widget.properties["hint_text"] = v
                "multiline" -> widget.properties["multiline"] = (v.lowercase() == "true")
                "bold" -> widget.properties["bold"] = (v.lowercase() == "true")
                "size_hint" -> {
                    val cleaned = v.removeSurrounding("(", ")").removeSurrounding("[", "]")
                    val parts = cleaned.split(",")
                    if (parts.size >= 2) {
                        val sx = parts[0].trim().toFloatOrNull() ?: 1f
                        val sy = parts[1].trim().toFloatOrNull() ?: 1f
                        widget.properties["size_hint"] = listOf(sx, sy)
                    }
                }
                "color", "background_color" -> {
                    val cleaned = v.removeSurrounding("(", ")").removeSurrounding("[", "]")
                    val parts = cleaned.split(",")
                    if (parts.size >= 3) {
                        val r = parts[0].trim().toFloatOrNull() ?: 1f
                        val g = parts[1].trim().toFloatOrNull() ?: 1f
                        val b = parts[2].trim().toFloatOrNull() ?: 1f
                        val a = parts.getOrNull(3)?.trim()?.toFloatOrNull() ?: 1f
                        widget.properties[k] = KivyColor(r, g, b, a)
                    }
                }
                "id" -> {
                    widget.properties["id"] = v
                }
                else -> widget.properties[k] = v
            }
        }

        // Attach event scripts
        node.eventHandlers.forEach { (ev, script) ->
            widget.kvEventScripts[ev] = script
        }

        // Construct child nodes if any
        node.children.forEach { childNode ->
            val childWidget = KivyWidgetNode(
                id = childNode.properties["id"] ?: "",
                type = childNode.type
            )
            applyNodeToWidget(childWidget, childNode, rootWidget)
            widget.addWidget(childWidget)
        }
    }
}
