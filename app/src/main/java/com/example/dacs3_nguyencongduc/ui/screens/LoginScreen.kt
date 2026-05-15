package com.example.dacs3_nguyencongduc.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3_nguyencongduc.ui.theme.LocketPurple
import com.example.dacs3_nguyencongduc.viewmodel.AuthState
import com.example.dacs3_nguyencongduc.viewmodel.AuthViewModel

/**
 * Màn hình đăng nhập OTP - giống Locket nhưng phong cách tím CapMoney
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(authViewModel: AuthViewModel) {
    val authState by authViewModel.authState.collectAsState()
    var phoneNumber by remember { mutableStateOf("+84") }
    var otpCode by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Logo animation
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A0A0A),
                        Color(0xFF120818),
                        Color(0xFF0A0A0A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // ── Logo ──
            Box(
                Modifier
                    .size(100.dp)
                    .scale(logoScale)
                    .background(
                        Brush.linearGradient(listOf(LocketPurple, Color(0xFF9C27B0))),
                        RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("💸", fontSize = 48.sp)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Spocket",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Quản lý chi tiêu thông minh",
                color = Color.White.copy(0.5f),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(48.dp))

            // ── Nội dung theo trạng thái ──
            AnimatedContent(
                targetState = authState is AuthState.CodeSent,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                },
                label = "steps"
            ) { isOtpStep ->
                if (!isOtpStep) {
                    // ── BƯỚC 1: Nhập số điện thoại ──
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Đăng nhập bằng số điện thoại",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Nhập số điện thoại để nhận mã OTP",
                            color = Color.White.copy(0.4f),
                            fontSize = 13.sp
                        )

                        Spacer(Modifier.height(28.dp))

                        // Phone input
                        Surface(
                            color = Color(0xFF1C1C1E),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🇻🇳", fontSize = 24.sp)
                                Spacer(Modifier.width(10.dp))
                                TextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp
                                    ),
                                    placeholder = {
                                        Text("+84 xxx xxx xxx", color = Color(0xFF555555), fontSize = 18.sp)
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = LocketPurple
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Send OTP button
                        GradientButton(
                            text = "Gửi mã OTP",
                            isLoading = authState is AuthState.Loading,
                            onClick = {
                                val activity = context as? Activity
                                if (activity != null) {
                                    authViewModel.sendOtp(phoneNumber.trim(), activity)
                                }
                            }
                        )
                    }
                } else {
                    // ── BƯỚC 2: Nhập mã OTP ──
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Nhập mã xác thực",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Mã OTP đã gửi đến $phoneNumber",
                            color = Color.White.copy(0.4f),
                            fontSize = 13.sp
                        )

                        Spacer(Modifier.height(28.dp))

                        // OTP input
                        Surface(
                            color = Color(0xFF1C1C1E),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = otpCode,
                                onValueChange = { if (it.length <= 6) otpCode = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 12.sp,
                                    textAlign = TextAlign.Center
                                ),
                                placeholder = {
                                    Text(
                                        "------",
                                        color = Color(0xFF555555),
                                        fontSize = 28.sp,
                                        letterSpacing = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = LocketPurple
                                )
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        // Verify button
                        GradientButton(
                            text = "Xác nhận",
                            isLoading = authState is AuthState.Loading,
                            onClick = {
                                if (otpCode.length == 6) {
                                    authViewModel.verifyOtp(otpCode)
                                }
                            }
                        )

                        Spacer(Modifier.height(16.dp))

                        // Resend link
                        Text(
                            "Gửi lại mã",
                            color = LocketPurple,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                val activity = context as? Activity
                                if (activity != null) {
                                    authViewModel.sendOtp(phoneNumber.trim(), activity)
                                }
                            }
                        )
                    }
                }
            }

            // Error message
            if (authState is AuthState.Error) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = Color(0xFFFF6B6B).copy(0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(18.dp))
                        Text(
                            (authState as AuthState.Error).message,
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Footer
            Text(
                "Bằng việc tiếp tục, bạn đồng ý với\nĐiều khoản sử dụng & Chính sách bảo mật",
                color = Color.White.copy(0.25f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun GradientButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                if (isLoading) Brush.horizontalGradient(
                    listOf(Color(0xFF555555), Color(0xFF444444))
                )
                else Brush.horizontalGradient(
                    listOf(LocketPurple, Color(0xFF9C27B0))
                ),
                RoundedCornerShape(28.dp)
            ),
        contentPadding = PaddingValues(0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private val EaseInOutSine: Easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
