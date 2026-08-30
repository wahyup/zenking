package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.components.KivyOutputView
import com.example.ui.viewmodel.KivyStudioViewModel

@Composable
fun RunnerScreen(
    viewModel: KivyStudioViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.runtime.uiState.collectAsState()
    val fps by viewModel.runtime.fps.collectAsState()
    val renderDuration by viewModel.runtime.renderDurationMs.collectAsState()
    val isInspectMode by viewModel.isInspectMode.collectAsState()
    val selectedInspectWidget by viewModel.selectedInspectWidget.collectAsState()
    val deviceFrameMode by viewModel.deviceFrameMode.collectAsState()

    KivyOutputView(
        uiState = uiState,
        runtime = viewModel.runtime,
        fps = fps,
        renderDurationMs = renderDuration,
        isInspectMode = isInspectMode,
        selectedInspectWidget = selectedInspectWidget,
        deviceFrameMode = deviceFrameMode,
        onRunCode = { viewModel.runCurrentCode() },
        onToggleInspect = { viewModel.toggleInspectMode() },
        onSelectInspectWidget = { viewModel.selectInspectWidget(it) },
        onClearInspectWidget = { viewModel.clearInspectWidget() },
        onChangeDeviceFrame = { viewModel.setDeviceFrameMode(it) },
        onNavigateToEditor = { viewModel.selectTab(0) },
        onNavigateToConsole = { viewModel.selectTab(2) },
        modifier = modifier.fillMaxSize()
    )
}
