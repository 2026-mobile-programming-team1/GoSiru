package com.example.gosiru.ui

import com.example.gosiru.network.Supabase



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.gosiru.viewmodel.NotificationViewModel
// 주의: 아래 SupabaseClient 경로는 준혁님 프로젝트에 맞게 import 하세요!
// import com.example.gosiru.network.SupabaseClient

class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val viewModel = NotificationViewModel(supabase = Supabase.client)

        // 2. 화면에 Compose UI 세팅
        setContent {
            NotificationScreen(viewModel = viewModel)
        }
    }
}