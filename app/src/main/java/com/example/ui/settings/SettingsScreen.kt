package com.example.ui.settings

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModel
import com.example.data.model.ProviderDefaults
import com.example.ui.theme.*
import com.example.viewmodel.AgentViewModel
import com.example.viewmodel.AppTab

@Composable
fun SettingsScreen(
  viewModel: AgentViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  var apiKeyInput by remember(uiState.apiKey) { mutableStateOf(uiState.apiKey) }
  var customUrlInput by remember(uiState.customBaseUrl) { mutableStateOf(uiState.customBaseUrl) }
  var showApiKey by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MinimalBackground)
  ) {
    // Header Bar
    Surface(
      color = MinimalSurface,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MinimalOutlineVariant)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { viewModel.setTab(AppTab.TERMINAL) },
          modifier = Modifier.size(36.dp)
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
            text = "AI Models & Providers",
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = MinimalTextPrimary
          )
          Text(
            text = "Configure OpenCode Zen and API keys",
            fontSize = 12.sp,
            color = MinimalTextSecondary
          )
        }
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Current active model banner
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
          colors = CardDefaults.cardColors(containerColor = MinimalPrimaryContainer)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MinimalPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Active AI Model",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalOnPrimaryContainer.copy(alpha = 0.7f)
              )
              Text(
                text = uiState.selectedModel.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MinimalOnPrimaryContainer
              )
              Text(
                text = "${uiState.selectedProvider.name} • ${uiState.selectedModel.badge}",
                fontSize = 12.sp,
                color = MinimalOnPrimaryContainer.copy(alpha = 0.8f)
              )
            }
          }
        }
      }

      // Free & Included Models
      item {
        Text(
          text = "FREE & INCLUDED MODELS",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = MinimalTextMuted
        )
      }

      items(ProviderDefaults.MODELS) { model ->
        val isSelected = model.id == uiState.selectedModel.id
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) MinimalPrimary else MinimalOutlineVariant,
              shape = RoundedCornerShape(16.dp)
            )
            .clickable { viewModel.setSelectedModel(model) },
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MinimalSurface else MinimalSurface
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = isSelected,
              onClick = { viewModel.setSelectedModel(model) },
              colors = RadioButtonDefaults.colors(
                selectedColor = MinimalPrimary,
                unselectedColor = MinimalTextMuted
              )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = model.name,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 14.sp,
                  color = MinimalTextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (model.isFree) MinimalSuccess.copy(alpha = 0.12f) else MinimalPrimaryContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = model.badge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (model.isFree) MinimalSuccess else MinimalPrimary
                  )
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = model.description,
                fontSize = 12.sp,
                color = MinimalTextSecondary
              )
            }
          }
        }
      }

      // Terminal Emulator Color Theme
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "TERMINAL COLOR THEME",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = MinimalTextMuted
        )
      }

      items(com.example.ui.theme.TerminalThemeType.entries) { themeType ->
        val isThemeSelected = uiState.terminalThemeType == themeType
        val previewScheme = com.example.ui.theme.TerminalThemes.getScheme(themeType)

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
              width = if (isThemeSelected) 2.dp else 1.dp,
              color = if (isThemeSelected) MinimalPrimary else MinimalOutlineVariant,
              shape = RoundedCornerShape(16.dp)
            )
            .clickable { viewModel.setTerminalTheme(themeType) },
          colors = CardDefaults.cardColors(containerColor = MinimalSurface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = isThemeSelected,
              onClick = { viewModel.setTerminalTheme(themeType) },
              colors = RadioButtonDefaults.colors(
                selectedColor = MinimalPrimary,
                unselectedColor = MinimalTextMuted
              )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = themeType.displayName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = MinimalTextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Theme Color Palette Swatches
                Row(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(previewScheme.background)
                    .padding(4.dp),
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(previewScheme.promptGreen))
                  Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(previewScheme.cyanAccent))
                  Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(previewScheme.amberAccent))
                  Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(previewScheme.roseAccent))
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = themeType.description,
                fontSize = 11.sp,
                color = MinimalTextSecondary
              )
            }
          }
        }
      }

      // Command Macros & Quick Actions Section
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "COMMAND MACROS & ALIASES",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = MinimalTextMuted
        )
      }

      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MinimalOutlineVariant, RoundedCornerShape(16.dp)),
          colors = CardDefaults.cardColors(containerColor = MinimalSurface)
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
                .clip(CircleShape)
                .background(MinimalPrimaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = MinimalPrimary,
                modifier = Modifier.size(22.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Manage Quick Macros (${uiState.commandMacros.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MinimalTextPrimary
              )
              Text(
                text = "Save, edit, and 1-tap execute common terminal tasks and bash aliases.",
                fontSize = 11.sp,
                color = MinimalTextSecondary
              )
            }

            Button(
              onClick = { viewModel.setShowCommandMacros(true) },
              colors = ButtonDefaults.buttonColors(
                containerColor = MinimalPrimary,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text("Open", fontSize = 12.sp)
            }
          }
        }
      }

      // API Key & Custom Configuration
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "OPTIONAL CREDENTIALS & ENDPOINTS",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = MinimalTextMuted
        )
      }

      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MinimalOutlineVariant, RoundedCornerShape(16.dp)),
          colors = CardDefaults.cardColors(containerColor = MinimalSurface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Custom API Key (Optional)",
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = MinimalTextPrimary
            )
            Text(
              text = "Leave empty to use OpenCode Zen built-in free tier or offline engine",
              fontSize = 11.sp,
              color = MinimalTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = apiKeyInput,
              onValueChange = { apiKeyInput = it },
              placeholder = { Text("sk-...", fontSize = 12.sp, color = MinimalTextMuted) },
              singleLine = true,
              trailingIcon = {
                IconButton(onClick = { showApiKey = !showApiKey }) {
                  Icon(
                    imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = MinimalTextMuted
                  )
                }
              },
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
              text = "Custom Base URL (OpenAI-compatible)",
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = MinimalTextPrimary
            )
            Text(
              text = "e.g., https://api.openai.com/v1 or http://localhost:11434/v1",
              fontSize = 11.sp,
              color = MinimalTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = customUrlInput,
              onValueChange = { customUrlInput = it },
              placeholder = { Text("https://...", fontSize = 12.sp, color = MinimalTextMuted) },
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = {
                viewModel.updateSettings(apiKeyInput.trim(), customUrlInput.trim())
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = MinimalPrimary,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Save Settings", fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }

      // Info Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
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
              text = "OpenCode CLI operates autonomously across your selected workspace root directory. Free tier models include standard rate limits.",
              fontSize = 12.sp,
              color = MinimalTextSecondary,
              lineHeight = 16.sp
            )
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}
