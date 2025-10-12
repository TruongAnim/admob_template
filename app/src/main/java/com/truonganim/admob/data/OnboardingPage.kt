package com.truonganim.admob.data

import androidx.annotation.DrawableRes
import com.truonganim.admob.R

/**
 * Onboarding Page Data
 * Represents a single page in the onboarding flow
 */
data class OnboardingPage(
    val title: String,
    @DrawableRes val imageRes: Int
)

/**
 * Onboarding Pages
 * Contains all onboarding pages
 */
object OnboardingPages {
    val pages = listOf(
        OnboardingPage(
            title = "Welcome to AdMob Base",
            imageRes = R.drawable.onboarding_1
        ),
        OnboardingPage(
            title = "Easy Integration",
            imageRes = R.drawable.onboarding_2
        ),
        OnboardingPage(
            title = "Maximize Revenue",
            imageRes = R.drawable.onboarding_3
        ),
        OnboardingPage(
            title = "Get Started Now",
            imageRes = R.drawable.onboarding_4
        )
    )
}

