package com.example.gosiru

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gosiru.databinding.ActivityMainBinding
import com.kakao.vectormap.KakaoMapSdk

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ⚡ SDK 초기화 (액티비티 시작 시 1회만 수행)
        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val appKey = appInfo.metaData.getString("com.kakao.sdk.AppKey") ?: ""
        KakaoMapSdk.init(this, appKey)

        val tabHome = findViewById<LinearLayout>(R.id.tabHome)
        val tabMap = findViewById<LinearLayout>(R.id.tabMap)
        val tabProfile = findViewById<LinearLayout>(R.id.tabProfile)

        // 첫 화면 설정
        replaceFragment(HomeFragment())

        // 홈 탭
        tabHome.setOnClickListener {
            replaceFragment(HomeFragment())
        }


        // 프로필 탭
        tabProfile.setOnClickListener {
            replaceFragment(ProfileFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}