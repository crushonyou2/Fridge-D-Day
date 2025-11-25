package app.fridgedday.ui.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fridgedday.data.db.entity.ItemEntity
import app.fridgedday.data.db.entity.StorageLocation
import app.fridgedday.data.repo.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddEditUiState(
    val isEditMode: Boolean = false,
    val name: String = "",
    val category: String = "",
    val location: StorageLocation = StorageLocation.FRIDGE,
    val quantity: String = "",
    val unit: String = "",
    val expiryDate: LocalDate = LocalDate.now().plusDays(7),
    val daysBeforeNotify: Int = 3,
    val note: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

class AddEditViewModel(
    private val repository: ItemRepository,
    private val itemId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        if (itemId != null) {
            loadItem(itemId)
        }
    }

    private fun loadItem(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val item = repository.getById(id)
            if (item != null) {
                _uiState.update {
                    it.copy(
                        isEditMode = true,
                        name = item.name,
                        category = item.category ?: "",
                        location = item.location,
                        quantity = item.quantity?.toString() ?: "",
                        unit = item.unit ?: "",
                        expiryDate = item.expiryDate,
                        daysBeforeNotify = item.daysBeforeNotify,
                        note = item.note ?: "",
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "항목을 찾을 수 없습니다"
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateLocation(location: StorageLocation) {
        _uiState.update { it.copy(location = location) }
    }

    fun updateQuantity(quantity: String) {
        _uiState.update { it.copy(quantity = quantity) }
    }

    fun updateUnit(unit: String) {
        _uiState.update { it.copy(unit = unit) }
    }

    fun updateExpiryDate(date: LocalDate) {
        _uiState.update { it.copy(expiryDate = date) }
    }

    fun updateDaysBeforeNotify(days: Int) {
        _uiState.update { it.copy(daysBeforeNotify = days) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun saveItem() {
        val state = _uiState.value

        // Validation
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "이름을 입력해주세요") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val quantityValue = state.quantity.toFloatOrNull()

                val item = ItemEntity(
                    id = itemId ?: 0,
                    name = state.name.trim(),
                    category = state.category.trim().ifBlank { null },
                    location = state.location,
                    quantity = quantityValue,
                    unit = state.unit.trim().ifBlank { null },
                    expiryDate = state.expiryDate,
                    daysBeforeNotify = state.daysBeforeNotify,
                    note = state.note.trim().ifBlank { null }
                )

                if (state.isEditMode) {
                    repository.update(item)
                } else {
                    repository.insert(item)
                }

                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "저장 중 오류가 발생했습니다: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
