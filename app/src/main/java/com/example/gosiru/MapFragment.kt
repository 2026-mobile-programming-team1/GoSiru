package com.example.gosiru

// 추가
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    // 추가
    // 디바운싱 작업을 저장하기 위한 Runnable 변수
    private var debounceRunnable: Runnable? = null
    // 메인(UI) 스레드에서 일정 시간 후 작업을 실행하기 위한 Handler
    private val handler = Handler(Looper.getMainLooper())
    //여기까지
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMapBinding.bind(view)

        // 지도 엔진 시작
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

                //추가된부분
                // 지도 이동 및 확대/축소 종료 시 호출되는 리스너
                kakaoMap.setOnCameraMoveEndListener { map, cameraPosition, gestureType ->
                    // 디바운싱 처리
                    debounceRunnable?.let { handler.removeCallbacks(it) }

                    // 일정 시간(0.5초) 후 실행할 작업 예약
                    debounceRunnable = Runnable {

                        // MapView의 실제 크기를 가져옴
                        val width = binding.mapView.width
                        val height = binding.mapView.height

                        // 화면 좌표(px)를 위경도(LatLng)로 변환
                        // 좌측 하단(SouthWest) 좌표
                        val sw = map.fromScreenPoint(0, height)
                        // 우측 상단(NorthEast) 좌표
                        val ne = map.fromScreenPoint(width, 0)


                        // 좌표 변환 성공 시 실행
                        if (sw != null && ne != null) {
                            // 화면 좌측 하단(SW) 위경도
                            val minLat = sw.latitude
                            val minLng = sw.longitude
                            // 화면 우측 상단(NE) 위경도
                            val maxLat = ne.latitude
                            val maxLng = ne.longitude
                            // 현재 지도 화면 영역 좌표 로그 출력
                            Log.d(
                                "MAP_BOUNDS",
                                """
                                 SW: $minLat, $minLng
                                 NE: $maxLat, $maxLng
                                 """.trimIndent()
                            )

                            // TODO: 서버 API 호출 예정
                            // 현재 지도 범위(min/max 좌표)를 서버로 전송하여
                            // 해당 영역 안의 가게 데이터를 조회할 예정
                            // fetchStoresInBounds(minLat, minLng, maxLat, maxLng)
                        }
                    }

                    // 0.5초 후 Runnable 실행 (Debounce)
                    handler.postDelayed(debounceRunnable!!, 500L)



                }
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

