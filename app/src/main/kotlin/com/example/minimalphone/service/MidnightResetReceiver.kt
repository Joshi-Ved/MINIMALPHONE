package com.example.minimalphone.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.minimalphone.FocusLiteApplication
import com.example.minimalphone.util.todayStamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MidnightResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val appContext = context.applicationContext
        val container = (appContext as FocusLiteApplication).appContainer

        CoroutineScope(Dispatchers.IO).launch {
            container.settingsRepository.setDailyResetStamp(todayStamp())
        }

        appContext.startForegroundService(Intent(appContext, FocusTrackingService::class.java))
    }
}
