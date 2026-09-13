package com.example.memory

import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()
    val memoryCount: Flow<Int> = memoryDao.getMemoryCount()

    fun getMemoriesByType(type: MemoryType): Flow<List<MemoryEntity>> {
        return memoryDao.getMemoriesByType(type)
    }

    suspend fun insertMemory(key: String, value: String, type: MemoryType): Long {
        return memoryDao.insertMemory(
            MemoryEntity(
                key = key.trim(),
                value = value.trim(),
                type = type
            )
        )
    }

    suspend fun deleteMemory(memory: MemoryEntity) {
        memoryDao.deleteMemory(memory)
    }

    suspend fun deleteMemoryById(id: Long) {
        memoryDao.deleteMemoryById(id)
    }

    suspend fun deleteMemoryByKey(key: String) {
        memoryDao.deleteMemoryByKey(key)
    }

    suspend fun searchMemories(query: String): List<MemoryEntity> {
        return memoryDao.searchMemories(query)
    }

    suspend fun clearAll() {
        memoryDao.clearAllMemories()
    }
}
