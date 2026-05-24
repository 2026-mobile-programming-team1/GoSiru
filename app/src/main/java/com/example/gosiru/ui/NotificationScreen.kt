package com.example.gosiru.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
                        color = Color(0xFF212121)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF4F5F9)
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "새로운 알림이 없습니다.",
                    color = Color(0xFF888888),
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                items(notifications) { item ->
                    NotificationCard(item = item) {
                        // 💡 [핵심] welfareId가 있을 때만 실행하는 안전장치
                        item.welfareId?.let { id ->
                            // 1. 뷰모델을 통해 DB에서 진짜 링크를 가져옵니다.
                            viewModel.getWelfareLink(id) { link ->
                                if (!link.isNullOrBlank()) {
                                    // 2. 링크 주소 가공 (https 보정)
                                    val safeLink = if (!link.startsWith("http")) "https://$link" else link

                                    try {
                                        // 3. 시스템 브라우저 실행
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeLink))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "브라우저를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "등록된 신청 링크가 없습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } ?: run {
                            // welfareId 자체가 null인 경우 처리
                            Toast.makeText(context, "연결된 복지 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(item: Notification, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEEF2FF))
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notification Icon",
                    tint = Color(0xFF3262E5),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

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
                        color = Color(0xFF212121)
                    )

                    Text(
                        text = item.created_at?.take(10) ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.body,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}