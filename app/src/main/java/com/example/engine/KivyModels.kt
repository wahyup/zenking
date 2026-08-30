package com.example.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class KivyColor(
    val r: Float = 1f,
    val g: Float = 1f,
    val b: Float = 1f,
    val a: Float = 1f
) {
    fun toComposeColor(): Color {
        return Color(
            red = r.coerceIn(0f, 1f),
            green = g.coerceIn(0f, 1f),
            blue = b.coerceIn(0f, 1f),
            alpha = a.coerceIn(0f, 1f)
        )
    }

    companion object {
        fun fromList(list: List<*>): KivyColor {
            val r = (list.getOrNull(0) as? Number)?.toFloat() ?: 1f
            val g = (list.getOrNull(1) as? Number)?.toFloat() ?: 1f
            val b = (list.getOrNull(2) as? Number)?.toFloat() ?: 1f
            val a = (list.getOrNull(3) as? Number)?.toFloat() ?: 1f
            return KivyColor(r, g, b, a)
        }
    }
}

sealed class KivyCanvasInstruction {
    data class SetColor(val color: KivyColor) : KivyCanvasInstruction()
    data class DrawRectangle(
        var x: Float = 0f,
        var y: Float = 0f,
        var width: Float = 100f,
        var height: Float = 100f,
        var cornerRadius: Float = 0f
    ) : KivyCanvasInstruction()
    data class DrawEllipse(
        var x: Float = 0f,
        var y: Float = 0f,
        var width: Float = 50f,
        var height: Float = 50f
    ) : KivyCanvasInstruction()
    data class DrawLine(
        val points: MutableList<Offset> = mutableListOf(),
        var width: Float = 3f,
        val color: KivyColor = KivyColor(0.2f, 0.8f, 1f, 1f)
    ) : KivyCanvasInstruction()
}

class KivyCanvasGroup {
    val instructions = mutableListOf<KivyCanvasInstruction>()

    fun clear() {
        instructions.clear()
    }

    fun add(instruction: KivyCanvasInstruction) {
        instructions.add(instruction)
    }
}

data class DrawingPoint(val x: Float, val y: Float)
data class DrawingStroke(
    val points: MutableList<DrawingPoint> = mutableListOf(),
    val color: KivyColor = KivyColor(0.1f, 0.9f, 0.6f, 1f),
    val strokeWidth: Float = 4f
)

class KivyWidgetNode(
    val id: String = "",
    val type: String = "Widget",
    val properties: MutableMap<String, Any?> = mutableMapOf(),
    val children: MutableList<KivyWidgetNode> = mutableListOf(),
    var parent: KivyWidgetNode? = null
) {
    val canvas: KivyCanvasGroup = KivyCanvasGroup()
    val canvasBefore: KivyCanvasGroup = KivyCanvasGroup()
    val canvasAfter: KivyCanvasGroup = KivyCanvasGroup()

    val eventHandlers = mutableMapOf<String, (args: List<Any?>) -> Unit>()
    val kvEventScripts = mutableMapOf<String, String>()
    val ids = mutableMapOf<String, KivyWidgetNode>()
    val drawingStrokes = mutableListOf<DrawingStroke>()

    // Helpers to get typed properties
    fun getString(key: String, default: String = ""): String {
        return properties[key]?.toString() ?: default
    }

    fun getFloat(key: String, default: Float = 0f): Float {
        return when (val v = properties[key]) {
            is Number -> v.toFloat()
            is String -> v.toFloatOrNull() ?: default
            else -> default
        }
    }

    fun getInt(key: String, default: Int = 0): Int {
        return when (val v = properties[key]) {
            is Number -> v.toInt()
            is String -> v.toIntOrNull() ?: default
            else -> default
        }
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return when (val v = properties[key]) {
            is Boolean -> v
            is String -> v.lowercase() == "true" || v == "1"
            is Number -> v.toInt() != 0
            else -> default
        }
    }

    fun getColor(key: String, default: KivyColor = KivyColor(1f, 1f, 1f, 1f)): KivyColor {
        return when (val v = properties[key]) {
            is KivyColor -> v
            is List<*> -> KivyColor.fromList(v)
            else -> default
        }
    }

    fun getSizeHint(): Pair<Float?, Float?> {
        val sh = properties["size_hint"]
        if (sh is List<*> && sh.size >= 2) {
            val x = (sh[0] as? Number)?.toFloat()
            val y = (sh[1] as? Number)?.toFloat()
            return Pair(x, y)
        }
        val sx = properties["size_hint_x"] as? Number
        val sy = properties["size_hint_y"] as? Number
        return Pair(sx?.toFloat(), sy?.toFloat())
    }

    fun addWidget(child: KivyWidgetNode) {
        child.parent = this
        children.add(child)
        if (child.id.isNotEmpty()) {
            ids[child.id] = child
            // Propagate up to root
            var curr: KivyWidgetNode? = this
            while (curr != null) {
                curr.ids[child.id] = child
                curr = curr.parent
            }
        }
    }

    fun removeWidget(child: KivyWidgetNode) {
        children.remove(child)
        child.parent = null
    }

    fun clearWidgets() {
        children.forEach { it.parent = null }
        children.clear()
    }

    fun findWidgetById(targetId: String): KivyWidgetNode? {
        if (id == targetId) return this
        ids[targetId]?.let { return it }
        for (child in children) {
            val found = child.findWidgetById(targetId)
            if (found != null) return found
        }
        return null
    }
}

data class ClockTask(
    val id: Long,
    val callback: (dt: Float) -> Unit,
    val intervalSeconds: Float,
    val isOnce: Boolean = false,
    var timeUntilNextRun: Float = intervalSeconds,
    var isActive: Boolean = true
)

data class ExecutionResult(
    val isSuccess: Boolean,
    val logs: List<String>,
    val error: String? = null,
    val rootWidget: KivyWidgetNode? = null,
    val executionTimeMs: Long = 0,
    val appTitle: String = "Kivy App"
)
