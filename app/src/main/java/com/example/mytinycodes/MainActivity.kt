package com.example.mytinycodes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.mytinycodes.ui.theme.MyTinyCodesTheme
import com.example.mytinycodes.ui.WaterTrackerApp
import com.example.mytinycodes.ui.RecordsScreen
import com.example.mytinycodes.ui.AchievementsScreen
import com.example.mytinycodes.ui.ReminderSettingsScreen
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTinyCodesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var selectedTab by remember { mutableStateOf(0) }
                    
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            when (selectedTab) {
                                0 -> WaterTrackerApp()
                                1 -> RecordsScreen()
                                2 -> AchievementsScreen()
                                3 -> ReminderSettingsScreen()
                            }
                        }
                        
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = "Ana Sayfa") },
                                label = { Text("Ana Sayfa") },
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.List, contentDescription = "Kayıtlar") },
                                label = { Text("Kayıtlar") },
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Başarılar") },
                                label = { Text("Başarılar") },
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Notifications, contentDescription = "Hatırlatıcı") },
                                label = { Text("Hatırlatıcı") },
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 }
                            )
                        }
                    }
                }
            }
        }
    }
}