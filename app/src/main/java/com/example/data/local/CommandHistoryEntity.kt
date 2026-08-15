package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "command_history",
  indices = [Index(value = ["workspaceId", "timestamp"])]
)
data class CommandHistoryEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val workspaceId: String,
  val command: String,
  val exitCode: Int = 0,
  val outputSnippet: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val executionType: String = "CLI", // "USER_CLI", "BASH", "AGENT_TOOL", "SLASH_CMD"
  val durationMs: Long = 0
)
