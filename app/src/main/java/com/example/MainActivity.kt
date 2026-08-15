package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.diff.DiffViewerScreen
import com.example.ui.files.FileExplorerScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.terminal.TerminalScreen
import com.example.ui.theme.*
import com.example.ui.workspace.WorkspaceSelectScreen
import com.example.viewmodel.AgentViewModel
import com.example.viewmodel.AppTab

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        OpenCodeApp()
      }
    }
  }
}

@Composable
fun OpenCodeApp(
  viewModel: AgentViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = MinimalBackground,
    topBar = {
      OpenCodeHeader(
        activeWorkspaceName = uiState.currentWorkspace?.name,
        onWorkspaceClick = { viewModel.setTab(AppTab.WORKSPACES) },
        onModelBadgeClick = { viewModel.setTab(AppTab.SETTINGS) },
        modelBadge = uiState.selectedModel.badge
      )
    },
    bottomBar = {
      OpenCodeBottomNav(
        activeTab = uiState.activeTab,
        onSelectTab = { viewModel.setTab(it) }
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (uiState.activeTab) {
        AppTab.TERMINAL -> TerminalScreen(viewModel = viewModel)
        AppTab.WORKSPACES -> WorkspaceSelectScreen(viewModel = viewModel)
        AppTab.FILES -> FileExplorerScreen(viewModel = viewModel)
        AppTab.DIFFS -> DiffViewerScreen(viewModel = viewModel)
        AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun OpenCodeHeader(
  activeWorkspaceName: String?,
  onWorkspaceClick: () -> Unit,
  onModelBadgeClick: () -> Unit,
  modelBadge: String
) {
  Surface(
    color = MinimalBackground,
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Brand Logo & Title
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onWorkspaceClick() }
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MinimalPrimary),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Terminal,
            contentDescription = "OpenCode Zen Logo",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Text(
            text = "OpenCode Zen",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = MinimalTextPrimary,
            letterSpacing = (-0.2).sp
          )
          Text(
            text = activeWorkspaceName ?: "Select Workspace",
            fontSize = 11.sp,
            color = MinimalTextSecondary
          )
        }
      }

      // Free Tier Pill Badge
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(MinimalSurfaceElevated)
          .clickable { onModelBadgeClick() }
          .padding(horizontal = 10.dp, vertical = 5.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(MinimalSuccess)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = modelBadge.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = MinimalTextSecondary
          )
        }
      }
    }
  }
}

@Composable
fun OpenCodeBottomNav(
  activeTab: AppTab,
  onSelectTab: (AppTab) -> Unit
) {
  Surface(
    color = MinimalSurface,
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .border(1.dp, MinimalOutlineVariant)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      NavTabItem(
        label = "CLI",
        icon = Icons.Default.Terminal,
        isSelected = activeTab == AppTab.TERMINAL,
        onClick = { onSelectTab(AppTab.TERMINAL) }
      )
      NavTabItem(
        label = "Workspace",
        icon = Icons.Default.Folder,
        isSelected = activeTab == AppTab.WORKSPACES,
        onClick = { onSelectTab(AppTab.WORKSPACES) }
      )
      NavTabItem(
        label = "Files",
        icon = Icons.Default.Code,
        isSelected = activeTab == AppTab.FILES,
        onClick = { onSelectTab(AppTab.FILES) }
      )
      NavTabItem(
        label = "Diffs",
        icon = Icons.Default.Difference,
        isSelected = activeTab == AppTab.DIFFS,
        onClick = { onSelectTab(AppTab.DIFFS) }
      )
      NavTabItem(
        label = "Models",
        icon = Icons.Default.SmartToy,
        isSelected = activeTab == AppTab.SETTINGS,
        onClick = { onSelectTab(AppTab.SETTINGS) }
      )
    }
  }
}

@Composable
fun NavTabItem(
  label: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .background(if (isSelected) MinimalPrimaryContainer else Color.Transparent)
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isSelected) MinimalPrimary else MinimalTextMuted,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) MinimalOnPrimaryContainer else MinimalTextMuted
      )
    }
  }
}
