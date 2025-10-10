package com.truonganim.admob.ui.language

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.truonganim.admob.MainActivity
import com.truonganim.admob.ui.theme.AdMobBaseTheme

/**
 * Language Selection Activity
 * Allows user to select their preferred language
 */
class LanguageActivity : ComponentActivity() {
    
    private lateinit var viewModel: LanguageViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        viewModel = ViewModelProvider(this)[LanguageViewModel::class.java]
        
        setContent {
            AdMobBaseTheme {
                LanguageScreen(
                    viewModel = viewModel,
                    onLanguageConfirmed = {
                        navigateToMain()
                    }
                )
            }
        }
    }
    
    /**
     * Navigate to MainActivity after language is confirmed
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close language activity
    }
    
    /**
     * Disable back button during language selection
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - prevent user from going back
    }
}

