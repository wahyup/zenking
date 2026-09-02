package com.example.ui.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

enum class EditorTheme(
    val displayName: String,
    val backgroundColor: Color,
    val gutterColor: Color,
    val gutterTextColor: Color,
    val textColor: Color,
    val keywordColor: Color,
    val functionColor: Color,
    val classColor: Color,
    val builtinColor: Color,
    val stringColor: Color,
    val numberColor: Color,
    val commentColor: Color,
    val decoratorColor: Color,
    val kivyColor: Color,
    val operatorColor: Color,
    val searchMatchColor: Color
) {
    VS_CODE_DARK(
        displayName = "VS Code Dark",
        backgroundColor = Color(0xFF1E1E1E),
        gutterColor = Color(0xFF181818),
        gutterTextColor = Color(0xFF6E7681),
        textColor = Color(0xFFD4D4D4),
        keywordColor = Color(0xFF569CD6),
        functionColor = Color(0xFFDCDCAA),
        classColor = Color(0xFF4EC9B0),
        builtinColor = Color(0xFF9CDCFE),
        stringColor = Color(0xFFCE9178),
        numberColor = Color(0xFFB5CEA8),
        commentColor = Color(0xFF6A9955),
        decoratorColor = Color(0xFFD7BA7D),
        kivyColor = Color(0xFF4FC1FF),
        operatorColor = Color(0xFFD4D4D4),
        searchMatchColor = Color(0x66FFD700)
    ),
    MONOKAI(
        displayName = "Monokai Pro",
        backgroundColor = Color(0xFF272822),
        gutterColor = Color(0xFF1E1F1C),
        gutterTextColor = Color(0xFF75715E),
        textColor = Color(0xFFF8F8F2),
        keywordColor = Color(0xFFF92672),
        functionColor = Color(0xFFA6E22E),
        classColor = Color(0xFF66D9EF),
        builtinColor = Color(0xFFFD971F),
        stringColor = Color(0xFFE6DB74),
        numberColor = Color(0xFFAE81FF),
        commentColor = Color(0xFF75715E),
        decoratorColor = Color(0xFFA6E22E),
        kivyColor = Color(0xFF66D9EF),
        operatorColor = Color(0xFFF92672),
        searchMatchColor = Color(0x66F92672)
    ),
    ONE_DARK(
        displayName = "One Dark",
        backgroundColor = Color(0xFF282C34),
        gutterColor = Color(0xFF21252B),
        gutterTextColor = Color(0xFF5C6370),
        textColor = Color(0xFFABB2BF),
        keywordColor = Color(0xFFC678DD),
        functionColor = Color(0xFF61AFEF),
        classColor = Color(0xFFE5C07B),
        builtinColor = Color(0xFF56B6C2),
        stringColor = Color(0xFF98C379),
        numberColor = Color(0xFFD19A66),
        commentColor = Color(0xFF5C6370),
        decoratorColor = Color(0xFFE06C75),
        kivyColor = Color(0xFF56B6C2),
        operatorColor = Color(0xFF56B6C2),
        searchMatchColor = Color(0x6661AFEF)
    ),
    DRACULA(
        displayName = "Dracula",
        backgroundColor = Color(0xFF282A36),
        gutterColor = Color(0xFF1E1F29),
        gutterTextColor = Color(0xFF6272A4),
        textColor = Color(0xFFF8F8F2),
        keywordColor = Color(0xFFFF79C6),
        functionColor = Color(0xFF50FA7B),
        classColor = Color(0xFF8BE9FD),
        builtinColor = Color(0xFFBD93F9),
        stringColor = Color(0xFFF1FA8C),
        numberColor = Color(0xFFBD93F9),
        commentColor = Color(0xFF6272A4),
        decoratorColor = Color(0xFFFFB86C),
        kivyColor = Color(0xFF8BE9FD),
        operatorColor = Color(0xFFFF79C6),
        searchMatchColor = Color(0x66FF79C6)
    ),
    CYBERPUNK(
        displayName = "Cyberpunk",
        backgroundColor = Color(0xFF090D16),
        gutterColor = Color(0xFF060910),
        gutterTextColor = Color(0xFF334155),
        textColor = Color(0xFFE2E8F0),
        keywordColor = Color(0xFFFF007F),
        functionColor = Color(0xFF00F0FF),
        classColor = Color(0xFF39FF14),
        builtinColor = Color(0xFFFFE600),
        stringColor = Color(0xFF00E676),
        numberColor = Color(0xFFFF9100),
        commentColor = Color(0xFF64748B),
        decoratorColor = Color(0xFFFF007F),
        kivyColor = Color(0xFF00F0FF),
        operatorColor = Color(0xFFFFE600),
        searchMatchColor = Color(0x8800F0FF)
    )
}

