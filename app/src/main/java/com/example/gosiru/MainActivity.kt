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
}