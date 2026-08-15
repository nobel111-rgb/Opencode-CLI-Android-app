package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workspaces")
data class WorkspaceEntity(
  @PrimaryKey val id: String,
  val name: String,
  val path: String,
  val description: String = "",
  val templateType: String = "EMPTY",
  val createdAt: Long = System.currentTimeMillis(),
  val lastAccessedAt: Long = System.currentTimeMillis(),
  val isDefault: Boolean = false
)