class PythonSyntaxHighlighter(
    var theme: EditorTheme = EditorTheme.VS_CODE_DARK,
    var searchQuery: String = "",
    var isKvLanguage: Boolean = false
) : VisualTransformation {

    companion object {
        // Python Keywords
        val PYTHON_KEYWORDS = setOf(
            "def", "class", "return", "if", "elif", "else", "while", "for", "in",
            "try", "except", "finally", "raise", "with", "as", "yield", "lambda",
            "async", "await", "import", "from", "pass", "break", "continue",
            "assert", "global", "nonlocal", "del", "not", "and", "or", "is"
        )

        // Built-ins and Special Constants
        val PYTHON_BUILTINS = setOf(
            "True", "False", "None", "self", "cls",
            "print", "len", "range", "str", "int", "float", "list", "dict", "set", "tuple",
            "bool", "type", "isinstance", "issubclass", "enumerate", "zip", "min", "max",
            "sum", "abs", "round", "open", "super", "id", "map", "filter", "any", "all",
            "iter", "next", "reversed", "sorted", "repr", "dir", "hasattr", "getattr", "setattr"
        )

        // Kivy UI framework classes & properties
        val KIVY_CLASSES = setOf(
            "App", "Widget", "BoxLayout", "GridLayout", "FloatLayout", "AnchorLayout",
            "StackLayout", "RelativeLayout", "ScrollView", "Button", "Label", "TextInput",
            "Slider", "Switch", "CheckBox", "ProgressBar", "Image", "AsyncImage",
            "ScreenManager", "Screen", "Popup", "ModalView", "Dropdown", "ActionBar",
            "TabbedPanel", "Clock", "Animation", "Builder", "StringProperty",
            "NumericProperty", "BooleanProperty", "ObjectProperty", "ListProperty",
            "DictProperty", "Color", "Rectangle", "Ellipse", "Line", "Canvas", "EventDispatcher"
        )

        // KV language specific layout properties and events
        val KV_PROPERTIES = setOf(
            "orientation", "size_hint", "size_hint_x", "size_hint_y", "size", "pos",
            "pos_hint", "padding", "spacing", "text", "font_size", "font_name",
            "background_color", "background_normal", "color", "multiline", "readonly",
            "min", "max", "value", "step", "active", "source", "keep_ratio",
            "allow_stretch", "cols", "rows", "spacing", "row_default_height",
            "on_press", "on_release", "on_touch_down", "on_touch_move", "on_touch_up",
            "on_text", "on_value", "on_active", "root", "app", "id", "canvas", "canvas.before", "canvas.after"
        )
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val annotated = buildAnnotatedString {
            append(raw)

            val length = raw.length

            // Helper to add style safely
            fun addSafeStyle(style: SpanStyle, start: Int, end: Int) {
                val s = start.coerceIn(0, length)
                val e = end.coerceIn(0, length)
                if (s < e) {
                    addStyle(style, s, e)
                }
            }

            // Track spans already styled as strings or comments to avoid regex conflict
            val stringOrCommentRanges = mutableListOf<IntRange>()

            // 1. Triple-quoted multiline strings (""" or ''')
            val tripleQuoteRegex = Regex("(\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?''')")
            tripleQuoteRegex.findAll(raw).forEach { match ->
                addSafeStyle(
                    SpanStyle(color = theme.stringColor, fontStyle = FontStyle.Italic),
                    match.range.first,
                    match.range.last + 1
                )
                stringOrCommentRanges.add(match.range)
            }

            // 2. Line-by-line parsing for single-line strings, comments, decorators, and tokens
            val lines = raw.split("\n")
            var lineOffset = 0

            for (line in lines) {
                val lineStart = lineOffset
                val lineEnd = lineStart + line.length

                // Check if this whole line is inside a triple-quote block
                val insideTripleQuote = stringOrCommentRanges.any { it.contains(lineStart) && it.contains(lineEnd - 1) }

                if (!insideTripleQuote) {
                    // Check for comments (# ...)
                    var commentStartIndex = -1
                    var inQuoteChar: Char? = null
                    var isEscaped = false

                    for (i in line.indices) {
                        val ch = line[i]
                        if (isEscaped) {
                            isEscaped = false
                            continue
                        }
                        if (ch == '\\') {
                            isEscaped = true
                            continue
                        }
                        if (inQuoteChar == null) {
                            if (ch == '"' || ch == '\'') {
                                inQuoteChar = ch
                            } else if (ch == '#') {
                                commentStartIndex = i
                                break
                            }
                        } else if (ch == inQuoteChar) {
                            inQuoteChar = null
                        }
                    }

                    // Apply comment style if found
                    val codeEndIndex = if (commentStartIndex != -1) {
                        val commentGlobalStart = lineStart + commentStartIndex
                        addSafeStyle(
                            SpanStyle(color = theme.commentColor, fontStyle = FontStyle.Italic),
                            commentGlobalStart,
                            lineEnd
                        )
                        stringOrCommentRanges.add(commentStartIndex until line.length)
                        commentStartIndex
                    } else {
                        line.length
                    }

                    val codeSection = line.substring(0, codeEndIndex)

                    // Match single-quoted and double-quoted strings (including f"", r"", b"" prefixes)
                    val strRegex = Regex("(?i)(?:[frb]{1,2})?(\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"|'[^'\\\\]*(?:\\\\.[^'\\\\]*)*')")
                    strRegex.findAll(codeSection).forEach { match ->
                        val globalStart = lineStart + match.range.first
                        val globalEnd = lineStart + match.range.last + 1
                        addSafeStyle(
                            SpanStyle(color = theme.stringColor),
                            globalStart,
                            globalEnd
                        )
                        stringOrCommentRanges.add(match.range)
                    }

                    // Match Decorators (@staticmethod, @property, etc.)
                    val trimmed = codeSection.trimStart()
                    if (trimmed.startsWith("@")) {
                        val decoratorStart = lineStart + codeSection.indexOf('@')
                        addSafeStyle(
                            SpanStyle(color = theme.decoratorColor, fontWeight = FontWeight.SemiBold),
                            decoratorStart,
                            lineStart + codeEndIndex
                        )
                    } else {
                        // Function definitions: def func_name(...)
                        val defRegex = Regex("\\bdef\\s+([a-zA-Z_][a-zA-Z0-9_]*)")
                        defRegex.findAll(codeSection).forEach { match ->
                            val group = match.groups[1]
                            if (group != null) {
                                val globalStart = lineStart + group.range.first
                                val globalEnd = lineStart + group.range.last + 1
                                addSafeStyle(
                                    SpanStyle(color = theme.functionColor, fontWeight = FontWeight.Bold),
                                    globalStart,
                                    globalEnd
                                )
                            }
                        }

                        // Class definitions: class ClassName(...)
                        val classRegex = Regex("\\bclass\\s+([a-zA-Z_][a-zA-Z0-9_]*)")
                        classRegex.findAll(codeSection).forEach { match ->
                            val group = match.groups[1]
                            if (group != null) {
                                val globalStart = lineStart + group.range.first
                                val globalEnd = lineStart + group.range.last + 1
                                addSafeStyle(
                                    SpanStyle(color = theme.classColor, fontWeight = FontWeight.Bold),
                                    globalStart,
                                    globalEnd
                                )
                            }
                        }

                        // KV Language Rule Header: <RootRule>: or CustomWidget:
                        if (isKvLanguage) {
                            val kvRuleRegex = Regex("^\\s*(<[a-zA-Z_][a-zA-Z0-9_@]*>|[a-zA-Z_][a-zA-Z0-9_]*):")
                            kvRuleRegex.findAll(codeSection).forEach { match ->
                                addSafeStyle(
                                    SpanStyle(color = theme.classColor, fontWeight = FontWeight.Bold),
                                    lineStart + match.range.first,
                                    lineStart + match.range.last + 1
                                )
                            }
                        }

                        // Match Numbers (hex, float, decimal, binary)
                        val numRegex = Regex("\\b(?:0[xX][0-9a-fA-F]+|0[bB][01]+|\\d+\\.?\\d*(?:[eE][+-]?\\d+)?)\\b")
                        numRegex.findAll(codeSection).forEach { match ->
                            if (!isInsideRanges(match.range, stringOrCommentRanges)) {
                                addSafeStyle(
                                    SpanStyle(color = theme.numberColor),
                                    lineStart + match.range.first,
                                    lineStart + match.range.last + 1
                                )
                            }
                        }

                        // Match Identifiers (Keywords, Built-ins, Kivy Classes, Operators)
                        val wordRegex = Regex("\\b[a-zA-Z_][a-zA-Z0-9_.]*\\b")
                        wordRegex.findAll(codeSection).forEach { match ->
                            if (!isInsideRanges(match.range, stringOrCommentRanges)) {
                                val word = match.value
                                val globalStart = lineStart + match.range.first
                                val globalEnd = lineStart + match.range.last + 1

                                when {
                                    PYTHON_KEYWORDS.contains(word) -> {
                                        addSafeStyle(
                                            SpanStyle(color = theme.keywordColor, fontWeight = FontWeight.Bold),
                                            globalStart,
                                            globalEnd
                                        )
                                    }
                                    PYTHON_BUILTINS.contains(word) -> {
                                        addSafeStyle(
                                            SpanStyle(color = theme.builtinColor, fontWeight = FontWeight.SemiBold),
                                            globalStart,
                                            globalEnd
                                        )
                                    }
                                    KIVY_CLASSES.contains(word) || (isKvLanguage && KV_PROPERTIES.contains(word)) -> {
                                        addSafeStyle(
                                            SpanStyle(color = theme.kivyColor, fontWeight = FontWeight.Bold),
                                            globalStart,
                                            globalEnd
                                        )
                                    }
                                    // Function calls: something(
                                    match.range.last + 1 < codeSection.length && codeSection[match.range.last + 1] == '(' -> {
                                        addSafeStyle(
                                            SpanStyle(color = theme.functionColor),
                                            globalStart,
                                            globalEnd
                                        )
                                    }
                                }
                            }
                        }

                        // Operators & punctuation symbols
                        val opRegex = Regex("[+\\-*/%=<>!&|^~:]+")
                        opRegex.findAll(codeSection).forEach { match ->
                            if (!isInsideRanges(match.range, stringOrCommentRanges)) {
                                addSafeStyle(
                                    SpanStyle(color = theme.operatorColor),
                                    lineStart + match.range.first,
                                    lineStart + match.range.last + 1
                                )
                            }
                        }
                    }
                }

                lineOffset += line.length + 1 // accounts for '\n'
            }

            // 3. Highlight Real-time Search Occurrences
            if (searchQuery.isNotBlank() && searchQuery.length >= 2) {
                var searchIdx = raw.indexOf(searchQuery, ignoreCase = true)
                while (searchIdx != -1) {
                    addSafeStyle(
                        SpanStyle(
                            background = theme.searchMatchColor,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        searchIdx,
                        searchIdx + searchQuery.length
                    )
                    searchIdx = raw.indexOf(searchQuery, searchIdx + searchQuery.length, ignoreCase = true)
                }
            }
        }

        return TransformedText(annotated, OffsetMapping.Identity)
    }

    private fun isInsideRanges(range: IntRange, list: List<IntRange>): Boolean {
        return list.any { it.contains(range.first) || it.contains(range.last) }
    }
}
