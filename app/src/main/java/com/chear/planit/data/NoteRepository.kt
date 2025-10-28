package com.chear.planit.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) : Repository<Note> {
    override fun getAll(): Flow<List<Note>> = noteDao.getNotes()
    override suspend fun insert(item: Note) = noteDao.insert(item)
    override suspend fun update(item: Note) = noteDao.update(item)
    override suspend fun delete(item: Note) = noteDao.delete(item)
}
