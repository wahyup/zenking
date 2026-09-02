package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.KivyColor
import com.example.engine.KivyRuntime
import com.example.engine.KivyWidgetNode

@Composable
fun KivyComposeRenderer(
    widget: KivyWidgetNode,
    runtime: KivyRuntime,
    modifier: Modifier = Modifier,
    isInspectMode: Boolean = false,
    selectedWidget: KivyWidgetNode? = null,
    onSelectWidget: (KivyWidgetNode) -> Unit = {}
) {
    val isSelected = selectedWidget == widget

    val inspectModifier = if (isInspectMode) {
        Modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF10B981) else Color(0x663B82F6),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onSelectWidget(widget) }
    } else Modifier

    val effectiveType = when {
        widget.type in listOf("BoxLayout", "GridLayout", "FloatLayout", "AnchorLayout", "ScreenManager", "Screen", "Button", "Label", "TextInput", "Slider", "Switch", "ProgressBar", "Image") -> widget.type
        widget.baseType in listOf("BoxLayout", "GridLayout", "FloatLayout", "AnchorLayout", "ScreenManager", "Screen", "Button", "Label", "TextInput", "Slider", "Switch", "ProgressBar", "Image") -> widget.baseType
        widget.properties.containsKey("cols") || widget.properties.containsKey("rows") -> "GridLayout"
        widget.properties.containsKey("orientation") || widget.children.isNotEmpty() -> "BoxLayout"
        else -> widget.type
    }

    when (effectiveType) {
        "BoxLayout", "FloatLayout", "AnchorLayout" -> BoxLayoutView(widget, runtime, modifier.then(inspectModifier), isInspectMode, selectedWidget, onSelectWidget)
        "GridLayout" -> GridLayoutView(widget, runtime, modifier.then(inspectModifier), isInspectMode, selectedWidget, onSelectWidget)
        "ScreenManager" -> ScreenManagerView(widget, runtime, modifier.then(inspectModifier), isInspectMode, selectedWidget, onSelectWidget)
        "Screen" -> ScreenView(widget, runtime, modifier.then(inspectModifier), isInspectMode, selectedWidget, onSelectWidget)
        "Button" -> ButtonView(widget, runtime, modifier.then(inspectModifier))
        "Label" -> LabelView(widget, modifier.then(inspectModifier))
        "TextInput" -> TextInputView(widget, runtime, modifier.then(inspectModifier))
        "Slider" -> SliderView(widget, runtime, modifier.then(inspectModifier))
        "Switch" -> SwitchView(widget, runtime, modifier.then(inspectModifier))
        "ProgressBar" -> ProgressBarView(widget, modifier.then(inspectModifier))
        "Image" -> ImageView(widget, modifier.then(inspectModifier))
        else -> GenericWidgetView(widget, runtime, modifier.then(inspectModifier), isInspectMode, selectedWidget, onSelectWidget)
    }
}

@Composable
private fun BoxLayoutView(
    widget: KivyWidgetNode,
    runtime: KivyRuntime,
    modifier: Modifier,
    isInspectMode: Boolean,
    selectedWidget: KivyWidgetNode?,
    onSelectWidget: (KivyWidgetNode) -> Unit
) {
    val orientation = widget.getString("orientation", "vertical")
    val padding = widget.getInt("padding", 12).dp
    val spacing = widget.getInt("spacing", 8).dp

    val hasAnyWeightedChild = widget.children.any {
        val (shX, shY) = it.getSizeHint()
        if (orientation == "horizontal") (shX != null && shX > 0f) else (shY != null && shY > 0f)
    }

    if (orientation == "horizontal") {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(padding),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            widget.children.forEach { child ->
                val (childShX, _) = child.getSizeHint()
                val childMod = if (childShX != null && childShX > 0f) {
                    Modifier.weight(childShX.coerceAtLeast(0.05f))
                } else if (!hasAnyWeightedChild) {
                    Modifier.weight(1f)
                } else {
                    Modifier.wrapContentWidth()
                }
                KivyComposeRenderer(child, runtime, childMod, isInspectMode, selectedWidget, onSelectWidget)
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            widget.children.forEach { child ->
                val (_, childShY) = child.getSizeHint()
                val childMod = if (childShY != null && childShY > 0f) {
                    Modifier.weight(childShY.coerceAtLeast(0.05f))
                } else {
                    Modifier.wrapContentHeight()
                }
                KivyComposeRenderer(child, runtime, childMod.fillMaxWidth(), isInspectMode, selectedWidget, onSelectWidget)
            }
        }
    }
}

