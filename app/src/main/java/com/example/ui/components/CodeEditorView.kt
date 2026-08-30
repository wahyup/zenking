package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.filesystem.LocalProjectFile

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

    Column(modifier = modifier.fillMaxSize().background(Color(0xFF0B0F19))) {
        // Multi-file tabs row
        if (projectFiles.isNotEmpty()) {
            val selectedIdx = projectFiles.indexOfFirst { it.absolutePath == activeLocalFile?.absolutePath }.coerceAtLeast(0)
            ScrollableTabRow(
                selectedTabIndex = selectedIdx,
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    if (selectedIdx < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedIdx]),
                            color = Color(0xFF10B981)
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
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF10B981)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🐍 main.py", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📐 app.kv", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
            }
        }

        // Quick Code snippet shortcuts toolbar
        val isKvActive = (activeLocalFile != null && activeLocalFile.extension == "kv") || (projectFiles.isEmpty() && selectedTab == 1)

        val shortcuts = if (!isKvActive) {
            listOf("def ", "class ", "self.", "import ", "from kivy.", "BoxLayout(", "Button(", "Label(", "Slider(", "Clock.", "on_press=", "size_hint=", "pos_hint=", ":", "( )", "[ ]", "{ }", "4-Space", "\" \"", "# ")
        } else {
            listOf("<Layout>:", "orientation: ", "size_hint: ", "padding: ", "spacing: ", "Button:", "Label:", "TextInput:", "Slider:", "Switch:", "id: ", "text: ", "on_press: ", "root.", "app.", ":", "( )", "[ ]")
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131C2E))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(shortcuts) { snippet ->
                AssistChip(
                    onClick = {
                        val currentText = if (!isKvActive) pythonCode else kvCode
                        val newText = if (snippet == "4-Space") {
                            "$currentText    "
                        } else if (snippet == "( )") {
                            "$currentText()"
                        } else if (snippet == "[ ]") {
                            "$currentText[]"
                        } else if (snippet == "{ }") {
                            "$currentText{}"
                        } else if (snippet == "\" \"") {
                            "$currentText\"\""
                        } else {
                            "$currentText$snippet"
                        }
                        if (!isKvActive) onPythonCodeChange(newText) else onKvCodeChange(newText)
                    },
                    label = {
                        Text(
                            text = snippet,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF38BDF8)
                        )
                    },
                    modifier = Modifier.padding(end = 6.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }

        // Code Editor Canvas with Line Numbers
        val codeText = if (!isKvActive) pythonCode else kvCode
        val linesCount = codeText.lines().size.coerceAtLeast(1)

        val vScrollState = rememberScrollState()
        val hScrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0A0E17))
        ) {
            // Line numbers column
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0F172A))
                    .verticalScroll(vScrollState)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                for (lineNum in 1..linesCount) {
                    Text(
                        text = "$lineNum",
                        color = Color(0xFF475569),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(end = 8.dp)
                    )
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
                    value = codeText,
                    onValueChange = {
                        if (!isKvActive) onPythonCodeChange(it) else onKvCodeChange(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("code_editor_input"),
                    textStyle = TextStyle(
                        color = Color(0xFFF1F5F9),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(Color(0xFF10B981)),
                    visualTransformation = PythonSyntaxVisualTransformation()
                )
            }
        }
    }
}

class PythonSyntaxVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val annotated = buildAnnotatedString {
            append(raw)

            // Python keywords
            val keywords = listOf(
                "def", "class", "return", "if", "else", "elif", "for", "while", "import", "from",
                "as", "in", "is", "not", "and", "or", "try", "except", "finally", "with", "lambda",
                "True", "False", "None", "self", "pass", "break", "continue"
            )

            // Kivy UI classes
            val kivyClasses = listOf(
                "App", "Widget", "BoxLayout", "GridLayout", "FloatLayout", "AnchorLayout", "StackLayout",
                "Button", "Label", "TextInput", "Slider", "Switch", "ProgressBar", "Image", "ScreenManager",
                "Screen", "Clock", "Animation", "Builder", "Color", "Rectangle", "Ellipse", "Line"
            )

            // Syntax coloring
            val lines = raw.lines()
            var currentOffset = 0

            for (line in lines) {
                val commentIdx = line.indexOf("#")
                val codePart = if (commentIdx != -1) line.substring(0, commentIdx) else line

                // Highlight Keywords
                for (kw in keywords) {
                    val regex = Regex("\\b$kw\\b")
                    regex.findAll(codePart).forEach { match ->
                        addStyle(
                            SpanStyle(color = Color(0xFFF472B6), fontWeight = FontWeight.Bold),
                            currentOffset + match.range.first,
                            currentOffset + match.range.last + 1
                        )
                    }
                }

                // Highlight Kivy classes
                for (kc in kivyClasses) {
                    val regex = Regex("\\b$kc\\b")
                    regex.findAll(codePart).forEach { match ->
                        addStyle(
                            SpanStyle(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold),
                            currentOffset + match.range.first,
                            currentOffset + match.range.last + 1
                        )
                    }
                }

                // Highlight Strings: "..." or '...'
                val strRegex = Regex("(\"[^\"]*\"|'[^']*')")
                strRegex.findAll(codePart).forEach { match ->
                    addStyle(
                        SpanStyle(color = Color(0xFF34D399)),
                        currentOffset + match.range.first,
                        currentOffset + match.range.last + 1
                    )
                }

                // Highlight Comments
                if (commentIdx != -1) {
                    addStyle(
                        SpanStyle(color = Color(0xFF64748B)),
                        currentOffset + commentIdx,
                        currentOffset + line.length
                    )
                }

                currentOffset += line.length + 1 // including \n
            }
        }

        return TransformedText(annotated, OffsetMapping.Identity)
    }
}
