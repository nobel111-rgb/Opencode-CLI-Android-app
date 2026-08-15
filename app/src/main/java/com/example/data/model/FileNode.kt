package com.example.data.model

data class FileNode(
  val name: String,
  val path: String, // relative to workspace root
  val isDirectory: Boolean,
  val size: Long = 0,
  val lastModified: Long = 0,
  val children: List<FileNode> = emptyList(),
  val extension: String = name.substringAfterLast('.', "")
)
