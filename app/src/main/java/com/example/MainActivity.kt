package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.configureAppCheck
import com.example.data.StudioPreferencesRepository
import com.example.ui.AiChatScreen
import com.example.ui.AiImageScreen
import com.example.ui.StickManStudioScreen
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.theme.DeepSpaceMid
import com.example.ui.theme.StickManStudioTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.AiChatViewModel
import com.example.viewmodel.AiImageViewModel
import com.example.viewmodel.StickManViewModel

private enum class AppDestination(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
) {
    STUDIO(R.string.nav_studio, Icons.Filled.Animation, Icons.Outlined.Animation, "nav_studio"),
    AI_ART(R.string.nav_ai_art, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_ai_art"),
    AI_CHAT(R.string.nav_ai_chat, Icons.Filled.Forum, Icons.Outlined.Forum, "nav_ai_chat"),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: Configure Firebase App Check with launch intent before any UI or ViewModels.
        configureAppCheck(this, intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        val prefsRepository = StudioPreferencesRepository(applicationContext)
        setContent {
            StickManStudioTheme {
                StickManRootApp(prefsRepository = prefsRepository)
            }
        }
    }
}

@Composable
private fun StickManRootApp(prefsRepository: StudioPreferencesRepository) {
    val studioViewModel: StickManViewModel = viewModel(
        factory = StickManViewModel.Factory(prefsRepository),
    )
    val aiImageViewModel: AiImageViewModel = viewModel()
    val aiChatViewModel: AiChatViewModel = viewModel()

    val studioState by studioViewModel.uiState.collectAsStateWithLifecycle()
    val imageState by aiImageViewModel.uiState.collectAsStateWithLifecycle()
    val chatState by aiChatViewModel.uiState.collectAsStateWithLifecycle()

    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.STUDIO) }
    val accent = studioState.characterStyle.glowTheme.primaryColor

    LiquidGlassBackground {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 680.dp

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(
                            containerColor = DeepSpaceMid.copy(alpha = 0.88f),
                            windowInsets = WindowInsets.navigationBars,
                        ) {
                            AppDestination.entries.forEach { dest ->
                                val selected = dest == currentDestination
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentDestination = dest },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = stringResource(dest.labelRes),
                                        )
                                    },
                                    label = { Text(stringResource(dest.labelRes)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = accent,
                                        selectedTextColor = TextPrimary,
                                        indicatorColor = accent.copy(alpha = 0.22f),
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted,
                                    ),
                                    modifier = Modifier.testTag(dest.testTag),
                                )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    if (useRail) {
                        NavigationRail(
                            containerColor = DeepSpaceMid.copy(alpha = 0.82f),
                        ) {
                            AppDestination.entries.forEach { dest ->
                                val selected = dest == currentDestination
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { currentDestination = dest },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = stringResource(dest.labelRes),
                                        )
                                    },
                                    label = { Text(stringResource(dest.labelRes)) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = accent,
                                        selectedTextColor = TextPrimary,
                                        indicatorColor = accent.copy(alpha = 0.22f),
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted,
                                    ),
                                    modifier = Modifier.testTag(dest.testTag),
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (currentDestination) {
                            AppDestination.STUDIO -> StickManStudioScreen(
                                uiState = studioState,
                                viewModel = studioViewModel,
                            )
                            AppDestination.AI_ART -> AiImageScreen(
                                state = imageState,
                                viewModel = aiImageViewModel,
                                currentPose = studioState.displayPose,
                                characterStyle = studioState.characterStyle,
                                onApplyBackdrop = { bmp ->
                                    studioViewModel.setStageBackdrop(bmp)
                                    currentDestination = AppDestination.STUDIO
                                },
                            )
                            AppDestination.AI_CHAT -> AiChatScreen(
                                state = chatState,
                                viewModel = aiChatViewModel,
                                accentColor = accent,
                            )
                        }
                    }
                }
            }
        }
    }
}
