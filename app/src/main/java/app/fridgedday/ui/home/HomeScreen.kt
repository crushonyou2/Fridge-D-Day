package app.fridgedday.ui.home

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import app.fridgedday.data.db.AppDatabase
import app.fridgedday.data.pref.SettingsDataStore
import app.fridgedday.data.repo.ItemRepository
import app.fridgedday.ui.components.ItemCard
import app.fridgedday.ui.navigation.Destinations
import app.fridgedday.util.DateUtils
import app.fridgedday.util.PermissionUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { SettingsDataStore(context) }
    val settings by dataStore.settingsFlow.collectAsState(initial = app.fridgedday.data.pref.AppSettings())

    val viewModel = remember {
        val repository = ItemRepository(AppDatabase.getDatabase(context).itemDao())
        HomeViewModel(repository)
    }
    val uiState by viewModel.uiState.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showLocationMenu by remember { mutableStateOf(false) }
    var showWelcomeDialog by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember {
        mutableStateOf(PermissionUtils.hasNotificationPermission(context))
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission =
                    PermissionUtils.hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 첫 실행 여부 확인 (한 번만 실행)
    LaunchedEffect(Unit) {
        if (settings.isFirstLaunch) {
            showWelcomeDialog = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    var showMenu by remember { mutableStateOf(false) }

    // 온보딩 다이얼로그
    if (showWelcomeDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("오늘도 신선에 오신 것을 환영합니다! 🌱") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("식품 유통기한을 효율적으로 관리하고 음식물 낭비를 줄여보세요!")
                    Text("주요 기능:", fontWeight = FontWeight.Bold)
                    Text("✓ 식품 등록 및 유통기한 알림")
                    Text("✓ 카메라로 유통기한 자동 인식")
                    Text("✓ 소비 완료 및 통계 확인")
                    Text("✓ 보관 위치별 관리")
                    Text("\n+ 버튼을 눌러 첫 식품을 등록해보세요!")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            dataStore.setFirstLaunchCompleted()
                            showWelcomeDialog = false
                        }
                    }
                ) {
                    Text("시작하기")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🌱",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text("오늘도 신선")
                    }
                },
                actions = {
                    // 정렬 버튼
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "정렬")
                    }

                    // 위치 필터 버튼
                    IconButton(onClick = { showLocationMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "위치 필터")
                    }

                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(Icons.Default.Search, contentDescription = "검색")
                    }

                    // 정렬 메뉴
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("유통기한 임박순") },
                            onClick = {
                                viewModel.setSortType(SortType.EXPIRY_DATE)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("이름순") },
                            onClick = {
                                viewModel.setSortType(SortType.NAME)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("등록일순") },
                            onClick = {
                                viewModel.setSortType(SortType.CREATED_DATE)
                                showSortMenu = false
                            }
                        )
                    }

                    // 위치 필터 메뉴
                    DropdownMenu(
                        expanded = showLocationMenu,
                        onDismissRequest = { showLocationMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("전체") },
                            onClick = {
                                viewModel.setLocationFilter(null)
                                showLocationMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("냉장") },
                            onClick = {
                                viewModel.setLocationFilter(app.fridgedday.data.db.entity.StorageLocation.FRIDGE)
                                showLocationMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("냉동") },
                            onClick = {
                                viewModel.setLocationFilter(app.fridgedday.data.db.entity.StorageLocation.FREEZER)
                                showLocationMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("실온") },
                            onClick = {
                                viewModel.setLocationFilter(app.fridgedday.data.db.entity.StorageLocation.PANTRY)
                                showLocationMenu = false
                            }
                        )
                    }

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "메뉴")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("통계") },
                            onClick = {
                                showMenu = false
                                navController.navigate(Destinations.STATISTICS)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.BarChart, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("목록 공유") },
                            onClick = {
                                showMenu = false
                                shareItemList(context, uiState.items)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Share, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("설정") },
                            onClick = {
                                showMenu = false
                                navController.navigate(Destinations.SETTINGS)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Destinations.ADD) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "추가")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Notification Permission Banner
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "알림 권한 필요",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "유통기한 알림을 받으려면 권한을 허용해주세요",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                }
                            ) {
                                Text("허용")
                            }
                            TextButton(
                                onClick = {
                                    PermissionUtils.openAppSettings(context)
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text("앱 설정")
                            }
                        }
                    }
                }
            }

            // Search Bar
            if (showSearchBar) {
                TextField(
                    value = uiState.searchKeyword,
                    onValueChange = { viewModel.setSearchKeyword(it) },
                    placeholder = { Text("이름으로 검색") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true
                )
            }

            // Filter Tabs
            TabRow(
                selectedTabIndex = uiState.filterType.ordinal
            ) {
                Tab(
                    selected = uiState.filterType == FilterType.ALL,
                    onClick = { viewModel.setFilter(FilterType.ALL) },
                    text = { Text("전체") }
                )
                Tab(
                    selected = uiState.filterType == FilterType.EXPIRING,
                    onClick = { viewModel.setFilter(FilterType.EXPIRING) },
                    text = { Text("임박") }
                )
                Tab(
                    selected = uiState.filterType == FilterType.EXPIRED,
                    onClick = { viewModel.setFilter(FilterType.EXPIRED) },
                    text = { Text("만료") }
                )
            }

            // Item List
            if (uiState.items.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.items,
                        key = { it.id }
                    ) { item ->
                        ItemCard(
                            item = item,
                            onClick = {
                                navController.navigate(Destinations.editRoute(item.id))
                            },
                            onMarkConsumed = {
                                viewModel.markConsumed(item.id)
                            },
                            onDelete = {
                                viewModel.deleteItem(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🥗",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "등록된 식품이 없어요",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "오른쪽 하단의 + 버튼을 눌러\n첫 식품을 등록해보세요!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun shareItemList(context: android.content.Context, items: List<app.fridgedday.data.db.entity.ItemEntity>) {
    if (items.isEmpty()) {
        return
    }

    val shareText = buildString {
        appendLine("📋 오늘도 신선 - 식품 관리 목록")
        appendLine()

        items.forEach { item ->
            val dDay = DateUtils.formatDDay(item.expiryDate)
            val location = when (item.location) {
                app.fridgedday.data.db.entity.StorageLocation.FRIDGE -> "냉장"
                app.fridgedday.data.db.entity.StorageLocation.FREEZER -> "냉동"
                app.fridgedday.data.db.entity.StorageLocation.PANTRY -> "실온"
            }
            appendLine("• ${item.name} [$location] - $dDay")
        }

        appendLine()
        appendLine("오늘도 신선 앱으로 관리 중")
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "목록 공유")
    context.startActivity(shareIntent)
}
