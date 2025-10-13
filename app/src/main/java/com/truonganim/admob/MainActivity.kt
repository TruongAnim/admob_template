package com.truonganim.admob

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.truonganim.admob.ui.home.HomeScreen
import com.truonganim.admob.ui.theme.AdMobBaseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdMobBaseTheme {
                HomeScreen()
            }
        }
    }
}