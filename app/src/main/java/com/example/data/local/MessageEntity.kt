package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sessionId: String,
  val role: String, // "user", "assistant", "system", "tool"
  val content: String,
  val thoughts: String? = null,
  val toolCallsJson: String? = null,
  val toolResultsJson: String? = null,
  val diffsJson: String? = null,
  val timestamp: Long = System.currentTimeMillis()
)
