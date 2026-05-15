package com.example.dacs3_nguyencongduc.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.dacs3_nguyencongduc.ui.screens.LocketTab
import com.example.dacs3_nguyencongduc.ui.theme.LocketPurple

/**
 * Bottom Navigation bar kiểu Locket
 */
@Composable
fun LocketBottomNav(selectedIndex: Int, onTabSelected: (LocketTab) -> Unit) {
    Surface(
        color = Color(0xFF2C2C2E).copy(alpha = 0.9f),
        shape = RoundedCornerShape(35.dp),
        modifier = Modifier
            .height(70.dp)
            .width(260.dp)
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(35.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Icons.Default.CalendarToday, selectedIndex == LocketTab.CALENDAR.index) { onTabSelected(LocketTab.CALENDAR) }
            NavItem(Icons.Default.Home, selectedIndex == LocketTab.HOME.index) { onTabSelected(LocketTab.HOME) }
            NavItem(Icons.Default.AccountBalanceWallet, selectedIndex == LocketTab.WALLET.index) { onTabSelected(LocketTab.WALLET) }
        }
    }
}

@Composable
private fun NavItem(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val tint by animateColorAsState(if (isSelected) LocketPurple else Color.White, label = "")
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color.White.copy(alpha = 0.05f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(30.dp))
    }
}
