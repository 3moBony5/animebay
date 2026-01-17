package io.animebay.stream

import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.animebay.stream.ui.screens.auth.AuthViewModel
import io.animebay.stream.ui.screens.auth.ForgotPasswordScreen
import io.animebay.stream.ui.screens.auth.LoginScreen
import io.animebay.stream.ui.screens.auth.SignUpScreen
import io.animebay.stream.ui.screens.comments.CommentsScreen // ✅ --- الاستيراد الجديد
import io.animebay.stream.ui.screens.details.AnimeDetailsScreen
import io.animebay.stream.ui.screens.details.EpisodesScreen
import io.animebay.stream.ui.screens.favorites.FavoritesScreen
import io.animebay.stream.ui.screens.home.AppDrawer
import io.animebay.stream.ui.screens.home.HomeScreen
import io.animebay.stream.ui.screens.home.viewmodel.HomeViewModel
import io.animebay.stream.ui.screens.news.NewsScreen
import io.animebay.stream.ui.screens.player.PlayerScreen
import io.animebay.stream.ui.screens.profile.ProfileScreen
import io.animebay.stream.ui.screens.profile.ProfileViewModel
import io.animebay.stream.ui.screens.schedule.ScheduleScreen
import io.animebay.stream.ui.screens.search.SearchScreen
import io.animebay.stream.ui.screens.servers.ServersScreen
import io.animebay.stream.ui.theme.AnimeBayTheme
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ProfileViewModel.configureCloudinary(applicationContext)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            AnimeBayTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1a2634),
                                    Color(0xFF12191f),
                                    Color(0xFF0a0f14)
                                )
                            )
                        )
                ) {
                    AppNavigation(homeViewModel = homeViewModel)
                }
            }
        }
    }
}

