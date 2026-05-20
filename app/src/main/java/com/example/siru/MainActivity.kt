package com.example.siru

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.siru.ui.theme.SiruTheme
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
// Firebase 토큰
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {
                    Log.e("FCM", "토큰 가져오기 실패")
                    return@addOnCompleteListener
                }

                val token = task.result

                Log.d("FCM_TOKEN", token)
            }

        // 알림 권한 요청 (Android 13 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }
        enableEdgeToEdge()
        setContent {
            SiruTheme {
                SignUpScreen(
                    onNavigateToLogin = { },
                    onNavigateToNext = { uid ->
                        // 다음 화면으로 이동 (나중에 채울게요)
                    }
                )
            }
        }
    }
}