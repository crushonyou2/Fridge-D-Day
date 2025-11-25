package app.fridgedday.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.fridgedday.data.db.entity.ItemEntity
import app.fridgedday.data.db.entity.StorageLocation
import app.fridgedday.util.DDayState
import app.fridgedday.util.DateUtils
import app.fridgedday.util.getDDayState

@Composable
fun ItemCard(
    item: ItemEntity,
    onClick: () -> Unit,
    onMarkConsumed: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showActions by remember { mutableStateOf(false) }
    var showConsumedDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 소비 완료 확인 다이얼로그
    if (showConsumedDialog) {
        AlertDialog(
            onDismissRequest = { showConsumedDialog = false },
            title = { Text("소비 완료") },
            text = { Text("'${item.name}'을(를) 소비 완료 처리하시겠습니까?\n통계에 반영됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onMarkConsumed()
                        showConsumedDialog = false
                        showActions = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConsumedDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("삭제 확인") },
            text = { Text("'${item.name}'을(를) 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                        showActions = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Icon + Name
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Storage Location Icon
                    Icon(
                        imageVector = when (item.location) {
                            StorageLocation.FRIDGE -> Icons.Default.Kitchen
                            StorageLocation.FREEZER -> Icons.Default.AcUnit
                            StorageLocation.PANTRY -> Icons.Default.Store
                        },
                        contentDescription = item.location.name,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    // Name + Category
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (item.category != null) {
                            Text(
                                text = item.category,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Quantity
                        if (item.quantity != null) {
                            val quantityText = if (item.quantity % 1 == 0f) {
                                "${item.quantity.toInt()}${item.unit ?: ""}"
                            } else {
                                "${item.quantity}${item.unit ?: ""}"
                            }
                            Text(
                                text = quantityText,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Right: D-Day Badge + More Button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DDayBadge(expiryDate = item.expiryDate)

                    IconButton(
                        onClick = { showActions = !showActions },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "옵션",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action Buttons (Animated Visibility)
            AnimatedVisibility(
                visible = showActions,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Consume Button
                    OutlinedButton(
                        onClick = {
                            showConsumedDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("소비 완료", fontSize = 14.sp)
                    }

                    // Delete Button
                    OutlinedButton(
                        onClick = {
                            showDeleteDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("삭제", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DDayBadge(expiryDate: java.time.LocalDate) {
    val days = DateUtils.daysUntil(expiryDate)
    val state = getDDayState(days)
    val text = DateUtils.formatDDay(expiryDate)

    val backgroundColor = when (state) {
        DDayState.SAFE -> Color(0xFF4CAF50)      // Green
        DDayState.WARNING -> Color(0xFFFFC107)   // Yellow
        DDayState.EXPIRED -> Color(0xFFF44336)   // Red
    }

    val textColor = when (state) {
        DDayState.WARNING -> Color.Black
        else -> Color.White
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
