package com.chear.planit.data

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()
    suspend fun insert(reminder: Reminder) = reminderDao.insert(reminder)
    suspend fun update(reminder: Reminder) = reminderDao.update(reminder)
    suspend fun delete(reminder: Reminder) = reminderDao.delete(reminder)
}
