package com.example.memory

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromMemoryType(type: MemoryType): String = type.name

    @TypeConverter
    fun toMemoryType(value: String): MemoryType = try {
        MemoryType.valueOf(value)
    } catch (e: Exception) {
        MemoryType.IMPORTANT_NOTE
    }
}

@Database(entities = [MemoryEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MyraaDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: MyraaDatabase? = null

        fun getInstance(context: Context): MyraaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyraaDatabase::class.java,
                    "myraa_assistant_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
