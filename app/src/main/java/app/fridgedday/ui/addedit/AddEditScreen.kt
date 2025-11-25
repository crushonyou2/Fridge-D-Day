package app.fridgedday.ui.addedit

import android.Manifest
import android.graphics.BitmapFactory
import android.os.Build
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
import androidx.navigation.NavHostController
import app.fridgedday.data.db.AppDatabase
import app.fridgedday.data.db.entity.StorageLocation
import app.fridgedday.data.repo.ItemRepository
import app.fridgedday.ui.components.CameraPreview
import app.fridgedday.ui.components.DatePickerField
import app.fridgedday.util.ocr.TextRecognitionHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    navController: NavHostController,
    itemId: Long?
) {
    val context = LocalContext.current
    val viewModel = remember(itemId) {
        val repository = ItemRepository(AppDatabase.getDatabase(context).itemDao())
        AddEditViewModel(repository, itemId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // OCR 관련 상태
    var showCamera by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var isProcessingOCR by remember { mutableStateOf(false) }

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
                        
                        // 갤러리 이미지는 회전 정보가 없을 수 있으므로 0도 시도 후 실패 시 90도 시도
                        val recognizedDate = TextRecognitionHelper.extractExpiryDate(bitmap, 0)
                            ?: TextRecognitionHelper.extractExpiryDate(bitmap, 90)

                        if (recognizedDate != null) {
                            viewModel.updateExpiryDate(recognizedDate)
                            snackbarHostState.showSnackbar("갤러리 이미지 인식 성공: $recognizedDate")
                        } else {
                            snackbarHostState.showSnackbar("인식 실패: 유통기한 날짜를 찾을 수 없습니다.")
                        }
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("오류 발생: ${e.message}")
                    e.printStackTrace()
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
        hasCameraPermission = isGranted
        if (isGranted) {
            showCamera = true
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("카메라 권한이 필요합니다")
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
                        onDateSelected = { viewModel.updateExpiryDate(it) },
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
                        // 1차 시도: 원본 각도
                        val recognizedDate = TextRecognitionHelper.extractExpiryDate(bitmap, 0)
                            // 2차 시도: 90도 회전 (실패 시)
                            ?: TextRecognitionHelper.extractExpiryDate(bitmap, 90)

                        if (recognizedDate != null) {
                            viewModel.updateExpiryDate(recognizedDate)
                            snackbarHostState.showSnackbar("날짜를 인식했습니다: $recognizedDate")
                        } else {
                            snackbarHostState.showSnackbar("날짜를 인식하지 못했습니다")
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("OCR 처리 실패: ${e.message}")
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
}