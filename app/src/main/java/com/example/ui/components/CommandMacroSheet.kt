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
import com.example.data.local.CommandMacroEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandMacroSheet(
  macroList: List<CommandMacroEntity>,
  workspaceName: String,
  onDismiss: () -> Unit,
  onRunMacro: (CommandMacroEntity) -> Unit,
  onSaveNewMacro: (name: String, command: String, category: String, description: String) -> Unit,
  onDeleteMacro: (Long) -> Unit
) {
  var selectedCategory by remember { mutableStateOf<String?>(null) }
  var searchQuery by remember { mutableStateOf("") }
  var showCreateDialog by remember { mutableStateOf(false) }
  val clipboardManager = LocalClipboardManager.current

  val categories = remember(macroList) {
    listOf("All") + macroList.map { it.category }.distinct()
  }

  val filteredMacros = remember(macroList, selectedCategory, searchQuery) {
    macroList.filter { macro ->
      val matchesCategory = selectedCategory == null || selectedCategory == "All" || macro.category == selectedCategory
      val matchesSearch = searchQuery.isBlank() ||
        macro.name.contains(searchQuery, ignoreCase = true) ||
        macro.command.contains(searchQuery, ignoreCase = true) ||
        macro.description.contains(searchQuery, ignoreCase = true)
      matchesCategory && matchesSearch
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
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = MinimalPrimary,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Command Macros & Aliases",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MinimalTextPrimary
          )
          Text(
            text = "Room Stored Quick Actions • ${macroList.size} macros available",
            fontSize = 11.sp,
            color = MinimalTextSecondary
          )
        }

        // Add Macro Button
        Button(
          onClick = { showCreateDialog = true },
          colors = ButtonDefaults.buttonColors(
            containerColor = MinimalPrimary,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.height(32.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("New Macro", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.width(6.dp))

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
        placeholder = { Text("Search macros, alias names, or commands...", fontSize = 12.sp, color = MinimalTextMuted) },
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

      // Category filter chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        categories.forEach { cat ->
          val isSelected = (selectedCategory == null && cat == "All") || selectedCategory == cat
          FilterChip(
            selected = isSelected,
            onClick = { selectedCategory = if (cat == "All") null else cat },
            label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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

      Spacer(modifier = Modifier.height(8.dp))

      // Macros List
      if (filteredMacros.isEmpty()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = null,
              tint = MinimalTextMuted,
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (searchQuery.isNotEmpty()) "No matching macros found" else "No macros saved yet",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = MinimalTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Create quick command aliases to speed up everyday development.",
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
          items(filteredMacros, key = { it.id }) { macro ->
            MacroCardItem(
              macro = macro,
              onRun = {
                onRunMacro(macro)
                onDismiss()
              },
              onCopy = {
                clipboardManager.setText(AnnotatedString(macro.command))
              },
              onDelete = {
                onDeleteMacro(macro.id)
              }
            )
          }
        }
      }
    }
  }

  // Create Macro Dialog
  if (showCreateDialog) {
    CreateMacroDialog(
      onDismiss = { showCreateDialog = false },
      onConfirm = { name, command, category, desc ->
        onSaveNewMacro(name, command, category, desc)
        showCreateDialog = false
      }
    )
  }
}

@Composable
private fun MacroCardItem(
  macro: CommandMacroEntity,
  onRun: () -> Unit,
  onCopy: () -> Unit,
  onDelete: () -> Unit
) {
  val catColor = when (macro.category) {
    "Git" -> Color(0xFFE65100)
    "Build" -> MinimalPrimary
    "Python" -> Color(0xFF00838F)
    "Node" -> Color(0xFF2E7D32)
    "Test" -> Color(0xFF6A1B9A)
    else -> Color(0xFF455A64)
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, MinimalOutlineVariant, RoundedCornerShape(14.dp)),
    colors = CardDefaults.cardColors(containerColor = MinimalBackground)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Category tag
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(catColor.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = macro.category,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = catColor
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = macro.name,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = MinimalTextPrimary,
          modifier = Modifier.weight(1f)
        )

        if (macro.usageCount > 0) {
          Text(
            text = "${macro.usageCount} runs",
            fontSize = 10.sp,
            color = MinimalTextMuted
          )
          Spacer(modifier = Modifier.width(6.dp))
        }

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

        // Run button
        IconButton(
          onClick = onRun,
          modifier = Modifier.size(26.dp)
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Execute Macro",
            tint = MinimalSuccess,
            modifier = Modifier.size(18.dp)
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

      if (macro.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = macro.description,
          fontSize = 11.sp,
          color = MinimalTextSecondary
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Command string in Dark Terminal Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(TerminalDarkBg)
          .clickable { onRun() }
          .padding(horizontal = 10.dp, vertical = 6.dp)
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
            text = macro.command,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TerminalTextPrimary,
            modifier = Modifier.weight(1f)
          )
          Icon(
            imageVector = Icons.Default.KeyboardReturn,
            contentDescription = "Run",
            tint = TerminalCyanBright,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun CreateMacroDialog(
  onDismiss: () -> Unit,
  onConfirm: (name: String, command: String, category: String, desc: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var command by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Custom") }
  var description by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Save New Command Macro", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Macro Name", fontSize = 12.sp) },
          placeholder = { Text("e.g. Run App, Clean Cache", fontSize = 12.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = command,
          onValueChange = { command = it },
          label = { Text("Terminal Command", fontSize = 12.sp) },
          placeholder = { Text("e.g. python main.py, git status", fontSize = 12.sp) },
          textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = category,
          onValueChange = { category = it },
          label = { Text("Category", fontSize = 12.sp) },
          placeholder = { Text("Git, Build, Test, Python, Custom", fontSize = 12.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description (Optional)", fontSize = 12.sp) },
          placeholder = { Text("Short note on what this does", fontSize = 12.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && command.isNotBlank()) {
            onConfirm(name.trim(), command.trim(), category.trim().ifBlank { "Custom" }, description.trim())
          }
        },
        enabled = name.isNotBlank() && command.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
          containerColor = MinimalPrimary,
          contentColor = Color.White
        )
      ) {
        Text("Save Macro")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = MinimalTextSecondary)
      }
    },
    containerColor = MinimalSurface
  )
}
