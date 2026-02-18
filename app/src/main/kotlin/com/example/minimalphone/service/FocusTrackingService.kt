package com.example.minimalphone.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.minimalphone.FocusLiteApplication
import com.example.minimalphone.MainActivity
import com.example.minimalphone.R
import com.example.minimalphone.util.nextMidnightMillis
import com.example.minimalphone.util.todayStamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FocusTrackingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val settingsRepository by lazy {
        (application as FocusLiteApplication).appContainer.settingsRepository
    }

    private val usageRepository by lazy {
        (application as FocusLiteApplication).appContainer.usageRepository
    }

    private var pollingJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        startForeground(TRACKING_NOTIFICATION_ID, buildTrackingNotification())
        scheduleMidnightReset()
        startPollingLoop()
        return START_STICKY
    }

    override fun onDestroy() {
        pollingJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startPollingLoop() {
        pollingJob?.cancel()
        pollingJob = serviceScope.launch {
            while (isActive) {
                val settings = settingsRepository.observeSettings().first()
                val hasPermission = usageRepository.hasUsageAccessPermission()

                if (hasPermission) {
                    val usage = usageRepository.queryTodayUsage(settings)
                    val overLimit = usage.totalForegroundMinutes >= settings.dailyLimitMinutes
                    if (overLimit && settings.lockModeEnabled) {
                        settingsRepository.setDailyResetStamp(todayStamp())
                        showLimitNotification()
                        launchLockScreen()
                    }
                }

                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private fun launchLockScreen() {
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_FORCE_LOCK, true)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    private fun buildTrackingNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID_TRACKING)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.tracking_notification_text))
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun showLimitNotification() {
        val openIntent = PendingIntent.getActivity(
            this,
            1,
            Intent(this, MainActivity::class.java).putExtra(MainActivity.EXTRA_FORCE_LOCK, true),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.limit_reached_title))
            .setContentText(getString(R.string.limit_reached_subtitle))
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(LIMIT_NOTIFICATION_ID, notification)
    }

    private fun scheduleMidnightReset() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val resetIntent = Intent(this, MidnightResetReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            RESET_REQUEST_CODE,
            resetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextMidnightMillis(),
            pendingIntent,
        )
    }

    private fun createChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID_TRACKING,
                getString(R.string.channel_tracking_name),
                NotificationManager.IMPORTANCE_LOW,
            )
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID_ALERTS,
                getString(R.string.channel_alert_name),
                NotificationManager.IMPORTANCE_HIGH,
            )
        )
    }

    companion object {
        private const val CHANNEL_ID_TRACKING = "focus_tracking"
        private const val CHANNEL_ID_ALERTS = "focus_alerts"
        private const val TRACKING_NOTIFICATION_ID = 1001
        private const val LIMIT_NOTIFICATION_ID = 1002
        private const val RESET_REQUEST_CODE = 1003
        private const val POLL_INTERVAL_MS = 15_000L
    }
}
