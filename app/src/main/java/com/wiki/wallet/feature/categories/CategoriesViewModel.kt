package com.wiki.wallet.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiki.wallet.domain.model.Category
import com.wiki.wallet.domain.repository.CategoryRepository
import com.wiki.wallet.domain.usecase.GetCategoriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val isDetailBottomSheetOpen: Boolean = false,
    val isEditingBudget: Boolean = false,
    val editedBudgetText: String = ""
)

sealed interface CategoriesUiEvent {
    data class OnCategoryClick(val category: Category) : CategoriesUiEvent
    data class OnBudgetTextChanged(val text: String) : CategoriesUiEvent
    data object OnToggleEditBudget : CategoriesUiEvent
    data object OnSaveBudgetClicked : CategoriesUiEvent
    data object OnDismissDetail : CategoriesUiEvent
    data object OnBackClicked : CategoriesUiEvent
}

class CategoriesViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        getCategoriesUseCase()
            .onEach { categories ->
                _uiState.update { state ->
                    val updatedSelected = state.selectedCategory?.let { sel -> categories.firstOrNull { it.id == sel.id } }
                    state.copy(
                        categories = categories,
                        selectedCategory = updatedSelected ?: state.selectedCategory
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: CategoriesUiEvent) {
        when (event) {
            is CategoriesUiEvent.OnCategoryClick -> {
                val budget = event.category.monthlyBudget
                val budgetStr = if (budget != null) String.format(java.util.Locale.US, "%.2f", budget) else ""
                _uiState.update {
                    it.copy(
                        selectedCategory = event.category,
                        isDetailBottomSheetOpen = true,
                        isEditingBudget = false,
                        editedBudgetText = budgetStr
                    )
                }
            }
            is CategoriesUiEvent.OnBudgetTextChanged -> {
                _uiState.update { it.copy(editedBudgetText = event.text) }
            }
            CategoriesUiEvent.OnToggleEditBudget -> {
                _uiState.update { it.copy(isEditingBudget = !it.isEditingBudget) }
            }
            CategoriesUiEvent.OnSaveBudgetClicked -> {
                saveBudget()
            }
            CategoriesUiEvent.OnDismissDetail -> {
                _uiState.update {
                    it.copy(isDetailBottomSheetOpen = false, isEditingBudget = false)
                }
            }
            CategoriesUiEvent.OnBackClicked -> {}
        }
    }

    private fun saveBudget() {
        val cat = _uiState.value.selectedCategory ?: return
        val newBudget = _uiState.value.editedBudgetText.toDoubleOrNull()

        viewModelScope.launch {
            categoryRepository.updateMonthlyBudget(cat.id, newBudget)
            _uiState.update { it.copy(isEditingBudget = false) }
        }
    }
}
