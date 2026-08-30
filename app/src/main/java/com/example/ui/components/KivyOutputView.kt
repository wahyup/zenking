package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.DeviceUnknown
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Tablet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.engine.KivyRuntime
import com.example.engine.KivyUiState
import com.example.engine.KivyWidgetNode

/**
 * Dedicated View component to render the Kivy interface output,
 * featuring a sandboxed execution viewport, device frames, metrics,
 * and a prominent 'Run' action to execute Python + Kivy code securely.
 */
@Composable
fun KivyOutputView(
    uiState: KivyUiState,
    runtime: KivyRuntime,
    fps: Float,
    renderDurationMs: Long,
    isInspectMode: Boolean,
    selectedInspectWidget: KivyWidgetNode?,
    deviceFrameMode: String,
    onRunCode: () -> Unit,
    onToggleInspect: () -> Unit,
    onSelectInspectWidget: (KivyWidgetNode) -> Unit,
    onClearInspectWidget: () -> Unit,
    onChangeDeviceFrame: (String) -> Unit,
    onNavigateToEditor: () -> Unit,
    onNavigateToConsole: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A10))
    ) {
        // Sandboxed Environment Header & Controls Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0F172A),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sandbox Status & Metrics Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0x2610B981),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4010B981))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Sandboxed Environment",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SANDBOX",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // FPS Counter
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            if (uiState is KivyUiState.Running) Color(0xFF10B981) else Color(0xFF64748B),
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${fps.toInt()} FPS",
                                    color = if (uiState is KivyUiState.Running) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (renderDurationMs > 0) {
                            Text(
                                text = "${renderDurationMs}ms",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Action Controls: Dedicated Run Button & Tools
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Inspect toggle
                        FilterChip(
                            selected = isInspectMode,
                            onClick = onToggleInspect,
                            label = { Text("Inspect", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF065F46),
                                selectedLabelColor = Color(0xFF34D399),
                                selectedLeadingIconColor = Color(0xFF34D399),
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFF94A3B8),
                                iconColor = Color(0xFF94A3B8)
                            )
                        )

                        // Dedicated 'Run' Launch button
                        Button(
                            onClick = onRunCode,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            ),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("sandbox_run_button")
                        ) {
                            Icon(
                                imageVector = if (uiState is KivyUiState.Running) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                contentDescription = "Run in Sandbox",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState is KivyUiState.Running) "Rerun" else "Run",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Device viewport selection row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Isolated Python 3.12 VM • Kivy Graphics Pipeline",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DeviceFrameChip(
                            label = "Phone",
                            isSelected = deviceFrameMode == "phone",
                            onClick = { onChangeDeviceFrame("phone") }
                        )
                        DeviceFrameChip(
                            label = "Tablet",
                            isSelected = deviceFrameMode == "tablet",
                            onClick = { onChangeDeviceFrame("tablet") }
                        )
                        DeviceFrameChip(
                            label = "Full",
                            isSelected = deviceFrameMode == "fullscreen",
                            onClick = { onChangeDeviceFrame("fullscreen") }
                        )
                    }
                }
            }
        }

        // Dedicated Kivy Viewport Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(if (deviceFrameMode == "phone") 12.dp else if (deviceFrameMode == "tablet") 8.dp else 0.dp),
            contentAlignment = Alignment.Center
        ) {
            val frameModifier = when (deviceFrameMode) {
                "tablet" -> Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                "fullscreen" -> Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                else -> Modifier // "phone"
                    .fillMaxSize()
                    .widthIn(max = 440.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(3.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
            }

            Box(
                modifier = frameModifier.testTag("kivy_output_viewport")
            ) {
                when (uiState) {
                    is KivyUiState.Idle -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Sandboxed Kivy Environment",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Execute your Python 3 code in an isolated runtime with full Kivy widget tree and 60 FPS clock cycle rendering.",
                                        color = Color(0xFF94A3B8),
                                        textAlign = TextAlign.Center,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = onRunCode,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("idle_launch_button")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Launch Sandbox App", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    is KivyUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = Color(0xFF10B981),
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Executing in Python Sandbox...",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Building Kivy widget tree & binding handlers",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    is KivyUiState.Running -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Simulated Phone Status Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0A0F1D))
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.appTitle,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "60Hz",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Dynamic Kivy Widget Tree Renderer Output
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                KivyComposeRenderer(
                                    widget = uiState.rootWidget,
                                    runtime = runtime,
                                    modifier = Modifier.fillMaxSize(),
                                    isInspectMode = isInspectMode,
                                    selectedWidget = selectedInspectWidget,
                                    onSelectWidget = onSelectInspectWidget
                                )
                            }
                        }
                    }

                    is KivyUiState.ScriptCompleted -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "✅ Script Execution Finished",
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Execution time: ${uiState.executionTimeMs}ms\nNo root widget was returned by build(). Check Terminal for outputs.",
                                        color = Color(0xFF94A3B8),
                                        textAlign = TextAlign.Center,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = onNavigateToConsole,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                                        ) {
                                            Text("View Console")
                                        }
                                        OutlinedButton(
                                            onClick = onRunCode,
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981))
                                        ) {
                                            Text("Rerun")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is KivyUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1515)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "⚠️ Python / Kivy Execution Error",
                                        color = Color(0xFFF87171),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.errorMessage,
                                        color = Color(0xFFFCA5A5),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = onNavigateToEditor,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                        ) {
                                            Text("Edit Code")
                                        }
                                        Button(
                                            onClick = onRunCode,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                                        ) {
                                            Text("Retry")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom overlay inspector sheet if widget is selected
            if (isInspectMode && selectedInspectWidget != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    WidgetInspectorSheet(
                        selectedWidget = selectedInspectWidget,
                        onClose = onClearInspectWidget
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceFrameChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Color(0xFF065F46) else Color(0xFF1E293B),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFF34D399) else Color(0xFF94A3B8),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
