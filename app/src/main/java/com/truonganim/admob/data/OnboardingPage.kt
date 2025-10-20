package com.truonganim.admob.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.truonganim.admob.R

/**
 * Onboarding Page Data
 * Represents a single page in the onboarding flow
 */
data class OnboardingPage(
    val title: String,
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
            title = "The Innocent\n" +
                    "Little Devil Sister",
            imageRes = R.drawable.onboarding_1,
            hozizontalPadding = 88.dp
        ),
        OnboardingPage(
            title = "Practice With\n" +
                    "The Beautiful Swimmer",
            imageRes = R.drawable.onboarding_2,
            hozizontalPadding = 54.dp
        ),
        OnboardingPage(
            title = "From Homeless Girl\n" +
                    "to A Lovely Maid",
            imageRes = R.drawable.onboarding_3,
            hozizontalPadding = 74.dp
        ),
        OnboardingPage(
            title = "A Typical Day\n" +
                    "of A School Girl",
            imageRes = R.drawable.onboarding_4,
            hozizontalPadding = 96.dp
        )
    )
}

