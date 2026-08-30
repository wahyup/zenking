package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.components.TerminalReplView
import com.example.ui.viewmodel.KivyStudioViewModel

@Composable
fun ConsoleScreen(
    viewModel: KivyStudioViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.runtime.logs.collectAsState()

    TerminalReplView(
        runtime = viewModel.runtime,
        logs = logs,
        onClearLogs = {
            // Clear runtime logs
            viewModel.runtime.executeReplExpression("print('')")
        },
        modifier = modifier.fillMaxSize()
    )
}
