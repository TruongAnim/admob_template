package com.truonganim.admob.ui.base

import android.content.Context
import androidx.activity.ComponentActivity
import com.truonganim.admob.utils.LocaleHelper

/**
 * Base Activity for all activities in the app
 * Handles locale wrapping to ensure correct language is applied
 */
abstract class BaseActivity : ComponentActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase))
    }
}

