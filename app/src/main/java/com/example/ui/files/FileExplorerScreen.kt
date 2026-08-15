package com.example.ui.files

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileNode
import com.example.ui.components.CodeEditorView
import com.example.ui.theme.*
import com.example.viewmodel.AgentViewModel
import com.example.viewmodel.AppTab

@Composable
fun FileExplorerScreen(
  viewModel: AgentViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  var showNewFileDialog by remember { mutableStateOf(false) }
  var showNewFolderDialog by remember { mutableStateOf(false) }
  var newFileName by remember { mutableStateOf("") }
  var newFolderName by remember { mutableStateOf("") }
  var fileSearchQuery by remember { mutableStateOf("") }
  var selectedFilterType by remember { mutableStateOf("All") } // "All", "Code", "Config", "Docs"

  // If a file is currently opened in editor, show the CodeEditorView
  if (uiState.openedFile != null) {
    CodeEditorView(
      fileName = uiState.openedFile!!.name,
      filePath = uiState.openedFile!!.path,
      initialContent = uiState.openedFileContent,
      onSave = { updatedContent ->
        viewModel.saveFileContent(uiState.openedFile!!, updatedContent)
      },
      onClose = {
        viewModel.closeOpenedFile()
      },
      onRunScript = { cmd ->
        viewModel.handleUserInput("$ $cmd")
        viewModel.setTab(AppTab.TERMINAL)
      }
    )
    return
  }

  // Flattened all files for fast search lookup
  val allFlatFiles = remember(uiState.fileTree) {
    val flat = mutableListOf<FileNode>()
    fun traverse(nodes: List<FileNode>) {
      for (node in nodes) {
        if (!node.isDirectory) flat.add(node)
        if (node.children.isNotEmpty()) traverse(node.children)
      }
    }
    traverse(uiState.fileTree)
    flat
  }

  val searchMatches = remember(allFlatFiles, fileSearchQuery, selectedFilterType) {
    if (fileSearchQuery.isBlank() && selectedFilterType == "All") {
      emptyList()
    } else {
      allFlatFiles.filter { node ->
        val matchesQuery = fileSearchQuery.isBlank() ||
          node.name.contains(fileSearchQuery, ignoreCase = true) ||
          node.path.contains(fileSearchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilterType) {
          "Code" -> node.extension.lowercase() in listOf("py", "js", "ts", "kt", "java", "html", "css", "sh", "sql")
          "Config" -> node.extension.lowercase() in listOf("json", "toml", "yaml", "yml", "xml", "env", "gradle", "properties")
          "Docs" -> node.extension.lowercase() in listOf("md", "txt", "rst", "doc")
          else -> true
        }
        matchesQuery && matchesFilter
      }
    }
  }

  val isSearching = fileSearchQuery.isNotBlank() || selectedFilterType != "All"

  val filteredTree = remember(uiState.fileTree, fileSearchQuery, selectedFilterType) {
    if (!isSearching) {
      uiState.fileTree
    } else {
      filterNodes(uiState.fileTree, fileSearchQuery.trim(), selectedFilterType)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MinimalBackground)
  ) {
    // Explorer Top Bar
    Surface(
      color = MinimalSurface,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MinimalOutlineVariant)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { viewModel.setTab(AppTab.TERMINAL) },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back to terminal",
            tint = MinimalTextPrimary
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Workspace File Explorer",
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = MinimalTextPrimary
          )
          Text(
            text = "Root: ${uiState.currentWorkspace?.name ?: "No Workspace"}",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MinimalTextSecondary
          )
        }

        // New Folder
        IconButton(
          onClick = { showNewFolderDialog = true },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CreateNewFolder,
            contentDescription = "New Folder",
            tint = MinimalTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }

        // New File
        IconButton(
          onClick = { showNewFileDialog = true },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.NoteAdd,
            contentDescription = "New File",
            tint = MinimalPrimary,
            modifier = Modifier.size(20.dp)
          )
        }

        // Refresh
        IconButton(
          onClick = { viewModel.refreshFileTree() },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh",
            tint = MinimalTextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }

    // Search Bar & Filter Chips
    if (uiState.fileTree.isNotEmpty()) {
      Surface(
        color = MinimalSurface,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Column {
          OutlinedTextField(
            value = fileSearchQuery,
            onValueChange = { fileSearchQuery = it },
            placeholder = { Text("Search files by name or path...", fontSize = 12.sp, color = MinimalTextMuted) },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = null, tint = MinimalPrimary, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
              if (fileSearchQuery.isNotEmpty()) {
                IconButton(onClick = { fileSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
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
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Filter chips & match indicator
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val filterOptions = listOf("All", "Code", "Config", "Docs")
            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.weight(1f)
            ) {
              filterOptions.forEach { opt ->
                val isSelected = selectedFilterType == opt
                FilterChip(
                  selected = isSelected,
                  onClick = { selectedFilterType = opt },
                  label = { Text(opt, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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
                  ),
                  modifier = Modifier.height(28.dp)
                )
              }
            }

            if (isSearching) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(MinimalPrimaryContainer)
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = "${searchMatches.size} found",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = MinimalOnPrimaryContainer
                )
              }
            }
          }
        }
      }
    }

    // Files List View
    if (uiState.fileTree.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            tint = MinimalTextMuted,
            modifier = Modifier.size(56.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Workspace is currently empty",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MinimalTextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Create files or let the AI Agent generate project code",
            fontSize = 12.sp,
            color = MinimalTextSecondary
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = { showNewFileDialog = true },
            colors = ButtonDefaults.buttonColors(
              containerColor = MinimalPrimary,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create File")
          }
        }
      }
    } else if (isSearching && searchMatches.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = MinimalTextMuted,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "No files match \"$fileSearchQuery\"",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = MinimalTextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Try searching for a different extension or file name",
            fontSize = 11.sp,
            color = MinimalTextSecondary
          )
        }
      }
    } else if (isSearching && fileSearchQuery.isNotBlank()) {
      // Direct Search Result Matches Flat List for lightning quick navigation!
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(searchMatches, key = { it.path }) { node ->
          SearchResultFileCard(
            node = node,
            query = fileSearchQuery,
            onOpenFile = { viewModel.openFileForEditing(node) },
            onDeleteFile = { viewModel.deleteFileInWorkspace(node.path) },
            onRunScript = { fileNode ->
              val cmd = when (fileNode.extension.lowercase()) {
                "py" -> "python ${fileNode.path}"
                "js" -> "node ${fileNode.path}"
                "ts" -> "npx ts-node ${fileNode.path}"
                "sh" -> "bash ${fileNode.path}"
                else -> "cat ${fileNode.path}"
              }
              viewModel.handleUserInput("$ $cmd")
              viewModel.setTab(AppTab.TERMINAL)
            }
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(filteredTree, key = { it.path }) { node ->
          FileTreeNodeItem(
            node = node,
            depth = 0,
            searchQuery = fileSearchQuery,
            onOpenFile = { fileNode ->
              viewModel.openFileForEditing(fileNode)
            },
            onDeleteFile = { path ->
              viewModel.deleteFileInWorkspace(path)
            },
            onRunScript = { fileNode ->
              val cmd = when (fileNode.extension.lowercase()) {
                "py" -> "python ${fileNode.path}"
                "js" -> "node ${fileNode.path}"
                "ts" -> "npx ts-node ${fileNode.path}"
                "sh" -> "bash ${fileNode.path}"
                else -> "cat ${fileNode.path}"
              }
              viewModel.handleUserInput("$ $cmd")
              viewModel.setTab(AppTab.TERMINAL)
            }
          )
        }
      }
    }
  }

  // Create File Dialog
  if (showNewFileDialog) {
    AlertDialog(
      onDismissRequest = { showNewFileDialog = false },
      title = {
        Text("Create New File", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      },
      text = {
        Column {
          Text(
            "Enter relative path (e.g. script.py, src/app.js):",
            fontSize = 12.sp,
            color = MinimalTextSecondary
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = newFileName,
            onValueChange = { newFileName = it },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
              fontFamily = FontFamily.Monospace,
              fontSize = 13.sp,
              color = MinimalTextPrimary
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MinimalPrimary,
              unfocusedBorderColor = MinimalOutlineVariant,
              focusedContainerColor = MinimalBackground,
              unfocusedContainerColor = MinimalBackground
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newFileName.isNotBlank()) {
              viewModel.createNewFileInWorkspace(newFileName.trim(), "")
              newFileName = ""
              showNewFileDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = MinimalPrimary,
            contentColor = Color.White
          )
        ) {
          Text("Create")
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewFileDialog = false }) {
          Text("Cancel", color = MinimalTextSecondary)
        }
      },
      containerColor = MinimalSurface
    )
  }

  // Create Folder Dialog
  if (showNewFolderDialog) {
    AlertDialog(
      onDismissRequest = { showNewFolderDialog = false },
      title = {
        Text("Create New Directory", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      },
      text = {
        Column {
          Text(
            "Enter folder name (e.g. src, models, tests):",
            fontSize = 12.sp,
            color = MinimalTextSecondary
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = newFolderName,
            onValueChange = { newFolderName = it },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
              fontFamily = FontFamily.Monospace,
              fontSize = 13.sp,
              color = MinimalTextPrimary
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MinimalPrimary,
              unfocusedBorderColor = MinimalOutlineVariant,
              focusedContainerColor = MinimalBackground,
              unfocusedContainerColor = MinimalBackground
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newFolderName.isNotBlank()) {
              viewModel.createNewFileInWorkspace("${newFolderName.trim()}/.gitkeep", "")
              newFolderName = ""
              showNewFolderDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = MinimalPrimary,
            contentColor = Color.White
          )
        ) {
          Text("Create")
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewFolderDialog = false }) {
          Text("Cancel", color = MinimalTextSecondary)
        }
      },
      containerColor = MinimalSurface
    )
  }
}

