package com.devseok.dbnow

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devseok.dbnow.ui.busroutedetail.BusRouteDetailScreen
import com.devseok.dbnow.ui.main.MainDashboardScreen
import com.devseok.dbnow.ui.search.SearchScreen
import com.devseok.dbnow.ui.stationdetail.StationDetailScreen

@Composable
fun DBNowNavGraph() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.MainDashBoard.route) {

        // 1. 메인 대시보드 화면
        composable(Screen.MainDashBoard.route) {
            MainDashboardScreen(
                onNavigateToSearch = {
                    // 검색 탭/화면으로 이동
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        // 2. 검색 화면 (이전에 작성했던 화면)
        composable(Screen.Search.route) {
            // SearchScreen 컴포저블 호출
            SearchScreen (
                onBackClick = { navController.popBackStack() },
                onNavigateToStation = { stationId, name ->
                    navController.navigate(Screen.StationDetail.createRoute(stationId, name))
                },
                onNavigateToBus = { routeId, name ->
                    navController.navigate(Screen.BusRouteDetail.createRoute(routeId, name))
                }
            )
        }

        composable(Screen.StationDetail.route) {
            StationDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.BusRouteDetail.route) {
            BusRouteDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {

    object MainDashBoard : Screen("main_dashboard")

    object Search : Screen("search")

    // 화면 A: 정류소 상세 화면 (stationId와 이름을 파라미터로 받음)
    object StationDetail : Screen("station_detail/{stationId}/{stationName}") {
        fun createRoute(stationId: String, stationName: String) = "station_detail/$stationId/$stationName"
    }

    // 화면 B: 버스 노선 상세 화면 (routeId와 이름을 파라미터로 받음)
    object BusRouteDetail : Screen("bus_route_detail/{routeId}/{routeName}") {
        fun createRoute(routeId: String, routeName: String) = "bus_route_detail/$routeId/$routeName"
    }
}