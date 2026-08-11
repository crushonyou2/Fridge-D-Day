package app.fridgedday.ui.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.fridgedday.data.db.entity.ItemEntity
import app.fridgedday.data.db.entity.StorageLocation
import app.fridgedday.data.repo.AddEditItemRepository
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
    val expiryDate: LocalDate? = null,
    val isExpiryDateConfirmed: Boolean = false,
    val pendingOcrDate: LocalDate? = null,
    val daysBeforeNotify: Int = 3,
    val note: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

class AddEditViewModel(
    private val repository: AddEditItemRepository,
    private val itemId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddEditUiState(isLoading = itemId != null)
    )
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()
    private var originalItem: ItemEntity? = null

    init {
        if (itemId != null) {
            loadItem(itemId)
        }
    }

    private fun loadItem(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val item = repository.getById(id)
                if (item != null) {
                    originalItem = item
                    _uiState.update {
                        it.copy(
                            isEditMode = true,
                            name = item.name,
                            category = item.category ?: "",
                            location = item.location,
                            quantity = item.quantity?.toString() ?: "",
                            unit = item.unit ?: "",
                            expiryDate = item.expiryDate,
                            isExpiryDateConfirmed = true,
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
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "항목을 불러오지 못했습니다"
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

    fun confirmManualExpiryDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                expiryDate = date,
                isExpiryDateConfirmed = true,
                pendingOcrDate = null,
                errorMessage = null
            )
        }
    }

    fun proposeOcrDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                pendingOcrDate = date,
                errorMessage = null
            )
        }
    }

    fun confirmPendingOcrDate() {
        _uiState.update { state ->
            val pendingDate = state.pendingOcrDate ?: return@update state
            state.copy(
                expiryDate = pendingDate,
                isExpiryDateConfirmed = true,
                pendingOcrDate = null,
                errorMessage = null
            )
        }
    }

    fun cancelPendingOcrDate() {
        _uiState.update { it.copy(pendingOcrDate = null) }
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

        val confirmedExpiryDate = state.expiryDate
        if (confirmedExpiryDate == null || !state.isExpiryDateConfirmed) {
            _uiState.update { it.copy(errorMessage = "유통기한을 선택하고 확인해주세요") }
            return
        }

        if (itemId != null && originalItem == null) {
            _uiState.update { it.copy(errorMessage = "수정할 항목을 불러오지 못했습니다") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val quantityValue = state.quantity.toFloatOrNull()

                val existingItem = originalItem
                val item = existingItem?.copy(
                    name = state.name.trim(),
                    category = state.category.trim().ifBlank { null },
                    location = state.location,
                    quantity = quantityValue,
                    unit = state.unit.trim().ifBlank { null },
                    expiryDate = confirmedExpiryDate,
                    daysBeforeNotify = state.daysBeforeNotify,
                    note = state.note.trim().ifBlank { null }
                ) ?: ItemEntity(
                    name = state.name.trim(),
                    category = state.category.trim().ifBlank { null },
                    location = state.location,
                    quantity = quantityValue,
                    unit = state.unit.trim().ifBlank { null },
                    expiryDate = confirmedExpiryDate,
                    daysBeforeNotify = state.daysBeforeNotify,
                    note = state.note.trim().ifBlank { null }
                )

                if (existingItem != null) {
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

class AddEditViewModelFactory(
    private val repository: AddEditItemRepository,
    private val itemId: Long?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AddEditViewModel::class.java))
        return AddEditViewModel(repository, itemId) as T
    }
}
