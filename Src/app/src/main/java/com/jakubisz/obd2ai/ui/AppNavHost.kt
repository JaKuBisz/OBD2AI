package com.jakubisz.obd2ai.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jakubisz.obd2ai.ui.connect.ConnectScreen
import com.jakubisz.obd2ai.ui.dashboard.DashboardScreen
import com.jakubisz.obd2ai.ui.dtc.DtcDetailScreen
import com.jakubisz.obd2ai.ui.dtc.DtcScanScreen
import com.jakubisz.obd2ai.ui.history.HistoryScreen
import com.jakubisz.obd2ai.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val CONNECT = "connect"
    const val DASHBOARD = "dashboard?demo={demo}"
    const val DTC_SCAN = "dtc_scan"
    const val DTC_DETAIL = "dtc_detail/{code}"
    const val HISTORY = "history"

    fun dashboard(demo: Boolean = false) = "dashboard?demo=$demo"
    fun dtcDetail(code: String) = "dtc_detail/$code"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onConnect = { navController.navigate(Routes.CONNECT) },
                onDashboard = { navController.navigate(Routes.dashboard()) },
                onScan = { navController.navigate(Routes.DTC_SCAN) },
                onHistory = { navController.navigate(Routes.HISTORY) }
            )
        }
        composable(Routes.CONNECT) {
            ConnectScreen(
                onBack = { navController.popBackStack() },
                onDemo = { navController.navigate(Routes.dashboard(demo = true)) },
                onConnected = { navController.popBackStack() }
            )
        }
        composable(
            Routes.DASHBOARD,
            arguments = listOf(navArgument("demo") { type = NavType.BoolType; defaultValue = false })
        ) { entry ->
            DashboardScreen(
                demo = entry.arguments?.getBoolean("demo") ?: false,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.DTC_SCAN) {
            DtcScanScreen(
                onBack = { navController.popBackStack() },
                onOpenDetail = { code -> navController.navigate(Routes.dtcDetail(code)) },
                onConnect = { navController.navigate(Routes.CONNECT) }
            )
        }
        composable(
            Routes.DTC_DETAIL,
            arguments = listOf(navArgument("code") { type = NavType.StringType })
        ) {
            DtcDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.HISTORY) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}
