package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
  @PrimaryKey val id: String,
  val workspaceId: String,
  val title: String,
  val modelId: String = "zen/free-deepseek-r1",
  val providerId: String = "opencode_zen",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
