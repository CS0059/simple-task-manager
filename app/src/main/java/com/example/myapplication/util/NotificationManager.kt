package com.example.myapplication.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.domain.model.Todo
import com.example.myapplication.presentation.MainActivity

class TodoNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = context.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
    private var isNotificationsEnabled = prefs.getBoolean("notifications_enabled", true)

    companion object {
        const val CHANNEL_ID = "todo_notifications"
        const val START_NOTIFICATION_ID = 1
        const val END_NOTIFICATION_ID = 2
        const val MAX_NOTIFICATIONS_PER_USER = 50
        const val NOTIFICATION_GROUP_TIME_WINDOW = 5 * 60 * 1000L // 5 dakikalık pencere
        const val DISTANT_FUTURE_THRESHOLD = 7 * 24 * 60 * 60 * 1000L // 1 hafta
    }

    private data class NotificationGroup(
        val time: Long,
        val todos: MutableList<Todo> = mutableListOf(),
        val isStartNotification: Boolean
    )

    private val pendingNotifications = mutableMapOf<Long, NotificationGroup>()

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Görev Bildirimleri",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Görev başlangıç ve bitiş bildirimleri"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        isNotificationsEnabled = enabled
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        
        if (!enabled) {
            // Cancel all existing notifications if notifications are disabled
            cancelAllNotifications()
        }
    }

    fun isNotificationsEnabled(): Boolean = isNotificationsEnabled

    private fun cancelAllNotifications() {
        // This will only cancel future notifications
        // You might want to implement a way to track all scheduled notifications
        // and cancel them specifically
        notificationManager.cancelAll()
    }

    fun scheduleTaskNotifications(todo: Todo) {
        if (!isNotificationsEnabled) return

        val currentTime = System.currentTimeMillis()

        todo.startTime?.let { startTime ->
            if (startTime > currentTime) {
                if (startTime - currentTime > DISTANT_FUTURE_THRESHOLD) {
                    // Uzak gelecekteki bildirimleri ertele
                    val deferredTime = currentTime + DISTANT_FUTURE_THRESHOLD
                    addToNotificationGroup(todo, deferredTime, true)
                } else {
                    addToNotificationGroup(todo, startTime, true)
                }
            }
        }

        todo.endTime?.let { endTime ->
            if (endTime > currentTime) {
                if (endTime - currentTime > DISTANT_FUTURE_THRESHOLD) {
                    val deferredTime = currentTime + DISTANT_FUTURE_THRESHOLD
                    addToNotificationGroup(todo, deferredTime, false)
                } else {
                    addToNotificationGroup(todo, endTime, false)
                }
            }
        }

        // Grupları planla
        scheduleNotificationGroups()
    }

    private fun addToNotificationGroup(todo: Todo, time: Long, isStart: Boolean) {
        // Zaman penceresine göre grupla
        val groupTime = (time / NOTIFICATION_GROUP_TIME_WINDOW) * NOTIFICATION_GROUP_TIME_WINDOW
        
        val group = pendingNotifications.getOrPut(groupTime) {
            NotificationGroup(groupTime, mutableListOf(), isStart)
        }
        group.todos.add(todo)
    }

    private fun scheduleNotificationGroups() {
        pendingNotifications.forEach { (time, group) ->
            val message = buildGroupMessage(group)
            scheduleNotification(time, group.todos.first().id, message)
        }
        pendingNotifications.clear()
    }

    private fun buildGroupMessage(group: NotificationGroup): String {
        return when {
            group.todos.size == 1 -> {
                val todo = group.todos.first()
                if (group.isStartNotification) "${todo.title} görevi başladı"
                else "${todo.title} görevi sona erdi"
            }
            else -> {
                val todoNames = group.todos.joinToString(", ") { it.title }
                if (group.isStartNotification) 
                    "Başlayan görevler: $todoNames"
                else 
                    "Sona eren görevler: $todoNames"
            }
        }
    }

    private fun scheduleNotification(time: Long, baseId: Int, message: String) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_id", baseId)
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            baseId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (time - System.currentTimeMillis() <= DISTANT_FUTURE_THRESHOLD) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(time, pendingIntent),
                pendingIntent
            )
        } else {
            // Uzak gelecekteki bildirimler için daha az kesin zamanlama kullan
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        }
    }

    fun cancelTaskNotifications(todo: Todo) {
        val startIntent = PendingIntent.getBroadcast(
            context,
            todo.id * 10 + START_NOTIFICATION_ID,
            Intent(context, NotificationReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        startIntent?.cancel()

        val endIntent = PendingIntent.getBroadcast(
            context,
            todo.id * 10 + END_NOTIFICATION_ID,
            Intent(context, NotificationReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        endIntent?.cancel()
    }
} 