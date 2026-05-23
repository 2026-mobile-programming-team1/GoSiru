package com.example.gosiru.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gosiru.data.Notification
import com.example.gosiru.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(viewModel: NotificationViewModel) {
    val notifications = viewModel.notificationList.value
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchNotifications()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "알림 목록",
                        fontWeight = FontWeight.Bold,
                        // XML의 @color/text1 느낌의 진한 색상
                        color = Color(0xFF212121)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        // XML의 @color/background 와 유사한 연한 회색/푸른빛 배경
        containerColor = Color(0xFFF4F5F9)
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "새로운 알림이 없습니다.",
                    // XML의 @color/text3 느낌의 색상
                    color = Color(0xFF888888),
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp), // XML 디자인의 paddingStart="24dp" 유지
                verticalArrangement = Arrangement.spacedBy(16.dp), // 카드 사이 간격
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                items(notifications) { item ->
                    NotificationCard(item = item) {
                        if (!item.url.isNullOrEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(item: Notification, onClick: () -> Unit) {
    // XML의 bg_admin_benefit_register 배경과 동일한 곡률(16dp)과 흰색 배경
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // 그림자 없이 깔끔하게
    ) {
        Row(
            modifier = Modifier.padding(20.dp), // XML 카드의 내부 padding 유지
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좌측 아이콘 (XML의 bg_benefit_icon 스타일 재현)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEEF2FF)) // 아주 연한 파란색 배경
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notification Icon",
                    // 앱의 메인 버튼 색상(Blue)과 톤앤매너 맞춤
                    tint = Color(0xFF3262E5),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 우측 텍스트 정보
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121) // @color/text1
                    )
                    Text(
                        text = item.created_at?.take(10) ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFF888888) // @color/text3
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.body,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF666666) // @color/text2 느낌
                )
            }
        }
    }
}