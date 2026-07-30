package app.fridgedday.ui.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.fridgedday.data.db.AppDatabase
import app.fridgedday.data.pref.SettingsDataStore
import app.fridgedday.data.pref.ThemeMode
import app.fridgedday.data.repo.ItemRepository
import app.fridgedday.data.repo.SettingsRepository
import app.fridgedday.ui.components.TimePickerDialog
import app.fridgedday.util.backup.BackupManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel = remember {
        val dataStore = SettingsDataStore(context)
        val repository = SettingsRepository(dataStore)
        SettingsViewModel(repository)
    }
    val settings by viewModel.settings.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var isBackupInProgress by remember { mutableStateOf(false) }
    var isRestoreInProgress by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val itemRepository = remember {
        ItemRepository(AppDatabase.getDatabase(context).itemDao())
    }

    // Backup launcher
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                isBackupInProgress = true
                try {
                    val items = itemRepository.getAllItems()
                    val result = BackupManager.exportToJson(context, items, it)
                    result.onSuccess {
                        snackbarHostState.showSnackbar("백업 완료")
                    }.onFailure { error ->
                        snackbarHostState.showSnackbar("백업 실패: ${error.message}")
                    }
                } finally {
                    isBackupInProgress = false
                }
            }
        }
    }

    // Restore launcher
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                isRestoreInProgress = true
                try {
                    val result = BackupManager.importFromJson(context, it)
                    result.onSuccess { items ->
                        items.forEach { item ->
                            itemRepository.insert(item)
                        }
                        snackbarHostState.showSnackbar("${items.size}개 항목 복원 완료")
                    }.onFailure { error ->
                        snackbarHostState.showSnackbar("복원 실패: ${error.message}")
                    }
                } finally {
                    isRestoreInProgress = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Daily Notification Toggle
            SettingItem(
                title = "매일 알림 받기",
                subtitle = if (settings.dailyNotify) "켜짐" else "꺼짐"
            ) {
                Switch(
                    checked = settings.dailyNotify,
                    onCheckedChange = { viewModel.toggleDailyNotify(it) }
                )
            }

            Divider()

            // Notification Time
            SettingItem(
                title = "알림 시간",
                subtitle = String.format("%02d:%02d", settings.notifyTime.hour, settings.notifyTime.minute),
                enabled = settings.dailyNotify,
                onClick = { if (settings.dailyNotify) showTimePicker = true }
            ) {}

            Divider()

            // Default Days Before
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "기본 임박 기준일",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${settings.defaultDaysBefore}일 전",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = settings.defaultDaysBefore.toFloat(),
                    onValueChange = { viewModel.updateDefaultDaysBefore(it.toInt()) },
                    valueRange = 1f..14f,
                    steps = 12,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Divider()

            // Theme Mode
            var expandedTheme by remember { mutableStateOf(false) }
            SettingItem(
                title = "테마 설정",
                subtitle = when (settings.themeMode) {
                    ThemeMode.SYSTEM -> "시스템 설정 따르기"
                    ThemeMode.LIGHT -> "라이트 모드"
                    ThemeMode.DARK -> "다크 모드"
                },
                onClick = { expandedTheme = true }
            ) {}

            if (expandedTheme) {
                AlertDialog(
                    onDismissRequest = { expandedTheme = false },
                    title = { Text("테마 선택") },
                    text = {
                        Column {
                            ThemeMode.values().forEach { mode ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.updateThemeMode(mode)
                                            expandedTheme = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = settings.themeMode == mode,
                                        onClick = {
                                            viewModel.updateThemeMode(mode)
                                            expandedTheme = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (mode) {
                                            ThemeMode.SYSTEM -> "시스템 설정 따르기"
                                            ThemeMode.LIGHT -> "라이트 모드"
                                            ThemeMode.DARK -> "다크 모드"
                                        }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { expandedTheme = false }) {
                            Text("닫기")
                        }
                    }
                )
            }

            Divider()

            // Backup & Restore
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "데이터 관리",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val timestamp = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                            backupLauncher.launch("fridge_dday_backup_$timestamp.json")
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isBackupInProgress && !isRestoreInProgress
                    ) {
                        if (isBackupInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("백업")
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            restoreLauncher.launch(arrayOf("application/json"))
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isBackupInProgress && !isRestoreInProgress
                    ) {
                        if (isRestoreInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("복원")
                        }
                    }
                }
                Text(
                    text = "백업: 모든 항목을 JSON 파일로 저장\n복원: JSON 파일에서 항목 가져오기",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider()

            // App Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "앱 정보",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "오늘도 신선 v1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "식자재 유통기한 관리 앱",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            currentTime = settings.notifyTime,
            onConfirm = { hour, minute ->
                viewModel.updateNotifyTime(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null && enabled) it.clickable(onClick = onClick) else it }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                }
            )
        }
        trailing()
    }
}
