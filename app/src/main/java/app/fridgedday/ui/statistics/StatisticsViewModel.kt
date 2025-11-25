package app.fridgedday.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.fridgedday.data.db.entity.ItemEntity
import app.fridgedday.data.db.entity.StorageLocation
import app.fridgedday.data.repo.ItemRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class StatisticsUiState(
    val currentItems: Int = 0,           // 현재 관리 중 (아카이브되지 않은 항목)
    val thisMonthAdded: Int = 0,         // 이번 달 추가된 항목
    val consumedBeforeExpiry: Int = 0,   // 만료 전 소비된 항목
    val expired: Int = 0,                // 만료된 항목 (소비되지 않음)
    val wasteRate: Float = 0f,           // 낭비율: 만료됨 / (소비됨 + 만료됨) × 100
    val locationDistribution: Map<StorageLocation, Int> = emptyMap(),
    val recentActivity: List<DayActivity> = emptyList()
)

data class DayActivity(
    val date: LocalDate,
    val itemCount: Int
)

class StatisticsViewModel(
    private val repository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            // observeAll()은 아카이브되지 않은 항목만 반환
            // 통계를 위해 모든 항목을 가져와야 함
            repository.observeAll().collect { currentItems ->
                // 모든 항목을 가져와서 통계 계산
                val allItems = repository.getAllItems()
                val statistics = calculateStatistics(currentItems, allItems)
                _uiState.value = statistics
            }
        }
    }

    private fun calculateStatistics(currentItems: List<ItemEntity>, allItems: List<ItemEntity>): StatisticsUiState {
        val now = LocalDate.now()
        val currentMonth = YearMonth.now()

        // 이번 달 추가된 항목 (createdDate가 이번 달인 것)
        val thisMonthAdded = allItems.count { item ->
            val itemMonth = YearMonth.from(item.createdDate)
            itemMonth == currentMonth
        }

        // 만료 전 소비된 항목 (consumedDate가 있고 consumedDate <= expiryDate)
        val consumedBeforeExpiry = allItems.count { item ->
            item.consumedDate != null && item.consumedDate <= item.expiryDate
        }

        // 만료된 항목 (expiryDate < 오늘 AND consumedDate == null)
        val expiredItems = allItems.count { item ->
            item.expiryDate < now && item.consumedDate == null
        }

        // 낭비율: 만료됨 / (소비됨 + 만료됨) × 100
        val wasteRate = if (consumedBeforeExpiry + expiredItems > 0) {
            (expiredItems.toFloat() / (consumedBeforeExpiry + expiredItems) * 100)
        } else 0f

        // 보관 위치별 분포 (현재 관리 중인 항목만)
        val locationDist = currentItems.groupBy { it.location }
            .mapValues { it.value.size }

        // 최근 7일 활동 (소비된 항목 수)
        val recentActivity = (0..6).map { daysAgo ->
            val date = now.minusDays(daysAgo.toLong())
            val count = allItems.count { item ->
                item.consumedDate == date
            }
            DayActivity(date, count)
        }.reversed()

        return StatisticsUiState(
            currentItems = currentItems.size,
            thisMonthAdded = thisMonthAdded,
            consumedBeforeExpiry = consumedBeforeExpiry,
            expired = expiredItems,
            wasteRate = wasteRate,
            locationDistribution = locationDist,
            recentActivity = recentActivity
        )
    }
}
