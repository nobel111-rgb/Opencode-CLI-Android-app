package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
  // Workspaces
  @Query("SELECT * FROM workspaces ORDER BY lastAccessedAt DESC")
  fun getAllWorkspaces(): Flow<List<WorkspaceEntity>>

  @Query("SELECT * FROM workspaces WHERE id = :id LIMIT 1")
  suspend fun getWorkspaceById(id: String): WorkspaceEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWorkspace(workspace: WorkspaceEntity)

  @Update
  suspend fun updateWorkspace(workspace: WorkspaceEntity)

  @Delete
  suspend fun deleteWorkspace(workspace: WorkspaceEntity)

  @Query("DELETE FROM workspaces WHERE id = :id")
  suspend fun deleteWorkspaceById(id: String)

  // Sessions
  @Query("SELECT * FROM sessions WHERE workspaceId = :workspaceId ORDER BY updatedAt DESC")
  fun getSessionsForWorkspace(workspaceId: String): Flow<List<SessionEntity>>

  @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
  suspend fun getSessionById(id: String): SessionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: SessionEntity)

  @Update
  suspend fun updateSession(session: SessionEntity)

  @Query("DELETE FROM sessions WHERE id = :id")
  suspend fun deleteSessionById(id: String)

  // Messages
  @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
  fun getMessagesForSession(sessionId: String): Flow<List<MessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: MessageEntity): Long

  @Query("DELETE FROM messages WHERE sessionId = :sessionId")
  suspend fun deleteMessagesForSession(sessionId: String)

  // Command History
  @Query("SELECT * FROM command_history WHERE workspaceId = :workspaceId ORDER BY timestamp DESC")
  fun getCommandHistoryForWorkspace(workspaceId: String): Flow<List<CommandHistoryEntity>>

  @Query("SELECT * FROM command_history ORDER BY timestamp DESC LIMIT 150")
  fun getAllCommandHistory(): Flow<List<CommandHistoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCommandHistory(entry: CommandHistoryEntity): Long

  @Query("DELETE FROM command_history WHERE id = :id")
  suspend fun deleteCommandHistoryById(id: Long)

  @Query("DELETE FROM command_history WHERE workspaceId = :workspaceId")
  suspend fun clearHistoryForWorkspace(workspaceId: String)

  // Command Macros & Aliases
  @Query("SELECT * FROM command_macros ORDER BY usageCount DESC, createdAt DESC")
  fun getAllMacros(): Flow<List<CommandMacroEntity>>

  @Query("SELECT * FROM command_macros WHERE workspaceId IS NULL OR workspaceId = :workspaceId ORDER BY usageCount DESC, createdAt DESC")
  fun getMacrosForWorkspace(workspaceId: String): Flow<List<CommandMacroEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMacro(macro: CommandMacroEntity): Long

  @Update
  suspend fun updateMacro(macro: CommandMacroEntity)

  @Query("UPDATE command_macros SET usageCount = usageCount + 1 WHERE id = :id")
  suspend fun incrementMacroUsage(id: Long)

  @Query("DELETE FROM command_macros WHERE id = :id")
  suspend fun deleteMacroById(id: Long)

  @Query("SELECT COUNT(*) FROM command_macros")
  suspend fun getMacroCount(): Int
}
