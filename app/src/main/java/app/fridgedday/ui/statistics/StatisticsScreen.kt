package app.fridgedday.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.fridgedday.data.db.AppDatabase
import app.fridgedday.data.db.entity.StorageLocation
import app.fridgedday.data.repo.ItemRepository
import app.fridgedday.ui.theme.containerColor
import app.fridgedday.ui.theme.solidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel = remember {
        val repository = ItemRepository(AppDatabase.getDatabase(context).itemDao())
        StatisticsViewModel(repository)
    }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("통계") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 이번 달 통계
            StatCard(
                title = "이번 달 통계",
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            label = "추가",
                            value = "${uiState.thisMonthAdded}",
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatItem(
                            label = "소비",
                            value = "${uiState.consumedBeforeExpiry}",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        StatItem(
                            label = "만료",
                            value = "${uiState.expired}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "추가: 이번 달 등록 / 소비: 만료 전 소비 완료 / 만료: 소비되지 않고 만료됨",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            )

            // 낭비율
            StatCard(
                title = "음식물 낭비율",
                content = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${uiState.wasteRate.toInt()}%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                uiState.wasteRate <= 10 -> MaterialTheme.colorScheme.tertiary
                                uiState.wasteRate <= 30 -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                        Text(
                            text = "만료됨 / (소비 + 만료)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LinearProgressIndicator(
                            progress = { uiState.wasteRate / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                        )
                        Text(
                            text = when {
                                uiState.wasteRate <= 10 -> "훌륭해요! 낭비가 거의 없습니다"
                                uiState.wasteRate <= 30 -> "잘하고 있어요! 조금만 더 신경 쓰면 좋겠어요"
                                else -> "주의! 음식물 낭비가 많습니다"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            // 보관 위치별 분포 차트
            StatCard(
                title = "보관 위치별 분포",
                content = {
                    if (uiState.locationDistribution.isEmpty()) {
                        Text(
                            text = "아직 항목이 없습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val totalCount = uiState.locationDistribution.values.sum().coerceAtLeast(1)
                            listOf(
                                StorageLocation.FRIDGE to "냉장",
                                StorageLocation.FREEZER to "냉동",
                                StorageLocation.PANTRY to "실온"
                            ).forEach { (location, label) ->
                                val count = uiState.locationDistribution[location] ?: 0
                                LocationDistributionRow(
                                    label = label,
                                    count = count,
                                    progress = count.toFloat() / totalCount,
                                    barColor = location.solidColor(),
                                    trackColor = location.containerColor()
                                )
                            }
                        }
                    }
                }
            )

            // 전체 통계
            StatCard(
                title = "전체 통계",
                content = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "현재 관리 중",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${uiState.currentItems}개",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LocationDistributionRow(
    label: String,
    count: Int,
    progress: Float,
    barColor: androidx.compose.ui.graphics.Color,
    trackColor: androidx.compose.ui.graphics.Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${count}개",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = barColor,
            trackColor = trackColor
        )
    }
}
