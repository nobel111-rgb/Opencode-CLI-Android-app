package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.agent.AgentEngine
import com.example.data.api.ChatMessage
import com.example.data.local.AppDatabase
import com.example.data.local.SessionEntity
import com.example.data.local.WorkspaceEntity
import com.example.data.model.*
import com.example.data.workspace.WorkspaceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

enum class AppTab {
  TERMINAL,
  FILES,
  DIFFS,
  WORKSPACES,
  SETTINGS
}

data class SlashCommand(
  val command: String,
  val description: String,
  val example: String
)

data class UiState(
  val currentWorkspace: WorkspaceEntity? = null,
  val allWorkspaces: List<WorkspaceEntity> = emptyList(),
  val currentSession: SessionEntity? = null,
  val terminalItems: List<TerminalItem> = emptyList(),
  val fileTree: List<FileNode> = emptyList(),
  val activeTab: AppTab = AppTab.TERMINAL,
  val selectedModel: AiModel = ProviderDefaults.DEFAULT_MODEL,
  val selectedProvider: ModelProvider = ProviderDefaults.PROVIDERS[0],
  val apiKey: String = "",
  val customBaseUrl: String = "",
  val isAgentRunning: Boolean = false,
  val showWorkspaceSelector: Boolean = false,
  val showCommandHistory: Boolean = false,
  val showCommandMacros: Boolean = false,
  val commandHistory: List<com.example.data.local.CommandHistoryEntity> = emptyList(),
  val commandMacros: List<com.example.data.local.CommandMacroEntity> = emptyList(),
  val terminalThemeType: com.example.ui.theme.TerminalThemeType = com.example.ui.theme.TerminalThemeType.DARK_MINIMAL,
  val openedFile: FileNode? = null,
  val openedFileContent: String = "",
  val recentDiffs: List<FileDiff> = emptyList(),
  val systemStatusMessage: String? = null
)

class AgentViewModel(application: Application) : AndroidViewModel(application) {

  val workspaceManager = WorkspaceManager(application)
  private val database = AppDatabase.getInstance(application)
  private val dao = database.appDao()
  private val agentEngine = AgentEngine(workspaceManager)

  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  val slashCommands = listOf(
    SlashCommand("/help", "Show all available OpenCode CLI commands", "/help"),
    SlashCommand("/macros", "Quick actions & command aliases from Room DB", "/macros"),
    SlashCommand("/theme", "Toggle terminal theme (Dark Minimal / Solarized)", "/theme"),
    SlashCommand("/history", "Browse & re-run command history from Room DB", "/history"),
    SlashCommand("/workspace", "Switch or create a workspace directory", "/workspace"),
    SlashCommand("/files", "Open workspace file explorer and code viewer", "/files"),
    SlashCommand("/init", "Initialize a project starter template", "/init web_game"),
    SlashCommand("/models", "Select OpenCode Zen or Gemini free model", "/models"),
    SlashCommand("/run", "Run a bash terminal command directly", "/run python main.py"),
    SlashCommand("/diff", "View recent code modifications and diffs", "/diff"),
    SlashCommand("/clear", "Clear terminal screen output", "/clear"),
    SlashCommand("/tree", "Print current workspace directory tree", "/tree"),
    SlashCommand("/status", "Display agent and workspace environment status", "/status")
  )

  private val linuxCommands = listOf(
    "ls", "ls -la", "cat", "grep", "cd", "pwd", "mkdir", "touch", "rm", "rm -rf",
    "cp", "mv", "echo", "find", "curl", "chmod +x", "python", "python3", "node",
    "npm", "npm start", "npm test", "git status", "git diff", "git log", "git add .",
    "pip install", "pip list", "head", "tail", "clear", "export", "source"
  )

  init {
    loadWorkspaces()
    observeCommandHistory()
    observeCommandMacros()
    seedDefaultMacrosIfEmpty()
  }

  private fun observeCommandHistory() {
    viewModelScope.launch {
      dao.getAllCommandHistory().collect { history ->
        _uiState.update { it.copy(commandHistory = history) }
      }
    }
  }

