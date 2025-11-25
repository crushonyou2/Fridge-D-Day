package app.fridgedday.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fridgedday.data.db.entity.ItemEntity
import app.fridgedday.data.db.entity.StorageLocation
import app.fridgedday.data.repo.ItemRepository
import app.fridgedday.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class FilterType {
    ALL, EXPIRING, EXPIRED
}

enum class SortType {
    EXPIRY_DATE,  // 유통기한 임박순
    NAME,         // 이름순
    CREATED_DATE  // 등록일순
}

data class HomeUiState(
    val items: List<ItemEntity> = emptyList(),
    val filterType: FilterType = FilterType.ALL,
    val locationFilter: StorageLocation? = null,  // null이면 전체
    val sortType: SortType = SortType.EXPIRY_DATE,
    val searchKeyword: String = "",
    val isLoading: Boolean = false
)

class HomeViewModel(private val repository: ItemRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            repository.observeAll()
                .combine(
                    _uiState.map { Triple(it.filterType, it.locationFilter, it.sortType) }
                ) { items, (filter, location, sort) ->
                    var filtered = filterItems(items, filter)

                    // 보관 위치 필터 적용
                    if (location != null) {
                        filtered = filtered.filter { it.location == location }
                    }

                    // 정렬 적용
                    filtered = sortItems(filtered, sort)

                    filtered
                }
                .combine(
                    _uiState.map { it.searchKeyword }
                ) { items, keyword ->
                    if (keyword.isBlank()) items
                    else items.filter { it.name.contains(keyword, ignoreCase = true) }
                }
                .collect { filteredItems ->
                    _uiState.update { it.copy(items = filteredItems, isLoading = false) }
                }
        }
    }

    private fun filterItems(items: List<ItemEntity>, filter: FilterType): List<ItemEntity> {
        val today = LocalDate.now()
        return when (filter) {
            FilterType.ALL -> items
            FilterType.EXPIRING -> items.filter { item ->
                val days = DateUtils.daysUntil(item.expiryDate)
                days in 1..item.daysBeforeNotify
            }
            FilterType.EXPIRED -> items.filter { item ->
                DateUtils.daysUntil(item.expiryDate) <= 0
            }
        }
    }

    private fun sortItems(items: List<ItemEntity>, sort: SortType): List<ItemEntity> {
        return when (sort) {
            SortType.EXPIRY_DATE -> items.sortedBy { it.expiryDate }
            SortType.NAME -> items.sortedBy { it.name }
            SortType.CREATED_DATE -> items.sortedByDescending { it.createdDate }
        }
    }

    fun setFilter(filter: FilterType) {
        _uiState.update { it.copy(filterType = filter) }
    }

    fun setLocationFilter(location: StorageLocation?) {
        _uiState.update { it.copy(locationFilter = location) }
    }

    fun setSortType(sort: SortType) {
        _uiState.update { it.copy(sortType = sort) }
    }

    fun setSearchKeyword(keyword: String) {
        _uiState.update { it.copy(searchKeyword = keyword) }
    }

    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun archiveItem(id: Long) {
        viewModelScope.launch {
            repository.archive(id)
        }
    }

    fun markConsumed(id: Long) {
        viewModelScope.launch {
            repository.markConsumed(id)
        }
    }
}
