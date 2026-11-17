package com.aks_labs.tulsi.compose.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.util.Log
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import com.aks_labs.tulsi.MainActivity.Companion.mainViewModel
import com.aks_labs.tulsi.R
import com.aks_labs.tulsi.datastore.Ocr
import com.aks_labs.tulsi.compose.PreferencesSwitchRow
import com.aks_labs.tulsi.compose.components.OcrProgressBar
import com.aks_labs.tulsi.database.entities.OcrProgressEntity
import com.aks_labs.tulsi.database.MediaDatabase
import com.aks_labs.tulsi.database.Migration3to4
import com.aks_labs.tulsi.database.Migration4to5
import com.aks_labs.tulsi.database.Migration5to6
import com.aks_labs.tulsi.database.Migration6to7
import com.aks_labs.tulsi.helpers.RowPosition
import com.aks_labs.tulsi.ocr.DevanagariOcrManager
import com.aks_labs.tulsi.ocr.OcrManager
import kotlinx.coroutines.launch

private const val TAG = "OcrLanguageModelsPage"

/**
 * Convert DevanagariOcrProgressEntity to OcrProgressEntity for UI compatibility
 */
private fun com.aks_labs.tulsi.database.entities.DevanagariOcrProgressEntity.toOcrProgressEntity(): OcrProgressEntity {
    return OcrProgressEntity(
        id = this.id,
        totalImages = this.totalImages,
        processedImages = this.processedImages,
        failedImages = this.failedImages,
        isProcessing = this.isProcessing,
        isPaused = this.isPaused,
        lastUpdated = this.lastUpdated,
        estimatedCompletionTime = this.estimatedCompletionTime,
        averageProcessingTimeMs = this.averageProcessingTimeMs,
        currentBatchId = this.currentBatchId,
        progressDismissed = this.progressDismissed
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrLanguageModelsPage(
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // OCR settings state
    val latinOcrEnabled by mainViewModel.settings.Ocr.latinOcrEnabled.collectAsStateWithLifecycle(initialValue = true)
    val devanagariOcrEnabled by mainViewModel.settings.Ocr.devanagariOcrEnabled.collectAsStateWithLifecycle(initialValue = false)

    Log.d(TAG, "OcrLanguageModelsPage composed - Latin OCR: $latinOcrEnabled, Devanagari OCR: $devanagariOcrEnabled")
    
    // Database instance
    val database = remember {
        Room.databaseBuilder(
            context,
            MediaDatabase::class.java,
            "media-database"
        ).apply {
            addMigrations(
                Migration3to4(context),
                Migration4to5(context),
                Migration5to6(context),
                Migration6to7(context),
                com.aks_labs.tulsi.database.migrations.Migration7to8
            )
        }.build()
    }
    
    // OCR managers
    val latinOcrManager = remember { OcrManager(context, database) }
    val devanagariOcrManager = remember { DevanagariOcrManager(context, database) }

    // Progress monitoring state
    val latinOcrProgress by latinOcrManager.getProgressFlow().collectAsStateWithLifecycle(initialValue = null)
    val devanagariOcrProgress by devanagariOcrManager.getProgressFlow().collectAsStateWithLifecycle(initialValue = null)

    Log.d(TAG, "Progress state - Latin: $latinOcrProgress, Devanagari: $devanagariOcrProgress")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("文字识别语言模型") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "文字识别语言模型",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "配置用于从图片中提取文字的语言模型。各语言模型独立处理图片。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Progress Bars for Active OCR Processing (positioned at top for visibility)

            // Check for active OCR systems
            val isDevanagariEnabledAndActive = devanagariOcrEnabled &&
                (devanagariOcrProgress?.isProcessing == true || devanagariOcrProgress?.isPaused == true)
            val isLatinEnabledAndActive = latinOcrEnabled &&
                (latinOcrProgress?.isProcessing == true || latinOcrProgress?.isPaused == true)

            Log.d(TAG, "Progress bar visibility check:")
            Log.d(TAG, "  Latin enabled: $latinOcrEnabled, active: $isLatinEnabledAndActive")
            Log.d(TAG, "  Devanagari enabled: $devanagariOcrEnabled, active: $isDevanagariEnabledAndActive")

            // Show Devanagari OCR progress bar (non-dismissible)
            if (isDevanagariEnabledAndActive && devanagariOcrProgress != null) {
                Log.d(TAG, "🟦 Showing Devanagari OCR progress: ${devanagariOcrProgress?.processedImages}/${devanagariOcrProgress?.totalImages}")
                val devanagariActiveProgress = devanagariOcrProgress?.toOcrProgressEntity()

                if (devanagariActiveProgress != null) {
                    OcrProgressBar(
                        progress = devanagariActiveProgress,
                        isVisible = true,
                        showDismissButton = false, // Hide dismiss button for Devanagari OCR in settings
                        horizontalPadding = 0.dp, // Use 0dp since page already has 16dp padding
                        title = "Devanagari OCR Progress: ${devanagariActiveProgress.processedImages}/${devanagariActiveProgress.totalImages} images",
                        onDismiss = {
                            // This won't be called since dismiss button is hidden
                            Log.d(TAG, "Devanagari OCR progress bar dismiss ignored in settings page")
                        },
                        onPauseResume = {
                            scope.launch {
                                if (devanagariActiveProgress?.isPaused == true) {
                                    Log.d(TAG, "Resuming Devanagari OCR processing")
                                    devanagariOcrManager.resumeProcessing()
                                } else {
                                    Log.d(TAG, "Pausing Devanagari OCR processing")
                                    devanagariOcrManager.pauseProcessing()
                                }
                            }
                        }
                    )
                }
            }

            // Show Latin OCR progress bar (dismissible at all times)
            if (isLatinEnabledAndActive && latinOcrProgress != null) {
                Log.d(TAG, "🟨 Showing Latin OCR progress: ${latinOcrProgress?.processedImages}/${latinOcrProgress?.totalImages}")

                OcrProgressBar(
                    progress = latinOcrProgress,
                    isVisible = true,
                    showDismissButton = false, // Hide dismiss button for Latin OCR in settings page for consistency
                    horizontalPadding = 0.dp, // Use 0dp since page already has 16dp padding
                    title = "Latin OCR Progress: ${latinOcrProgress?.processedImages}/${latinOcrProgress?.totalImages} images",
                    onDismiss = {
                        // This won't be called since dismiss button is hidden
                        Log.d(TAG, "Latin OCR progress bar dismiss ignored in settings page")
                    },
                    onPauseResume = {
                        scope.launch {
                            if (latinOcrProgress?.isPaused == true) {
                                Log.d(TAG, "Resuming Latin OCR processing")
                                latinOcrManager.resumeProcessing()
                            } else {
                                Log.d(TAG, "Pausing Latin OCR processing")
                                latinOcrManager.pauseProcessing()
                            }
                        }
                    }
                )
            }

            // Latin OCR Settings
            PreferencesSwitchRow(
                title = "中文 / 拉丁文识别",
                summary = "简体中文、英文及数字；也支持西班牙语、法语、德语、意大利语、葡萄牙语等拉丁字母语言",
                iconResID = R.drawable.ocr,
                checked = latinOcrEnabled,
                position = RowPosition.Top,
                showBackground = false,
                onSwitchClick = { enabled ->
                    Log.d(TAG, "Latin OCR toggle clicked: $enabled")
                    mainViewModel.settings.Ocr.setLatinOcrEnabled(enabled)

                    scope.launch {
                        if (enabled) {
                            Log.d(TAG, "Starting Latin OCR continuous processing")
                            // Start Latin OCR processing
                            latinOcrManager.startContinuousProcessing()
                        } else {
                            Log.d(TAG, "Pausing Latin OCR processing")
                            // Pause Latin OCR processing
                            latinOcrManager.pauseProcessing()
                        }
                    }
                }
            )

            // Devanagari OCR Settings
            PreferencesSwitchRow(
                title = "天城文文字识别",
                summary = "印地语、马拉地语、尼泊尔语、梵语及其他使用天城文的语言",
                iconResID = R.drawable.ocr,
                checked = devanagariOcrEnabled,
                position = RowPosition.Bottom,
                showBackground = false,
                onSwitchClick = { enabled ->
                    Log.d(TAG, "Devanagari OCR toggle clicked: $enabled")
                    mainViewModel.settings.Ocr.setDevanagariOcrEnabled(enabled)

                    scope.launch {
                        if (enabled) {
                            Log.d(TAG, "Starting Devanagari OCR continuous processing")
                            // Start Devanagari OCR processing
                            val workId = devanagariOcrManager.startContinuousProcessing()
                            Log.d(TAG, "Devanagari OCR work enqueued with ID: $workId")
                        } else {
                            Log.d(TAG, "Pausing Devanagari OCR processing")
                            // Pause Devanagari OCR processing
                            devanagariOcrManager.pauseProcessing()
                        }
                    }
                }
            )

            // Processing Status Information
            if (latinOcrEnabled || devanagariOcrEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "处理状态",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        if (latinOcrEnabled) {
                            Text(
                                text = "• 中文 / 拉丁文识别：正在后台处理图片",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        if (devanagariOcrEnabled) {
                            Text(
                                text = "• 天城文识别：正在后台处理图片",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Text(
                            text = "开始处理后会显示进度通知。处理完成后即可搜索图片中的文字。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Performance Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "性能说明",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• 默认开启中文 / 拉丁文识别以获得最佳效果\n" +
                              "• 默认关闭天城文识别以节省资源\n" +
                              "• 两套系统可同时运行，互不冲突\n" +
                              "• 处理在后台进行，不影响应用性能",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            var showRebuildDialog by remember { mutableStateOf(false) }
            val rebuildScope = rememberCoroutineScope()

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "重建文字索引",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "清空已提取的文字并重新扫描全部图片。换用中文识别后建议执行一次，这样旧照片里的中文也能被搜索到。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { showRebuildDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("立即重建")
                    }
                }
            }

            if (showRebuildDialog) {
                AlertDialog(
                    onDismissRequest = { showRebuildDialog = false },
                    title = { Text("重建文字索引？") },
                    text = { Text("这会清空已提取的文字并重新扫描所有图片，处理在后台进行，期间可以正常使用。") },
                    confirmButton = {
                        TextButton(onClick = {
                            showRebuildDialog = false
                            rebuildScope.launch { latinOcrManager.rebuildTextIndex() }
                        }) { Text("开始重建") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRebuildDialog = false }) { Text("取消") }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
