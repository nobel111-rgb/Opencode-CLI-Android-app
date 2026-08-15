package com.example.ui.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.*

/**
 * Syntax highlighter for code viewing across common workspace languages:
 * Python, JavaScript/TypeScript, Kotlin/Java, HTML/XML, CSS, JSON, Shell/Bash, SQL, Markdown.
 */
object SyntaxHighlighter {

  enum class Language(val displayName: String, val extensions: List<String>) {
    PYTHON("Python", listOf("py", "pyw", "ipy")),
    JAVASCRIPT("JavaScript", listOf("js", "jsx", "mjs", "cjs")),
    TYPESCRIPT("TypeScript", listOf("ts", "tsx")),
    KOTLIN("Kotlin", listOf("kt", "kts")),
    JAVA("Java", listOf("java")),
    HTML("HTML", listOf("html", "htm", "svg")),
    XML("XML", listOf("xml")),
    CSS("CSS", listOf("css", "scss", "sass", "less")),
    JSON("JSON", listOf("json")),
    SHELL("Bash / Shell", listOf("sh", "bash", "zsh", "env")),
    SQL("SQL", listOf("sql")),
    MARKDOWN("Markdown", listOf("md", "markdown")),
    PLAIN("Plain Text", listOf("txt", "log", "conf", "cfg", "ini"));

    companion object {
      fun fromFileName(fileName: String): Language {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        val nameLower = fileName.lowercase()
        if (nameLower == "dockerfile") return SHELL
        if (nameLower == "makefile") return SHELL
        return entries.firstOrNull { lang -> lang.extensions.contains(ext) } ?: PLAIN
      }
    }
  }

  // Syntax Palette Colors (VS Code / JetBrains Dark Theme inspired)
  private val ColorKeyword = Color(0xFFC77DFF) // Purple/Magenta
  private val ColorType = Color(0xFF48CAE4) // Cyan
  private val ColorFunction = Color(0xFFFEBC2E) // Amber/Yellow
  private val ColorString = Color(0xFF28C840) // Green
  private val ColorNumber = Color(0xFFF4A261) // Orange
  private val ColorComment = Color(0xFF74777F) // Slate Gray
  private val ColorAnnotation = Color(0xFFFF5F57) // Rose/Red
  private val ColorOperator = Color(0xFF00B4D8) // Blue/Cyan
  private val ColorTag = Color(0xFFFF5F57) // Rose/Coral
  private val ColorAttribute = Color(0xFFFEBC2E) // Amber
  private val ColorDefault = Color(0xFFF1F5F9) // Off-white

  // Keywords definitions
  private val PYTHON_KEYWORDS = setOf(
    "and", "as", "assert", "async", "await", "break", "class", "continue", "def",
    "del", "elif", "else", "except", "finally", "for", "from", "global", "if",
    "import", "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise",
    "return", "try", "while", "with", "yield", "match", "case", "self", "cls"
  )
  private val PYTHON_BUILTINS = setOf(
    "True", "False", "None", "print", "len", "range", "int", "str", "float", "bool",
    "list", "dict", "set", "tuple", "super", "type", "open", "enumerate", "zip", "map", "filter"
  )

  private val JS_TS_KEYWORDS = setOf(
    "abstract", "any", "as", "async", "await", "boolean", "break", "case", "catch",
    "class", "const", "constructor", "continue", "debugger", "declare", "default",
    "delete", "do", "else", "enum", "export", "extends", "false", "finally", "for",
    "from", "function", "get", "if", "implements", "import", "in", "instanceof",
    "interface", "is", "keyof", "let", "module", "namespace", "never", "new", "null",
    "number", "object", "of", "package", "private", "protected", "public", "readonly",
    "require", "return", "set", "static", "string", "super", "switch", "symbol", "this",
    "throw", "true", "try", "type", "typeof", "undefined", "unknown", "var", "void",
    "while", "with", "yield"
  )

