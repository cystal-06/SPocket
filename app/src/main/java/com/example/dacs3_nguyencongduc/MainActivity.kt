package com.example.dacs3_nguyencongduc

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import com.example.dacs3_nguyencongduc.data.*
import com.example.dacs3_nguyencongduc.data.repository.TransactionRepository
import com.example.dacs3_nguyencongduc.ui.screens.LoginScreen
import com.example.dacs3_nguyencongduc.ui.screens.MainScreen
import com.example.dacs3_nguyencongduc.ui.screens.SplashScreen
import com.example.dacs3_nguyencongduc.viewmodel.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Kích hoạt giao diện tràn viền và đặt màu thanh điều hướng thành đen
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK)
        )
        super.onCreate(savedInstanceState)
        
        val dao = AppDatabase.getDatabase(applicationContext).transactionDao()
        val repository = TransactionRepository(dao)
        val financeViewModel = ViewModelProvider(this, FinanceViewModelFactory(repository))[FinanceViewModel::class.java]
        val authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        setContent { 
            MaterialTheme { 
                Surface {
                    var showSplash by remember { mutableStateOf(true) }
                    val authState by authViewModel.authState.collectAsState()

                    AnimatedContent(
                        targetState = showSplash,
                        transitionSpec = {
                            if (targetState) {
                                // splash xuất hiện
                                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                            } else {
                                // splash biến mất → login fade in nhẹ nhàng từ màu tím
                                fadeIn(tween(600)) togetherWith fadeOut(tween(200))
                            }
                        },
                        label = "SplashTransition"
                    ) { isSplash ->
                        if (isSplash) {
                            SplashScreen(onFinished = { showSplash = false })
                        } else {
                            AnimatedContent(
                                targetState = authState is AuthState.Success,
                                transitionSpec = {
                                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                                },
                                label = "AuthTransition"
                            ) { isAuthenticated ->
                                if (isAuthenticated) {
                                    MainScreen(financeViewModel, authViewModel)
                                } else {
                                    LoginScreen(authViewModel)
                                }
                            }
                        }
                    }
                } 
            } 
        }
    }
}