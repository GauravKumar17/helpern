package com.example.helpern2.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object ServiceDetail : Screen("service_detail/{serviceId}") {
        fun createRoute(serviceId: String) = "service_detail/$serviceId"
    }
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object About : Screen("about")
    object Bookings : Screen("bookings")
    object Admin : Screen("admin")
    object AdminAddEdit : Screen("admin_add_edit/{serviceId}") {
        fun createRoute(serviceId: String = "new") = "admin_add_edit/$serviceId"
    }
}
