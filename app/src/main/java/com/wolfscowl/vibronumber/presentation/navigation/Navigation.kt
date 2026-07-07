package com.wolfscowl.vibronumber.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wolfscowl.vibronumber.presentation.screen.bluettoth_screen.BluetoothScreen
import com.wolfscowl.vibronumber.presentation.screen.home_screen.HomeScreen
import com.wolfscowl.vibronumber.presentation.screen.settings_screen.SettingsScreen


@Composable
fun Navigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.entries.first().route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
//        enterTransition = {
//            fadeIn(animationSpec = tween(700), initialAlpha = 0.1f)
//        },
//        exitTransition = {
//            fadeOut(animationSpec = tween(700), targetAlpha = 0.1f)
//        },
//        popEnterTransition = {
//            fadeIn(animationSpec = tween(700), initialAlpha = 0.1f)
//        },
//        popExitTransition = {
//            fadeOut(animationSpec = tween(700), targetAlpha = 0.1f)
//        }
    ) {
        composable(route = Screen.HOME.route) {
            HomeScreen()
        }
        composable(route = Screen.BLUETOOTH.route) {
            BluetoothScreen()
        }
        composable(route = Screen.SETTINGS.route) {
            SettingsScreen()
        }
    }
}

//fun NavHostController.navigateSingleTopTo(route: String) =
//    this.navigate(route) {
//        // Pop up to the start destination of the graph to
//        // avoid building up a large stack of destinations
//        // on the back stack as users select items
//        popUpTo(
//            this@navigateSingleTopTo.graph.findStartDestination().id
//        ) {
//            saveState = true
//        }
//        // Avoid multiple copies of the same destination when
//        // reselecting the same item
//        launchSingleTop = true
//        // Restore state when reselecting a previously selected item
//        restoreState = true
//    }