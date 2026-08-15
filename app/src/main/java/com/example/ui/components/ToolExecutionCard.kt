package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ToolCall
import com.example.data.model.ToolStatus
import com.example.ui.theme.*

@Composable
fun ToolExecutionCard(
  toolCall: ToolCall,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }

  val badgeColor = when (toolCall.name) {
    "write_file", "create_file" -> MinimalSuccess
    "edit_file", "patch_file" -> MinimalPrimary
    "run_command", "bash" -> Color(0xFFE65100)
    "view_file", "read_file", "list_dir" -> Color(0xFF6750A4)
    "delete_file" -> MinimalDanger
    else -> MinimalPrimary
  }

  val statusColor = when (toolCall.status) {
    ToolStatus.PENDING -> MinimalTextMuted
    ToolStatus.RUNNING -> MinimalWarning
    ToolStatus.SUCCESS -> MinimalSuccess
    ToolStatus.FAILED -> MinimalDanger
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, MinimalOutlineVariant, RoundedCornerShape(16.dp)),
    colors = CardDefaults.cardColors(containerColor = MinimalSurface)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isExpanded = !isExpanded }
          .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Tool Icon Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeColor.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = toolCall.name,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = badgeColor
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Tool Target parameter (e.g. path or command)
        val mainArg = toolCall.arguments["path"] ?: toolCall.arguments["command"] ?: toolCall.arguments["query"] ?: ""
        Text(
          text = mainArg.ifEmpty { "Executing tool..." },
          fontFamily = FontFamily.Monospace,
          fontSize = 12.sp,
          color = MinimalTextPrimary,
          maxLines = 1,
          modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Status indicator
        if (toolCall.status == ToolStatus.RUNNING) {
          CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MinimalPrimary
          )
        } else {
          Icon(
            imageVector = when (toolCall.status) {
              ToolStatus.SUCCESS -> Icons.Default.CheckCircle
              ToolStatus.FAILED -> Icons.Default.Cancel
              else -> Icons.Default.HourglassEmpty
            },
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Icon(
          imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = null,
          tint = MinimalTextMuted,
          modifier = Modifier.size(18.dp)
        )
      }

      AnimatedVisibility(visible = isExpanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(MinimalSurfaceVariant)
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Arguments
          Text(
            text = "ARGUMENTS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MinimalTextMuted
          )
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, Color(0xFF2F3033), RoundedCornerShape(10.dp)),
            colors = CardDefaults.cardColors(containerColor = TerminalBlack)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              toolCall.arguments.forEach { (k, v) ->
                Row(modifier = Modifier.fillMaxWidth()) {
                  Text(
                    text = "$k: ",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MinimalPrimaryContainer
                  )
                  Text(
                    text = v.take(300),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TerminalTextSecondary
                  )
                }
              }
            }
          }

          // Result output
          if (toolCall.result != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "EXECUTION RESULT",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = MinimalTextMuted
            )
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF2F3033), RoundedCornerShape(10.dp)),
              colors = CardDefaults.cardColors(containerColor = TerminalBlack)
            ) {
              AnsiText(
                text = toolCall.result ?: "",
                fontSize = 11.sp,
                lineHeight = 15.sp,
                defaultColor = if (toolCall.status == ToolStatus.FAILED) TerminalRoseBright else TerminalTextSecondary,
                modifier = Modifier.padding(10.dp)
              )
            }
          }
        }
      }
    }
  }
}
