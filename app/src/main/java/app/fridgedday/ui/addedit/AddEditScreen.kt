package app.fridgedday.ui.addedit

import android.Manifest
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import app.fridgedday.data.db.AppDatabase
import app.fridgedday.data.db.entity.StorageLocation
import app.fridgedday.data.repo.ItemRepository
import app.fridgedday.ui.components.CameraPreview
import app.fridgedday.ui.components.DatePickerField
import app.fridgedday.util.PermissionUtils
import app.fridgedday.util.ocr.TextRecognitionHelper
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    navController: NavHostController,
    itemId: Long?
) {
    val context = LocalContext.current
    val repository = remember {
        val repository = ItemRepository(AppDatabase.getDatabase(context).itemDao())
        repository
    }
    val viewModel: AddEditViewModel = viewModel(
        key = "add-edit-${itemId ?: "new"}",
        factory = AddEditViewModelFactory(repository, itemId)
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // OCR 관련 상태
    var showCamera by remember { mutableStateOf(false) }
    var isProcessingOCR by remember { mutableStateOf(false) }
    var ocrDateToEdit by remember { mutableStateOf<LocalDate?>(null) }

    // 1. 갤러리 이미지 선택 런처 (새로 추가됨)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isProcessingOCR = true
            coroutineScope.launch {
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        
                        val evaluation = TextRecognitionHelper.evaluateExpiryDate(bitmap, 0)
                        val recognizedDate = evaluation.selectedDate
                        // showSnackbar는 스낵바가 닫힐 때까지 반환하지 않는다. 진행 표시를 먼저 끈다.
                        isProcessingOCR = false

                        when {
                            recognizedDate != null -> {
                                viewModel.proposeOcrDate(recognizedDate)
                                snackbarHostState.showSnackbar(
                                    "날짜 후보를 찾았습니다. 확인 후 저장해주세요."
                                )
                            }
                            evaluation.processedVariantCount == 0 && evaluation.failedVariantCount > 0 -> {
                                snackbarHostState.showSnackbar(
                                    "문자 인식을 준비하지 못했습니다. 인터넷 연결 후 다시 시도하거나 날짜를 직접 선택해주세요."
                                )
                            }
                            else -> {
                                snackbarHostState.showSnackbar(
                                    "날짜를 찾지 못했습니다. 날짜를 직접 선택해주세요."
                                )
                            }
                        }
                    }
                } catch (_: Exception) {
                    isProcessingOCR = false
                    snackbarHostState.showSnackbar(
                        "이미지를 처리하지 못했습니다. 날짜를 직접 선택해주세요."
                    )
                } finally {
                    isProcessingOCR = false
                }
            }
        }
    }

    // 2. 카메라 권한 런처
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCamera = true
        } else {
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "촬영하려면 카메라 권한이 필요합니다.",
                    actionLabel = "설정"
                )
                if (result == SnackbarResult.ActionPerformed) {
                    PermissionUtils.openAppSettings(context)
                }
            }
        }
    }

    // 저장 성공 시 뒤로 가기
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }

    // 에러 메시지 표시
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "항목 수정" else "항목 추가") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveItem() },
                        enabled = !uiState.isLoading
                    ) {
                        Text("저장")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("이름 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.errorMessage?.contains("이름") == true
            )

            // Category
            OutlinedTextField(
                value = uiState.category,
                onValueChange = { viewModel.updateCategory(it) },
                label = { Text("카테고리") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Storage Location
            var expandedLocation by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedLocation,
                onExpandedChange = { expandedLocation = it }
            ) {
                OutlinedTextField(
                    value = when (uiState.location) {
                        StorageLocation.FRIDGE -> "냉장"
                        StorageLocation.FREEZER -> "냉동"
                        StorageLocation.PANTRY -> "실온"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("보관 위치 *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedLocation) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedLocation,
                    onDismissRequest = { expandedLocation = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("냉장") },
                        onClick = {
                            viewModel.updateLocation(StorageLocation.FRIDGE)
                            expandedLocation = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("냉동") },
                        onClick = {
                            viewModel.updateLocation(StorageLocation.FREEZER)
                            expandedLocation = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("실온") },
                        onClick = {
                            viewModel.updateLocation(StorageLocation.PANTRY)
                            expandedLocation = false
                        }
                    )
                }
            }

            // Quantity & Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = { viewModel.updateQuantity(it) },
                    label = { Text("수량") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.unit,
                    onValueChange = { viewModel.updateUnit(it) },
                    label = { Text("단위") },
                    placeholder = { Text("개, g, ml") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Expiry Date with OCR (Camera & Gallery)
            Column {
                Text(
                    text = "유통기한 *",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 날짜 선택 필드 (가중치 1.2)
                    DatePickerField(
                        label = "",
                        selectedDate = uiState.expiryDate,
                        onDateSelected = { viewModel.confirmManualExpiryDate(it) },
                        modifier = Modifier.weight(1.2f)
                    )

                    // 카메라 버튼 (가중치 0.5)
                    OutlinedButton(
                        onClick = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        enabled = !isProcessingOCR,
                        modifier = Modifier
                            .height(56.dp)
                            .weight(0.5f),
                        contentPadding = PaddingValues(0.dp) // 아이콘 중심 정렬
                    ) {
                        if (isProcessingOCR) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.CameraAlt, contentDescription = "촬영")
                        }
                    }

                    // 갤러리 버튼 (가중치 0.5)
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !isProcessingOCR,
                        modifier = Modifier
                            .height(56.dp)
                            .weight(0.5f),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "갤러리")
                    }
                }
                
                // 진행 상태 텍스트
                if (isProcessingOCR) {
                    Text(
                        text = "이미지 분석 및 날짜 인식 중...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (uiState.expiryDate == null) {
                    Text(
                        text = "날짜를 선택해야 저장할 수 있습니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (uiState.isExpiryDateConfirmed) {
                    Text(
                        text = "확인된 날짜: ${uiState.expiryDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Days Before Notify
            Column {
                Text(
                    text = "임박 알림: ${uiState.daysBeforeNotify}일 전",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3, 5, 7).forEach { days ->
                        FilterChip(
                            selected = uiState.daysBeforeNotify == days,
                            onClick = { viewModel.updateDaysBeforeNotify(days) },
                            label = { Text("${days}일") }
                        )
                    }
                }
            }

            // Note
            OutlinedTextField(
                value = uiState.note,
                onValueChange = { viewModel.updateNote(it) },
                label = { Text("메모") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }

    // Camera Preview Dialog
    if (showCamera) {
        CameraPreview(
            onImageCaptured = { bitmap ->
                showCamera = false
                isProcessingOCR = true

                coroutineScope.launch {
                    try {
                        val evaluation = TextRecognitionHelper.evaluateExpiryDate(bitmap, 0)
                        val recognizedDate = evaluation.selectedDate
                        // showSnackbar는 스낵바가 닫힐 때까지 반환하지 않는다. 진행 표시를 먼저 끈다.
                        isProcessingOCR = false

                        when {
                            recognizedDate != null -> {
                                viewModel.proposeOcrDate(recognizedDate)
                                snackbarHostState.showSnackbar(
                                    "날짜 후보를 찾았습니다. 확인 후 저장해주세요."
                                )
                            }
                            evaluation.processedVariantCount == 0 && evaluation.failedVariantCount > 0 -> {
                                snackbarHostState.showSnackbar(
                                    "문자 인식을 준비하지 못했습니다. 인터넷 연결 후 다시 시도하거나 날짜를 직접 선택해주세요."
                                )
                            }
                            else -> {
                                snackbarHostState.showSnackbar(
                                    "날짜를 찾지 못했습니다. 날짜를 직접 선택해주세요."
                                )
                            }
                        }
                    } catch (_: Exception) {
                        isProcessingOCR = false
                        snackbarHostState.showSnackbar(
                            "이미지를 처리하지 못했습니다. 날짜를 직접 선택해주세요."
                        )
                    } finally {
                        isProcessingOCR = false
                    }
                }
            },
            onDismiss = {
                showCamera = false
            }
        )
    }

    uiState.pendingOcrDate?.let { pendingDate ->
        OcrDateConfirmationDialog(
            recognizedDate = pendingDate,
            onConfirm = { viewModel.confirmPendingOcrDate() },
            onEdit = {
                ocrDateToEdit = pendingDate
                viewModel.cancelPendingOcrDate()
            },
            onCancel = { viewModel.cancelPendingOcrDate() }
        )
    }

    ocrDateToEdit?.let { initialDate ->
        OcrDateEditDialog(
            initialDate = initialDate,
            onDateSelected = { selectedDate ->
                viewModel.confirmManualExpiryDate(selectedDate)
                ocrDateToEdit = null
            },
            onDismiss = { ocrDateToEdit = null }
        )
    }
}

@Composable
internal fun OcrDateConfirmationDialog(
    recognizedDate: LocalDate,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("인식된 날짜가 맞나요?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = recognizedDate.toString(),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text("라벨의 실제 유통기한과 같은지 확인해주세요.")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("이 날짜 확인")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onEdit) {
                    Text("수정")
                }
                TextButton(onClick = onCancel) {
                    Text("취소")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OcrDateEditDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                }
            ) {
                Text("날짜 확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
