package com.example.ui.components

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AnsiText(
  text: String,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = 13.sp,
  lineHeight: TextUnit = 18.sp,
  defaultColor: Color = TerminalTextPrimary
) {
  val annotated = parseAnsiToAnnotatedString(text, defaultColor)
  SelectionContainer {
    Text(
      text = annotated,
      modifier = modifier,
      fontFamily = FontFamily.Monospace,
      fontSize = fontSize,
      lineHeight = lineHeight
    )
  }
}

fun parseAnsiToAnnotatedString(raw: String, defaultColor: Color): AnnotatedString {
  return buildAnnotatedString {
    var cursor = 0
    var currentColor = defaultColor
    var isBold = false

    val regex = Regex("""\u001B\[([0-9;]*)m""")
    val matches = regex.findAll(raw).toList()

    if (matches.isEmpty()) {
      append(raw)
      addStyle(SpanStyle(color = defaultColor), 0, raw.length)
      return@buildAnnotatedString
    }

    matches.forEach { match ->
      val start = match.range.first
      val end = match.range.last + 1

      if (start > cursor) {
        val segment = raw.substring(cursor, start)
        val segStart = length
        append(segment)
        addStyle(
          SpanStyle(
            color = currentColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
          ),
          segStart,
          length
        )
      }

      val codes = match.groupValues[1].split(';').mapNotNull { it.toIntOrNull() }
      if (codes.isEmpty() || codes.contains(0)) {
        currentColor = defaultColor
        isBold = false
      }
      if (codes.contains(1)) isBold = true
      if (codes.contains(31)) currentColor = TerminalRoseBright
      if (codes.contains(32)) currentColor = TerminalGreenBright
      if (codes.contains(33)) currentColor = TerminalAmberBright
      if (codes.contains(34)) currentColor = TerminalCyanBright
      if (codes.contains(35)) currentColor = TerminalPurpleBright
      if (codes.contains(36)) currentColor = TerminalCyan
      if (codes.contains(37)) currentColor = TerminalTextPrimary

      cursor = end
    }

    if (cursor < raw.length) {
      val remaining = raw.substring(cursor)
      val segStart = length
      append(remaining)
      addStyle(
        SpanStyle(
          color = currentColor,
          fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        ),
        segStart,
        length
      )
    }
  }
}
