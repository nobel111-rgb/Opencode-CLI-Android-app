package com.example.ui.terminal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.AnsiText
import com.example.ui.components.DiffCard
import com.example.ui.components.ToolExecutionCard
import com.example.ui.theme.*
import com.example.viewmodel.AgentViewModel
import com.example.viewmodel.AppTab

@Composable
fun TerminalScreen(
  viewModel: AgentViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  val clipboardManager = LocalClipboardManager.current
  val terminalTheme = remember(uiState.terminalThemeType) {
    com.example.ui.theme.TerminalThemes.getScheme(uiState.terminalThemeType)
  }

  val tabCompletions = remember(inputText, uiState.fileTree) {
    viewModel.getTabCompletions(inputText)
  }

  // Auto scroll to bottom
  LaunchedEffect(uiState.terminalItems.size) {
    if (uiState.terminalItems.isNotEmpty()) {
      listState.animateScrollToItem(uiState.terminalItems.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MinimalBackground)
  ) {
    // Quick Context Banner
    Surface(
      color = MinimalSurface,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MinimalOutlineVariant)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Active Folder Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MinimalSurfaceVariant)
            .clickable { viewModel.setTab(AppTab.WORKSPACES) }
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Folder,
              contentDescription = "Workspace",
              tint = MinimalPrimary,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = uiState.currentWorkspace?.name ?: "No Workspace",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MinimalTextPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.ArrowDropDown,
              contentDescription = null,
              tint = MinimalTextMuted,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Model Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MinimalPrimaryContainer)
            .clickable { viewModel.setTab(AppTab.SETTINGS) }
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MinimalPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = uiState.selectedModel.name.take(15),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MinimalOnPrimaryContainer
            )
          }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Macros button
        IconButton(
          onClick = { viewModel.setShowCommandMacros(true) },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Command Macros",
            tint = MinimalPrimary,
            modifier = Modifier.size(18.dp)
          )
        }

        // Theme Toggle button
        IconButton(
          onClick = { viewModel.toggleTerminalTheme() },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = "Switch Terminal Theme (${uiState.terminalThemeType.displayName})",
            tint = if (uiState.terminalThemeType == com.example.ui.theme.TerminalThemeType.SOLARIZED) Color(0xFF2AA198) else MinimalPrimary,
            modifier = Modifier.size(18.dp)
          )
        }

        // History button
        IconButton(
          onClick = { viewModel.setShowCommandHistory(true) },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = "Command History",
            tint = MinimalPrimary,
            modifier = Modifier.size(18.dp)
          )
        }

        // Clear terminal
        IconButton(
          onClick = { viewModel.clearTerminal() },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Clear Terminal",
            tint = MinimalTextMuted,
            modifier = Modifier.size(18.dp)
          )
        }

        // Tree command shortcut
        IconButton(
          onClick = { viewModel.handleUserInput("/tree") },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AccountTree,
            contentDescription = "Directory Tree",
            tint = MinimalPrimary,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // Main Terminal Content Stream
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
    ) {
      LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(uiState.terminalItems, key = { it.id }) { item ->
          when (item) {
            is TerminalItem.Prompt -> {
              TerminalPromptRow(item, terminalTheme)
            }
            is TerminalItem.AgentThought -> {
              TerminalThoughtCard(item)
            }
            is TerminalItem.ToolExecution -> {
              ToolExecutionCard(item.toolCall)
            }
            is TerminalItem.DiffProposal -> {
              DiffCard(diff = item.diff, isApplied = item.isApplied)
            }
            is TerminalItem.AgentResponse -> {
              TerminalAgentResponseCard(item, onCopy = {
                clipboardManager.setText(AnnotatedString(item.text))
              })
            }
            is TerminalItem.CommandOutput -> {
              TerminalCommandOutputCard(item, terminalTheme)
            }
            is TerminalItem.SystemMessage -> {
              TerminalSystemMessageRow(item)
            }
          }
        }

        if (uiState.isAgentRunning) {
          item {
            TerminalGeneratingIndicator()
          }
        }
      }
    }

    // Interactive Tab-Completion Bar
    if (tabCompletions.isNotEmpty()) {
      Surface(
        color = terminalTheme.surfaceBg,
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, terminalTheme.cardBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Tab helper tag
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(terminalTheme.cyanAccent.copy(alpha = 0.15f))
              .padding(horizontal = 6.dp, vertical = 3.dp)
          ) {
            Text(
              text = "TAB ⇥",
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = terminalTheme.cyanAccent
            )
          }

          tabCompletions.forEach { suggestion ->
            val isSlash = suggestion.startsWith("/")
            val isPath = suggestion.contains("/") && !isSlash
            val isBash = suggestion.startsWith("$ ")

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(terminalTheme.cardBg)
                .border(1.dp, terminalTheme.cardBorder, RoundedCornerShape(8.dp))
                .clickable {
                  inputText = if (isSlash || isBash) "$suggestion " else suggestion
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = when {
                    isSlash -> Icons.Default.Terminal
                    isPath -> Icons.Default.InsertDriveFile
                    isBash -> Icons.Default.PlayArrow
                    else -> Icons.Default.Code
                  },
                  contentDescription = null,
                  tint = if (isSlash) terminalTheme.promptGreen else if (isPath) terminalTheme.cyanAccent else terminalTheme.amberAccent,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = suggestion,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = terminalTheme.textPrimary
                )
              }
            }
          }
        }
      }
    }

    // Slash Command Chips (Clean Minimalist horizontal scroll)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(MinimalSurface)
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      viewModel.slashCommands.forEach { cmd ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MinimalSurfaceVariant)
            .border(1.dp, MinimalOutlineVariant, RoundedCornerShape(20.dp))
            .clickable { inputText = cmd.command + " " }
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Text(
            text = cmd.command,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = MinimalPrimary
          )
        }
      }
    }

    // Bottom Input Bar
    Surface(
      color = MinimalSurface,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MinimalOutlineVariant)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Tab key for direct completion
        if (tabCompletions.isNotEmpty()) {
          FilledTonalButton(
            onClick = {
              val first = tabCompletions.firstOrNull()
              if (first != null) {
                inputText = if (first.startsWith("/") || first.startsWith("$ ")) "$first " else first
              }
            },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.height(42.dp)
          ) {
            Text("⇥ TAB", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
          Spacer(modifier = Modifier.width(6.dp))
        }

        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          placeholder = {
            Text(
              "Ask agent or type /help, /macros, $ cmd...",
              fontSize = 13.sp,
              color = MinimalTextMuted
            )
          },
          singleLine = false,
          maxLines = 4,
          textStyle = TextStyle(
            fontSize = 13.sp,
            color = MinimalTextPrimary
          ),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
          keyboardActions = KeyboardActions(
            onSend = {
              if (inputText.isNotBlank() && !uiState.isAgentRunning) {
                viewModel.handleUserInput(inputText)
                inputText = ""
              }
            }
          ),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MinimalPrimary,
            unfocusedBorderColor = MinimalOutlineVariant,
            focusedContainerColor = MinimalBackground,
            unfocusedContainerColor = MinimalBackground
          ),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Send Button
        IconButton(
          onClick = {
            if (inputText.isNotBlank() && !uiState.isAgentRunning) {
              viewModel.handleUserInput(inputText)
              inputText = ""
            }
          },
          enabled = inputText.isNotBlank() && !uiState.isAgentRunning,
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(if (inputText.isNotBlank() && !uiState.isAgentRunning) MinimalPrimary else MinimalSurfaceElevated)
        ) {
          Icon(
            imageVector = Icons.Default.Send,
            contentDescription = "Send",
            tint = if (inputText.isNotBlank() && !uiState.isAgentRunning) Color.White else MinimalTextMuted
          )
        }
      }
    }

    // Command History Sheet
    if (uiState.showCommandHistory) {
      com.example.ui.components.CommandHistorySheet(
        historyList = uiState.commandHistory,
        workspaceName = uiState.currentWorkspace?.name ?: "Workspace",
        onDismiss = { viewModel.setShowCommandHistory(false) },
        onReRunCommand = { cmd ->
          viewModel.reRunCommand(cmd)
        },
        onDeleteHistoryItem = { id ->
          viewModel.deleteHistoryItem(id)
        },
        onClearWorkspaceHistory = {
          viewModel.clearWorkspaceHistory()
        }
      )
    }

    // Command Macros Sheet
    if (uiState.showCommandMacros) {
      com.example.ui.components.CommandMacroSheet(
        macroList = uiState.commandMacros,
        workspaceName = uiState.currentWorkspace?.name ?: "Workspace",
        onDismiss = { viewModel.setShowCommandMacros(false) },
        onRunMacro = { macro ->
          viewModel.runMacro(macro)
        },
        onSaveNewMacro = { name, cmd, cat, desc ->
          viewModel.saveMacro(name, cmd, cat, desc)
        },
        onDeleteMacro = { id ->
          viewModel.deleteMacro(id)
        }
      )
    }
  }
}

