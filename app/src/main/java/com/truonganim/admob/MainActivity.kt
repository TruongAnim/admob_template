package com.truonganim.admob

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.truonganim.admob.navigation.AppNavigation
import com.truonganim.admob.ui.base.BaseActivity
import com.truonganim.admob.ui.theme.AdMobBaseTheme

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdMobBaseTheme {
                AppNavigation()
            }
        }
    }
}