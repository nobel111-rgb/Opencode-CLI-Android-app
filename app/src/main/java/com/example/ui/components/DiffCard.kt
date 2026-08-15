package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiffLineType
import com.example.data.model.FileDiff
import com.example.ui.theme.*

@Composable
fun DiffCard(
  diff: FileDiff,
  isApplied: Boolean = true,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, MinimalOutlineVariant, RoundedCornerShape(16.dp)),
    colors = CardDefaults.cardColors(containerColor = MinimalSurface)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MinimalSurfaceVariant)
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Code,
          contentDescription = "Diff",
          tint = MinimalPrimary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = diff.filePath,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = MinimalTextPrimary,
          modifier = Modifier.weight(1f)
        )

        // Additions / Deletions count
        if (diff.additionsCount > 0) {
          Text(
            text = "+${diff.additionsCount}",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = MinimalSuccess
          )
          Spacer(modifier = Modifier.width(8.dp))
        }
        if (diff.deletionsCount > 0) {
          Text(
            text = "-${diff.deletionsCount}",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = MinimalDanger
          )
          Spacer(modifier = Modifier.width(8.dp))
        }

        // Applied badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MinimalSuccessContainer)
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Applied",
              tint = MinimalSuccess,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = if (isApplied) "Applied" else "Proposed",
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = MinimalSuccess
            )
          }
        }
      }

      // Diff lines content in code window
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(TerminalBlack)
          .horizontalScroll(rememberScrollState())
          .padding(8.dp)
      ) {
        Column {
          diff.lines.forEach { line ->
            val bg = when (line.type) {
              DiffLineType.ADDITION -> TerminalGreenBg
              DiffLineType.DELETION -> TerminalRoseBg
              DiffLineType.HEADER -> TerminalSurfaceElevatedCode
              DiffLineType.EQUAL -> Color.Transparent
            }
            val textColor = when (line.type) {
              DiffLineType.ADDITION -> TerminalGreenBright
              DiffLineType.DELETION -> TerminalRoseBright
              DiffLineType.HEADER -> MinimalPrimaryContainer
              DiffLineType.EQUAL -> TerminalTextSecondary
            }
            val prefix = when (line.type) {
              DiffLineType.ADDITION -> "+ "
              DiffLineType.DELETION -> "- "
              DiffLineType.HEADER -> "@@ "
              DiffLineType.EQUAL -> "  "
            }

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(bg)
                .padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              val lineNo = line.newLineNum ?: line.oldLineNum
              Text(
                text = lineNo?.toString()?.padStart(3, ' ') ?: "   ",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TerminalTextDim,
                modifier = Modifier.width(30.dp)
              )
              Text(
                text = prefix + line.text,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = textColor
              )
            }
          }
        }
      }
    }
  }
}
