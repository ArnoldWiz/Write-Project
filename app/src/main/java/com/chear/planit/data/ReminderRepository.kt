package com.chear.planit.data

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) : Repository<Reminder> {
    override fun getAll(): Flow<List<Reminder>> = reminderDao.getAllReminders()
    override suspend fun insert(item: Reminder) = reminderDao.insert(item)
    override suspend fun update(item: Reminder) = reminderDao.update(item)
    override suspend fun delete(item: Reminder) = reminderDao.delete(item)
}
