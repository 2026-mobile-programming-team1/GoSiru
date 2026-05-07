package com.example.gosiru

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.gosiru.databinding.ActivityMainBinding
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.MapLifeCycleCallback
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val appKey = appInfo.metaData.getString("com.kakao.sdk.AppKey") ?:""

        // ⚡ 2. 카카오맵 SDK 엔진 시동 걸기! (제일 중요)
        KakaoMapSdk.init(this, appKey)

        // 🗺️ 지도 엔진 시작
        binding.mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.d("KakaoMap", "지도 종료")
            }

            override fun onMapError(error: Exception) {
                Log.e("KakaoMap", "지도 에러: ${error.message}")
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                // 🎉 팀장님이 원하시는 '메인에 지도 띄우기' 성공 지점!
                Log.d("KakaoMap", "지도 준비 완료")
            }
        })
        // 🔥 슈파베이스 연동 테스트 코드 시작!
        testSupabaseConnection()
    }

    // 필수 생명주기 관리
    override fun onResume() {
        super.onResume()
        binding.mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()
    }
    private fun testSupabaseConnection() {
        // 슈파베이스는 네트워크 통신이라 '코루틴' 안에서 실행해야 해
        lifecycleScope.launch {
            try {
                // 1. 슈파베이스 DB의 특정 테이블에서 데이터 하나만 가져와보기
                // 아직 테이블 안 만들었으면 이 부분에서 에러 날 수 있으니 참고!
                val response = Supabase.client.from("stores").select().data

                Log.d("SupabaseTest", "✅ 연결 성공! 데이터 결과: $response")
            } catch (e: Exception) {
                Log.e("SupabaseTest", "❌ 연결 실패 에러: ${e.message}")
                // 에러가 '401'이면 키 값이 틀린 거, '404'면 테이블 이름이 틀린 거야.
            }
        }
    }
}