package com.example.dacs3_nguyencongduc.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.dacs3_nguyencongduc.data.model.Conversation
import com.example.dacs3_nguyencongduc.data.model.SAMPLE_CONVERSATIONS
import com.example.dacs3_nguyencongduc.ui.components.LocketBottomNav
import com.example.dacs3_nguyencongduc.viewmodel.FinanceViewModel
import com.example.dacs3_nguyencongduc.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Enum cho các tab điều hướng
 */
enum class LocketTab(val index: Int) {
    CALENDAR(0), HOME(1), WALLET(2)
}

/**
 * Trạng thái navigation chính
 */
private sealed class AppScreen {
    data object Main : AppScreen()
    data class SaveTransaction(val image: Any) : AppScreen()
    data object ChatList : AppScreen()
    data class ChatDetail(val conversation: Conversation) : AppScreen()
    data class TransactionDetail(val dayGroups: List<DayGroup>, val initialIndex: Int) : AppScreen()
    data object FriendsList : AppScreen()
    data object Settings : AppScreen()
}

/**
 * Màn hình chính - điều phối navigation giữa tất cả các screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FinanceViewModel, authViewModel: AuthViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Main) }

    val pagerState = rememberPagerState(initialPage = LocketTab.HOME.index) { 3 }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Camera permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasCameraPermission = it }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (!hasCameraPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Cấp quyền Camera")
                }
            }
        } else {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val duration = 200
                    val animSpec = tween<androidx.compose.ui.unit.IntOffset>(duration, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                    val fadeSpec = tween<Float>(duration)

                    when {
                        targetState is AppScreen.TransactionDetail ->
                            slideInVertically(animSpec) { it } + fadeIn(fadeSpec) togetherWith
                                    slideOutVertically(animSpec) { -it / 3 } + fadeOut(fadeSpec)
                                    
                        initialState is AppScreen.TransactionDetail && targetState is AppScreen.Main ->
                            slideInVertically(animSpec) { -it / 3 } + fadeIn(fadeSpec) togetherWith
                                    slideOutVertically(animSpec) { it } + fadeOut(fadeSpec)
                                    
                        targetState is AppScreen.ChatList || targetState is AppScreen.ChatDetail
                                || targetState is AppScreen.FriendsList ->
                            slideInHorizontally(animSpec) { it } + fadeIn(fadeSpec) togetherWith
                                    slideOutHorizontally(animSpec) { -it / 3 } + fadeOut(fadeSpec)
                                    
                        initialState is AppScreen.ChatList || initialState is AppScreen.ChatDetail
                                || initialState is AppScreen.FriendsList ->
                            slideInHorizontally(animSpec) { -it } + fadeIn(fadeSpec) togetherWith
                                    slideOutHorizontally(animSpec) { it / 3 } + fadeOut(fadeSpec)
                                    
                        else ->
                            fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                    }
                },
                label = "ScreenNav"
            ) { screen ->
                when (screen) {
                    is AppScreen.SaveTransaction -> {
                        SaveTransactionScreen(
                            imageSource = screen.image,
                            onSave = { title, amount, type, category, date ->
                                val finalUri = if (screen.image is Bitmap) {
                                    saveBitmapToFile(context, screen.image).toString()
                                } else {
                                    screen.image.toString()
                                }
                                viewModel.addTransaction(title, amount, type, category, date, finalUri, context)
                                currentScreen = AppScreen.Main
                            },
                            onCancel = { currentScreen = AppScreen.Main }
                        )
                    }

                    is AppScreen.ChatList -> {
                        ChatListScreen(
                            conversations = SAMPLE_CONVERSATIONS,
                            onConversationClick = { conv ->
                                currentScreen = AppScreen.ChatDetail(conv)
                            },
                            onBack = { currentScreen = AppScreen.Main }
                        )
                    }

                    is AppScreen.ChatDetail -> {
                        ChatDetailScreen(
                            conversation = screen.conversation,
                            onBack = { currentScreen = AppScreen.ChatList }
                        )
                    }

                    is AppScreen.TransactionDetail -> {
                        TransactionDetailScreen(
                            dayGroups = screen.dayGroups,
                            initialDayIndex = screen.initialIndex,
                            onClose = { currentScreen = AppScreen.Main }
                        )
                    }

                    is AppScreen.FriendsList -> {
                        FriendsScreen(
                            onBack = { currentScreen = AppScreen.Main }
                        )
                    }
                    
                    is AppScreen.Settings -> {
                        SettingsScreen(
                            onBack = { currentScreen = AppScreen.Main },
                            authViewModel = authViewModel
                        )
                    }

                    is AppScreen.Main -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = true,
                                beyondViewportPageCount = 1
                            ) { page ->
                                when (page) {
                                    LocketTab.CALENDAR.index -> CalendarScreen(
                                        transactions = transactions,
                                        onDayClick = { groups, idx ->
                                            currentScreen = AppScreen.TransactionDetail(groups, idx)
                                        }
                                    )
                                    LocketTab.HOME.index -> LocketCameraHome(
                                        onImageCaptured = {
                                            currentScreen = AppScreen.SaveTransaction(it)
                                        },
                                        onChatClick = {
                                            currentScreen = AppScreen.ChatList
                                        },
                                        onFriendsClick = {
                                            currentScreen = AppScreen.FriendsList
                                        },
                                        onSettingsClick = {
                                            currentScreen = AppScreen.Settings
                                        }
                                    )
                                    LocketTab.WALLET.index -> WalletScreen(transactions)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 30.dp)
                                    .navigationBarsPadding()
                            ) {
                                LocketBottomNav(
                                    selectedIndex = pagerState.currentPage,
                                    onTabSelected = { tab ->
                                        scope.launch { pagerState.animateScrollToPage(tab.index) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Hàm phụ để lưu Bitmap ra file cache
 */
private fun saveBitmapToFile(context: android.content.Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "locket_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return Uri.fromFile(file)
}
