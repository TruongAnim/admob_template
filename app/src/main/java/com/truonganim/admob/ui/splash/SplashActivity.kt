package com.truonganim.admob.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.truonganim.admob.MainActivity
import com.truonganim.admob.ui.theme.AdMobBaseTheme

/**
 * Splash Activity
 * Entry point of the app, displays splash screen with loading animation
 * Prevents back button press during loading
 */
class SplashActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AdMobBaseTheme {
                SplashScreen(
                    onLoadingComplete = {
                        navigateToMain()
                    }
                )
            }
        }
    }
    
    /**
     * Navigate to MainActivity after loading completes
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close splash activity
    }
    
    /**
     * Disable back button during splash screen
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - prevent user from going back during splash
    }
}

