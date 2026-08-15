package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CommandHistoryEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandHistorySheet(
  historyList: List<CommandHistoryEntity>,
  workspaceName: String,
  onDismiss: () -> Unit,
  onReRunCommand: (String) -> Unit,
  onDeleteHistoryItem: (Long) -> Unit,
  onClearWorkspaceHistory: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var filterType by remember { mutableStateOf<String?>(null) }
  val clipboardManager = LocalClipboardManager.current

  val filteredList = remember(historyList, searchQuery, filterType) {
    historyList.filter { item ->
      val matchesQuery = searchQuery.isBlank() ||
        item.command.contains(searchQuery, ignoreCase = true) ||
        item.outputSnippet.contains(searchQuery, ignoreCase = true)
      val matchesFilter = filterType == null || item.executionType == filterType
      matchesQuery && matchesFilter
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = MinimalSurface,
    tonalElevation = 4.dp,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.85f)
        .padding(horizontal = 16.dp)
        .padding(bottom = 16.dp)
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MinimalPrimaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MinimalPrimary,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "CLI Command History",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MinimalTextPrimary
          )
          Text(
            text = "Tracked in Room Database • $workspaceName (${historyList.size} logs)",
            fontSize = 11.sp,
            color = MinimalTextSecondary
          )
        }

        if (historyList.isNotEmpty()) {
          IconButton(
            onClick = onClearWorkspaceHistory,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.DeleteSweep,
              contentDescription = "Clear History",
              tint = MinimalDanger,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = MinimalTextMuted,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search executed commands or logs...", fontSize = 12.sp, color = MinimalTextMuted) },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = MinimalTextMuted, modifier = Modifier.size(16.dp))
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MinimalTextMuted, modifier = Modifier.size(14.dp))
            }
          }
        },
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
          fontFamily = FontFamily.Monospace,
          fontSize = 12.sp,
          color = MinimalTextPrimary
        ),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MinimalPrimary,
          unfocusedBorderColor = MinimalOutlineVariant,
          focusedContainerColor = MinimalBackground,
          unfocusedContainerColor = MinimalBackground
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp)
      )

      // Filter Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        listOf(
          null to "All",
          "USER_CLI" to "CLI Prompts",
          "BASH" to "Bash ($)",
          "SLASH_CMD" to "Slash (/)",
          "AGENT_TOOL" to "AI Tools"
        ).forEach { (type, label) ->
          val isSelected = filterType == type
          FilterChip(
            selected = isSelected,
            onClick = { filterType = type },
            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MinimalPrimaryContainer,
              selectedLabelColor = MinimalOnPrimaryContainer,
              containerColor = MinimalSurfaceVariant,
              labelColor = MinimalTextSecondary
            ),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = isSelected,
              borderColor = if (isSelected) MinimalPrimary else MinimalOutlineVariant
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // List of History Items
      if (filteredList.isEmpty()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Terminal,
              contentDescription = null,
              tint = MinimalTextMuted,
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (searchQuery.isNotEmpty()) "No matching commands found" else "No command history recorded yet",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = MinimalTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Commands executed in terminal or by AI agent will be stored here.",
              fontSize = 11.sp,
              color = MinimalTextSecondary
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredList, key = { it.id }) { item ->
            CommandHistoryItemCard(
              item = item,
              onReRun = {
                onReRunCommand(item.command)
                onDismiss()
              },
              onCopy = {
                clipboardManager.setText(AnnotatedString(item.command))
              },
              onDelete = {
                onDeleteHistoryItem(item.id)
              }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CommandHistoryItemCard(
  item: CommandHistoryEntity,
  onReRun: () -> Unit,
  onCopy: () -> Unit,
  onDelete: () -> Unit
) {
  var isExpanded by remember { mutableStateOf(false) }
  val dateFormat = remember { SimpleDateFormat("HH:mm:ss • MMM dd", Locale.getDefault()) }
  val timeString = remember(item.timestamp) { dateFormat.format(Date(item.timestamp)) }

  val typeColor = when (item.executionType) {
    "BASH" -> Color(0xFF00838F)
    "SLASH_CMD" -> MinimalPrimary
    "AGENT_TOOL" -> Color(0xFFE65100)
    else -> MinimalTextPrimary
  }

  val statusColor = if (item.exitCode == 0) MinimalSuccess else MinimalDanger

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, MinimalOutlineVariant, RoundedCornerShape(14.dp)),
    colors = CardDefaults.cardColors(containerColor = MinimalBackground)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      // Top row: Type badge, Time, Exit code badge, Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Type Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(typeColor.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = item.executionType,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = typeColor
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Exit Code Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(statusColor.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = if (item.exitCode == 0) "exit:0 (OK)" else "exit:${item.exitCode} (ERR)",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = statusColor
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = timeString,
          fontSize = 10.sp,
          color = MinimalTextMuted,
          modifier = Modifier.weight(1f)
        )

        // Copy button
        IconButton(
          onClick = onCopy,
          modifier = Modifier.size(26.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy",
            tint = MinimalTextMuted,
            modifier = Modifier.size(14.dp)
          )
        }

        // Re-run button
        IconButton(
          onClick = onReRun,
          modifier = Modifier.size(26.dp)
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Re-run",
            tint = MinimalPrimary,
            modifier = Modifier.size(16.dp)
          )
        }

        // Delete button
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(26.dp)
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Delete",
            tint = MinimalDanger.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Command string (Dark terminal box)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(TerminalDarkBg)
          .padding(horizontal = 10.dp, vertical = 6.dp)
          .clickable { isExpanded = !isExpanded }
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "$ ",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = TerminalGreenBright
          )
          Text(
            text = item.command,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TerminalTextPrimary,
            maxLines = if (isExpanded) 10 else 2,
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Output Snippet if available
      if (item.outputSnippet.isNotBlank()) {
        AnimatedVisibility(visible = isExpanded) {
          Column(modifier = Modifier.padding(top = 6.dp)) {
            Text(
              text = "Output Log:",
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = MinimalTextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TerminalBlack)
                .padding(8.dp)
            ) {
              Text(
                text = item.outputSnippet,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = TerminalTextSecondary,
                maxLines = 8
              )
            }
          }
        }
      }
    }
  }
}
