package com.truonganim.admob.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.truonganim.admob.R

/**
 * Onboarding Page Data
 * Represents a single page in the onboarding flow
 */
data class OnboardingPage(
    @StringRes val titleRes: Int,
    @DrawableRes val imageRes: Int,
    val hozizontalPadding: Dp = 0.dp
)

/**
 * Onboarding Pages
 * Contains all onboarding pages
 */
object OnboardingPages {
    val pages = listOf(
        OnboardingPage(
            titleRes = R.string.onboarding_page_1_title,
            imageRes = R.drawable.onboarding_1,
            hozizontalPadding = 88.dp
        ),
        OnboardingPage(
            titleRes = R.string.onboarding_page_2_title,
            imageRes = R.drawable.onboarding_2,
            hozizontalPadding = 54.dp
        ),
        OnboardingPage(
            titleRes = R.string.onboarding_page_3_title,
            imageRes = R.drawable.onboarding_3,
            hozizontalPadding = 74.dp
        ),
        OnboardingPage(
            titleRes = R.string.onboarding_page_4_title,
            imageRes = R.drawable.onboarding_4,
            hozizontalPadding = 96.dp
        )
    )
}