@Composable
private fun GridLayoutView(
    widget: KivyWidgetNode,
    runtime: KivyRuntime,
    modifier: Modifier,
    isInspectMode: Boolean,
    selectedWidget: KivyWidgetNode?,
    onSelectWidget: (KivyWidgetNode) -> Unit
) {
    val cols = widget.getInt("cols", 2).coerceAtLeast(1)
    val spacing = widget.getInt("spacing", 8).dp
    val padding = widget.getInt("padding", 8).dp
    val (_, shY) = widget.getSizeHint()

    val chunkedRows = widget.children.chunked(cols)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        chunkedRows.forEach { rowChildren ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (shY != null && shY > 0f) Modifier.weight(1f) else Modifier.wrapContentHeight()
                    ),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowChildren.forEach { child ->
                    Box(modifier = Modifier.weight(1f)) {
                        KivyComposeRenderer(child, runtime, Modifier.fillMaxWidth(), isInspectMode, selectedWidget, onSelectWidget)
                    }
                }
                // Fill in empty slots if last row has fewer items
                val remaining = cols - rowChildren.size
                if (remaining > 0) {
                    repeat(remaining) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenManagerView(
    widget: KivyWidgetNode,
    runtime: KivyRuntime,
    modifier: Modifier,
    isInspectMode: Boolean,
    selectedWidget: KivyWidgetNode?,
    onSelectWidget: (KivyWidgetNode) -> Unit
) {
    val currentScreenName = widget.getString("current", "")
    val activeScreen = widget.children.firstOrNull {
        if (currentScreenName.isNotEmpty()) it.getString("name") == currentScreenName
        else true
    } ?: widget.children.firstOrNull()

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = activeScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition"
        ) { screen ->
            if (screen != null) {
                KivyComposeRenderer(screen, runtime, Modifier.fillMaxSize(), isInspectMode, selectedWidget, onSelectWidget)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No screens in ScreenManager", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ScreenView(
    widget: KivyWidgetNode,
    runtime: KivyRuntime,
    modifier: Modifier,
    isInspectMode: Boolean,
    selectedWidget: KivyWidgetNode?,
    onSelectWidget: (KivyWidgetNode) -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        widget.children.forEach { child ->
            KivyComposeRenderer(child, runtime, Modifier.fillMaxSize(), isInspectMode, selectedWidget, onSelectWidget)
        }
    }
}

@Composable
private fun ButtonView(
    widget: KivyWidgetNode,
    runtime: KivyRuntime,
    modifier: Modifier
) {
    val text = widget.getString("text", "Button")
    val fontSize = widget.getInt("font_size", 16).sp
    val bgColor = widget.getColor("background_color", KivyColor(0.12f, 0.55f, 0.95f, 1f)).toComposeColor()
    val textColor = widget.getColor("color", KivyColor(1f, 1f, 1f, 1f)).toComposeColor()
    val isBold = widget.getBoolean("bold", false)

    Button(
        onClick = {
            runtime.triggerEvent(widget, "on_press", listOf(widget))
            runtime.triggerEvent(widget, "on_release", listOf(widget))
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .testTag("kivy_btn_${widget.id.ifEmpty { text.lowercase().replace(" ", "_") }}"),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun LabelView(
    widget: KivyWidgetNode,
    modifier: Modifier
) {
    val text = widget.getString("text", "")
    val fontSize = widget.getInt("font_size", 16).sp
    val color = widget.getColor("color", KivyColor(0.9f, 0.95f, 1f, 1f)).toComposeColor()
    val bgColor = widget.getColor("background_color", KivyColor(0f, 0f, 0f, 0f)).toComposeColor()
    val isBold = widget.getBoolean("bold", false)
    val halign = widget.getString("halign", "left")

    val textAlign = when (halign) {
        "center" -> TextAlign.Center
        "right" -> TextAlign.End
        else -> TextAlign.Start
    }

    val isCardLike = text.contains("\n") || bgColor.alpha > 0.05f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (bgColor.alpha > 0.05f) bgColor else if (isCardLike) Color(0x221E293B) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isCardLike) 1.dp else 0.dp,
                color = if (isCardLike) Color(0x33475569) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = if (isCardLike) 8.dp else 4.dp),
        contentAlignment = when (halign) {
            "center" -> Alignment.Center
            "right" -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        }
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            color = color,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            textAlign = textAlign,
            fontFamily = if (text.contains("\n") && (text.contains("%") || text.contains("FPS") || text.contains("MB"))) FontFamily.Monospace else FontFamily.Default,
            lineHeight = (fontSize.value * 1.3f).sp
        )
    }
}

@Composable
private fun TextInputView(
    widget: KivyWidgetNode,
    runtime: KivyRuntime,
    modifier: Modifier
) {
    val text = widget.getString("text", "")
    val hint = widget.getString("hint_text", "Enter value...")
    val isMultiline = widget.getBoolean("multiline", false)

    OutlinedTextField(
        value = text,
        onValueChange = {
            widget.properties["text"] = it
            runtime.triggerEvent(widget, "on_text_validate", listOf(widget, it))
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        placeholder = { Text(hint, color = Color.Gray, fontSize = 14.sp) },
        singleLine = !isMultiline,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF10B981),
            unfocusedBorderColor = Color(0xFF334155),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color(0xFFE2E8F0),
            focusedContainerColor = Color(0xFF1E293B),
            unfocusedContainerColor = Color(0xFF0F172A)
        )
    )
}

