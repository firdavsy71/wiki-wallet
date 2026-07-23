package com.wiki.wallet.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.domain.model.Category
import com.wiki.wallet.domain.usecase.GetCategoriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val isDetailBottomSheetOpen: Boolean = false
)

sealed interface CategoriesUiEvent {
    data class OnCategoryClick(val category: Category) : CategoriesUiEvent
    data object OnDismissDetail : CategoriesUiEvent
    data object OnBackClicked : CategoriesUiEvent
}

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        getCategoriesUseCase()
            .onEach { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: CategoriesUiEvent) {
        when (event) {
            is CategoriesUiEvent.OnCategoryClick -> {
                _uiState.update {
                    it.copy(
                        selectedCategory = event.category,
                        isDetailBottomSheetOpen = true
                    )
                }
            }
            CategoriesUiEvent.OnDismissDetail -> {
                _uiState.update {
                    it.copy(isDetailBottomSheetOpen = false)
                }
            }
            CategoriesUiEvent.OnBackClicked -> {}
        }
    }
}