@Composable
private fun SearchResultFileCard(
  node: FileNode,
  query: String,
  onOpenFile: (FileNode) -> Unit,
  onDeleteFile: (String) -> Unit,
  onRunScript: (FileNode) -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  val icon = when {
    node.extension.lowercase() == "py" -> Icons.Default.Terminal
    node.extension.lowercase() in listOf("js", "ts", "jsx", "tsx") -> Icons.Default.Javascript
    node.extension.lowercase() in listOf("html", "xml") -> Icons.Default.Html
    node.extension.lowercase() in listOf("css", "scss") -> Icons.Default.Css
    node.extension.lowercase() == "kt" -> Icons.Default.Bolt
    node.extension.lowercase() in listOf("md", "txt", "doc") -> Icons.Default.Description
    node.extension.lowercase() == "json" -> Icons.Default.DataArray
    else -> Icons.Default.InsertDriveFile
  }

  val iconColor = when {
    node.extension.lowercase() == "py" -> Color(0xFF00838F)
    node.extension.lowercase() in listOf("js", "ts") -> Color(0xFFE65100)
    node.extension.lowercase() in listOf("html", "xml") -> Color(0xFFC2185B)
    node.extension.lowercase() in listOf("css", "scss") -> Color(0xFF512DA8)
    node.extension.lowercase() == "kt" -> Color(0xFF7B1FA2)
    node.extension.lowercase() == "json" -> Color(0xFFF57F17)
    else -> MinimalTextSecondary
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .border(1.dp, MinimalOutlineVariant, RoundedCornerShape(12.dp))
      .clickable { onOpenFile(node) },
    colors = CardDefaults.cardColors(containerColor = MinimalSurface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconColor,
        modifier = Modifier.size(22.dp)
      )

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        // Highlight matched query in filename
        val annotatedName = buildAnnotatedString {
          val name = node.name
          val idx = name.indexOf(query, ignoreCase = true)
          if (idx >= 0 && query.isNotEmpty()) {
            append(name.substring(0, idx))
            withStyle(SpanStyle(background = MinimalPrimaryContainer, color = MinimalPrimary, fontWeight = FontWeight.Bold)) {
              append(name.substring(idx, idx + query.length))
            }
            append(name.substring(idx + query.length))
          } else {
            append(name)
          }
        }

        Text(
          text = annotatedName,
          fontFamily = FontFamily.Monospace,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = MinimalTextPrimary
        )

        Text(
          text = node.path,
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          color = MinimalTextMuted
        )
      }

      val sizeStr = if (node.size < 1024) "${node.size}B" else "${node.size / 1024}KB"
      Text(
        text = sizeStr,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        color = MinimalTextMuted
      )

      Spacer(modifier = Modifier.width(4.dp))

      Box {
        IconButton(
          onClick = { showMenu = true },
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Options",
            tint = MinimalTextMuted,
            modifier = Modifier.size(18.dp)
          )
        }

        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false },
          modifier = Modifier.background(MinimalSurface)
        ) {
          DropdownMenuItem(
            text = { Text("Open in Code Viewer", fontSize = 13.sp) },
            onClick = {
              showMenu = false
              onOpenFile(node)
            },
            leadingIcon = { Icon(Icons.Default.Code, contentDescription = null, tint = MinimalPrimary) }
          )
          if (node.extension.lowercase() in listOf("py", "js", "ts", "sh")) {
            DropdownMenuItem(
              text = { Text("Execute in CLI", fontSize = 13.sp) },
              onClick = {
                showMenu = false
                onRunScript(node)
              },
              leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MinimalSuccess) }
            )
          }
          DropdownMenuItem(
            text = { Text("Delete", fontSize = 13.sp, color = MinimalDanger) },
            onClick = {
              showMenu = false
              onDeleteFile(node.path)
            },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MinimalDanger) }
          )
        }
      }
    }
  }
}

