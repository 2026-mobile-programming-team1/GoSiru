package com.example.app_admin.data

import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WelfareRepository(private val postgrest: Postgrest) {

    // 복지 정보를 슈파베이스에 업로드하는 함수
    suspend fun uploadWelfare(item: WelfareRequest): Boolean = withContext(Dispatchers.IO) {
        try {
            // "welfare_list"는 네가 슈파베이스에 만든 테이블 이름이랑 똑같아야 해!
            postgrest.from("welfare_list").insert(item)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}