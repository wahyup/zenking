package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.filesystem.LocalProjectFile
import com.example.ui.syntax.EditorTheme
import com.example.ui.syntax.PythonSyntaxHighlighter

@Composable
fun CodeEditorView(
    pythonCode: String,
    kvCode: String,
    projectFiles: List<LocalProjectFile> = emptyList(),
    activeLocalFile: LocalProjectFile? = null,
    onSelectFile: (LocalProjectFile) -> Unit = {},
    onNewFileClick: () -> Unit = {},
    onPythonCodeChange: (String) -> Unit,
    onKvCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Python (.py), 1: KV Lang (.kv)
    var currentTheme by remember { mutableStateOf(EditorTheme.VS_CODE_DARK) }
    var fontSizeSp by remember { mutableFloatStateOf(13f) }
    var showLineNumbers by remember { mutableStateOf(true) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showThemeDialog by remember { mutableStateOf(false) }

    val isKvActive = (activeLocalFile != null && activeLocalFile.extension == "kv") || (projectFiles.isEmpty() && selectedTab == 1)
    val activeCode = if (!isKvActive) pythonCode else kvCode

    val syntaxHighlighter = remember(currentTheme, searchQuery, isKvActive) {
        PythonSyntaxHighlighter(
            theme = currentTheme,
            searchQuery = searchQuery,
            isKvLanguage = isKvActive
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.backgroundColor)
    ) {
        // Multi-file tabs row or Standard Tabs
        if (projectFiles.isNotEmpty()) {
            val selectedIdx = projectFiles.indexOfFirst { it.absolutePath == activeLocalFile?.absolutePath }.coerceAtLeast(0)
            ScrollableTabRow(
                selectedTabIndex = selectedIdx,
                containerColor = currentTheme.gutterColor,
                contentColor = Color.White,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    if (selectedIdx < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedIdx]),
                            color = currentTheme.keywordColor
                        )
                    }
                }
            ) {
                projectFiles.forEach { file ->
                    val isSelected = activeLocalFile?.absolutePath == file.absolutePath
                    Tab(
                        selected = isSelected,
                        onClick = { onSelectFile(file) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (file.extension == "py") "🐍 ${file.name}" else "📐 ${file.name}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) currentTheme.keywordColor else currentTheme.textColor.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                }

                // Add File Tab Action
                Tab(
                    selected = false,
                    onClick = onNewFileClick,
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = "New File", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("New", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        } else {
            // Default two-tab fallback
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = currentTheme.gutterColor,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = currentTheme.keywordColor
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🐍 main.py",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) currentTheme.keywordColor else currentTheme.textColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📐 app.kv",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) currentTheme.keywordColor else currentTheme.textColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                )
            }
        }

        // Toolbar: Theme Picker, Font Scale, Find, Line Numbers toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(currentTheme.gutterColor)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Syntax Theme Chip Button
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = currentTheme.backgroundColor,
                    modifier = Modifier
                        .clickable { showThemeDialog = true }
                        .testTag("theme_selector_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ColorLens,
                            contentDescription = "Theme",
                            tint = currentTheme.keywordColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentTheme.displayName,
                            color = currentTheme.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Syntax Mode Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isKvActive) Color(0x3338BDF8) else Color(0x3310B981)
                ) {
                    Text(
                        text = if (isKvActive) "KV Lang Syntax" else "Python 3 Syntax",
                        color = if (isKvActive) Color(0xFF38BDF8) else Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Font Size Decrement
                IconButton(
                    onClick = { if (fontSizeSp > 10f) fontSizeSp -= 1f },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("A-", color = currentTheme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "${fontSizeSp.toInt()}pt",
                    color = currentTheme.gutterTextColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )

                // Font Size Increment
                IconButton(
                    onClick = { if (fontSizeSp < 22f) fontSizeSp += 1f },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("A+", color = currentTheme.textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Line Numbers Toggle
                IconButton(
                    onClick = { showLineNumbers = !showLineNumbers },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Numbers,
                        contentDescription = "Toggle Line Numbers",
                        tint = if (showLineNumbers) currentTheme.keywordColor else currentTheme.gutterTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Search in Code Toggle
                IconButton(
                    onClick = {
                        showSearchBar = !showSearchBar
                        if (!showSearchBar) searchQuery = ""
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search in Code",
                        tint = if (showSearchBar || searchQuery.isNotBlank()) currentTheme.functionColor else currentTheme.gutterTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Animated Search in Code Bar
        AnimatedVisibility(visible = showSearchBar) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(currentTheme.gutterColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Find in code...", fontSize = 12.sp, color = currentTheme.gutterTextColor) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("code_search_field"),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 12.sp, color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = currentTheme.functionColor,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = currentTheme.backgroundColor,
                        unfocusedContainerColor = currentTheme.backgroundColor
                    )
                )

                if (searchQuery.isNotBlank()) {
                    val matchCount = Regex.escape(searchQuery).toRegex(RegexOption.IGNORE_CASE).findAll(activeCode).count()
                    Text(
                        text = "$matchCount matches",
                        color = currentTheme.functionColor,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                IconButton(
                    onClick = {
                        searchQuery = ""
                        showSearchBar = false
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Search", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Quick Code snippet shortcuts toolbar
        val shortcuts = if (!isKvActive) {
            listOf("def ", "class ", "self.", "import ", "from kivy.", "BoxLayout(", "Button(", "Label(", "Slider(", "Clock.schedule_", "on_press=", "size_hint=", "pos_hint=", ":", "( )", "[ ]", "{ }", "4-Space", "\" \"", "# ")
        } else {
            listOf("<Layout>:", "orientation: ", "size_hint: ", "padding: ", "spacing: ", "Button:", "Label:", "TextInput:", "Slider:", "Switch:", "id: ", "text: ", "on_press: ", "root.", "app.", ":", "( )", "[ ]")
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(currentTheme.backgroundColor.copy(alpha = 0.95f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(shortcuts) { snippet ->
                AssistChip(
                    onClick = {
                        val newText = when (snippet) {
                            "4-Space" -> "$activeCode    "
                            "( )" -> "$activeCode()"
                            "[ ]" -> "$activeCode[]"
                            "{ }" -> "$activeCode{}"
                            "\" \"" -> "$activeCode\"\""
                            else -> "$activeCode$snippet"
                        }
                        if (!isKvActive) onPythonCodeChange(newText) else onKvCodeChange(newText)
                    },
                    label = {
                        Text(
                            text = snippet,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = currentTheme.keywordColor
                        )
                    },
                    modifier = Modifier.padding(end = 6.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = currentTheme.gutterColor
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }

        // Code Editor Canvas with Line Numbers & Syntax Transformation
        val linesCount = activeCode.lines().size.coerceAtLeast(1)
        val vScrollState = rememberScrollState()
        val hScrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(currentTheme.backgroundColor)
        ) {
            // Line numbers column
            if (showLineNumbers) {
                Column(
                    modifier = Modifier
                        .width(46.dp)
                        .fillMaxHeight()
                        .background(currentTheme.gutterColor)
                        .verticalScroll(vScrollState)
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (lineNum in 1..linesCount) {
                        Text(
                            text = "$lineNum",
                            color = currentTheme.gutterTextColor,
                            fontSize = fontSizeSp.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            // Text field editor
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(vScrollState)
                    .horizontalScroll(hScrollState)
                    .padding(start = 12.dp, top = 12.dp, end = 16.dp, bottom = 32.dp)
            ) {
                BasicTextField(
                    value = activeCode,
                    onValueChange = {
                        if (!isKvActive) onPythonCodeChange(it) else onKvCodeChange(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("code_editor_input"),
                    textStyle = TextStyle(
                        color = currentTheme.textColor,
                        fontSize = fontSizeSp.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = (fontSizeSp * 1.55f).sp
                    ),
                    cursorBrush = SolidColor(currentTheme.keywordColor),
                    visualTransformation = syntaxHighlighter
                )
            }
        }

        // Status footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(currentTheme.gutterColor)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${linesCount} lines • ${activeCode.length} chars • UTF-8",
                color = currentTheme.gutterTextColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = if (isKvActive) "📐 Kivy KV Lang" else "🐍 Python 3.10+",
                color = currentTheme.keywordColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ColorLens, contentDescription = null, tint = Color(0xFF10B981))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Syntax Highlighting Theme", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a color palette for Python syntax highlighting:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    EditorTheme.values().forEach { theme ->
                        val isSelected = currentTheme == theme
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentTheme = theme
                                    showThemeDialog = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = theme.backgroundColor,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF10B981)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = theme.displayName,
                                        color = theme.textColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Row(modifier = Modifier.padding(top = 4.dp)) {
                                        Text("def ", color = theme.keywordColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                        Text("build", color = theme.functionColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                        Text("(self): ", color = theme.builtinColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                        Text("\"Hello\"", color = theme.stringColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }

                                if (isSelected) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.size(10.dp)
                                    ) {}
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Done", color = Color(0xFF10B981))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
