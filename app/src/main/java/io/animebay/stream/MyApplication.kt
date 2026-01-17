package io.animebay.stream

import android.app.Application
import android.content.Intent
import io.animebay.stream.utils.ErrorActivity // ✅ --- هذا هو السطر الذي تم إضافته --- ✅
import kotlin.system.exitProcess

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupGlobalErrorHandler()
    }

    private fun setupGlobalErrorHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            // إنشاء Intent لفتح شاشة الخطأ
            val intent = Intent(applicationContext, ErrorActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("error_details", throwable.stackTraceToString())
            }
            
            // فتح شاشة الخطأ
            startActivity(intent)

            // قتل عملية التطبيق الحالية لمنع حالة "التطبيق لا يستجيب"
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(1)
        }
    }
}
