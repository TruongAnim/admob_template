package com.truonganim.admob.billing

/**
 * EXAMPLE: How to add Premium button to Settings Screen
 * 
 * This is an example showing how to integrate premium features
 * You can copy this code to SettingsScreen.kt
 */

/*

// Add this import
import com.truonganim.admob.billing.PremiumHelper
import com.truonganim.admob.billing.PremiumPreferencesManager
import androidx.compose.material.icons.filled.WorkspacePremium

// In SettingsScreen composable, add this:
@Composable
fun SettingsScreen(
    onLanguageClick: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Add this to observe premium status
    val premiumPrefs = remember { PremiumPreferencesManager.getInstance(context) }
    val isPremium by premiumPrefs.isPremium.collectAsState()
    
    val uiState by viewModel.uiState.collectAsState()
    
    // ... rest of the code
    
    SettingsContent(
        uiState = uiState,
        isPremium = isPremium, // Pass premium status
        onLanguageClick = onLanguageClick,
        onPremiumClick = { // Add premium click handler
            PremiumHelper.showPremiumScreen(context)
        },
        // ... other handlers
    )
}

// Update SettingsContent to include premium button:
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    isPremium: Boolean, // Add this parameter
    onLanguageClick: () -> Unit,
    onPremiumClick: () -> Unit, // Add this parameter
    onFeedbackClick: () -> Unit,
    onShareAppClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Premium Status / Upgrade Button
        if (isPremium) {
            // Show premium badge
            PremiumBadge()
        } else {
            // Show upgrade button
            UpgradeToPremiumButton(onClick = onPremiumClick)
        }
        
        HorizontalDivider()
        
        // Language
        SettingClickableItem(
            icon = Icons.Default.Language,
            title = "Language",
            onClick = onLanguageClick
        )
        
        // ... rest of the items
    }
}

// Add these new composables:
@Composable
private fun PremiumBadge() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.WorkspacePremium,
            contentDescription = "Premium",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = "Premium Member",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Enjoying all premium features",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun UpgradeToPremiumButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFF1744)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.WorkspacePremium,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "Upgrade to Premium",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

*/

/**
 * EXAMPLE 2: How to hide ads for premium users
 */

/*

// In any screen where you show ads:
@Composable
fun SomeScreen() {
    val context = LocalContext.current
    val premiumPrefs = remember { PremiumPreferencesManager.getInstance(context) }
    val isPremium by premiumPrefs.isPremium.collectAsState()
    
    Column {
        // Your content
        
        // Show ads only for non-premium users
        if (!isPremium) {
            NativeAdView(
                adPosition = NativeAdPosition.HOME_SCREEN
            )
        }
    }
}

*/

/**
 * EXAMPLE 3: How to lock features for non-premium users
 */

/*

@Composable
fun LockedFeatureButton() {
    val context = LocalContext.current
    val premiumPrefs = remember { PremiumPreferencesManager.getInstance(context) }
    val isPremium by premiumPrefs.isPremium.collectAsState()
    
    Button(
        onClick = {
            if (isPremium) {
                // Access premium feature
                accessPremiumFeature()
            } else {
                // Show premium screen
                PremiumHelper.showPremiumScreen(context)
            }
        }
    ) {
        if (isPremium) {
            Text("Access Feature")
        } else {
            Row {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Unlock with Premium")
            }
        }
    }
}

*/

/**
 * EXAMPLE 4: How to check premium status in ViewModel
 */

/*

class MyViewModel(private val context: Context) : ViewModel() {
    
    private val premiumPrefs = PremiumPreferencesManager.getInstance(context)
    
    // Observe premium status
    val isPremium: StateFlow<Boolean> = premiumPrefs.isPremium
    
    // Or check once
    fun checkPremiumAndDoSomething() {
        viewModelScope.launch {
            val isPremium = premiumPrefs.getIsPremiumSync()
            if (isPremium) {
                // Do premium stuff
            } else {
                // Show upgrade prompt
            }
        }
    }
}

*/

/**
 * EXAMPLE 5: How to test premium features (clear premium data)
 */

/*

// Add this to your debug menu or settings
@Composable
fun DebugMenu() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Button(
        onClick = {
            scope.launch {
                PremiumHelper.clearPremiumData(context)
                Toast.makeText(context, "Premium data cleared", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        Text("Clear Premium Data (Debug)")
    }
}

*/

