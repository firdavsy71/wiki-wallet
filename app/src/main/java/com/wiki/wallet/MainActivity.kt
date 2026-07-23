package com.wiki.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wiki.wallet.core.designsystem.theme.WalletColors
import com.wiki.wallet.core.designsystem.theme.WikiWalletTheme
import com.wiki.wallet.feature.categories.CategoriesRoute
import com.wiki.wallet.feature.categories.CategoriesViewModel
import com.wiki.wallet.feature.dashboard.DashboardRoute
import com.wiki.wallet.feature.dashboard.DashboardViewModel
import com.wiki.wallet.feature.history.HistoryRoute
import com.wiki.wallet.feature.history.HistoryViewModel
import com.wiki.wallet.feature.settings.SettingsRoute
import com.wiki.wallet.feature.settings.SettingsViewModel
import com.wiki.wallet.feature.swap.SwapRoute
import com.wiki.wallet.feature.swap.SwapViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as WikiWalletApplication).container

        setContent {
            WikiWalletTheme {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = WalletColors.PaperPure
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard"
                    ) {
                        composable(
                            route = "dashboard",
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                )
                            }
                        ) {
                            val viewModel: DashboardViewModel = viewModel(
                                factory = appContainer.dashboardViewModelFactory
                            )
                            DashboardRoute(
                                onNavigateToAddTransaction = {
                                    navController.navigate("add_transaction")
                                },
                                onNavigateToHistory = {
                                    navController.navigate("history")
                                },
                                onNavigateToCategories = {
                                    navController.navigate("categories")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                viewModel = viewModel
                            )
                        }

                        composable(
                            route = "add_transaction",
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                )
                            }
                        ) {
                            val viewModel: SwapViewModel = viewModel(
                                factory = appContainer.swapViewModelFactory
                            )
                            SwapRoute(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                viewModel = viewModel
                            )
                        }

                        composable(
                            route = "history",
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                )
                            }
                        ) {
                            val viewModel: HistoryViewModel = viewModel(
                                factory = appContainer.historyViewModelFactory
                            )
                            HistoryRoute(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                viewModel = viewModel
                            )
                        }

                        composable(
                            route = "categories",
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                )
                            }
                        ) {
                            val viewModel: CategoriesViewModel = viewModel(
                                factory = appContainer.categoriesViewModelFactory
                            )
                            CategoriesRoute(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                viewModel = viewModel
                            )
                        }

                        composable(
                            route = "settings",
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                )
                            }
                        ) {
                            val viewModel: SettingsViewModel = viewModel(
                                factory = appContainer.settingsViewModelFactory
                            )
                            SettingsRoute(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