private fun filterNodes(nodes: List<FileNode>, query: String, filterType: String): List<FileNode> {
  return nodes.mapNotNull { node ->
    val matchesFilter = when (filterType) {
      "Code" -> node.isDirectory || node.extension.lowercase() in listOf("py", "js", "ts", "kt", "java", "html", "css", "sh", "sql")
      "Config" -> node.isDirectory || node.extension.lowercase() in listOf("json", "toml", "yaml", "yml", "xml", "env", "gradle", "properties")
      "Docs" -> node.isDirectory || node.extension.lowercase() in listOf("md", "txt", "rst", "doc")
      else -> true
    }

    if (node.name.contains(query, ignoreCase = true) && matchesFilter) {
      node
    } else if (node.isDirectory) {
      val filteredChildren = filterNodes(node.children, query, filterType)
      if (filteredChildren.isNotEmpty()) {
        node.copy(children = filteredChildren)
      } else {
        null
      }
    } else {
      null
    }
  }
}

@Composable
fun FileTreeNodeItem(
  node: FileNode,
  depth: Int,
  searchQuery: String = "",
  onOpenFile: (FileNode) -> Unit,
  onDeleteFile: (String) -> Unit,
  onRunScript: (FileNode) -> Unit
) {
  var isExpanded by remember { mutableStateOf(true) }
  var showMenu by remember { mutableStateOf(false) }

  val icon = when {
    node.isDirectory -> if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder
    node.extension.lowercase() == "py" -> Icons.Default.Terminal
    node.extension.lowercase() in listOf("js", "ts", "jsx", "tsx") -> Icons.Default.Javascript
    node.extension.lowercase() in listOf("html", "xml") -> Icons.Default.Html
    node.extension.lowercase() in listOf("css", "scss") -> Icons.Default.Css
    node.extension.lowercase() == "kt" -> Icons.Default.Bolt
    node.extension.lowercase() in listOf("md", "txt", "doc") -> Icons.Default.Description
    node.extension.lowercase() == "json" -> Icons.Default.DataArray
    else -> Icons.Default.InsertDriveFile
  }

  val iconColor = when {
    node.isDirectory -> MinimalPrimary
    node.extension.lowercase() == "py" -> Color(0xFF00838F)
    node.extension.lowercase() in listOf("js", "ts") -> Color(0xFFE65100)
    node.extension.lowercase() in listOf("html", "xml") -> Color(0xFFC2185B)
    node.extension.lowercase() in listOf("css", "scss") -> Color(0xFF512DA8)
    node.extension.lowercase() == "kt" -> Color(0xFF7B1FA2)
    node.extension.lowercase() == "json" -> Color(0xFFF57F17)
    else -> MinimalTextSecondary
  }

  Column {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MinimalSurface)
        .border(1.dp, MinimalOutlineVariant, RoundedCornerShape(12.dp))
        .clickable {
          if (node.isDirectory) {
            isExpanded = !isExpanded
          } else {
            onOpenFile(node)
          }
        }
        .padding(start = (depth * 14 + 10).dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconColor,
        modifier = Modifier.size(20.dp)
      )

      Spacer(modifier = Modifier.width(10.dp))

      Text(
        text = node.name,
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        fontWeight = if (node.isDirectory) FontWeight.SemiBold else FontWeight.Normal,
        color = MinimalTextPrimary,
        modifier = Modifier.weight(1f)
      )

      if (!node.isDirectory) {
        val sizeStr = if (node.size < 1024) "${node.size}B" else "${node.size / 1024}KB"
        Text(
          text = sizeStr,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          color = MinimalTextMuted
        )
      }

      Box {
        IconButton(
          onClick = { showMenu = true },
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Options",
            tint = MinimalTextMuted,
            modifier = Modifier.size(18.dp)
          )
        }

        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false },
          modifier = Modifier.background(MinimalSurface)
        ) {
          if (!node.isDirectory) {
            DropdownMenuItem(
              text = { Text("Open in Code Viewer", fontSize = 13.sp) },
              onClick = {
                showMenu = false
                onOpenFile(node)
              },
              leadingIcon = { Icon(Icons.Default.Code, contentDescription = null, tint = MinimalPrimary) }
            )
            if (node.extension.lowercase() in listOf("py", "js", "ts", "sh")) {
              DropdownMenuItem(
                text = { Text("Execute in CLI", fontSize = 13.sp) },
                onClick = {
                  showMenu = false
                  onRunScript(node)
                },
                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MinimalSuccess) }
              )
            }
          }
          DropdownMenuItem(
            text = { Text("Delete", fontSize = 13.sp, color = MinimalDanger) },
            onClick = {
              showMenu = false
              onDeleteFile(node.path)
            },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MinimalDanger) }
          )
        }
      }
    }

    if (node.isDirectory && isExpanded) {
      node.children.forEach { child ->
        FileTreeNodeItem(
          node = child,
          depth = depth + 1,
          searchQuery = searchQuery,
          onOpenFile = onOpenFile,
          onDeleteFile = onDeleteFile,
          onRunScript = onRunScript
        )
      }
    }
  }
}
