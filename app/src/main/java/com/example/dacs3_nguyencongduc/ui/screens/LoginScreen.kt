package com.example.dacs3_nguyencongduc.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3_nguyencongduc.viewmodel.AuthState
import com.example.dacs3_nguyencongduc.viewmodel.AuthViewModel

private val LDark        = Color(0xFF0D0D0F)
private val LCardBg      = Color(0xFF1A1A1F)
private val LPurple      = Color(0xFF9D4EDD)
private val LPurpleLight = Color(0xFFBB86FC)
private val LTextPri     = Color(0xFFFFFFFF)
private val LTextSec     = Color(0xFFA0A0A0)
private val LTextDim     = Color(0xFF606070)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(authViewModel: AuthViewModel) {
    val context    = LocalContext.current
    val authState  by authViewModel.authState.collectAsState()
    val isOtpStep  = authState is AuthState.CodeSent
    val isLoading  = authState is AuthState.Loading

    var phoneNumber     by remember { mutableStateOf("+84") }
    var otpCode         by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Slide-up form after short delay
    var showForm by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        showForm = true
    }

    // Logo pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "logoPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D0D0F), Color(0xFF120818), Color(0xFF0D0D0F))))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Logo + App name (top center) ──
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .size(80.dp)
                    .scale(logoScale)
            ) {
                val w = size.width; val h = size.height
                // Glow
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        listOf(Color(0xFFBB86FC).copy(0.28f), Color.Transparent),
                        center = Offset(w / 2f, h * 0.62f), radius = w * 0.80f
                    ),
                    radius = w * 0.80f, center = Offset(w / 2f, h * 0.62f)
                )
                // Drop body
                val path = androidx.compose.ui.graphics.Path().apply {
                    val cx = w / 2f; val tipY = h * 0.03f
                    val bodyTop = h * 0.28f; val botY = h * 0.78f; val r = w * 0.44f
                    moveTo(cx, tipY)
                    cubicTo(cx - w * 0.05f, h * 0.16f, cx - r, bodyTop, cx - r, botY)
                    cubicTo(cx - r, botY + r, cx + r, botY + r, cx + r, botY)
                    cubicTo(cx + r, bodyTop, cx + w * 0.05f, h * 0.16f, cx, tipY)
                    close()
                }
                drawPath(path, androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(Color(0xFFBB86FC), Color(0xFF9D4EDD), Color(0xFF6A0DAD)),
                    start = Offset(w / 2f, 0f), end = Offset(w / 2f, h)
                ))
                // Highlight
                val hi = androidx.compose.ui.graphics.Path().apply {
                    val cx2 = w * 0.38f; val cy2 = h * 0.28f
                    moveTo(cx2, cy2)
                    cubicTo(cx2 - w*0.07f, cy2+h*0.06f, cx2+w*0.04f, cy2+h*0.13f, cx2+w*0.09f, cy2+h*0.03f)
                    cubicTo(cx2+w*0.09f, cy2-h*0.04f, cx2+w*0.03f, cy2-h*0.05f, cx2, cy2)
                    close()
                }
                drawPath(hi, Color.White.copy(0.32f))
            }

            Spacer(Modifier.height(14.dp))
            Text("Spocket", color = LTextPri, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Text("AI Expense Tracker", color = LPurpleLight.copy(0.7f), fontSize = 13.sp)
        }

        // ── Login form slides up from bottom ──
        AnimatedVisibility(
            visible = showForm,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.78f, stiffness = 260f)
            ) + fadeIn(tween(400)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = LCardBg,
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 28.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Handle bar
                    Box(
                        Modifier
                            .width(44.dp).height(5.dp)
                            .background(LTextDim.copy(0.5f), CircleShape)
                    )

                    Text(
                        if (isOtpStep) "Nhập mã OTP" else "Đăng nhập",
                        color = LTextPri, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        if (isOtpStep) "Mã 6 số đã gửi đến $phoneNumber" else "Chào mừng trở lại 👋",
                        color = LTextSec, fontSize = 14.sp
                    )

                    AnimatedContent(
                        targetState = isOtpStep,
                        transitionSpec = {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        },
                        label = "formStep"
                    ) { isOtp ->
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            if (!isOtp) {
                                // Phone field
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text("Số điện thoại", color = LTextDim, fontSize = 13.sp) },
                                    leadingIcon = { Text("🇻🇳", fontSize = 20.sp, modifier = Modifier.padding(start = 4.dp)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    colors = loginFieldColors(),
                                    textStyle = TextStyle(color = LTextPri, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                // Password field
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Mật khẩu", color = LTextDim, fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = LPurple.copy(0.7f)) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                null, tint = LPurple.copy(0.7f)
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    colors = loginFieldColors(),
                                    textStyle = TextStyle(color = LTextPri, fontSize = 15.sp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                // OTP field
                                OutlinedTextField(
                                    value = otpCode,
                                    onValueChange = { otpCode = it },
                                    label = { Text("Mã OTP", color = LTextDim, fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Key, null, tint = LPurple.copy(0.7f)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = loginFieldColors(),
                                    textStyle = TextStyle(color = LTextPri, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // CTA Button
                            Button(
                                onClick = {
                                    if (isOtpStep) authViewModel.verifyOtp(otpCode)
                                    else authViewModel.sendOtp(phoneNumber, context as android.app.Activity)
                                },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LPurple),
                                enabled = !isLoading,
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                                } else {
                                    Text(
                                        if (isOtpStep) "Xác nhận" else "Log In",
                                        fontSize = 16.sp, fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Terms
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = LTextDim, fontSize = 11.sp)) { append("By tapping Continue, you agree to our ") }
                            withStyle(SpanStyle(color = LPurpleLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)) { append("Terms") }
                            withStyle(SpanStyle(color = LTextDim, fontSize = 11.sp)) { append(" and our ") }
                            withStyle(SpanStyle(color = LPurpleLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)) { append("Privacy Policy") }
                            withStyle(SpanStyle(color = LTextDim, fontSize = 11.sp)) { append(".") }
                        },
                        textAlign = TextAlign.Center, lineHeight = 16.sp
                    )

                    // Divider
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(Modifier.weight(1f), color = LTextDim.copy(0.25f))
                        Text("  hoặc đăng nhập bằng  ", color = LTextDim, fontSize = 12.sp)
                        HorizontalDivider(Modifier.weight(1f), color = LTextDim.copy(0.25f))
                    }

                    // Social sign-in
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SocialBtn("🍎", "Apple",    Color.White)
                        SocialBtn("G",  "Google",   Color(0xFF4285F4))
                        SocialBtn("f",  "Facebook", Color(0xFF1877F2))
                    }

                    // Sign up link
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Chưa có tài khoản? ", color = LTextDim, fontSize = 13.sp)
                        Text("Đăng ký", color = LPurpleLight, fontSize = 13.sp,
                            fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor     = LTextPri,
    unfocusedTextColor   = LTextPri,
    focusedBorderColor   = LPurple,
    unfocusedBorderColor = LTextDim.copy(0.3f),
    cursorColor          = LPurple,
    focusedContainerColor    = LDark,
    unfocusedContainerColor  = LDark
)

@Composable
private fun SocialBtn(label: String, name: String, color: Color) {
    Surface(
        onClick = {}, color = LCardBg,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, color.copy(0.3f))
    ) {
        Column(
            Modifier.width(88.dp).padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, fontSize = 22.sp, color = color, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Text(name, color = LTextDim, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}
