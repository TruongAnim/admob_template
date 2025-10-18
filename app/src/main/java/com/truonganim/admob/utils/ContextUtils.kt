package com.truonganim.admob.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Find Activity from Context
 */
fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("No Activity found in Context")
}

