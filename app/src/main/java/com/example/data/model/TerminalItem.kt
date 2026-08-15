package com.example.data.model

sealed class TerminalItem(
  open val id: String = java.util.UUID.randomUUID().toString(),
  open val timestamp: Long = System.currentTimeMillis()
) {
  data class Prompt(
    override val id: String = java.util.UUID.randomUUID().toString(),
    val command: String,
    val workspaceName: String,
    val isUserQuery: Boolean = true,
    override val timestamp: Long = System.currentTimeMillis()
  ) : TerminalItem(id, timestamp)

  data class AgentThought(
    override val id: String = java.util.UUID.randomUUID().toString(),
    val thought: String,
    val modelName: String,
    override val timestamp: Long = System.currentTimeMillis()
  ) : TerminalItem(id, timestamp)

  data class ToolExecution(
    override val id: String = java.util.UUID.randomUUID().toString(),
    val toolCall: ToolCall,
    override val timestamp: Long = System.currentTimeMillis()
  ) : TerminalItem(id, timestamp)

  data class DiffProposal(
    override val id: String = java.util.UUID.randomUUID().toString(),
    val diff: FileDiff,
    val isApplied: Boolean = true,
    override val timestamp: Long = System.currentTimeMillis()
  ) : TerminalItem(id, timestamp)

  data class AgentResponse(
    override val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val modelName: String,
    val tokensUsed: Int = 0,
    override val timestamp: Long = System.currentTimeMillis()
  ) : TerminalItem(id, timestamp)

  data class CommandOutput(
    override val id: String = java.util.UUID.randomUUID().toString(),
    val output: String,
    val exitCode: Int = 0,
    override val timestamp: Long = System.currentTimeMillis()
  ) : TerminalItem(id, timestamp)

  data class SystemMessage(
    override val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val level: SystemLogLevel = SystemLogLevel.INFO,
    override val timestamp: Long = System.currentTimeMillis()
  ) : TerminalItem(id, timestamp)
}

enum class SystemLogLevel {
  INFO,
  SUCCESS,
  WARNING,
  ERROR
}
