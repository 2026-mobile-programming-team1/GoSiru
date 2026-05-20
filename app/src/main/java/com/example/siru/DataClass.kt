package com.example.siru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import android.content.Context
import android.content.Intent
import android.net.Uri
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.postgrest
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat


@Serializable
// 혜택정보 WelfareItem 데이터 클래스
data class WelfareItem(
    val id: Long,
    val title: String,
    val content: String,
    @SerialName("min_age") val minAge: Int,
    @SerialName("max_age") val maxAge: Int,
    @SerialName("target_job") val targetJob: String,
    @SerialName("income_limit") val incomeLimit: Int,
    val amount: String,
    @SerialName("is_siru") val isSiru: Boolean,
    @SerialName("apply_link") val applyLink: String
)

// 유저 정보 UserProfile 데이터 클래스
@Serializable
data class UserProfile(
    val id: String,
    @SerialName("birth_date") val birthDate: String,
    @SerialName("job_status") val jobStatus: String,
    @SerialName("income_level") val incomeLevel: Int,
    @SerialName("is_admin") val isAdmin: Boolean,
    @SerialName("fcm_token") val fcmToken: String? = null
)

// RecyclerView에 데이터 뿌리기

// apply_link 로 브라우저 열기
fun openApplyLink(context: Context, applyLink: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(applyLink))
    context.startActivity(intent)
}

// Supabase에서 매칭된 복지 리스트 가져오기
suspend fun getMatchedWelfare(
    age: Int,
    jobStatus: String,
    incomeLevel: Int
): List<WelfareItem> {
    return supabase.postgrest.rpc("get_matched_welfare") {
        mapOf(
            "u_age" to age,
            "u_job" to jobStatus,
            "u_income" to incomeLevel
        )
    }.decodeList<WelfareItem>()
}

// TODO: UI - 리스트가 비어있으면 "조건에 맞는 혜택이 없어요" 문구 표시
// TODO: UI - isSiru == true 인 아이템에 시루 뱃지 표시
// TODO: UI - RecyclerView 어댑터 연결



// FCM 토큰이 새로 발급되거나 갱신될 때 자동 호출
// FCM 토큰이 새로 발급되거나 갱신될 때 자동 호출
class FcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveFcmToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 1. 데이터 가져오기
        val title = remoteMessage.notification?.title ?: "새로운 시루 혜택!"
        val body = remoteMessage.notification?.body ?: "조건에 맞는 복지 정보가 등록됐어요"

        // 2. 알림 표시하기
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "siru_notification_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 3. Notification Channel 설정 (Android 8.0 이상 필수)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "복지 알림", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // 4. 알림 클릭 시 이동할 화면 (Intent)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // 5. 알림 생성
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
// FCM 토큰을 Supabase profiles 테이블에 저장
fun saveFcmToken(userUid: String) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        val token = task.result
        // profiles 테이블의 fcm_token 컬럼 업데이트
        MainScope().launch {
            supabase.postgrest.from("profiles").update(
                mapOf("fcm_token" to token)
            ) { filter { eq("id", userUid) } }
        }
    }
}