package com.example.gosiru

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gosiru.databinding.FragmentMapBinding
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class MapFragment : Fragment(R.layout.fragment_map) {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMapBinding.bind(view)

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
                Log.d("KakaoMap", "지도 준비 완료")
                // 여기서 나중에 좌표 추출 및 RPC 호출 로직이 시작됩니다.
            }
        })

        testSupabaseConnection()
    }

    private fun testSupabaseConnection() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = Supabase.client.from("stores").select().data
                Log.d("SupabaseTest", "✅ 연결 성공! 결과: $response")
            } catch (e: Exception) {
                Log.e("SupabaseTest", "❌ 에러: ${e.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}