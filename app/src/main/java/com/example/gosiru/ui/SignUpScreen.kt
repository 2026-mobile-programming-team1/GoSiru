package com.example.gosiru.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gosiru.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToNext: (String) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val bgColor = Color(0xFF1C1C1C)
    val cardColor = Color(0xFF2A2A2A)
    val borderColor = Color(0xFF3A3A3A)
    val greenColor = Color(0xFF3ECF8E)
    val textColor = Color(0xFFEDEDED)
    val subTextColor = Color(0xFF9A9A9A)

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            uiState.uid?.let { uid -> onNavigateToNext(uid) }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "siru", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = greenColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Welcome to GO-시루", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = textColor)
            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardColor, RoundedCornerShape(12.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .padding(24.dp)
            ) {
                // 이메일 입력
                Text("Email address", fontSize = 13.sp, color = textColor, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("you@example.com", color = subTextColor) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor, unfocusedBorderColor = borderColor,
                        focusedTextColor = textColor, unfocusedTextColor = textColor,
                        cursorColor = greenColor, focusedContainerColor = Color(0xFF1C1C1C),
                        unfocusedContainerColor = Color(0xFF1C1C1C)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 비밀번호 입력
                Text("Password", fontSize = 13.sp, color = textColor, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("••••••••", color = subTextColor) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(if (passwordVisible) "Hide" else "Show", color = subTextColor, fontSize = 12.sp)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor, unfocusedBorderColor = borderColor,
                        focusedTextColor = textColor, unfocusedTextColor = textColor,
                        cursorColor = greenColor, focusedContainerColor = Color(0xFF1C1C1C),
                        unfocusedContainerColor = Color(0xFF1C1C1C)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.error != null) {
                    Text(text = uiState.error!!, color = Color(0xFFFF4444), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // --- 버튼 영역 ---

                // 1. 로그인 버튼 (상단)
                Button(
                    onClick = { viewModel.signIn(email, password) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = greenColor),
                    enabled = !uiState.isLoading
                ) {
                    Text("로그인", color = Color.Black, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. 회원가입 버튼 (하단)
                OutlinedButton(
                    onClick = { viewModel.signUp(email, password) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, greenColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = greenColor),
                    enabled = !uiState.isLoading
                ) {
                    Text("회원가입", fontWeight = FontWeight.SemiBold)
                }

                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        color = greenColor,
                        modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}