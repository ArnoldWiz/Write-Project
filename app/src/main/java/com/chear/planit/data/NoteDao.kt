package com.chear.planit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nota: Note): Long

    @Update
    suspend fun update(nota: Note)

    @Delete
    suspend fun delete(nota: Note)

    @Query("SELECT * FROM table_notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    @Query("SELECT * FROM table_notes ORDER BY date DESC")
    fun getNotes(): Flow<List<Note>>
}