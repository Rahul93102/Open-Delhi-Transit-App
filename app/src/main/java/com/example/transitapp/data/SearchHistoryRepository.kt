package com.example.transitapp.data

import com.example.transitapp.data.database.SearchHistoryDao
import com.example.transitapp.data.database.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

class SearchHistoryRepository(private val searchHistoryDao: SearchHistoryDao) {
    
    // Get top 3 searches as a Flow
    val topSearches: Flow<List<SearchHistoryEntity>> = searchHistoryDao.getTopSearches(3)
    
    // Default suggestions for first-time users
    private val defaultSuggestions = listOf(
        "DL1PD1034", 
        "DL1PC6453", 
        "DL1PB2277"
    )
    
    suspend fun addSearch(query: String) {
        // Check if search exists
        val existingSearch = searchHistoryDao.getSearchByQuery(query)
        
        if (existingSearch != null) {
            // Update count
            searchHistoryDao.incrementSearchCount(query)
        } else {
            // Insert new search
            val searchHistory = SearchHistoryEntity(
                query = query,
                timestamp = System.currentTimeMillis(),
                searchCount = 1
            )
            searchHistoryDao.insertOrUpdateSearch(searchHistory)
        }
    }
    
    suspend fun getDefaultSuggestions(): List<String> {
        return defaultSuggestions
    }
} 