@Composable
private fun TerminalPromptRow(
  item: TerminalItem.Prompt,
  theme: TerminalColorScheme = TerminalThemes.DarkMinimal
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.Top
  ) {
    Text(
      text = "user@${item.workspaceName}:~$ ",
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp,
      color = theme.promptGreen
    )
    SelectionContainer {
      Text(
        text = item.command,
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        color = MinimalTextPrimary
      )
    }
  }
}

@Composable
private fun TerminalThoughtCard(item: TerminalItem.AgentThought) {
  var expanded by remember { mutableStateOf(true) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, MinimalWarning.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
    colors = CardDefaults.cardColors(containerColor = MinimalSurfaceVariant)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { expanded = !expanded },
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Lightbulb,
          contentDescription = "Thinking",
          tint = MinimalWarning,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Agent Chain-of-Thought",
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp,
          color = MinimalTextPrimary,
          modifier = Modifier.weight(1f)
        )
        Icon(
          imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = null,
          tint = MinimalTextMuted,
          modifier = Modifier.size(18.dp)
        )
      }

      AnimatedVisibility(visible = expanded) {
        SelectionContainer {
          Text(
            text = item.thought,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = MinimalTextSecondary,
            modifier = Modifier.padding(top = 8.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun TerminalAgentResponseCard(
  item: TerminalItem.AgentResponse,
  onCopy: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .border(1.dp, MinimalOutlineVariant, RoundedCornerShape(18.dp)),
    colors = CardDefaults.cardColors(containerColor = MinimalSurface)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MinimalSuccess)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "OpenCode Agent (${item.modelName})",
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = MinimalPrimary,
          modifier = Modifier.weight(1f)
        )

        IconButton(
          onClick = onCopy,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy response",
            tint = MinimalTextMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      SelectionContainer {
        Text(
          text = item.text,
          fontSize = 13.sp,
          lineHeight = 19.sp,
          color = MinimalTextPrimary
        )
      }
    }
  }
}

@Composable
private fun TerminalCommandOutputCard(
  item: TerminalItem.CommandOutput,
  theme: TerminalColorScheme = TerminalThemes.DarkMinimal
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp)),
    colors = CardDefaults.cardColors(containerColor = theme.background)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      // Traffic Light Dots
      Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.padding(bottom = 8.dp)
      ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(theme.dotRed))
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(theme.dotYellow))
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(theme.dotGreen))
      }

      AnsiText(
        text = item.output,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        defaultColor = if (item.exitCode == 0) theme.textPrimary else theme.roseAccent
      )
    }
  }
}

@Composable
private fun TerminalSystemMessageRow(item: TerminalItem.SystemMessage) {
  val (color, icon) = when (item.level) {
    SystemLogLevel.INFO -> Pair(MinimalPrimary, Icons.Default.Info)
    SystemLogLevel.SUCCESS -> Pair(MinimalSuccess, Icons.Default.CheckCircle)
    SystemLogLevel.WARNING -> Pair(MinimalWarning, Icons.Default.Warning)
    SystemLogLevel.ERROR -> Pair(MinimalDanger, Icons.Default.Error)
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(color.copy(alpha = 0.08f))
      .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
      .padding(horizontal = 10.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = color,
      modifier = Modifier.size(16.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = item.message,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      color = color
    )
  }
}

@Composable
private fun TerminalGeneratingIndicator() {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, MinimalPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
    colors = CardDefaults.cardColors(containerColor = MinimalPrimaryContainer.copy(alpha = 0.4f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      CircularProgressIndicator(
        modifier = Modifier.size(16.dp),
        strokeWidth = 2.dp,
        color = MinimalPrimary
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "OpenCode Agent is executing tools & generating solution...",
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MinimalOnPrimaryContainer
      )
    }
  }
}
