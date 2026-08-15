package com.example.data.model

data class ToolCall(
  val id: String = java.util.UUID.randomUUID().toString().take(8),
  val name: String,
  val arguments: Map<String, String>,
  var status: ToolStatus = ToolStatus.RUNNING,
  var result: String? = null,
  var error: String? = null,
  var diff: FileDiff? = null
)

enum class ToolStatus {
  PENDING,
  RUNNING,
  SUCCESS,
  FAILED
}

data class FileDiff(
  val filePath: String,
  val oldContent: String,
  val newContent: String,
  val additionsCount: Int,
  val deletionsCount: Int,
  val lines: List<DiffLine>
)

data class DiffLine(
  val type: DiffLineType,
  val text: String,
  val oldLineNum: Int? = null,
  val newLineNum: Int? = null
)

enum class DiffLineType {
  EQUAL,
  ADDITION,
  DELETION,
  HEADER
}