  private fun observeCommandMacros() {
    viewModelScope.launch {
      dao.getAllMacros().collect { macros ->
        _uiState.update { it.copy(commandMacros = macros) }
      }
    }
  }

  private fun seedDefaultMacrosIfEmpty() {
    viewModelScope.launch(Dispatchers.IO) {
      if (dao.getMacroCount() == 0) {
        val defaultMacros = listOf(
          com.example.data.local.CommandMacroEntity(
            name = "Git Status",
            command = "$ git status --short",
            category = "Git",
            description = "Quick view of modified and untracked files"
          ),
          com.example.data.local.CommandMacroEntity(
            name = "List Detailed",
            command = "$ ls -la",
            category = "General",
            description = "List all files including hidden with permissions"
          ),
          com.example.data.local.CommandMacroEntity(
            name = "Run Python Script",
            command = "$ python main.py",
            category = "Python",
            description = "Execute primary python entry point"
          ),
          com.example.data.local.CommandMacroEntity(
            name = "Run Node App",
            command = "$ node index.js",
            category = "Node",
            description = "Execute Node.js entry point"
          ),
          com.example.data.local.CommandMacroEntity(
            name = "Directory Tree",
            command = "/tree",
            category = "General",
            description = "Render complete workspace file hierarchy"
          ),
          com.example.data.local.CommandMacroEntity(
            name = "Check Environment",
            command = "$ python --version && node --version",
            category = "Build",
            description = "Inspect Python and Node runtime versions"
          )
        )
        defaultMacros.forEach { dao.insertMacro(it) }
      }
    }
  }

  private fun loadWorkspaces() {
    viewModelScope.launch {
      dao.getAllWorkspaces().collect { workspaces ->
        _uiState.update { it.copy(allWorkspaces = workspaces) }
        if (_uiState.value.currentWorkspace == null) {
          if (workspaces.isNotEmpty()) {
            selectWorkspace(workspaces.first())
          } else {
            // First time open -> Create a welcoming default workspace or show selector
            val (dir, path) = workspaceManager.createWorkspaceFolder("my-project", "PYTHON_APP", "Default Python Workspace")
            val entity = WorkspaceEntity(
              id = UUID.randomUUID().toString(),
              name = "my-project",
              path = path,
              description = "Default Python Project",
              templateType = "PYTHON_APP",
              isDefault = true
            )
            dao.insertWorkspace(entity)
            selectWorkspace(entity)
            addWelcomeBanner(entity.name)
          }
        }
      }
    }
  }

