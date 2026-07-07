package com.wolfscowl.vibronumber.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

@Composable

fun NavigationWrapper(
    navController: NavHostController,
    currentRoute: String?,
    content: @Composable () -> Unit
) {
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            Screen.entries.forEach { screen ->
                item(
                    icon = { Icon(screen.icon, contentDescription = screen.label) },
                    label = { Text(screen.label) },
                    selected = currentRoute == screen.route, // <-- Einfach vergleichen
                    onClick = {
                        // Pop up to the start destination to avoid building up a large stack
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination in stack when reselecting the same menu item
                            launchSingleTop = true
                            // Restores UI state (e.g. scroll position) when returning to this screen via menu or back navigation
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        content()
    }
}