  private val KOTLIN_KEYWORDS = setOf(
    "abstract", "actual", "annotation", "as", "break", "by", "catch", "class",
    "companion", "const", "constructor", "continue", "crossinline", "data", "delegate",
    "do", "dynamic", "else", "enum", "expect", "external", "false", "field", "file",
    "final", "finally", "for", "fun", "get", "if", "import", "in", "infix", "init",
    "inline", "inner", "interface", "internal", "is", "it", "lateinit", "noinline",
    "null", "object", "open", "operator", "out", "override", "package", "param",
    "private", "property", "protected", "public", "receiver", "reified", "return",
    "sealed", "set", "setparam", "super", "suspend", "tailrec", "this", "throw",
    "true", "try", "typealias", "typeof", "val", "var", "vararg", "when", "where", "while"
  )

  private val SQL_KEYWORDS = setOf(
    "SELECT", "FROM", "WHERE", "INSERT", "INTO", "UPDATE", "DELETE", "CREATE", "TABLE",
    "DROP", "ALTER", "JOIN", "INNER", "LEFT", "RIGHT", "OUTER", "ON", "AND", "OR",
    "NOT", "NULL", "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "GROUP", "BY", "ORDER",
    "ASC", "DESC", "HAVING", "LIMIT", "OFFSET", "UNION", "AS", "DISTINCT", "COUNT", "SUM", "AVG"
  )

  private val SHELL_KEYWORDS = setOf(
    "if", "then", "else", "elif", "fi", "case", "esac", "for", "select", "while",
    "until", "do", "done", "in", "function", "time", "echo", "export", "source",
    "alias", "cd", "mkdir", "rm", "cp", "mv", "touch", "cat", "grep", "chmod", "curl"
  )

  /**
   * Highlights a line of code according to the given language.
   */
  fun highlightLine(line: String, language: Language): AnnotatedString {
    if (line.isEmpty()) return AnnotatedString("")

    return buildAnnotatedString {
      append(line)

      when (language) {
        Language.PYTHON -> highlightPython(line, this)
        Language.JAVASCRIPT, Language.TYPESCRIPT -> highlightJsTs(line, this)
        Language.KOTLIN, Language.JAVA -> highlightKotlin(line, this)
        Language.HTML, Language.XML -> highlightHtml(line, this)
        Language.CSS -> highlightCss(line, this)
        Language.JSON -> highlightJson(line, this)
        Language.SHELL -> highlightShell(line, this)
        Language.SQL -> highlightSql(line, this)
        Language.MARKDOWN -> highlightMarkdown(line, this)
        Language.PLAIN -> {
          // Default styling
        }
      }
    }
  }

  private fun highlightPython(line: String, builder: AnnotatedString.Builder) {
    // 1. Comments
    val commentIdx = line.indexOf('#')
    if (commentIdx != -1) {
      builder.addStyle(
        SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic),
        commentIdx,
        line.length
      )
    }

    val codePart = if (commentIdx != -1) line.substring(0, commentIdx) else line

    // 2. Strings ("..." or '...')
    highlightRegex(codePart, Regex("(\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|\"[^\"]*\"|'[^']*')"), builder, ColorString)

    // 3. Decorators (@decorator)
    highlightRegex(codePart, Regex("""@\w+"""), builder, ColorAnnotation)

    // 4. Numbers
    highlightRegex(codePart, Regex("""\b\d+(\.\d+)?\b"""), builder, ColorNumber)

    // 5. Function & Class definitions
    val defMatch = Regex("""\bdef\s+([a-zA-Z_]\w*)""").find(codePart)
    if (defMatch != null && defMatch.groups[1] != null) {
      val g = defMatch.groups[1]!!
      builder.addStyle(SpanStyle(color = ColorFunction, fontWeight = FontWeight.Bold), g.range.first, g.range.last + 1)
    }
    val classMatch = Regex("""\bclass\s+([a-zA-Z_]\w*)""").find(codePart)
    if (classMatch != null && classMatch.groups[1] != null) {
      val g = classMatch.groups[1]!!
      builder.addStyle(SpanStyle(color = ColorType, fontWeight = FontWeight.Bold), g.range.first, g.range.last + 1)
    }

