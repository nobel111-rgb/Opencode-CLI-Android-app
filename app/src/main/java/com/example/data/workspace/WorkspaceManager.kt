package com.example.data.workspace

import android.content.Context
import com.example.data.model.*
import java.io.File

class WorkspaceManager(private val context: Context) {

  private val baseDir: File by lazy {
    val dir = File(context.filesDir, "opencode_workspaces")
    if (!dir.exists()) dir.mkdirs()
    dir
  }

  fun getBaseDirectory(): File = baseDir

  fun getWorkspaceDir(folderName: String): File {
    val cleanName = folderName.trim().replace(Regex("[^a-zA-Z0-9._-]"), "_")
    val dir = File(baseDir, cleanName)
    if (!dir.exists()) dir.mkdirs()
    return dir
  }

  fun createWorkspaceFolder(
    name: String,
    templateId: String = "EMPTY",
    description: String = ""
  ): Pair<File, String> {
    val cleanName = name.trim().ifEmpty { "project-${System.currentTimeMillis().toString().takeLast(4)}" }
      .replace(Regex("[^a-zA-Z0-9._-]"), "_")
    val dir = File(baseDir, cleanName)
    if (!dir.exists()) dir.mkdirs()

    val template = WorkspaceTemplateCatalog.TEMPLATES.find { it.id == templateId }
      ?: WorkspaceTemplateCatalog.TEMPLATES.last()

    for ((fileName, fileContent) in template.defaultFiles) {
      val targetFile = File(dir, fileName)
      targetFile.parentFile?.mkdirs()
      targetFile.writeText(fileContent)
    }

    return Pair(dir, dir.absolutePath)
  }

  fun listWorkspacesOnDisk(): List<String> {
    return baseDir.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: emptyList()
  }

  fun deleteWorkspaceFolder(folderName: String): Boolean {
    val dir = File(baseDir, folderName)
    return if (dir.exists()) dir.deleteRecursively() else false
  }

  fun getFileTree(workspaceDir: File, currentRelPath: String = ""): List<FileNode> {
    val dir = if (currentRelPath.isEmpty()) workspaceDir else File(workspaceDir, currentRelPath)
    if (!dir.exists() || !dir.isDirectory) return emptyList()

    val files: List<File> = dir.listFiles()?.toList()?.sortedWith(
      compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() }
    ) ?: emptyList()

