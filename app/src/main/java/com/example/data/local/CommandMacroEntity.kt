package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "command_macros",
  indices = [Index(value = ["category"]), Index(value = ["workspaceId"])]
)
data class CommandMacroEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val command: String,
  val description: String = "",
  val category: String = "General", // "Git", "Build", "Python", "Node", "Custom"
  val workspaceId: String? = null, // null for global or specific workspaceId
  val iconName: String = "Terminal",
  val createdAt: Long = System.currentTimeMillis(),
  val usageCount: Int = 0
)
