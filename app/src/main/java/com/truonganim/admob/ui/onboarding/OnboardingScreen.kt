package com.truonganim.admob.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.truonganim.admob.ads.native_ads.NativeAdPosition
import com.truonganim.admob.ads.native_ads.NativeAdView
import com.truonganim.admob.ui.theme.AdMobBaseTheme

/**
 * Onboarding Screen
 * Displays a horizontal pager with onboarding pages
 * Shows dot indicator or "Get Started" button on last page
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onGetStarted: () -> Unit = {}
) {
    val pages by viewModel.pages.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )

    // Update ViewModel when page changes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.updateCurrentPage(page)
        }
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Horizontal Pager - Title and Image
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            OnboardingPageItem(
                page = pages[page],
                modifier = Modifier.fillMaxSize()
            )
        }

        // Dot Indicator or Get Started button
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (currentPage == pages.size - 1) {
                // Last page: Show "Get Started" button

                Text(
                    text = "GET STARTED",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable {
                            println("🚀 Get Started clicked!")
                            onGetStarted()
                        }
                )

            } else {
                // Other pages: Show dot indicator
                DotIndicator(
                    totalDots = pages.size,
                    selectedIndex = currentPage,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Determine which ad position to show
        val adPosition = when (currentPage) {
            0 -> NativeAdPosition.ONBOARDING_PAGE_1  // Page 1 (index 0)
            2 -> NativeAdPosition.ONBOARDING_PAGE_3  // Page 3 (index 2)
            else -> null  // Pages 2 and 4 have no ads
        }

        // Native Ad at the bottom (for pages 1 and 3)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if(adPosition != null) 300.dp else 30.dp),
            contentAlignment = Alignment.Center
        ) {
            if (adPosition != null) {
                NativeAdView(
                    position = adPosition,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // If no ad, just empty space to maintain consistent layout
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingScreenPreview() {
    AdMobBaseTheme {
        OnboardingScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OnboardingScreenDarkPreview() {
    AdMobBaseTheme {
        OnboardingScreen()
    }
}

