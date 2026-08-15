package com.example.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WorkspaceEntity
import com.example.data.model.WorkspaceTemplateCatalog
import com.example.ui.theme.*
import com.example.viewmodel.AgentViewModel
import com.example.viewmodel.AppTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkspaceSelectScreen(
  viewModel: AgentViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  var newFolderName by remember { mutableStateOf("") }
  var newFolderDesc by remember { mutableStateOf("") }
  var selectedTemplateId by remember { mutableStateOf("PYTHON_APP") }
  var isCreatingNew by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MinimalBackground)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header Section
      item {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
          Text(
            text = "Welcome, Agent.",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = MinimalTextPrimary,
            letterSpacing = (-0.5).sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Initialize your coding workspace to begin using the OpenCode CLI environment.",
            fontSize = 13.sp,
            color = MinimalTextSecondary,
            lineHeight = 18.sp
          )
        }
      }

      // Terminal Preview Box with Traffic Light Dots (from HTML Design spec)
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFF2F3033), RoundedCornerShape(20.dp)),
          colors = CardDefaults.cardColors(containerColor = TerminalBlack)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Traffic Light Dots
            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(TerminalDotRed))
              Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(TerminalDotYellow))
              Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(TerminalDotGreen))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row {
                Text(text = "$ ", fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = MinimalTextMuted)
                Text(text = "opencode init", fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = TerminalTextPrimary)
              }
              Text(
                text = if (uiState.currentWorkspace != null)
                  "> Workspace active: ${uiState.currentWorkspace?.name}"
                else
                  "> Searching for root directory...",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = MinimalPrimaryContainer
              )
              if (uiState.currentWorkspace == null) {
                Text(
                  text = "! No workspace detected.",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 13.sp,
                  color = MinimalDanger
                )
              } else {
                Text(
                  text = "✓ Environment initialized successfully.",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 13.sp,
                  color = MinimalSuccess
                )
              }
            }
          }
        }
      }

      // Section Title
      item {
        Text(
          text = "WORKSPACE SETUP",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = MinimalTextMuted,
          modifier = Modifier.padding(horizontal = 4.dp)
        )
      }

      // Primary Action Buttons (Matching HTML Design: Select folder & Create root directory)
      if (!isCreatingNew) {
        item {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Button 1: Select existing folder
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable { isCreatingNew = false },
              colors = CardDefaults.cardColors(containerColor = MinimalPrimaryContainer)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.FolderOpen,
                  contentDescription = null,
                  tint = MinimalOnPrimaryContainer,
                  modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Select existing workspace",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MinimalOnPrimaryContainer
                  )
                  Text(
                    text = "${uiState.allWorkspaces.size} workspace(s) available",
                    fontSize = 12.sp,
                    color = MinimalOnPrimaryContainer.copy(alpha = 0.7f)
                  )
                }
                Icon(
                  imageVector = Icons.Default.ChevronRight,
                  contentDescription = null,
                  tint = MinimalOnPrimaryContainer
                )
              }
            }

            // Button 2: Create root directory
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MinimalOutlineVariant, RoundedCornerShape(24.dp))
                .clickable { isCreatingNew = true },
              colors = CardDefaults.cardColors(containerColor = MinimalSurface)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CreateNewFolder,
                  contentDescription = null,
                  tint = MinimalTextPrimary,
                  modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Create root directory",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MinimalTextPrimary
                  )
                  Text(
                    text = "A fresh start for your coding projects",
                    fontSize = 12.sp,
                    color = MinimalTextSecondary
                  )
                }
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  tint = MinimalTextMuted
                )
              }
            }
          }
        }

        // List of existing workspaces
        items(uiState.allWorkspaces, key = { it.id }) { ws ->
          val isCurrent = ws.id == uiState.currentWorkspace?.id
          WorkspaceListItem(
            workspace = ws,
            isCurrent = isCurrent,
            onSelect = {
              viewModel.selectWorkspace(ws)
              viewModel.setTab(AppTab.TERMINAL)
            },
            onDelete = {
              viewModel.deleteWorkspace(ws)
            }
          )
        }
      } else {
        // Create New Workspace Form
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(20.dp))
              .border(1.dp, MinimalOutlineVariant, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MinimalSurface)
          ) {
            Column(modifier = Modifier.padding(20.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Create Workspace Folder",
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = MinimalTextPrimary
                )
                IconButton(onClick = { isCreatingNew = false }) {
                  Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MinimalTextMuted)
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = "FOLDER / PROJECT NAME",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalTextMuted
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = newFolderName,
                onValueChange = { newFolderName = it },
                placeholder = { Text("e.g. my-python-tool, web-app", fontSize = 13.sp, color = MinimalTextMuted) },
                singleLine = true,
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = MinimalTextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = MinimalPrimary,
                  unfocusedBorderColor = MinimalOutlineVariant,
                  focusedContainerColor = MinimalBackground,
                  unfocusedContainerColor = MinimalBackground
                ),
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(modifier = Modifier.height(14.dp))

              Text(
                text = "STARTER TEMPLATE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalTextMuted
              )
              Spacer(modifier = Modifier.height(6.dp))
              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                items(WorkspaceTemplateCatalog.TEMPLATES) { template ->
                  val isSelected = template.id == selectedTemplateId
                  Box(
                    modifier = Modifier
                      .width(130.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(if (isSelected) MinimalPrimaryContainer else MinimalSurfaceVariant)
                      .border(
                        1.dp,
                        if (isSelected) MinimalPrimary else MinimalOutlineVariant,
                        RoundedCornerShape(12.dp)
                      )
                      .clickable { selectedTemplateId = template.id }
                      .padding(10.dp)
                  ) {
                    Column {
                      Text(text = template.icon, fontSize = 18.sp)
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                        text = template.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = if (isSelected) MinimalOnPrimaryContainer else MinimalTextPrimary,
                        maxLines = 1
                      )
                      Text(
                        text = template.description,
                        fontSize = 9.sp,
                        color = MinimalTextSecondary,
                        maxLines = 2
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              Text(
                text = "DESCRIPTION (OPTIONAL)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalTextMuted
              )
              Spacer(modifier = Modifier.height(4.dp))
              OutlinedTextField(
                value = newFolderDesc,
                onValueChange = { newFolderDesc = it },
                placeholder = { Text("Brief purpose for this workspace", fontSize = 12.sp, color = MinimalTextMuted) },
                singleLine = true,
                textStyle = TextStyle(fontSize = 12.sp, color = MinimalTextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = MinimalPrimary,
                  unfocusedBorderColor = MinimalOutlineVariant,
                  focusedContainerColor = MinimalBackground,
                  unfocusedContainerColor = MinimalBackground
                ),
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(modifier = Modifier.height(18.dp))

              Button(
                onClick = {
                  val name = newFolderName.trim().ifEmpty { "project-${System.currentTimeMillis().toString().takeLast(4)}" }
                  viewModel.createWorkspace(name, selectedTemplateId, newFolderDesc)
                  isCreatingNew = false
                  viewModel.setTab(AppTab.TERMINAL)
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = MinimalPrimary,
                  contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create & Open Workspace", fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }

      // Information Footer (Matching HTML Design spec)
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
          colors = CardDefaults.cardColors(containerColor = MinimalSurfaceVariant)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = MinimalTextSecondary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "By continuing, the OpenCode AI agent will use Zen Providers free models for code generation, file modification, and analysis.",
              fontSize = 12.sp,
              color = MinimalTextSecondary,
              lineHeight = 16.sp
            )
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun WorkspaceListItem(
  workspace: WorkspaceEntity,
  isCurrent: Boolean,
  onSelect: () -> Unit,
  onDelete: () -> Unit
) {
  var showDeleteDialog by remember { mutableStateOf(false) }
  val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(workspace.lastAccessedAt))

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(
        width = if (isCurrent) 2.dp else 1.dp,
        color = if (isCurrent) MinimalPrimary else MinimalOutlineVariant,
        shape = RoundedCornerShape(16.dp)
      )
      .clickable { onSelect() },
    colors = CardDefaults.cardColors(
      containerColor = if (isCurrent) MinimalPrimaryContainer.copy(alpha = 0.3f) else MinimalSurface
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(if (isCurrent) MinimalPrimary else MinimalSurfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isCurrent) Icons.Default.FolderSpecial else Icons.Default.Folder,
          contentDescription = null,
          tint = if (isCurrent) Color.White else MinimalTextSecondary,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = workspace.name,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MinimalTextPrimary
          )
          if (isCurrent) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MinimalPrimary)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "ACTIVE",
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = Color.White
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = workspace.path,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          color = MinimalTextMuted,
          maxLines = 1
        )
        Text(
          text = "Template: ${workspace.templateType} • $dateStr",
          fontSize = 11.sp,
          color = MinimalTextMuted
        )
      }

      IconButton(
        onClick = { showDeleteDialog = true },
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Icons.Default.DeleteOutline,
          contentDescription = "Delete workspace",
          tint = MinimalTextMuted,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }

  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = {
        Text("Delete Workspace?", fontWeight = FontWeight.Bold)
      },
      text = {
        Text(
          "Delete '${workspace.name}' and remove its project files from storage?",
          fontSize = 13.sp,
          color = MinimalTextSecondary
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            onDelete()
            showDeleteDialog = false
          }
        ) {
          Text("Delete", color = MinimalDanger, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) {
          Text("Cancel", color = MinimalTextSecondary)
        }
      },
      containerColor = MinimalSurface
    )
  }
}
