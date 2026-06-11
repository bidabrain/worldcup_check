package com.worldcup2026.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.worldcup2026.R
import com.worldcup2026.data.repository.MatchRepository
import com.worldcup2026.ui.knockout.KnockoutScreen
import com.worldcup2026.ui.knockout.KnockoutViewModel
import com.worldcup2026.ui.matchdetail.MatchDetailScreen
import com.worldcup2026.ui.matchdetail.MatchDetailViewModel
import com.worldcup2026.ui.schedule.ScheduleScreen
import com.worldcup2026.ui.schedule.ScheduleViewModel
import com.worldcup2026.ui.standings.StandingsScreen
import com.worldcup2026.ui.standings.StandingsViewModel
import com.worldcup2026.ui.support.SupportScreen
import com.worldcup2026.ui.theme.WcBlue
import com.worldcup2026.ui.theme.WcGold

private sealed class Screen(val route: String) {
    object Schedule : Screen("schedule")
    object Standings : Screen("standings")
    object Knockout : Screen("knockout")
    object Support : Screen("support")
    object MatchDetail : Screen("match/{matchId}") {
        fun createRoute(matchId: Int) = "match/$matchId"
    }
}

private data class BottomTab(
    val screen: Screen,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomTabs = listOf(
    BottomTab(Screen.Schedule,  R.string.tab_schedule,  Icons.Filled.Home,         Icons.Outlined.Home),
    BottomTab(Screen.Standings, R.string.tab_standings, Icons.Filled.EmojiEvents,  Icons.Outlined.EmojiEvents),
    BottomTab(Screen.Knockout,  R.string.tab_knockout,  Icons.Filled.SportsSoccer, Icons.Outlined.SportsSoccer),
)

@Composable
fun AppNavigation(repository: MatchRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomTabs.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.screen.route } == true
    }

    val onSupportClick: () -> Unit = { navController.navigate(Screen.Support.route) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = WcBlue,
                    contentColor = WcGold
                ) {
                    bottomTabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == tab.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = stringResource(tab.labelRes)
                                )
                            },
                            label = { Text(stringResource(tab.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = WcGold,
                                selectedTextColor = WcGold,
                                unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                indicatorColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Schedule.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Schedule.route) {
                val vm: ScheduleViewModel = viewModel(factory = ScheduleViewModel.factory(repository))
                ScheduleScreen(
                    viewModel = vm,
                    onMatchClick = { matchId -> navController.navigate(Screen.MatchDetail.createRoute(matchId)) },
                    onSupportClick = onSupportClick
                )
            }

            composable(Screen.Standings.route) {
                val vm: StandingsViewModel = viewModel(factory = StandingsViewModel.factory(repository))
                StandingsScreen(viewModel = vm, onSupportClick = onSupportClick)
            }

            composable(Screen.Knockout.route) {
                val vm: KnockoutViewModel = viewModel(factory = KnockoutViewModel.factory(repository))
                KnockoutScreen(
                    viewModel = vm,
                    onMatchClick = { matchId -> navController.navigate(Screen.MatchDetail.createRoute(matchId)) },
                    onSupportClick = onSupportClick
                )
            }

            composable(Screen.Support.route) {
                SupportScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.MatchDetail.route,
                arguments = listOf(navArgument("matchId") { type = NavType.IntType })
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments!!.getInt("matchId")
                val vm: MatchDetailViewModel = viewModel(
                    key = "match_$matchId",
                    factory = MatchDetailViewModel.factory(matchId, repository)
                )
                MatchDetailScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }
        }
    }
}
