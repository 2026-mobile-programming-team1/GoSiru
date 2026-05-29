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
        // 💡 수정됨: notification 객체뿐만 아니라 data 객체에서도 제목/내용을 찾도록 보강
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "새로운 시루 혜택!"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "조건에 맞는 복지 정보가 등록됐어요"

        Log.d("FCM_TEST", "🔥 FcmService 작동함!! 제목: $title, 내용: $body")
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        // 💡 1. 채널 ID를 바꿔줍니다 (기존에 '조용히 받기'로 캐싱된 설정을 리셋하기 위함)
        val channelId = "siru_notification_high_priority"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 💡 2. IMPORTANCE_DEFAULT -> IMPORTANCE_HIGH 로 변경! (그래야 화면 위에서 뚝 떨어집니다)
            val channel = NotificationChannel(channelId, "복지 알림", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId)
            // 💡 3. 아이콘 확실한 걸로 변경! (ic_notification 파일이 없어서 알림이 증발했을 수 있습니다)
            .setSmallIcon(R.mipmap.ic_launcher) // 앱 기본 아이콘으로 변경 (나중에 예쁜 걸로 바꾸세요)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 안드로이드 7 이하를 위한 설정
            .setDefaults(NotificationCompat.DEFAULT_ALL) // 진동/소리 기본값 켜기
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}