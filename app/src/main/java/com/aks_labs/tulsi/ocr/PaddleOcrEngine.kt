package com.aks_labs.tulsi.ocr

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Rect
import android.util.Log
import androidx.compose.ui.geometry.Size
import com.paddle.ocr.PaddleOCR
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * PP-OCRv6（PaddleOCR）封装。
 * 中文场景优先用它；初始化或推理失败时返回 null，由调用方回退到 ML Kit。
 */
object PaddleOcrEngine {
    private const val TAG = "PaddleOcrEngine"

    @Volatile
    private var instance: PaddleOCR? = null
    private val initLock = Mutex()
    private val runLock = Mutex()

    private suspend fun engine(context: Context): PaddleOCR? {
        instance?.let { return it }
        return initLock.withLock {
            instance ?: try {
                val created = PaddleOCR.create(context.applicationContext)
                instance = created
                Log.d(TAG, "PaddleOCR ready, cold load ${created.coldLoadTimeMs}ms")
                created
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to init PaddleOCR", t)
                null
            }
        }
    }

    /** 只取文本，用于后台索引 */
    suspend fun extractText(context: Context, bitmap: Bitmap): String? {
        return try {
            val e = engine(context) ?: return null
            val result = runLock.withLock { e.recognize(bitmap) }
            result.results.joinToString("\n") { it.text }.trim().ifEmpty { null }
        } catch (t: Throwable) {
            Log.e(TAG, "PaddleOCR text extraction failed", t)
            null
        }
    }

    /** 带坐标的结果，用于文字选择 */
    suspend fun extractSelectable(context: Context, bitmap: Bitmap): SelectableOcrResult? {
        return try {
            val e = engine(context) ?: return null
            val start = System.currentTimeMillis()
            val result = runLock.withLock { e.recognize(bitmap) }
            val blocks = result.results.mapIndexedNotNull { index, item ->
                if (item.text.isBlank()) return@mapIndexedNotNull null
                val left = item.box.points.minOf { it.x }
                val top = item.box.points.minOf { it.y }
                val right = item.box.points.maxOf { it.x }
                val bottom = item.box.points.maxOf { it.y }
                val rect = Rect(left, top, right, bottom)
                val element = SelectableTextElement(
                    id = "paddle_${index}_e0",
                    text = item.text,
                    boundingBox = rect,
                    confidence = item.confidence
                )
                val line = SelectableTextLine(
                    id = "paddle_${index}_l0",
                    text = item.text,
                    boundingBox = rect,
                    confidence = item.confidence,
                    elements = listOf(element)
                )
                SelectableTextBlock(
                    id = "paddle_block_$index",
                    text = item.text,
                    boundingBox = rect,
                    confidence = item.confidence,
                    lines = listOf(line)
                )
            }
            if (blocks.isEmpty()) {
                null
            } else {
                SelectableOcrResult(
                    textBlocks = blocks,
                    fullText = blocks.joinToString("\n") { it.text },
                    imageSize = Size(bitmap.width.toFloat(), bitmap.height.toFloat()),
                    processingTimeMs = System.currentTimeMillis() - start
                )
            }
        } catch (t: Throwable) {
            Log.e(TAG, "PaddleOCR selectable extraction failed", t)
            null
        }
    }
}
