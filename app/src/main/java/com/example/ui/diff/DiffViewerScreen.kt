package com.example.ui.diff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DiffCard
import com.example.ui.theme.*
import com.example.viewmodel.AgentViewModel
import com.example.viewmodel.AppTab

@Composable
fun DiffViewerScreen(
  viewModel: AgentViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MinimalBackground)
  ) {
    // Diff Top Bar
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

        Column {
          Text(
            text = "Code Diff Inspector",
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = MinimalTextPrimary
          )
          Text(
            text = "${uiState.recentDiffs.size} recent change(s)",
            fontSize = 11.sp,
            color = MinimalTextSecondary
          )
        }
      }
    }

    if (uiState.recentDiffs.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Difference,
            contentDescription = null,
            tint = MinimalTextMuted,
            modifier = Modifier.size(56.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No code changes recorded yet",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MinimalTextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "When OpenCode Agent edits files, live diffs will appear here.",
            fontSize = 12.sp,
            color = MinimalTextSecondary
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(uiState.recentDiffs.reversed(), key = { it.filePath + it.oldContent.hashCode() }) { diff ->
          DiffCard(
            diff = diff,
            isApplied = true
          )
        }
      }
    }
  }
}
