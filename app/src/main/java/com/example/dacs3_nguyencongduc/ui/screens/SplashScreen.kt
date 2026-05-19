package com.example.dacs3_nguyencongduc.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val SPurple      = Color(0xFF9D4EDD)
private val SPurpleDark  = Color(0xFF6A0DAD)
private val SPurpleLight = Color(0xFFBB86FC)
private val SDark        = Color(0xFF0D0D0F)
private val SCurtain     = Color(0xFF7B2FBE)  // một màu tím duy nhất cho 2 tấm màn

/**
 * Splash Sequence:
 * Phase 0 (0–700ms)    : logo giọt nước nhỏ nhịp đập trên nền đen
 * Phase 1 (700–1700ms) : giọt nước phóng to → tím phủ toàn màn hình
 * Phase 2 (1700–2400ms): logo giọt nước TRẮNG + "Spocket" hiện trên nền tím
 * Phase 3 (2400–3300ms): nền tím XẺ ĐÔI trái-phải → lộ nền đen + logo tím
 * Phase 4 (3300ms+)    : onFinished()
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp

    var phase by remember { mutableIntStateOf(0) }

    // Phase 0: idle heartbeat
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse"
    )

    // Phase 1: purple flood 0→1
    val purpleExpand = remember { Animatable(0f) }

    // Phase 2: logo fade on purple bg
    val logoAlpha = remember { Animatable(0f) }

    // Phase 3: curtain open 0→1 (each half moves outward)
    val curtainOpen = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(700)
        // Phase 1
        phase = 1
        purpleExpand.animateTo(1f, tween(1000, easing = CubicBezierEasing(0.22f, 0f, 0.36f, 1f)))
        // Phase 2
        phase = 2
        logoAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        delay(600)
        // Phase 3
        phase = 3
        curtainOpen.animateTo(1f, tween(850, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)))
        delay(100)
        onFinished()
    }

    // Drop scale & alpha for phase 0 & 1
    val dropScale = if (phase == 0) idlePulse else 1f + purpleExpand.value * 72f
    val dropAlpha = if (phase == 0) 1f else (1f - purpleExpand.value * 1.8f).coerceIn(0f, 1f)

    // Curtain offset: left panel slides left, right panel slides right
    val curtainOffset = (screenWidthDp / 2 * curtainOpen.value)

    Box(
        modifier = Modifier.fillMaxSize().background(SDark),
        contentAlignment = Alignment.Center
    ) {

        // ── LAYER 1 (bottom): Logo tím trên nền đen — luôn hiện phía sau màn ──
        if (phase >= 2) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    drawWaterDropColored(SPurple, SPurpleDark, SPurpleLight)
                }
                Spacer(Modifier.height(16.dp))
                Text("Spocket", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
                Text("AI Expense Tracker", color = SPurpleLight.copy(0.7f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        // ── LAYER 2: Màn trái (left curtain) — trượt sang trái khi phase 3 ──
        if (phase >= 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .offset { IntOffset((-curtainOffset).roundToPx(), 0) }
                    .background(SCurtain)
            )
        }

        // ── LAYER 3: Màn phải (right curtain) — trượt sang phải khi phase 3 ──
        if (phase >= 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .offset { IntOffset(curtainOffset.roundToPx(), 0) }
                    .background(SCurtain)
            )
        }

        // ── LAYER 4: Logo trắng trên màn tím (phase 2, mờ dần khi màn mở) ──
        if (phase == 2 || (phase == 3 && curtainOpen.value < 0.4f)) {
            val whiteLogoAlpha = logoAlpha.value * (1f - curtainOpen.value * 3f).coerceIn(0f, 1f)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer { alpha = whiteLogoAlpha },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    drawWaterDropWhite()
                }
                Spacer(Modifier.height(16.dp))
                Text("Spocket", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
                Text("AI Expense Tracker", color = Color.White.copy(0.65f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        // ── LAYER 5: Logo giọt nước nhỏ (phase 0 & 1) ──
        if (phase <= 1) {
            Canvas(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer { scaleX = dropScale; scaleY = dropScale; alpha = dropAlpha }
            ) {
                drawWaterDropColored(SPurple, SPurpleDark, SPurpleLight)
            }
        }
    }
}

/** Giọt nước màu tím (dùng trên nền đen ở phase 0 & sau khi màn mở) */
private fun DrawScope.drawWaterDropColored(main: Color, dark: Color, light: Color) {
    val w = size.width; val h = size.height
    drawCircle(
        brush = Brush.radialGradient(
            listOf(light.copy(0.30f), Color.Transparent),
            center = Offset(w / 2f, h * 0.62f), radius = w * 0.80f
        ),
        radius = w * 0.80f, center = Offset(w / 2f, h * 0.62f)
    )
    drawPath(
        buildDropPath(w, h),
        Brush.linearGradient(listOf(light, main, dark), start = Offset(w / 2f, 0f), end = Offset(w / 2f, h))
    )
    drawPath(buildHighlightPath(w, h), Color.White.copy(0.32f))
}

/** Giọt nước màu trắng (dùng trên nền tím ở phase 2) */
private fun DrawScope.drawWaterDropWhite() {
    val w = size.width; val h = size.height
    drawPath(buildDropPath(w, h), Color.White)
    drawPath(buildHighlightPath(w, h), Color.White.copy(0.30f))
}

private fun buildDropPath(w: Float, h: Float) = Path().apply {
    val cx = w / 2f; val tipY = h * 0.03f
    val bodyTop = h * 0.28f; val botY = h * 0.78f; val r = w * 0.44f
    moveTo(cx, tipY)
    cubicTo(cx - w * 0.05f, h * 0.16f, cx - r, bodyTop, cx - r, botY)
    cubicTo(cx - r, botY + r, cx + r, botY + r, cx + r, botY)
    cubicTo(cx + r, bodyTop, cx + w * 0.05f, h * 0.16f, cx, tipY)
    close()
}

private fun buildHighlightPath(w: Float, h: Float) = Path().apply {
    val cx = w * 0.38f; val cy = h * 0.28f
    moveTo(cx, cy)
    cubicTo(cx - w * 0.07f, cy + h * 0.06f, cx + w * 0.04f, cy + h * 0.13f, cx + w * 0.09f, cy + h * 0.03f)
    cubicTo(cx + w * 0.09f, cy - h * 0.04f, cx + w * 0.03f, cy - h * 0.05f, cx, cy)
    close()
}
