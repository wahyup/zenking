package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.engine.DrawingPoint
import com.example.engine.DrawingStroke
import com.example.engine.KivyCanvasInstruction
import com.example.engine.KivyColor
import com.example.engine.KivyWidgetNode

@Composable
fun KivyCanvasView(
    widget: KivyWidgetNode,
    modifier: Modifier = Modifier
) {
    // Current touch drawing state
    var currentStroke by remember { mutableStateOf<DrawingStroke?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(widget) {
                detectTapGestures(
                    onTap = { offset ->
                        val stroke = DrawingStroke(
                            points = mutableListOf(DrawingPoint(offset.x, offset.y)),
                            color = widget.getColor("current_color", KivyColor(0.1f, 0.9f, 0.6f, 1f)),
                            strokeWidth = widget.getFloat("stroke_width", 4f)
                        )
                        widget.drawingStrokes.add(stroke)
                    }
                )
            }
            .pointerInput(widget) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val newStroke = DrawingStroke(
                            points = mutableListOf(DrawingPoint(offset.x, offset.y)),
                            color = widget.getColor("current_color", KivyColor(0.1f, 0.9f, 0.6f, 1f)),
                            strokeWidth = widget.getFloat("stroke_width", 4f)
                        )
                        currentStroke = newStroke
                        widget.drawingStrokes.add(newStroke)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentStroke?.points?.add(DrawingPoint(change.position.x, change.position.y))
                    },
                    onDragEnd = {
                        currentStroke = null
                    }
                )
            }
    ) {
        var activeColor = Color(0xFF10B981)

        // Draw Canvas Instructions from Python/Kivy
        for (instruction in widget.canvas.instructions) {
            when (instruction) {
                is KivyCanvasInstruction.SetColor -> {
                    activeColor = instruction.color.toComposeColor()
                }
                is KivyCanvasInstruction.DrawRectangle -> {
                    val scaleX = size.width / 360f
                    val scaleY = size.height / 380f
                    val rx = instruction.x * scaleX
                    val ry = size.height - (instruction.y * scaleY) - (instruction.height * scaleY)
                    val rw = instruction.width * scaleX
                    val rh = instruction.height * scaleY

                    if (instruction.cornerRadius > 0f) {
                        drawRoundRect(
                            color = activeColor,
                            topLeft = Offset(rx.coerceAtLeast(0f), ry.coerceAtLeast(0f)),
                            size = Size(rw.coerceAtLeast(10f), rh.coerceAtLeast(10f)),
                            cornerRadius = CornerRadius(instruction.cornerRadius, instruction.cornerRadius)
                        )
                    } else {
                        drawRect(
                            color = activeColor,
                            topLeft = Offset(rx.coerceAtLeast(0f), ry.coerceAtLeast(0f)),
                            size = Size(rw.coerceAtLeast(10f), rh.coerceAtLeast(10f))
                        )
                    }
                }
                is KivyCanvasInstruction.DrawEllipse -> {
                    val scaleX = size.width / 360f
                    val scaleY = size.height / 380f
                    val ex = instruction.x * scaleX
                    val ey = size.height - (instruction.y * scaleY) - (instruction.height * scaleY)
                    val ew = instruction.width * scaleX
                    val eh = instruction.height * scaleY

                    drawOval(
                        color = activeColor,
                        topLeft = Offset(ex.coerceAtLeast(0f), ey.coerceAtLeast(0f)),
                        size = Size(ew.coerceAtLeast(10f), eh.coerceAtLeast(10f))
                    )
                }
                is KivyCanvasInstruction.DrawLine -> {
                    if (instruction.points.size >= 2) {
                        val path = Path()
                        path.moveTo(instruction.points[0].x, instruction.points[0].y)
                        for (p in 1 until instruction.points.size) {
                            path.lineTo(instruction.points[p].x, instruction.points[p].y)
                        }
                        drawPath(
                            path = path,
                            color = instruction.color.toComposeColor(),
                            style = Stroke(
                                width = instruction.width,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
        }

        // Draw Interactive User Paint strokes
        for (stroke in widget.drawingStrokes) {
            if (stroke.points.size == 1) {
                drawCircle(
                    color = stroke.color.toComposeColor(),
                    radius = stroke.strokeWidth / 2f,
                    center = Offset(stroke.points[0].x, stroke.points[0].y)
                )
            } else if (stroke.points.size > 1) {
                val path = Path()
                path.moveTo(stroke.points[0].x, stroke.points[0].y)
                for (p in 1 until stroke.points.size) {
                    path.lineTo(stroke.points[p].x, stroke.points[p].y)
                }
                drawPath(
                    path = path,
                    color = stroke.color.toComposeColor(),
                    style = Stroke(
                        width = stroke.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
