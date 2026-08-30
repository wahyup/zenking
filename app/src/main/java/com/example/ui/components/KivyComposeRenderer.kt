package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DrawingPoint
import com.example.engine.DrawingStroke
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

    when (widget.type) {
        "BoxLayout" -> BoxLayoutView(widget, runtime, modifier.then(inspectModifier), isInspectMode, selectedWidget, onSelectWidget)
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
    val padding = widget.getInt("padding", 10).dp
    val spacing = widget.getInt("spacing", 8).dp
    val (shX, shY) = widget.getSizeHint()

    val baseModifier = modifier
        .padding(padding)

    if (orientation == "horizontal") {
        Row(
            modifier = if (shY != null && shY > 0f) baseModifier.fillMaxWidth().fillMaxHeight(shY) else baseModifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            widget.children.forEach { child ->
                val (childShX, _) = child.getSizeHint()
                val childMod = if (childShX != null && childShX > 0f) {
                    Modifier.weight(childShX.coerceAtLeast(0.1f))
                } else {
                    Modifier
                }
                KivyComposeRenderer(child, runtime, childMod, isInspectMode, selectedWidget, onSelectWidget)
            }
        }
    } else {
        Column(
            modifier = if (shY != null && shY > 0f) baseModifier.fillMaxWidth().fillMaxHeight(shY) else baseModifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            widget.children.forEach { child ->
                val (_, childShY) = child.getSizeHint()
                val childMod = if (childShY != null && childShY > 0f) {
                    Modifier.weight(childShY.coerceAtLeast(0.1f))
                } else {
                    Modifier.wrapContentHeight()
                }
                KivyComposeRenderer(child, runtime, childMod, isInspectMode, selectedWidget, onSelectWidget)
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
    val (shX, shY) = widget.getSizeHint()

    val baseModifier = modifier
        .padding(padding)

    LazyVerticalGrid(
        columns = GridCells.Fixed(cols),
        modifier = if (shY != null && shY > 0f) baseModifier.fillMaxWidth().fillMaxHeight(shY) else baseModifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items(widget.children) { child ->
            KivyComposeRenderer(child, runtime, Modifier.fillMaxWidth(), isInspectMode, selectedWidget, onSelectWidget)
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
                    Text("No screens added to ScreenManager", color = Color.Gray)
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
    val bgColor = widget.getColor("background_color", KivyColor(0.2f, 0.5f, 0.9f, 1f)).toComposeColor()
    val textColor = widget.getColor("color", KivyColor(1f, 1f, 1f, 1f)).toComposeColor()
    val isBold = widget.getBoolean("bold", false)

    Button(
        onClick = {
            runtime.triggerEvent(widget, "on_press", listOf(widget))
            runtime.triggerEvent(widget, "on_release", listOf(widget))
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .testTag("kivy_btn_${widget.id.ifEmpty { text.lowercase().replace(" ", "_") }}"),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center
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
    val isBold = widget.getBoolean("bold", false)
    val halign = widget.getString("halign", "left")

    val textAlign = when (halign) {
        "center" -> TextAlign.Center
        "right" -> TextAlign.End
        else -> TextAlign.Start
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            fontSize = fontSize,
            color = color,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            textAlign = textAlign,
            fontFamily = if (text.contains("\n") && text.contains("%")) FontFamily.Monospace else FontFamily.Default
        )
    }
}

@Composable
private fun TextInputView(
    widget: KivyWidgetNode,
    runtime: KivyRuntime,
    modifier: Modifier
) {
    var text by remember(widget.getString("text")) { mutableStateOf(widget.getString("text")) }
    val hint = widget.getString("hint_text", "Enter value...")
    val isMultiline = widget.getBoolean("multiline", false)

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            widget.properties["text"] = it
            runtime.triggerEvent(widget, "on_text_validate", listOf(widget, it))
        },
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
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
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
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
        modifier = modifier.heightIn(min = 48.dp),
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
    // If widget has canvas instructions or interactive drawing strokes
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
    ) {
        KivyCanvasView(widget = widget, modifier = Modifier.fillMaxSize())

        // Child widgets if any
        if (widget.children.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                widget.children.forEach { child ->
                    KivyComposeRenderer(child, runtime, Modifier.fillMaxWidth(), isInspectMode, selectedWidget, onSelectWidget)
                }
            }
        }
    }
}
