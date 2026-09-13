package com.example.memory

import kotlinx.coroutines.flow.first

class MemoryManager(private val repository: MemoryRepository) {

    suspend fun remember(key: String, value: String, type: MemoryType = MemoryType.PREFERENCE): String {
        repository.insertMemory(key, value, type)
        return "Haan Piyush, maine yaad rakh liya: '$value' ❤️"
    }

    suspend fun forget(query: String): String {
        val matches = repository.searchMemories(query)
        if (matches.isEmpty()) {
            return "Piyush, mujhe '$query' ke baare mein kuch specific yaad nahi tha."
        }
        for (item in matches) {
            repository.deleteMemory(item)
        }
        return "Theek hai Piyush, maine '$query' se related information bhula di."
    }

    suspend fun recallAll(): String {
        val memories = repository.allMemories.first()
        if (memories.isEmpty()) {
            return "Piyush, abhi tak meri memory mein koi saved details nahi hain. Aap mujhe kuch yaad rakhne ko bol sakte ho!"
        }
        val builder = StringBuilder("Piyush, mujhe aapke baare mein ye yaad hai:\n")
        memories.take(10).forEachIndexed { index, mem ->
            builder.append("${index + 1}. ${mem.value}\n")
        }
        return builder.toString().trim()
    }

    suspend fun clearAll(): String {
        repository.clearAll()
        return "Piyush, aapki sari saved memories clear kar di gayi hain."
    }

    suspend fun getMemoryContextForPrompt(): String {
        return try {
            val list = repository.allMemories.first()
            if (list.isEmpty()) {
                "No custom memories recorded yet."
            } else {
                list.take(8).joinToString("; ") { "${it.key}: ${it.value}" }
            }
        } catch (e: Exception) {
            "No memory available"
        }
    }
}
