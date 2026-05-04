package com.devseok.dbnow

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devseok.dbnow.ui.main.MainDashboardScreen
import com.devseok.dbnow.ui.search.SearchScreen

@Composable
fun DBNowNavGraph() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_dashboard") {

        // 1. 메인 대시보드 화면
        composable("main_dashboard") {
            MainDashboardScreen(
                onNavigateToSearch = {
                    // 검색 탭/화면으로 이동
                    navController.navigate("search_screen")
                }
            )
        }

        // 2. 검색 화면 (이전에 작성했던 화면)
        composable("search_screen") {
            // SearchScreen 컴포저블 호출
            SearchScreen (
                onBackClick = { navController.popBackStack() },
                onItemClick = {

                }
            )


        }
    }
}