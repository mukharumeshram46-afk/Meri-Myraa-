package com.example.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MemoryType {
    PROFILE,
    PREFERENCE,
    CONVERSATION_SUMMARY,
    TASK,
    IMPORTANT_NOTE,
    DEVICE_PREFERENCE
}

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String,
    val type: MemoryType,
    val timestamp: Long = System.currentTimeMillis()
)
