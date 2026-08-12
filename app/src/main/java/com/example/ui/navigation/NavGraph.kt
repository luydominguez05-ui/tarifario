package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.CommissionsScreen
import com.example.ui.screens.CompareScreen
import com.example.ui.screens.FeasibilityScreen
import com.example.ui.screens.QuotesHistoryScreen
import com.example.ui.viewmodel.NetTarifaViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Catalog : Screen("catalog", "Catálogo", Icons.Default.Speed)
    object Calculator : Screen("calculator", "Cotizador", Icons.Default.Calculate)
    object Compare : Screen("compare", "Comparar", Icons.Default.Compare)
    object History : Screen("history", "Cotizaciones", Icons.Default.History)
    object Commissions : Screen("commissions", "Comisiones", Icons.Default.Payments)
    object Feasibility : Screen("feasibility", "Factibilidad", Icons.Default.Map)
}

val bottomNavItems = listOf(
    Screen.Catalog,
    Screen.Calculator,
    Screen.Compare,
    Screen.History,
    Screen.Commissions,
    Screen.Feasibility
)

@Composable
fun MainNavHost(viewModel: NetTarifaViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Catalog.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Catalog.route) {
                CatalogScreen(
                    viewModel = viewModel,
                    onNavigateToCalculator = {
                        navController.navigate(Screen.Calculator.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToCompare = {
                        navController.navigate(Screen.Compare.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Calculator.route) {
                CalculatorScreen(
                    viewModel = viewModel,
                    onQuoteSavedSuccess = {
                        navController.navigate(Screen.History.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Compare.route) {
                CompareScreen(
                    viewModel = viewModel,
                    onQuotePlan = {
                        navController.navigate(Screen.Calculator.route) {
                            launchSingleTop = true
                        }
                    },
                    onBackToCatalog = {
                        navController.navigate(Screen.Catalog.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.History.route) {
                QuotesHistoryScreen(
                    viewModel = viewModel
                )
            }

            composable(Screen.Commissions.route) {
                CommissionsScreen(
                    viewModel = viewModel
                )
            }

            composable(Screen.Feasibility.route) {
                FeasibilityScreen(
                    viewModel = viewModel,
                    onQuoteZonePlans = {
                        navController.navigate(Screen.Catalog.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
