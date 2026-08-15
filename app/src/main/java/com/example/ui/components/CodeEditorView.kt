package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.syntax.SyntaxHighlighter
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorView(
  fileName: String,
  filePath: String,
  initialContent: String,
  isReadOnly: Boolean = false,
  onSave: (String) -> Unit,
  onClose: () -> Unit,
  onRunScript: ((String) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var content by remember(initialContent) { mutableStateOf(initialContent) }
  var isEditMode by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }
  var showSearch by remember { mutableStateOf(false) }
  var showLangDropdown by remember { mutableStateOf(false) }
  var selectedLanguage by remember(fileName) {
    mutableStateOf(SyntaxHighlighter.Language.fromFileName(fileName))
  }
  var fontSizeSp by remember { mutableStateOf(12) }
  val clipboardManager = LocalClipboardManager.current
  val listState = rememberLazyListState()

  val lines = remember(content) { content.lines() }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MinimalBackground)
  ) {
    // Editor Top Toolbar
    Surface(
      color = MinimalSurface,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MinimalOutlineVariant)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onClose,
          modifier = Modifier.size(34.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = MinimalTextPrimary
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = fileName,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MinimalTextPrimary,
              maxLines = 1
            )
            Spacer(modifier = Modifier.width(8.dp))
            
            // Language selector badge pill
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MinimalPrimaryContainer)
                .border(1.dp, MinimalPrimary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                .clickable { showLangDropdown = true }
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = selectedLanguage.displayName,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = MinimalOnPrimaryContainer
                )
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = null,
                  tint = MinimalOnPrimaryContainer,
                  modifier = Modifier.size(14.dp)
                )
              }

              DropdownMenu(
                expanded = showLangDropdown,
                onDismissRequest = { showLangDropdown = false },
                modifier = Modifier.background(MinimalSurface)
              ) {
                SyntaxHighlighter.Language.entries.forEach { lang ->
                  DropdownMenuItem(
                    text = {
                      Text(
                        text = lang.displayName,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = if (lang == selectedLanguage) FontWeight.Bold else FontWeight.Normal,
                        color = if (lang == selectedLanguage) MinimalPrimary else MinimalTextPrimary
                      )
                    },
                    onClick = {
                      selectedLanguage = lang
                      showLangDropdown = false
                    }
                  )
                }
              }
            }
          }

          Text(
            text = filePath,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = MinimalTextSecondary,
            maxLines = 1
          )
        }

        // Run Script Button (if runnable)
        if (onRunScript != null && selectedLanguage in listOf(
            SyntaxHighlighter.Language.PYTHON,
            SyntaxHighlighter.Language.JAVASCRIPT,
            SyntaxHighlighter.Language.TYPESCRIPT,
            SyntaxHighlighter.Language.SHELL
          )
        ) {
          IconButton(
            onClick = {
              val cmd = when (selectedLanguage) {
                SyntaxHighlighter.Language.PYTHON -> "python $filePath"
                SyntaxHighlighter.Language.JAVASCRIPT -> "node $filePath"
                SyntaxHighlighter.Language.TYPESCRIPT -> "npx ts-node $filePath"
                SyntaxHighlighter.Language.SHELL -> "bash $filePath"
                else -> "cat $filePath"
              }
              onRunScript(cmd)
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Run Script",
              tint = MinimalSuccess,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Toggle Syntax View / Edit Mode
        IconButton(
          onClick = { isEditMode = !isEditMode },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = if (isEditMode) Icons.Default.Visibility else Icons.Default.Edit,
            contentDescription = if (isEditMode) "Switch to Syntax Viewer" else "Switch to Editor",
            tint = if (isEditMode) MinimalPrimary else MinimalTextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }

        // Search toggle
        IconButton(
          onClick = { showSearch = !showSearch },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search in Code",
            tint = if (showSearch) MinimalPrimary else MinimalTextMuted,
            modifier = Modifier.size(18.dp)
          )
        }

        // Copy button
        IconButton(
          onClick = {
            clipboardManager.setText(AnnotatedString(content))
          },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy code",
            tint = MinimalTextMuted,
            modifier = Modifier.size(18.dp)
          )
        }

        // Save button
        if (!isReadOnly) {
          IconButton(
            onClick = {
              onSave(content)
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Save,
              contentDescription = "Save",
              tint = MinimalPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    // Search Bar
    if (showSearch) {
      Surface(
        color = MinimalSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MinimalTextMuted,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          BasicTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            textStyle = TextStyle(
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              color = MinimalTextPrimary
            ),
            cursorBrush = SolidColor(MinimalPrimary),
            modifier = Modifier.weight(1f)
          )
          if (searchQuery.isNotEmpty()) {
            val matches = lines.count { it.contains(searchQuery, ignoreCase = true) }
            Text(
              text = "$matches matches",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (matches > 0) MinimalPrimary else MinimalDanger
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
              onClick = { searchQuery = "" },
              modifier = Modifier.size(20.dp)
            ) {
              Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MinimalTextMuted, modifier = Modifier.size(14.dp))
            }
          }
        }
      }
    }

    // Code Container (Dark Terminal Window)
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .background(TerminalBlack)
    ) {
      if (isEditMode) {
        // Editable Code Area
        Row(
          modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
        ) {
          // Line numbers gutter
          Column(
            modifier = Modifier
              .background(TerminalDarkBg)
              .padding(horizontal = 10.dp, vertical = 8.dp)
          ) {
            lines.indices.forEach { index ->
              Text(
                text = "${index + 1}",
                fontFamily = FontFamily.Monospace,
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp + 6).sp,
                color = TerminalTextDim
              )
            }
          }

          // Editable text area
          BasicTextField(
            value = content,
            onValueChange = { content = it },
            textStyle = TextStyle(
              fontFamily = FontFamily.Monospace,
              fontSize = fontSizeSp.sp,
              lineHeight = (fontSizeSp + 6).sp,
              color = TerminalTextPrimary
            ),
            cursorBrush = SolidColor(MinimalPrimaryContainer),
            modifier = Modifier
              .fillMaxSize()
              .padding(8.dp)
          )
        }
      } else {
        // Syntax Highlighted View Mode with line numbers & selection
        SelectionContainer {
          LazyColumn(
            state = listState,
            modifier = Modifier
              .fillMaxSize()
              .horizontalScroll(rememberScrollState())
          ) {
            itemsIndexed(lines) { index, lineText ->
              val isMatched = searchQuery.isNotEmpty() && lineText.contains(searchQuery, ignoreCase = true)
              val highlightedLine = remember(lineText, selectedLanguage) {
                SyntaxHighlighter.highlightLine(lineText, selectedLanguage)
              }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(
                    if (isMatched) MinimalPrimary.copy(alpha = 0.25f)
                    else if (index % 2 == 1) Color(0xFF141517)
                    else Color.Transparent
                  )
                  .padding(horizontal = 8.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Line Number
                Text(
                  text = "${index + 1}".padStart(4, ' '),
                  fontFamily = FontFamily.Monospace,
                  fontSize = fontSizeSp.sp,
                  color = if (isMatched) MinimalPrimary else TerminalTextDim,
                  fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier
                    .width(40.dp)
                    .padding(end = 8.dp)
                )

                // Syntax Highlighted Line Text
                Text(
                  text = highlightedLine,
                  fontFamily = FontFamily.Monospace,
                  fontSize = fontSizeSp.sp,
                  lineHeight = (fontSizeSp + 6).sp,
                  color = TerminalTextPrimary
                )
              }
            }
          }
        }
      }
    }

    // Status bar at bottom
    Surface(
      color = MinimalSurface,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MinimalOutlineVariant)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${lines.size} lines • ${content.length} chars • UTF-8",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MinimalTextSecondary
          )
          Spacer(modifier = Modifier.width(8.dp))
          // Font size adjust
          Text(
            text = "Font: ${fontSizeSp}pt",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = MinimalTextMuted
          )
          IconButton(
            onClick = { if (fontSizeSp > 9) fontSizeSp -= 1 },
            modifier = Modifier.size(20.dp)
          ) {
            Text("-", fontWeight = FontWeight.Bold, color = MinimalPrimary, fontSize = 12.sp)
          }
          IconButton(
            onClick = { if (fontSizeSp < 20) fontSizeSp += 1 },
            modifier = Modifier.size(20.dp)
          ) {
            Text("+", fontWeight = FontWeight.Bold, color = MinimalPrimary, fontSize = 12.sp)
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(if (isEditMode) MinimalWarning else MinimalSuccess)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isEditMode) "SOURCE EDIT" else "SYNTAX VIEW",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = if (isEditMode) MinimalWarning else MinimalSuccess
          )
        }
      }
    }
  }
}
