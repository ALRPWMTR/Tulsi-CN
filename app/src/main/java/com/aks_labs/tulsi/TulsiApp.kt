package com.aks_labs.tulsi

import android.app.Application
import android.os.Build
import android.util.Log
import java.io.File

/**
 * 捕获未处理异常并写入应用私有目录，便于在没有 adb 的设备上排查闪退。
 */
class TulsiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                val dir = getExternalFilesDir(null) ?: filesDir
                val file = File(dir, "crash-${System.currentTimeMillis()}.txt")
                file.writeText(
                    buildString {
                        appendLine("device=${Build.MANUFACTURER} ${Build.MODEL}")
                        appendLine("android=${Build.VERSION.RELEASE} (sdk ${Build.VERSION.SDK_INT})")
                        appendLine("thread=${thread.name}")
                        appendLine()
                        appendLine(Log.getStackTraceString(error))
                        error.cause?.let {
                            appendLine()
                            appendLine("--- cause ---")
                            appendLine(Log.getStackTraceString(it))
                        }
                    }
                )
            } catch (_: Throwable) {
            }
            previous?.uncaughtException(thread, error)
        }
    }
}