private fun String.toBase64(): String {
    return Base64.encodeToString(this.toByteArray(StandardCharsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)
}

private fun String.fromBase64(): String {
    return try {
        String(Base64.decode(this, Base64.URL_SAFE or Base64.NO_WRAP), StandardCharsets.UTF_8)
    } catch (e: Exception) {
        this
    }
}


@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel(),
    homeViewModel: HomeViewModel
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val uiState by authViewModel.uiState.collectAsState()

    Scaffold(
        scaffoldState = scaffoldState,
        backgroundColor = Color.Transparent,
        drawerContent = {
            Box(modifier = Modifier.width(280.dp)) {
                AppDrawer(
                    isUserLoggedIn = uiState.isLoggedIn,
                    userEmail = uiState.userEmail,
                    onNavigate = { route ->
                        if ((route == "favorites" || route == "profile") && !uiState.isLoggedIn) {
                            navController.navigate("login")
                        } else {
                            navController.navigate(route) {
                                if (route == "home") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        }
                        scope.launch { scaffoldState.drawerState.close() }
                    },
                    onSignOut = {
                        authViewModel.signOut()
                        scope.launch { scaffoldState.drawerState.close() }
                    }
                )
            }
        },
        drawerGesturesEnabled = scaffoldState.drawerState.isOpen,
        drawerBackgroundColor = Color.Transparent,
        drawerScrimColor = Color.Black.copy(alpha = 0.6f)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    homeViewModel = homeViewModel,
                    onAnimeClick = { animeUrl ->
                        val encodedUrl = URLEncoder.encode(animeUrl, StandardCharsets.UTF_8.toString())
                        navController.navigate("details/$encodedUrl")
                    },
                    onMenuClick = {
                        scope.launch { scaffoldState.drawerState.open() }
                    },
                    onSearchClick = {
                        navController.navigate("search")
                    }
                )
            }
            
            composable("news") {
                NewsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("schedule") {
                ScheduleScreen(
                    onAnimeClick = { animeUrl ->
                        val encodedUrl = URLEncoder.encode(animeUrl, StandardCharsets.UTF_8.toString())
                        navController.navigate("details/$encodedUrl")
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("search") {
                SearchScreen(
                    onAnimeClick = { animeUrl ->
                        val encodedUrl = URLEncoder.encode(animeUrl, StandardCharsets.UTF_8.toString())
                        navController.navigate("details/$encodedUrl")
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("favorites") {
                FavoritesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onAnimeClick = { animeUrl ->
                        val encodedUrl = URLEncoder.encode(animeUrl, StandardCharsets.UTF_8.toString())
                        navController.navigate("details/$encodedUrl")
                    }
                )
            }
            
            composable("profile") {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSignOutClick = {
                        authViewModel.signOut()
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                "details/{animeUrl}",
                arguments = listOf(navArgument("animeUrl") { type = NavType.StringType })
            ) { backStackEntry ->
                val animeUrl = backStackEntry.arguments?.getString("animeUrl")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
                AnimeDetailsScreen(
                    animeUrl = animeUrl,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEpisodes = { animeName, url, type ->
                        val encodedName = URLEncoder.encode(animeName, StandardCharsets.UTF_8.toString())
                        val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                        navController.navigate("episodes/$encodedName/$encodedUrl/$type")
                    },
                    // ✅ --- التعديل الأول ---
                    onNavigateToComments = { url ->
                        if (uiState.isLoggedIn) {
                            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                            navController.navigate("comments/$encodedUrl")
                        } else {
                            navController.navigate("login")
                        }
                    }
                )
            }
            
            // ✅ --- التعديل الثاني: إضافة الشاشة الجديدة ---
            composable(
                "comments/{animeUrl}",
                arguments = listOf(navArgument("animeUrl") { type = NavType.StringType })
            ) { backStackEntry ->
                val animeUrl = backStackEntry.arguments?.getString("animeUrl")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
                CommentsScreen(
                    animeId = animeUrl,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                "episodes/{animeName}/{animeUrl}/{animeType}",
                arguments = listOf(
                    navArgument("animeName") { type = NavType.StringType },
                    navArgument("animeUrl") { type = NavType.StringType },
                    navArgument("animeType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val animeName = backStackEntry.arguments?.getString("animeName")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
                val animeUrl = backStackEntry.arguments?.getString("animeUrl")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
                val animeType = backStackEntry.arguments?.getString("animeType") ?: "TV"
                
                EpisodesScreen(
                    animeName = animeName,
                    animeUrl = animeUrl,
                    animeType = animeType,
                    onEpisodeClick = { episode ->
                        val encodedEpisodeUrl = URLEncoder.encode(episode.episodeUrl, StandardCharsets.UTF_8.toString())
                        val encodedAnimeName = URLEncoder.encode(animeName, StandardCharsets.UTF_8.toString())
                        val episodeNumberToEncode = episode.episodeNumber ?: ""
                        val encodedEpisodeNumber = URLEncoder.encode(episodeNumberToEncode, StandardCharsets.UTF_8.toString())
                        navController.navigate("servers/$encodedAnimeName/$encodedEpisodeNumber/$encodedEpisodeUrl")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                "servers/{animeName}/{episodeNumber}/{episodeUrl}",
                arguments = listOf(
                    navArgument("animeName") { type = NavType.StringType },
                    navArgument("episodeNumber") { type = NavType.StringType },
                    navArgument("episodeUrl") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val animeName = backStackEntry.arguments?.getString("animeName")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
                val episodeNumber = backStackEntry.arguments?.getString("episodeNumber")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
                val episodeUrl = backStackEntry.arguments?.getString("episodeUrl")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
                ServersScreen(
                    episodeUrl = episodeUrl,
                    animeName = animeName,
                    episodeNumber = episodeNumber,
                    onNavigateBack = { navController.popBackStack() },
                    onServerClick = { server ->
                        val encodedAnimeName = URLEncoder.encode(animeName, StandardCharsets.UTF_8.toString())
                        val encodedEpisodeNumber = URLEncoder.encode(episodeNumber, StandardCharsets.UTF_8.toString())
                        val encodedEmbedUrl = server.embedUrl.toBase64()
                        navController.navigate("player/$encodedAnimeName/$encodedEpisodeNumber/$encodedEmbedUrl")
                    }
                )
            }
            composable(
                "player/{animeName}/{episodeNumber}/{embedUrl}",
                arguments = listOf(
                    navArgument("animeName") { type = NavType.StringType },
                    navArgument("episodeNumber") { type = NavType.StringType },
                    navArgument("embedUrl") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val animeName = backStackEntry.arguments?.getString("animeName")?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
                val episodeNumber = backStackEntry.arguments?.getString("episodeNumber") ?: ""
                val embedUrl = backStackEntry.arguments?.getString("embedUrl")?.fromBase64() ?: ""
                PlayerScreen(
                    embedUrl = embedUrl,
                    animeName = animeName,
                    episodeNumber = episodeNumber,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("login") {
                LoginScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    },
                    onSignUpClick = {
                        navController.navigate("signup")
                    },
                    onForgotPasswordClick = {
                        navController.navigate("forgot_password")
                    }
                )
            }

            composable("signup") {
                SignUpScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSignUpSuccess = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    },
                    onLoginClick = {
                        navController.navigate("login") {
                            popUpTo("signup") { inclusive = true }
                        }
                    }
                )
            }

            composable("forgot_password") {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
