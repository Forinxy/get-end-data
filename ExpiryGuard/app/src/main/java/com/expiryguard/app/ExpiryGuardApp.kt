package com.expiryguard.app

import android.app.Application
import com.expiryguard.app.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExpiryGuardApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // 创建通知渠道
        NotificationHelper.createNotificationChannel(this)
    }
}