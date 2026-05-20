package com.example.gosiru.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.gosiru.theme.SiruTheme

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SiruTheme {
                // 팀원이 Supabase UI로 구현한(또는 구현할) 화면 호출
                SignUpScreen(
                    onNavigateToLogin = {
                        // 로그인 화면으로 돌아가거나 액티비티 종료
                        finish()
                    },
                    onNavigateToNext = { uid ->
                        // 가입 완료 후 메인 화면으로 이동
                        val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // 뒤로 가기 눌렀을 때 다시 회원가입 창 안 뜨게 종료
                    }
                )
            }
        }
    }
}