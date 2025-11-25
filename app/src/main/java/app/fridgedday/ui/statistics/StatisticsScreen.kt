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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.fridgedday.data.db.AppDatabase
import app.fridgedday.data.db.entity.StorageLocation
import app.fridgedday.data.repo.ItemRepository

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
                            // 차트 데이터
                            val fridgeCount = uiState.locationDistribution[StorageLocation.FRIDGE] ?: 0
                            val freezerCount = uiState.locationDistribution[StorageLocation.FREEZER] ?: 0
                            val pantryCount = uiState.locationDistribution[StorageLocation.PANTRY] ?: 0

                            // 최대값 계산
                            val maxValue = maxOf(fridgeCount, freezerCount, pantryCount, 1)

                            // 각 막대별 색상 지정
                            val fridgeColor = Color(0xFF4CAF50)
                            val freezerColor = Color(0xFF2196F3)
                            val pantryColor = Color(0xFFFF9800)
                            val textColor = MaterialTheme.colorScheme.onSurface
                            val gridColor = MaterialTheme.colorScheme.outlineVariant

                            val textMeasurer = rememberTextMeasurer()

                            // 커스텀 막대 차트
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val padding = 50f
                                val chartHeight = canvasHeight - padding * 2
                                val chartWidth = canvasWidth - padding * 2

                                // Y축 눈금 그리기
                                for (i in 0..maxValue) {
                                    val y = canvasHeight - padding - (i.toFloat() / maxValue * chartHeight)
                                    // 그리드 라인
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(padding, y),
                                        end = Offset(canvasWidth - padding, y),
                                        strokeWidth = 1f
                                    )
                                    // Y축 레이블
                                    drawText(
                                        textMeasurer = textMeasurer,
                                        text = i.toString(),
                                        topLeft = Offset(10f, y - 10f),
                                        style = TextStyle(
                                            color = textColor,
                                            fontSize = 12.sp
                                        )
                                    )
                                }

                                // 막대 그리기
                                val barWidth = chartWidth / 6
                                val barSpacing = chartWidth / 3

                                // 냉장 (녹색)
                                if (fridgeCount > 0) {
                                    val barHeight = (fridgeCount.toFloat() / maxValue) * chartHeight
                                    drawRoundRect(
                                        color = fridgeColor,
                                        topLeft = Offset(padding + barSpacing * 0 + barWidth / 2, canvasHeight - padding - barHeight),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(8f, 8f)
                                    )
                                }

                                // 냉동 (파란색)
                                if (freezerCount > 0) {
                                    val barHeight = (freezerCount.toFloat() / maxValue) * chartHeight
                                    drawRoundRect(
                                        color = freezerColor,
                                        topLeft = Offset(padding + barSpacing * 1 + barWidth / 2, canvasHeight - padding - barHeight),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(8f, 8f)
                                    )
                                }

                                // 실온 (주황색)
                                if (pantryCount > 0) {
                                    val barHeight = (pantryCount.toFloat() / maxValue) * chartHeight
                                    drawRoundRect(
                                        color = pantryColor,
                                        topLeft = Offset(padding + barSpacing * 2 + barWidth / 2, canvasHeight - padding - barHeight),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(8f, 8f)
                                    )
                                }

                                // X축 레이블
                                val labels = listOf("냉장", "냉동", "실온")
                                labels.forEachIndexed { index, label ->
                                    drawText(
                                        textMeasurer = textMeasurer,
                                        text = label,
                                        topLeft = Offset(
                                            padding + barSpacing * index + barWidth / 2 + barWidth / 4,
                                            canvasHeight - padding + 10f
                                        ),
                                        style = TextStyle(
                                            color = textColor,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }

                            // 범례
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf(
                                    Triple(StorageLocation.FRIDGE, "냉장", fridgeColor),
                                    Triple(StorageLocation.FREEZER, "냉동", freezerColor),
                                    Triple(StorageLocation.PANTRY, "실온", pantryColor)
                                ).forEach { (location, label, color) ->
                                    val count = uiState.locationDistribution[location] ?: 0
                                    if (count > 0) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(12.dp),
                                                color = color,
                                                shape = MaterialTheme.shapes.small
                                            ) {}
                                            Text(
                                                text = "$label: ${count}개",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
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