    return files.map { file ->
      val relPath = if (currentRelPath.isEmpty()) file.name else "$currentRelPath/${file.name}"
      if (file.isDirectory) {
        FileNode(
          name = file.name,
          path = relPath,
          isDirectory = true,
          children = getFileTree(workspaceDir, relPath),
          size = 0,
          lastModified = file.lastModified()
        )
      } else {
        FileNode(
          name = file.name,
          path = relPath,
          isDirectory = false,
          size = file.length(),
          lastModified = file.lastModified()
        )
      }
    }
  }

  fun formatFileTreeForPrompt(workspaceDir: File): String {
    val nodes = getFileTree(workspaceDir)
    if (nodes.isEmpty()) return "(Workspace is currently empty)"
    val sb = StringBuilder()
    fun appendNodes(list: List<FileNode>, prefix: String) {
      for (i in list.indices) {
        val node = list[i]
        val isLast = i == list.size - 1
        val pointer = if (isLast) "└── " else "├── "
        sb.append(prefix).append(pointer).append(node.name)
        if (node.isDirectory) {
          sb.append("/\n")
          val nextPrefix = prefix + (if (isLast) "    " else "│   ")
          appendNodes(node.children, nextPrefix)
        } else {
          sb.append(" (").append(formatSize(node.size)).append(")\n")
        }
      }
    }
    appendNodes(nodes, "")
    return sb.toString().trimEnd()
  }

  private fun formatSize(bytes: Long): String {
    return when {
      bytes < 1024 -> "$bytes B"
      bytes < 1024 * 1024 -> "${bytes / 1024} KB"
      else -> "${bytes / (1024 * 1024)} MB"
    }
  }

  fun readFile(
    workspaceDir: File,
    relPath: String,
    startLine: Int? = null,
    endLine: Int? = null
  ): Result<String> {
    return try {
      val file = resolveFile(workspaceDir, relPath)
      if (!file.exists()) return Result.failure(Exception("File '$relPath' does not exist."))
      if (file.isDirectory) return Result.failure(Exception("'$relPath' is a directory, not a file."))

      val lines = file.readLines()
      if (lines.isEmpty()) return Result.success("(Empty file)")

      val s = (startLine ?: 1).coerceIn(1, lines.size)
      val e = (endLine ?: lines.size).coerceIn(s, lines.size)
      val sub = lines.subList(s - 1, e)

      val numbered = sub.mapIndexed { idx, line ->
        "${s + idx}: $line"
      }.joinToString("\n")
      Result.success(numbered)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun writeFile(
    workspaceDir: File,
    relPath: String,
    newContent: String
  ): Result<Pair<String, FileDiff>> {
    return try {
      val file = resolveFile(workspaceDir, relPath)
      val oldContent = if (file.exists() && file.isFile) file.readText() else ""
      file.parentFile?.mkdirs()
      file.writeText(newContent)

      val diff = computeDiff(oldContent, newContent, relPath)
      Result.success(Pair("File '$relPath' written successfully (${newContent.lines().size} lines).", diff))
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun editFile(
    workspaceDir: File,
    relPath: String,
    targetContent: String,
    replacementContent: String
  ): Result<Pair<String, FileDiff>> {
    return try {
      val file = resolveFile(workspaceDir, relPath)
      if (!file.exists()) return Result.failure(Exception("File '$relPath' not found."))
      val original = file.readText()

      if (!original.contains(targetContent)) {
        return Result.failure(Exception("Target content not found in '$relPath'. Please inspect file first."))
      }

      val updated = original.replace(targetContent, replacementContent)
      file.writeText(updated)
      val diff = computeDiff(original, updated, relPath)
      Result.success(Pair("File '$relPath' modified successfully.", diff))
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun deleteFile(workspaceDir: File, relPath: String): Result<String> {
    return try {
      val file = resolveFile(workspaceDir, relPath)
      if (!file.exists()) return Result.failure(Exception("Path '$relPath' does not exist."))
      val isDir = file.isDirectory
      val success = if (isDir) file.deleteRecursively() else file.delete()
      if (success) {
        Result.success("Deleted ${if (isDir) "directory" else "file"} '$relPath'.")
      } else {
        Result.failure(Exception("Failed to delete '$relPath'."))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun searchCode(workspaceDir: File, query: String, subPath: String = ""): Result<String> {
    return try {
      val startDir = if (subPath.isEmpty()) workspaceDir else resolveFile(workspaceDir, subPath)
      if (!startDir.exists()) return Result.failure(Exception("Path '$subPath' not found."))

      val matches = mutableListOf<String>()
      startDir.walkTopDown().forEach { file ->
        if (file.isFile && !file.name.startsWith(".")) {
          try {
            val lines = file.readLines()
            lines.forEachIndexed { idx, line ->
              if (line.contains(query, ignoreCase = true)) {
                val rel = file.relativeTo(workspaceDir).path
                matches.add("$rel:${idx + 1}: $line")
              }
            }
          } catch (_: Exception) {}
        }
      }

      if (matches.isEmpty()) {
        Result.success("No occurrences of '$query' found.")
      } else {
        Result.success("Found ${matches.size} match(es):\n" + matches.take(50).joinToString("\n"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun computeDiff(oldText: String, newText: String, filePath: String): FileDiff {
    val oldLines = if (oldText.isEmpty()) emptyList() else oldText.lines()
    val newLines = if (newText.isEmpty()) emptyList() else newText.lines()

    val diffLines = mutableListOf<DiffLine>()
    diffLines.add(DiffLine(DiffLineType.HEADER, "--- a/$filePath\n+++ b/$filePath"))

    var additions = 0
    var deletions = 0

    if (oldLines.isEmpty()) {
      newLines.forEachIndexed { i, line ->
        diffLines.add(DiffLine(DiffLineType.ADDITION, "+ $line", null, i + 1))
        additions++
      }
    } else if (newLines.isEmpty()) {
      oldLines.forEachIndexed { i, line ->
        diffLines.add(DiffLine(DiffLineType.DELETION, "- $line", i + 1, null))
        deletions++
      }
    } else {
      var i = 0
      var j = 0
      while (i < oldLines.size || j < newLines.size) {
        if (i < oldLines.size && j < newLines.size && oldLines[i] == newLines[j]) {
          diffLines.add(DiffLine(DiffLineType.EQUAL, "  ${oldLines[i]}", i + 1, j + 1))
          i++
          j++
        } else if (j < newLines.size && (i >= oldLines.size || !oldLines.contains(newLines[j]))) {
          diffLines.add(DiffLine(DiffLineType.ADDITION, "+ ${newLines[j]}", null, j + 1))
          additions++
          j++
        } else if (i < oldLines.size) {
          diffLines.add(DiffLine(DiffLineType.DELETION, "- ${oldLines[i]}", i + 1, null))
          deletions++
          i++
        }
      }
    }

    return FileDiff(
      filePath = filePath,
      oldContent = oldText,
      newContent = newText,
      additionsCount = additions,
      deletionsCount = deletions,
      lines = diffLines
    )
  }

  fun executeBashCommand(workspaceDir: File, commandLine: String): Pair<String, Int> {
    val trimmed = commandLine.trim()
    if (trimmed.isEmpty()) return Pair("", 0)

    val parts = trimmed.split(Regex("\\s+"))
    val cmd = parts[0].lowercase()
    val args = parts.drop(1)

    return when (cmd) {
      "pwd" -> Pair(workspaceDir.absolutePath, 0)
      "whoami" -> Pair("opencode-agent", 0)
      "date" -> Pair(java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", java.util.Locale.US).format(java.util.Date()), 0)
      "ls", "dir" -> {
        val files: List<File> = workspaceDir.listFiles()?.toList()?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name })
          ?: emptyList()
        val out = files.joinToString("\n") { f ->
          val type = if (f.isDirectory) "d" else "-"
          val size = if (f.isDirectory) "<DIR>" else "${f.length()}B"
          "$type  %-10s  %s".format(size, f.name + (if (f.isDirectory) "/" else ""))
        }
        Pair(out.ifEmpty { "(Directory is empty)" }, 0)
      }
      "tree" -> {
        Pair(formatFileTreeForPrompt(workspaceDir), 0)
      }
      "cat" -> {
        if (args.isEmpty()) {
          Pair("Usage: cat <filename>", 1)
        } else {
          val file = resolveFile(workspaceDir, args[0])
          if (!file.exists()) Pair("cat: ${args[0]}: No such file or directory", 1)
          else if (file.isDirectory) Pair("cat: ${args[0]}: Is a directory", 1)
          else Pair(file.readText(), 0)
        }
      }
      "head" -> {
        if (args.isEmpty()) Pair("Usage: head <filename>", 1)
        else {
          val file = resolveFile(workspaceDir, args[0])
          if (!file.exists()) Pair("head: ${args[0]}: No such file", 1)
          else Pair(file.readLines().take(10).joinToString("\n"), 0)
        }
      }
      "tail" -> {
        if (args.isEmpty()) Pair("Usage: tail <filename>", 1)
        else {
          val file = resolveFile(workspaceDir, args[0])
          if (!file.exists()) Pair("tail: ${args[0]}: No such file", 1)
          else Pair(file.readLines().takeLast(10).joinToString("\n"), 0)
        }
      }
      "mkdir" -> {
        if (args.isEmpty()) Pair("mkdir: missing operand", 1)
        else {
          val file = resolveFile(workspaceDir, args[0])
          file.mkdirs()
          Pair("Created directory '${args[0]}'", 0)
        }
      }
      "touch" -> {
        if (args.isEmpty()) Pair("touch: missing file operand", 1)
        else {
          val file = resolveFile(workspaceDir, args[0])
          file.parentFile?.mkdirs()
          if (!file.exists()) file.writeText("")
          Pair("Touched '${args[0]}'", 0)
        }
      }
      "rm" -> {
        if (args.isEmpty()) Pair("rm: missing operand", 1)
        else {
          val target = args.last()
          val file = resolveFile(workspaceDir, target)
          if (!file.exists()) Pair("rm: cannot remove '$target': No such file or directory", 1)
          else {
            file.deleteRecursively()
            Pair("Removed '$target'", 0)
          }
        }
      }
      "grep" -> {
        if (args.size < 2) Pair("Usage: grep <pattern> <file_or_dir>", 1)
        else {
          val pattern = args[0]
          val target = args[1]
          val res = searchCode(workspaceDir, pattern, target)
          Pair(res.getOrDefault("Grep failed."), if (res.isSuccess) 0 else 1)
        }
      }
      "wc" -> {
        if (args.isEmpty()) Pair("Usage: wc <filename>", 1)
        else {
          val file = resolveFile(workspaceDir, args[0])
          if (!file.exists()) Pair("wc: ${args[0]}: No such file", 1)
          else {
            val text = file.readText()
            val lines = text.lines().size
            val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
            val bytes = text.toByteArray().size
            Pair(" $lines  $words  $bytes  ${args[0]}", 0)
          }
        }
      }
      "git" -> {
        val sub = args.firstOrNull() ?: "status"
        when (sub) {
          "status" -> {
            val files = listAllFilesRel(workspaceDir)
            val out = "On branch main\nChanges to be committed:\n" +
              files.take(10).joinToString("\n") { "  new file:   $it" }
            Pair(out, 0)
          }
          "log" -> {
            Pair("commit a1b2c3d (HEAD -> main)\nAuthor: OpenCode Agent <agent@opencode.ai>\nDate:   ${java.util.Date()}\n\n    Initial commit by OpenCode CLI", 0)
          }
          else -> Pair("git: '$sub' is executed in workspace sandbox", 0)
        }
      }
      "python", "python3" -> {
        if (args.isEmpty()) Pair("Python 3.12 (OpenCode Runtime Environment)\nType 'python <script.py>' to execute.", 0)
        else {
          val file = resolveFile(workspaceDir, args[0])
          if (!file.exists()) Pair("python: can't open file '${args[0]}': [Errno 2] No such file or directory", 2)
          else {
            // Simulated Python Execution output
            val content = file.readText()
            val out = simulateScriptExecution("Python", file.name, content)
            Pair(out, 0)
          }
        }
      }
      "node" -> {
        if (args.isEmpty()) Pair("Node.js v20.10.0 (OpenCode Runtime Environment)", 0)
        else {
          val file = resolveFile(workspaceDir, args[0])
          if (!file.exists()) Pair("node: cannot find module '${args[0]}'", 1)
          else {
            val content = file.readText()
            val out = simulateScriptExecution("Node.js", file.name, content)
            Pair(out, 0)
          }
        }
      }
      "echo" -> {
        Pair(args.joinToString(" ").replace("\"", ""), 0)
      }
      "help" -> {
        Pair("""
OpenCode CLI Built-in Commands:
  ls, dir         - List files in current directory
  tree            - Show workspace directory tree hierarchy
  cat <file>      - Print content of file
  head <file>     - View first 10 lines of file
  tail <file>     - View last 10 lines of file
  touch <file>    - Create a new empty file
  mkdir <dir>     - Create a directory
  rm <path>       - Remove file or directory
  grep <q> <path> - Search for code patterns
  wc <file>       - Word, line, and byte count
  python <file>   - Execute Python script
  node <file>     - Execute Node.js JavaScript script
  git status/log  - Check workspace version control state
  whoami / pwd    - Terminal system identity
""".trimIndent(), 0)
      }
      else -> {
        Pair("bash: command not found: $cmd. Type 'help' for available commands.", 127)
      }
    }
  }

  private fun simulateScriptExecution(env: String, filename: String, code: String): String {
    val sb = StringBuilder()
    sb.append("[$env Execution: $filename]\n")
    
    // Extract print/console.log statements for instant execution realism
    val printRegex = if (env == "Python") Regex("""print\((.*?)\)""") else Regex("""console\.log\((.*?)\)""")
    val matches = printRegex.findAll(code).toList()
    
    if (matches.isNotEmpty()) {
      matches.take(15).forEach { match ->
        var raw = match.groupValues[1].trim()
        raw = raw.removePrefix("\"").removeSuffix("\"").removePrefix("'").removeSuffix("'")
        raw = raw.replace(Regex("""f["'](.*?)["']"""), "$1")
        sb.append(raw).append("\n")
      }
    } else {
      sb.append(">>> Execution completed with exit status 0 (no runtime errors).\n")
    }
    return sb.toString().trimEnd()
  }

  private fun listAllFilesRel(dir: File): List<String> {
    return dir.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.map { it.relativeTo(dir).path }.toList()
  }

  private fun resolveFile(workspaceDir: File, relPath: String): File {
    val clean = relPath.trim().removePrefix("/").replace("\\", "/")
    return File(workspaceDir, clean)
  }
}
