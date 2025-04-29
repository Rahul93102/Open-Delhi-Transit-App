package com.example.transitapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transitapp.data.model.Bus
import com.example.transitapp.data.repository.TransitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class BusSearchViewModel(
    private val repository: TransitRepository
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Initial)
    val searchState: StateFlow<SearchState> = _searchState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun searchBuses() {
        val query = _searchQuery.value
        if (query.isBlank()) {
            _searchState.value = SearchState.Error("Please enter an address")
            return
        }

        _searchState.value = SearchState.Loading
        viewModelScope.launch {
            repository.findNearbyBuses(query)
                .catch { e ->
                    _searchState.value = SearchState.Error(e.message ?: "Unknown error occurred")
                }
                .collect { buses ->
                    _searchState.value = if (buses.isEmpty()) {
                        SearchState.Empty
                    } else {
                        SearchState.Success(buses)
                    }
                }
        }
    }

    sealed class SearchState {
        object Initial : SearchState()
        object Loading : SearchState()
        object Empty : SearchState()
        data class Success(val buses: List<Bus>) : SearchState()
        data class Error(val message: String) : SearchState()
    }
} 