    // 6. Keywords
    highlightWords(codePart, PYTHON_KEYWORDS, builder, ColorKeyword, FontWeight.Bold)
    highlightWords(codePart, PYTHON_BUILTINS, builder, ColorType, FontWeight.Normal)
  }

  private fun highlightJsTs(line: String, builder: AnnotatedString.Builder) {
    // 1. Line comment //
    val commentIdx = line.indexOf("//")
    if (commentIdx != -1) {
      builder.addStyle(
        SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic),
        commentIdx,
        line.length
      )
    }
    val codePart = if (commentIdx != -1) line.substring(0, commentIdx) else line

    // 2. Strings
    highlightRegex(codePart, Regex("""(`.*?`|".*?"|'.*?')"""), builder, ColorString)

    // 3. Numbers
    highlightRegex(codePart, Regex("""\b\d+(\.\d+)?\b"""), builder, ColorNumber)

    // 4. Function definitions
    val funcMatch = Regex("""\bfunction\s+([a-zA-Z_$]\w*)""").find(codePart)
    if (funcMatch != null && funcMatch.groups[1] != null) {
      val g = funcMatch.groups[1]!!
      builder.addStyle(SpanStyle(color = ColorFunction, fontWeight = FontWeight.Bold), g.range.first, g.range.last + 1)
    }

    // Arrow functions or calls
    highlightRegex(codePart, Regex("""\b([a-zA-Z_$]\w*)\s*(?=\()"""), builder, ColorFunction)

    // 5. Keywords
    highlightWords(codePart, JS_TS_KEYWORDS, builder, ColorKeyword, FontWeight.Bold)
  }

  private fun highlightKotlin(line: String, builder: AnnotatedString.Builder) {
    // 1. Line comment //
    val commentIdx = line.indexOf("//")
    if (commentIdx != -1) {
      builder.addStyle(
        SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic),
        commentIdx,
        line.length
      )
    }
    val codePart = if (commentIdx != -1) line.substring(0, commentIdx) else line

    // 2. Annotations @Composable
    highlightRegex(codePart, Regex("""@\w+"""), builder, ColorAnnotation)

    // 3. Strings ("""...""" or "...")
    highlightRegex(codePart, Regex("""(".*?"|'.*?')"""), builder, ColorString)

    // 4. Numbers
    highlightRegex(codePart, Regex("""\b\d+(\.\d+)?[fFL]?\b"""), builder, ColorNumber)

    // 5. Function definitions
    val funMatch = Regex("""\bfun\s+([a-zA-Z_]\w*)""").find(codePart)
    if (funMatch != null && funMatch.groups[1] != null) {
      val g = funMatch.groups[1]!!
      builder.addStyle(SpanStyle(color = ColorFunction, fontWeight = FontWeight.Bold), g.range.first, g.range.last + 1)
    }

    // 6. Keywords
    highlightWords(codePart, KOTLIN_KEYWORDS, builder, ColorKeyword, FontWeight.Bold)
  }

  private fun highlightHtml(line: String, builder: AnnotatedString.Builder) {
    // 1. Strings
    highlightRegex(line, Regex("""(".*?"|'.*?')"""), builder, ColorString)

    // 2. HTML Tags (<tag>, </tag>)
    Regex("""(</?[a-zA-Z0-9_\-]+|/?>)""").findAll(line).forEach { match ->
      builder.addStyle(SpanStyle(color = ColorTag, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
    }

    // 3. Attributes (attr=)
    Regex("""\b([a-zA-Z0-9_\-]+)(?==)""").findAll(line).forEach { match ->
      builder.addStyle(SpanStyle(color = ColorAttribute), match.range.first, match.range.last + 1)
    }

    // 4. Comments <!-- ... -->
    highlightRegex(line, Regex("""<!--.*?-->"""), builder, ColorComment)
  }

  private fun highlightCss(line: String, builder: AnnotatedString.Builder) {
    // Comments
    highlightRegex(line, Regex("""/\*.*?\*/"""), builder, ColorComment)

    // Properties (color:, background-color:)
    Regex("""\b([a-zA-Z\-]+)(?=\s*:)""").findAll(line).forEach { match ->
      builder.addStyle(SpanStyle(color = ColorAttribute), match.range.first, match.range.last + 1)
    }

    // Values with units (12px, 1.5rem, #fff)
    highlightRegex(line, Regex("""#[0-9a-fA-F]{3,8}\b"""), builder, ColorNumber)
    highlightRegex(line, Regex("""\b\d+(\.\d+)?(px|rem|em|vh|vw|%|s|ms|deg)?\b"""), builder, ColorNumber)
    highlightRegex(line, Regex("""(".*?"|'.*?')"""), builder, ColorString)
  }

  private fun highlightJson(line: String, builder: AnnotatedString.Builder) {
    // JSON Key ("key":)
    Regex("""("[^"]*")\s*:""").findAll(line).forEach { match ->
      val g = match.groups[1]
      if (g != null) {
        builder.addStyle(SpanStyle(color = ColorKeyword, fontWeight = FontWeight.SemiBold), g.range.first, g.range.last + 1)
      }
    }

    // JSON String values
    Regex(""":\s*("[^"]*")""").findAll(line).forEach { match ->
      val g = match.groups[1]
      if (g != null) {
        builder.addStyle(SpanStyle(color = ColorString), g.range.first, g.range.last + 1)
      }
    }

    // Numbers & Booleans
    highlightRegex(line, Regex("""\b(true|false|null)\b"""), builder, ColorType)
    highlightRegex(line, Regex("""\b\d+(\.\d+)?\b"""), builder, ColorNumber)
  }

  private fun highlightShell(line: String, builder: AnnotatedString.Builder) {
    // Comments
    val commentIdx = line.indexOf('#')
    if (commentIdx != -1) {
      builder.addStyle(SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic), commentIdx, line.length)
    }
    val codePart = if (commentIdx != -1) line.substring(0, commentIdx) else line

    // Variables ($VAR, ${VAR})
    highlightRegex(codePart, Regex("""\$\w+|\$\{\w+\}"""), builder, ColorAnnotation)

    // Strings
    highlightRegex(codePart, Regex("""(".*?"|'.*?')"""), builder, ColorString)

    // Keywords
    highlightWords(codePart, SHELL_KEYWORDS, builder, ColorKeyword, FontWeight.Bold)

    // Flags (-f, --help)
    highlightRegex(codePart, Regex("""\B--?[\w\-]+"""), builder, ColorOperator)
  }

  private fun highlightSql(line: String, builder: AnnotatedString.Builder) {
    // Comments --
    val commentIdx = line.indexOf("--")
    if (commentIdx != -1) {
      builder.addStyle(SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic), commentIdx, line.length)
    }
    val codePart = if (commentIdx != -1) line.substring(0, commentIdx) else line

    // Strings
    highlightRegex(codePart, Regex("""'.*?'"""), builder, ColorString)

    // Numbers
    highlightRegex(codePart, Regex("""\b\d+\b"""), builder, ColorNumber)

    // Keywords (case insensitive)
    SQL_KEYWORDS.forEach { kw ->
      Regex("""(?i)\b$kw\b""").findAll(codePart).forEach { match ->
        builder.addStyle(SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
      }
    }
  }

  private fun highlightMarkdown(line: String, builder: AnnotatedString.Builder) {
    // Headers #, ##, ###
    if (line.startsWith("#")) {
      builder.addStyle(SpanStyle(color = ColorType, fontWeight = FontWeight.Bold), 0, line.length)
      return
    }
    // Bold **text**
    highlightRegex(line, Regex("""\*\*.*?\*\*"""), builder, ColorFunction)
    // Inline code `code`
    highlightRegex(line, Regex("""`.*?`"""), builder, ColorString)
    // Blockquote >
    if (line.startsWith(">")) {
      builder.addStyle(SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic), 0, line.length)
    }
  }

  private fun highlightRegex(
    text: String,
    regex: Regex,
    builder: AnnotatedString.Builder,
    color: Color,
    weight: FontWeight = FontWeight.Normal
  ) {
    regex.findAll(text).forEach { match ->
      builder.addStyle(SpanStyle(color = color, fontWeight = weight), match.range.first, match.range.last + 1)
    }
  }

  private fun highlightWords(
    text: String,
    words: Set<String>,
    builder: AnnotatedString.Builder,
    color: Color,
    weight: FontWeight
  ) {
    Regex("""\b[a-zA-Z_]\w*\b""").findAll(text).forEach { match ->
      if (words.contains(match.value)) {
        builder.addStyle(
          SpanStyle(color = color, fontWeight = weight),
          match.range.first,
          match.range.last + 1
        )
      }
    }
  }
}