@Composable
private fun SliderView(
    widget: KivyWidgetNode,
    runtime: KivyRuntime,
    modifier: Modifier
) {
    val minVal = widget.getFloat("min", 0f)
    val maxVal = widget.getFloat("max", 100f).coerceAtLeast(minVal + 1f)
    val currentVal = widget.getFloat("value", minVal).coerceIn(minVal, maxVal)

    Slider(
        value = currentVal,
        onValueChange = { newVal ->
            widget.properties["value"] = newVal
            runtime.triggerEvent(widget, "on_value", listOf(widget, newVal))
        },
        valueRange = minVal..maxVal,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFF10B981),
            activeTrackColor = Color(0xFF059669),
            inactiveTrackColor = Color(0xFF334155)
        )
    )
}

@Composable
private fun SwitchView(
    widget: KivyWidgetNode,
    runtime: KivyRuntime,
    modifier: Modifier
) {
    val active = widget.getBoolean("active", false)

    Row(
        modifier = modifier.heightIn(min = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = active,
            onCheckedChange = { isChecked ->
                widget.properties["active"] = isChecked
                runtime.triggerEvent(widget, "on_active", listOf(widget, isChecked))
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF10B981),
                checkedTrackColor = Color(0xFF065F46),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFF1E293B)
            )
        )
    }
}

@Composable
private fun ProgressBarView(
    widget: KivyWidgetNode,
    modifier: Modifier
) {
    val maxVal = widget.getFloat("max", 100f).coerceAtLeast(1f)
    val value = widget.getFloat("value", 0f).coerceIn(0f, maxVal)
    val progress = (value / maxVal).coerceIn(0f, 1f)

    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp)),
        color = Color(0xFF10B981),
        trackColor = Color(0xFF1E293B)
    )
}

@Composable
private fun ImageView(
    widget: KivyWidgetNode,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("🖼️ Kivy Image Widget", color = Color(0xFF94A3B8), fontSize = 14.sp)
    }
}

@Composable
private fun GenericWidgetView(
    widget: KivyWidgetNode,
    runtime: KivyRuntime,
    modifier: Modifier,
    isInspectMode: Boolean,
    selectedWidget: KivyWidgetNode?,
    onSelectWidget: (KivyWidgetNode) -> Unit
) {
    if (widget.children.isNotEmpty() && widget.canvas.instructions.isEmpty() && widget.drawingStrokes.isEmpty()) {
        BoxLayoutView(widget, runtime, modifier, isInspectMode, selectedWidget, onSelectWidget)
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
        ) {
            KivyCanvasView(widget = widget, modifier = Modifier.fillMaxSize())

            if (widget.children.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    widget.children.forEach { child ->
                        KivyComposeRenderer(child, runtime, Modifier.fillMaxWidth(), isInspectMode, selectedWidget, onSelectWidget)
                    }
                }
            }
        }
    }
}
