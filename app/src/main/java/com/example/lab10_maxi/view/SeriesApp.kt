package com.example.lab10_maxi.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab10_maxi.data.SerieApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// ✅ Composable principal de la App
@Composable
fun SeriesApp() {
    // Usa 10.0.2.2 si estás en emulador, o tu IP local si estás en dispositivo físico
    val urlBase = "http://10.0.2.2:8000/"

    val retrofit = Retrofit.Builder()
        .baseUrl(urlBase)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val servicio = retrofit.create(SerieApiService::class.java)
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { BarraSuperior() },
        bottomBar = { BarraInferior(navController) },
        floatingActionButton = { BotonFAB(navController) }
    ) { paddingValues ->
        Contenido(paddingValues, navController, servicio)
    }
}

// ✅ Floating Action Button (agregar nueva serie)
@Composable
fun BotonFAB(navController: NavHostController) {
    val cbeState by navController.currentBackStackEntryAsState()
    val rutaActual = cbeState?.destination?.route
    if (rutaActual == "series") {
        FloatingActionButton(
            containerColor = Color(0xFF4CAF50),
            contentColor = Color.White,
            onClick = { navController.navigate("serieNuevo") }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar Serie")
        }
    }
}

// ✅ Barra superior de la aplicación
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "📺 SERIES APP",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFF1976D2)
        )
    )
}

// ✅ Barra inferior de navegación
@Composable
fun BarraInferior(navController: NavHostController) {
    val cbeState by navController.currentBackStackEntryAsState()
    val rutaActual = cbeState?.destination?.route

    NavigationBar(containerColor = Color(0xFFF1F1F1)) {
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = rutaActual == "inicio",
            onClick = { navController.navigate("inicio") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Favorite, contentDescription = "Series") },
            label = { Text("Series") },
            selected = rutaActual == "series",
            onClick = { navController.navigate("series") }
        )
    }
}

// ✅ Navegación entre pantallas
@Composable
fun Contenido(pv: PaddingValues, navController: NavHostController, servicio: SerieApiService) {
    Box(modifier = Modifier.fillMaxSize().padding(pv)) {
        NavHost(navController = navController, startDestination = "inicio") {
            composable("inicio") { ScreenInicio() }
            composable("series") { ContenidoSeriesListado(navController, servicio) }
            composable("serieNuevo") { ContenidoSerieEditar(navController, servicio, 0) }
            composable(
                "serieVer/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) {
                ContenidoSerieEditar(navController, servicio, it.arguments!!.getInt("id"))
            }
            composable(
                "serieDel/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) {
                ContenidoSerieEliminar(navController, servicio, it.arguments!!.getInt("id"))
            }
        }
    }
}
