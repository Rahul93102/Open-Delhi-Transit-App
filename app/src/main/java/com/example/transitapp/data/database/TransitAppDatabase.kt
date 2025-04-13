package com.example.transitapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SearchHistoryEntity::class], version = 1)
abstract class TransitAppDatabase : RoomDatabase() {
    
    abstract fun searchHistoryDao(): SearchHistoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: TransitAppDatabase? = null
        
        fun getDatabase(context: Context): TransitAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransitAppDatabase::class.java,
                    "transit_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 