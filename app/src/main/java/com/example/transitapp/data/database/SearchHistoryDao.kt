package com.example.transitapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSearch(searchHistory: SearchHistoryEntity)
    
    @Query("SELECT * FROM search_history ORDER BY searchCount DESC, timestamp DESC LIMIT :limit")
    fun getTopSearches(limit: Int): Flow<List<SearchHistoryEntity>>
    
    @Query("SELECT * FROM search_history WHERE query = :query")
    suspend fun getSearchByQuery(query: String): SearchHistoryEntity?
    
    @Query("UPDATE search_history SET searchCount = searchCount + 1, timestamp = :timestamp WHERE query = :query")
    suspend fun incrementSearchCount(query: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearches(limit: Int): Flow<List<SearchHistoryEntity>>
    
    @Query("SELECT COUNT(*) FROM search_history")
    suspend fun getSearchCount(): Int
    
    @Query("SELECT * FROM search_history ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomSearches(limit: Int): List<SearchHistoryEntity>
} 