  fun selectWorkspace(workspace: WorkspaceEntity) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        val updated = workspace.copy(lastAccessedAt = System.currentTimeMillis())
        dao.updateWorkspace(updated)
      }
      val dir = File(workspace.path)
      val tree = workspaceManager.getFileTree(dir)
      
      // Load or create session
      val sessionId = "session-${workspace.id.take(8)}"
      var session = dao.getSessionById(sessionId)
      if (session == null) {
        session = SessionEntity(
          id = sessionId,
          workspaceId = workspace.id,
          title = "${workspace.name} CLI Session",
          modelId = _uiState.value.selectedModel.id,
          providerId = _uiState.value.selectedProvider.id
        )
        dao.insertSession(session)
      }

      _uiState.update {
        it.copy(
          currentWorkspace = workspace,
          currentSession = session,
          fileTree = tree,
          showWorkspaceSelector = false
        )
      }
      refreshFileTree()
    }
  }

  fun createWorkspace(name: String, templateId: String, description: String) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        val (dir, path) = workspaceManager.createWorkspaceFolder(name, templateId, description)
        val entity = WorkspaceEntity(
          id = UUID.randomUUID().toString(),
          name = dir.name,
          path = path,
          description = description.ifEmpty { "Workspace project created with $templateId template" },
          templateType = templateId,
          isDefault = false
        )
        dao.insertWorkspace(entity)
        selectWorkspace(entity)
        
        appendTerminalItem(
          TerminalItem.SystemMessage(
            message = "✨ Workspace '${dir.name}' created successfully with template $templateId\nRoot Directory: ${dir.absolutePath}",
            level = SystemLogLevel.SUCCESS
          )
        )
      }
    }
  }

  fun deleteWorkspace(workspace: WorkspaceEntity) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        workspaceManager.deleteWorkspaceFolder(workspace.name)
        dao.deleteWorkspaceById(workspace.id)
      }
      if (_uiState.value.currentWorkspace?.id == workspace.id) {
        val remaining = _uiState.value.allWorkspaces.filter { it.id != workspace.id }
        if (remaining.isNotEmpty()) {
          selectWorkspace(remaining.first())
        } else {
          _uiState.update { it.copy(currentWorkspace = null, showWorkspaceSelector = true) }
        }
      }
    }
  }

  fun refreshFileTree() {
    val ws = _uiState.value.currentWorkspace ?: return
    val dir = File(ws.path)
    val tree = workspaceManager.getFileTree(dir)
    _uiState.update { it.copy(fileTree = tree) }
  }

  fun setTab(tab: AppTab) {
    _uiState.update { it.copy(activeTab = tab) }
    if (tab == AppTab.FILES) refreshFileTree()
  }

  fun setShowWorkspaceSelector(show: Boolean) {
    _uiState.update { it.copy(showWorkspaceSelector = show) }
  }

  fun setSelectedModel(model: AiModel) {
    val provider = ProviderDefaults.PROVIDERS.find { it.id == model.providerId } ?: _uiState.value.selectedProvider
    _uiState.update { it.copy(selectedModel = model, selectedProvider = provider) }
    appendTerminalItem(
      TerminalItem.SystemMessage(
        message = "🤖 Active model switched to [${model.name}] (${model.badge})",
        level = SystemLogLevel.INFO
      )
    )
  }

  fun updateSettings(apiKey: String, customBaseUrl: String) {
    _uiState.update { it.copy(apiKey = apiKey, customBaseUrl = customBaseUrl) }
    appendTerminalItem(
      TerminalItem.SystemMessage(
        message = "⚙️ Settings saved. Ready for agent execution.",
        level = SystemLogLevel.SUCCESS
      )
    )
  }

  fun handleUserInput(input: String) {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return

    val currentWs = _uiState.value.currentWorkspace
    if (currentWs == null) {
      appendTerminalItem(TerminalItem.SystemMessage(message = "Please select or create a workspace first.", level = SystemLogLevel.WARNING))
      _uiState.update { it.copy(showWorkspaceSelector = true) }
      return
    }

    // Echo user prompt in terminal
    appendTerminalItem(
      TerminalItem.Prompt(
        command = trimmed,
        workspaceName = currentWs.name,
        isUserQuery = true
      )
    )

    // Check if slash command
    if (trimmed.startsWith("/")) {
      handleSlashCommand(trimmed, File(currentWs.path))
      return
    }

    // Check if raw terminal command (e.g., $ ls or starts with standard bash commands)
    if (trimmed.startsWith("$ ")) {
      executeTerminalCommand(trimmed.removePrefix("$ ").trim(), File(currentWs.path))
      return
    }

    // AI Agent execution loop
    runAgentPrompt(trimmed, File(currentWs.path))
  }

  private fun handleSlashCommand(command: String, workspaceDir: File) {
    val parts = command.split(Regex("\\s+"))
    val root = parts[0].lowercase()
    val arg = parts.drop(1).joinToString(" ")

    when (root) {
      "/history" -> {
        _uiState.update { it.copy(showCommandHistory = true) }
      }
      "/macros" -> {
        _uiState.update { it.copy(showCommandMacros = true) }
      }
      "/theme" -> {
        val nextTheme = if (_uiState.value.terminalThemeType == com.example.ui.theme.TerminalThemeType.DARK_MINIMAL) {
          com.example.ui.theme.TerminalThemeType.SOLARIZED
        } else {
          com.example.ui.theme.TerminalThemeType.DARK_MINIMAL
        }
        setTerminalTheme(nextTheme)
      }
      "/help" -> {
        val helpText = buildString {
          appendLine("╔═══════════════════════════════════════════════════════╗")
          appendLine("║               OpenCode CLI Command Matrix             ║")
          appendLine("╚═══════════════════════════════════════════════════════╝")
          slashCommands.forEach {
            appendLine("  %-12s - %s".format(it.command, it.description))
          }
          appendLine("\n💡 Pro-tip: You can type natural language coding requests, or prefix commands with '$ ls' for raw bash execution!")
        }
        appendTerminalItem(TerminalItem.CommandOutput(output = helpText, exitCode = 0))
      }
      "/clear" -> {
        clearTerminal()
      }
      "/tree" -> {
        val tree = workspaceManager.formatFileTreeForPrompt(workspaceDir)
        appendTerminalItem(TerminalItem.CommandOutput(output = tree, exitCode = 0))
      }
      "/files" -> {
        setTab(AppTab.FILES)
      }
      "/workspace" -> {
        setTab(AppTab.WORKSPACES)
      }
      "/diff" -> {
        setTab(AppTab.DIFFS)
      }
      "/models" -> {
        setTab(AppTab.SETTINGS)
      }
      "/status" -> {
        val statusText = """
[OpenCode CLI Environment Status]
 • Workspace Root : ${workspaceDir.name} (${workspaceDir.absolutePath})
 • Active Model   : ${_uiState.value.selectedModel.name}
 • Provider       : ${_uiState.value.selectedProvider.name}
 • Free Tier      : ${_uiState.value.selectedModel.badge}
 • File Count     : ${_uiState.value.fileTree.size} top-level items
 • Agent Status   : ${if (_uiState.value.isAgentRunning) "BUSY (Generating)" else "IDLE (Ready)"}
""".trimIndent()
        appendTerminalItem(TerminalItem.CommandOutput(output = statusText, exitCode = 0))
      }
      "/run" -> {
        if (arg.isBlank()) {
          appendTerminalItem(TerminalItem.SystemMessage(message = "Usage: /run <command> (e.g., /run python main.py)", level = SystemLogLevel.WARNING))
        } else {
          executeTerminalCommand(arg, workspaceDir)
        }
      }
      "/init" -> {
        if (arg.isBlank()) {
          appendTerminalItem(
            TerminalItem.SystemMessage(
              message = "Available templates: python_app, web_game, kotlin_cli, node_api, empty.\nUsage: /init <template_name>",
              level = SystemLogLevel.INFO
            )
          )
        } else {
          val matching = WorkspaceTemplateCatalog.TEMPLATES.find { it.id.equals(arg, ignoreCase = true) || it.name.contains(arg, ignoreCase = true) }
          if (matching != null) {
            createWorkspace("${workspaceDir.name}-${matching.id.lowercase().take(6)}", matching.id, "Scaffolded with ${matching.name}")
          } else {
            appendTerminalItem(TerminalItem.SystemMessage(message = "Unknown template '$arg'. Type '/init' to list.", level = SystemLogLevel.ERROR))
          }
        }
      }
      else -> {
        appendTerminalItem(
          TerminalItem.SystemMessage(
            message = "Unknown slash command '$root'. Type '/help' for available commands.",
            level = SystemLogLevel.ERROR
          )
        )
      }
    }
  }

  fun setShowCommandHistory(show: Boolean) {
    _uiState.update { it.copy(showCommandHistory = show) }
  }

  fun reRunCommand(cmd: String) {
    handleUserInput(cmd)
  }

  fun deleteHistoryItem(id: Long) {
    viewModelScope.launch(Dispatchers.IO) {
      dao.deleteCommandHistoryById(id)
    }
  }

  fun clearWorkspaceHistory() {
    val currentWs = _uiState.value.currentWorkspace ?: return
    viewModelScope.launch(Dispatchers.IO) {
      dao.clearHistoryForWorkspace(currentWs.id)
    }
  }

  fun executeTerminalCommand(cmd: String, workspaceDir: File) {
    val currentWs = _uiState.value.currentWorkspace
    val startTime = System.currentTimeMillis()
    viewModelScope.launch(Dispatchers.IO) {
      val (output, exitCode) = workspaceManager.executeBashCommand(workspaceDir, cmd)
      val duration = System.currentTimeMillis() - startTime

      // Record in Room Database
      if (currentWs != null) {
        dao.insertCommandHistory(
          com.example.data.local.CommandHistoryEntity(
            workspaceId = currentWs.id,
            command = cmd,
            exitCode = exitCode,
            outputSnippet = output.take(600),
            timestamp = System.currentTimeMillis(),
            executionType = "BASH",
            durationMs = duration
          )
        )
      }

      withContext(Dispatchers.Main) {
        appendTerminalItem(TerminalItem.CommandOutput(output = output, exitCode = exitCode))
        refreshFileTree()
      }
    }
  }

  private fun runAgentPrompt(prompt: String, workspaceDir: File) {
    val currentWs = _uiState.value.currentWorkspace
    _uiState.update { it.copy(isAgentRunning = true) }

    // Save prompt to Room History
    if (currentWs != null) {
      viewModelScope.launch(Dispatchers.IO) {
        dao.insertCommandHistory(
          com.example.data.local.CommandHistoryEntity(
            workspaceId = currentWs.id,
            command = prompt,
            exitCode = 0,
            outputSnippet = "Prompt to AI Agent (${_uiState.value.selectedModel.name})",
            timestamp = System.currentTimeMillis(),
            executionType = "USER_CLI",
            durationMs = 0
          )
        )
      }
    }

    viewModelScope.launch(Dispatchers.IO) {
      try {
        // Collect prior messages for context
        val history = _uiState.value.terminalItems.filterIsInstance<TerminalItem.AgentResponse>().map {
          ChatMessage("assistant", it.text)
        }

        agentEngine.runAgentLoop(
          workspaceDir = workspaceDir,
          userPrompt = prompt,
          model = _uiState.value.selectedModel,
          provider = _uiState.value.selectedProvider,
          apiKey = _uiState.value.apiKey,
          customBaseUrl = _uiState.value.customBaseUrl,
          conversationHistory = history,
          onItemEmitted = { item ->
            withContext(Dispatchers.Main) {
              appendTerminalItem(item)
              if (item is TerminalItem.DiffProposal) {
                _uiState.update {
                  it.copy(recentDiffs = listOf(item.diff) + it.recentDiffs.take(15))
                }
              }
              if (item is TerminalItem.ToolExecution && currentWs != null) {
                // Also record tool execution in history
                viewModelScope.launch(Dispatchers.IO) {
                  dao.insertCommandHistory(
                    com.example.data.local.CommandHistoryEntity(
                      workspaceId = currentWs.id,
                      command = "${item.toolCall.name} ${item.toolCall.arguments}",
                      exitCode = if (item.toolCall.status == com.example.data.model.ToolStatus.FAILED || item.toolCall.error != null) 1 else 0,
                      outputSnippet = item.toolCall.result?.take(600) ?: (item.toolCall.error ?: ""),
                      timestamp = item.timestamp,
                      executionType = "AGENT_TOOL",
                      durationMs = 0L
                    )
                  )
                }
              }
              refreshFileTree()
            }
          }
        )
      } catch (e: Exception) {
        withContext(Dispatchers.Main) {
          appendTerminalItem(
            TerminalItem.SystemMessage(
              message = "Agent execution exception: ${e.localizedMessage}",
              level = SystemLogLevel.ERROR
            )
          )
        }
      } finally {
        withContext(Dispatchers.Main) {
          _uiState.update { it.copy(isAgentRunning = false) }
          refreshFileTree()
        }
      }
    }
  }

  fun openFileForEditing(node: FileNode) {
    val currentWs = _uiState.value.currentWorkspace ?: return
    viewModelScope.launch(Dispatchers.IO) {
      val readRes = workspaceManager.readFile(File(currentWs.path), node.path)
      val content = readRes.getOrDefault("")
      withContext(Dispatchers.Main) {
        _uiState.update {
          it.copy(
            openedFile = node,
            openedFileContent = content
          )
        }
      }
    }
  }

  fun saveFileContent(node: FileNode, newContent: String) {
    val currentWs = _uiState.value.currentWorkspace ?: return
    viewModelScope.launch(Dispatchers.IO) {
      val writeRes = workspaceManager.writeFile(File(currentWs.path), node.path, newContent)
      writeRes.onSuccess { (_, diff) ->
        withContext(Dispatchers.Main) {
          _uiState.update {
            it.copy(
              openedFileContent = newContent,
              recentDiffs = listOf(diff) + it.recentDiffs.take(15)
            )
          }
          appendTerminalItem(
            TerminalItem.SystemMessage(
              message = "Saved '${node.path}' (${newContent.lines().size} lines).",
              level = SystemLogLevel.SUCCESS
            )
          )
          refreshFileTree()
        }
      }
    }
  }

  fun createNewFileInWorkspace(relPath: String, initialContent: String = "") {
    val currentWs = _uiState.value.currentWorkspace ?: return
    viewModelScope.launch(Dispatchers.IO) {
      workspaceManager.writeFile(File(currentWs.path), relPath, initialContent)
      withContext(Dispatchers.Main) {
        appendTerminalItem(
          TerminalItem.SystemMessage(
            message = "Created file '$relPath'",
            level = SystemLogLevel.SUCCESS
          )
        )
        refreshFileTree()
      }
    }
  }

  fun deleteFileInWorkspace(relPath: String) {
    val currentWs = _uiState.value.currentWorkspace ?: return
    viewModelScope.launch(Dispatchers.IO) {
      workspaceManager.deleteFile(File(currentWs.path), relPath)
      withContext(Dispatchers.Main) {
        appendTerminalItem(
          TerminalItem.SystemMessage(
            message = "Deleted '$relPath'",
            level = SystemLogLevel.INFO
          )
        )
        if (_uiState.value.openedFile?.path == relPath) {
          _uiState.update { it.copy(openedFile = null) }
        }
        refreshFileTree()
      }
    }
  }

  fun closeOpenedFile() {
    _uiState.update { it.copy(openedFile = null) }
  }

  fun setTerminalTheme(theme: com.example.ui.theme.TerminalThemeType) {
    _uiState.update { it.copy(terminalThemeType = theme) }
    appendTerminalItem(
      TerminalItem.SystemMessage(
        message = "🎨 Terminal theme changed to [${theme.displayName}]",
        level = SystemLogLevel.INFO
      )
    )
  }

  fun toggleTerminalTheme() {
    val next = if (_uiState.value.terminalThemeType == com.example.ui.theme.TerminalThemeType.DARK_MINIMAL) {
      com.example.ui.theme.TerminalThemeType.SOLARIZED
    } else {
      com.example.ui.theme.TerminalThemeType.DARK_MINIMAL
    }
    setTerminalTheme(next)
  }

  fun setShowCommandMacros(show: Boolean) {
    _uiState.update { it.copy(showCommandMacros = show) }
  }

  fun saveMacro(name: String, command: String, category: String, description: String) {
    val currentWs = _uiState.value.currentWorkspace
    viewModelScope.launch(Dispatchers.IO) {
      val macro = com.example.data.local.CommandMacroEntity(
        name = name,
        command = command,
        category = category,
        description = description,
        workspaceId = currentWs?.id
      )
      dao.insertMacro(macro)
      withContext(Dispatchers.Main) {
        appendTerminalItem(
          TerminalItem.SystemMessage(
            message = "⚡ Saved command macro '$name' (Category: $category)",
            level = SystemLogLevel.SUCCESS
          )
        )
      }
    }
  }

  fun deleteMacro(id: Long) {
    viewModelScope.launch(Dispatchers.IO) {
      dao.deleteMacroById(id)
    }
  }

  fun runMacro(macro: com.example.data.local.CommandMacroEntity) {
    viewModelScope.launch(Dispatchers.IO) {
      dao.incrementMacroUsage(macro.id)
    }
    handleUserInput(macro.command)
  }

  fun getTabCompletions(input: String): List<String> {
    val trimmed = input.trimStart()
    if (trimmed.isEmpty()) {
      return listOf("/help", "/macros", "/theme", "/files", "$ ls", "$ git status")
    }

    val results = mutableListOf<String>()

    // 1. Slash commands matching
    if (trimmed.startsWith("/")) {
      val prefix = trimmed.lowercase()
      slashCommands.forEach { cmd ->
        if (cmd.command.lowercase().startsWith(prefix)) {
          results.add(cmd.command)
        }
      }
      return results.distinct().take(8)
    }

    // 2. Linux bash commands matching ($ prefix or raw word)
    val isBashPrefix = trimmed.startsWith("$ ")
    val bashQuery = if (isBashPrefix) trimmed.removePrefix("$ ").trimStart() else trimmed

    val tokens = bashQuery.split(Regex("\\s+"))
    val currentWord = tokens.lastOrNull() ?: ""

    // Collect all workspace relative file paths
    val allPaths = mutableListOf<String>()
    fun collectPaths(nodes: List<FileNode>) {
      for (node in nodes) {
        allPaths.add(node.path)
        if (node.isDirectory) {
          allPaths.add("${node.path}/")
          collectPaths(node.children)
        }
      }
    }
    collectPaths(_uiState.value.fileTree)

    if (tokens.size <= 1) {
      // First command token: suggest Linux binaries / commands
      val prefix = currentWord.lowercase()
      linuxCommands.forEach { cmd ->
        if (cmd.lowercase().startsWith(prefix)) {
          results.add(if (isBashPrefix) "$ $cmd" else cmd)
        }
      }
      // Also suggest top-level files/dirs
      allPaths.forEach { p ->
        if (p.lowercase().startsWith(prefix)) {
          results.add(if (isBashPrefix) "$ $p" else p)
        }
      }
    } else {
      // Subsequent arguments: suggest file and folder paths
      val prefix = currentWord.lowercase()
      val baseTokens = tokens.dropLast(1).joinToString(" ")
      allPaths.forEach { p ->
        if (p.lowercase().startsWith(prefix) || p.substringAfterLast("/").lowercase().startsWith(prefix)) {
          val completed = if (isBashPrefix) "$ $baseTokens $p" else "$baseTokens $p"
          results.add(completed)
        }
      }
    }

    return results.distinct().take(8)
  }

  fun clearTerminal() {
    _uiState.update { it.copy(terminalItems = emptyList()) }
    _uiState.value.currentWorkspace?.let { addWelcomeBanner(it.name) }
  }

  private fun appendTerminalItem(item: TerminalItem) {
    _uiState.update {
      it.copy(terminalItems = it.terminalItems + item)
    }
  }

  private fun addWelcomeBanner(workspaceName: String) {
    val banner = """
   ____                      ______          __       ________    ____
  / __ \____  ___  ____     / ____/___  ____/ /__    / ____/ /   /  _/
 / / / / __ \/ _ \/ __ \   / /   / __ \/ __  / _ \  / /   / /    / /  
/ /_/ / /_/ /  __/ / / /  / /___/ /_/ / /_/ /  __/ / /___/ /____/ /   
\____/ .___/\___/_/ /_/   \____/\____/\__,_/\___/  \____/_____/___/   
    /_/                                                               
======================================================================
⚡ Autonomous AI Coding Agent for Android
📁 Root Workspace : $workspaceName
🤖 Default Model   : ${_uiState.value.selectedModel.name} (${_uiState.value.selectedModel.badge})
Type '/help' for commands, '/workspace' to switch project folder.
======================================================================
""".trimIndent()
    appendTerminalItem(TerminalItem.CommandOutput(output = banner, exitCode = 0))
  }
}
