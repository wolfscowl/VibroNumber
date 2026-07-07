package com.wolfscowl.vibronumber.presentation.app

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wolfscowl.vibronumber.presentation.navigation.Navigation
import com.wolfscowl.vibronumber.presentation.navigation.NavigationWrapper


@Composable
fun App() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentNavRoute = navBackStackEntry?.destination?.route

    // ── BACKPRESS HANDLER ────────────────────────────────────────────────────────────────────────
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }
    BackHandler {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        } else {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                (context as? Activity)?.finish()
            } else {
                Toast.makeText(context, "Press again to exit", Toast.LENGTH_SHORT).show()
                backPressedTime = System.currentTimeMillis()
            }
        }
    }


    // ── NAVIGATION & LAYOUT ──────────────────────────────────────────────────────────────────────
    NavigationWrapper(
        navController = navController,
        currentRoute = currentNavRoute
    ) {
        Navigation(navController = navController)
    }
}
