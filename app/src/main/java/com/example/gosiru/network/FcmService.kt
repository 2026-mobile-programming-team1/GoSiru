package com.example.gosiru.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.gosiru.R
import com.example.gosiru.ui.MainActivity // MainActivity가 있는 경로 확인
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.jan.supabase.gotrue.auth

class FcmService : FirebaseMessagingService() {

    // 🔥 구글이 토큰을 새로 발급할 때마다 자동으로 호출되는 곳
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_SERVICE", "새로운 토큰 발급됨: $token")

        // 현재 로그인된 유저가 있다면, 새로 받은 토큰을 서버에 쏴줌
        val userId = Supabase.client.auth.currentUserOrNull()?.id
        if (userId != null) {
            WelfareRepository.updateFcmToken(userId)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "새로운 시루 혜택!"
        val body = remoteMessage.notification?.body ?: "조건에 맞는 복지 정보가 등록됐어요"
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "siru_notification_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "복지 알림", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}