
package com.vathos.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vathos.app.WelcomeScreen
import com.vathos.app.ui.screens.ChatScreen
import com.vathos.app.ui.screens.DashboardScreen
import com.vathos.app.ui.screens.DocumentVaultScreen
import com.vathos.app.ui.screens.ModelManagerScreen
import com.vathos.app.ui.screens.SettingsScreen

object VathosRoutes {
    const val WELCOME = "welcome"
    const val HOME = "home"
    const val VAULT = "vault"
    const val CHAT = "chat"
    const val MODEL_MANAGER = "model_manager"
    const val SETTINGS = "settings"
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun VathosNavigation() {

    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(VathosRoutes.HOME, "Home", Icons.Default.Home),
        BottomNavItem(VathosRoutes.VAULT, "Vault", Icons.Default.Description),
        BottomNavItem(VathosRoutes.CHAT, "Chat", Icons.Default.Chat),
        BottomNavItem(VathosRoutes.MODEL_MANAGER, "Models", Icons.Default.Memory),
        BottomNavItem(VathosRoutes.SETTINGS, "Settings", Icons.Default.Settings)
    )

    Scaffold(
        bottomBar = {
            if (currentRoute != VathosRoutes.WELCOME) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(VathosRoutes.HOME) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(item.label)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = VathosRoutes.WELCOME,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(VathosRoutes.WELCOME) {
                WelcomeScreen(
                    onGetStarted = {
                        navController.navigate(VathosRoutes.HOME) {
                            popUpTo(VathosRoutes.WELCOME) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(VathosRoutes.HOME) {
                DashboardScreen(
                    onNavigateToVault = {
                        navController.navigate(VathosRoutes.VAULT)
                    },
                    onNavigateToChat = {
                        navController.navigate(VathosRoutes.CHAT)
                    }
                )
            }

            composable(VathosRoutes.VAULT) {
                DocumentVaultScreen()
            }

            composable(VathosRoutes.CHAT) {
                ChatScreen(
                    onNavigateToModelManager = {
                        navController.navigate(VathosRoutes.MODEL_MANAGER)
                    }
                )
            }

            composable(VathosRoutes.MODEL_MANAGER) {
                ModelManagerScreen()
            }

            composable(VathosRoutes.SETTINGS) {
                SettingsScreen()
            }
        }
    }
}
