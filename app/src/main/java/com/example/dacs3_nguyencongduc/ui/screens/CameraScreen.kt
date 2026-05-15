package com.example.dacs3_nguyencongduc.ui.screens

import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dacs3_nguyencongduc.ui.theme.LocketPurple

/**
 * Màn hình Camera chính - hiển thị preview camera và nút chụp
 */
@Composable
fun LocketCameraHome(onImageCaptured: (Any) -> Unit, onChatClick: () -> Unit = {}, onFriendsClick: () -> Unit = {}, onSettingsClick: () -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    var isCapturing by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    // PreviewView quan trọng nhất để lấy bitmap "ăn liền"
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    LaunchedEffect(lensFacing) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.Builder().requireLensFacing(lensFacing).build(),
                preview,
                imageCapture
            )
            camera?.cameraControl?.setZoomRatio(zoomRatio)
        } catch (e: Exception) {
            Log.e("CameraX", "Binding failed", e)
        }
    }

    LaunchedEffect(zoomRatio) {
        camera?.cameraControl?.setZoomRatio(zoomRatio)
    }

    LaunchedEffect(isFlashOn) {
        camera?.cameraControl?.enableTorch(isFlashOn)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CameraTopBar(
            onChatClick = onChatClick,
            onFriendsClick = onFriendsClick,
            onSettingsClick = onSettingsClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Camera Preview
        CameraPreviewBox(
            previewView = previewView,
            zoomRatio = zoomRatio,
            isFlashOn = isFlashOn,
            onZoomToggle = { zoomRatio = if (zoomRatio == 1f) 2f else 1f },
            onZoomGesture = { scale ->
                val newZoom = zoomRatio * scale
                zoomRatio = newZoom.coerceIn(1f, 5f) // Giới hạn zoom từ 1x đến 5x
            },
            onFlashToggle = { isFlashOn = !isFlashOn },
            onFlipCamera = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                    CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Shutter Button
        ShutterControls(
            isCapturing = isCapturing,
            onCapturingChanged = { isCapturing = it },
            onCapture = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                previewView.bitmap?.let { bitmap ->
                    onImageCaptured(bitmap)
                }
            }
        )
    }
}

@Composable
private fun CameraTopBar(onChatClick: () -> Unit, onFriendsClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onSettingsClick) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Settings, null, tint = Color.White)
            }
        }
        Surface(
            onClick = onFriendsClick,
            color = Color.White.copy(alpha = 0.12f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.People, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("4 người bạn", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
        IconButton(onClick = onChatClick) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ChatBubbleOutline, null, tint = Color.White)
            }
        }
    }
}

@Composable
private fun CameraPreviewBox(
    previewView: PreviewView,
    zoomRatio: Float,
    isFlashOn: Boolean,
    onZoomToggle: () -> Unit,
    onZoomGesture: (Float) -> Unit,
    onFlashToggle: () -> Unit,
    onFlipCamera: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(64.dp))
            .background(Color.DarkGray)
    ) {
        AndroidView(
            factory = { previewView }, 
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        onZoomGesture(zoom)
                    }
                }
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isFlashOn) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clickable { onFlashToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff, 
                    null, 
                    tint = if (isFlashOn) Color.Black else Color.White, 
                    modifier = Modifier.size(20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clickable { onZoomToggle() },
                contentAlignment = Alignment.Center
            ) {
                Text("${zoomRatio.toInt()}x", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clickable { onFlipCamera() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FlipCameraIos, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ShutterControls(
    isCapturing: Boolean,
    onCapturingChanged: (Boolean) -> Unit,
    onCapture: () -> Unit
) {
    val scale by animateFloatAsState(
        if (isCapturing) 0.88f else 1f,
        animationSpec = tween(100),
        label = ""
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(Icons.Default.Image, null, tint = Color.White, modifier = Modifier.size(32.dp))

        Box(
            modifier = Modifier
                .size(95.dp)
                .scale(scale)
                .border(4.dp, LocketPurple, CircleShape)
                .padding(6.dp)
                .clip(CircleShape)
                .background(Color.White)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onCapturingChanged(true)
                            tryAwaitRelease()
                            onCapturingChanged(false)
                        },
                        onTap = { onCapture() }
                    )
                }
        )

        Icon(Icons.Default.Mic, null, tint = Color.White, modifier = Modifier.size(32.dp))
    }
}
