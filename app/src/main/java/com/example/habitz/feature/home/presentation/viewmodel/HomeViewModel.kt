package com.example.habitz.feature.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitz.core.database.entity.HabitCategory
import com.example.habitz.core.services.interfaces.IHomeService
import com.example.habitz.feature.home.presentation.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeService: IHomeService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState.from(homeService.getDashboardData()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onCategorySelected(category: HabitCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun searchHabits(query: String){
        _uiState.update{ it.copy(habits = homeService.searchHabits(query))}
    }
}
