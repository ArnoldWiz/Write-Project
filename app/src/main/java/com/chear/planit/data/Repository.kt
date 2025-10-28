package com.chear.planit.data

import kotlinx.coroutines.flow.Flow
interface Repository<T> {
    fun getAll(): Flow<List<T>>
    suspend fun insert(item: T)
    suspend fun update(item: T)
    suspend fun delete(item: T)
}
