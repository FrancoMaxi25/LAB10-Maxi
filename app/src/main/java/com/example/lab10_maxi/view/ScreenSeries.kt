package com.example.lab10_maxi.view

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lab10_maxi.data.SerieApiService
import com.example.lab10_maxi.data.SerieModel

// 🔹 LISTADO DE SERIES
@Composable
fun ContenidoSeriesListado(navController: NavHostController, servicio: SerieApiService) {
    val listaSeries: SnapshotStateList<SerieModel> = remember { mutableStateListOf() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val listado = servicio.selectSeries()
            listaSeries.clear()
            listaSeries.addAll(listado)
        } catch (e: Exception) {
            Log.e("SERIES_APP", "Error al obtener series: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ID", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Text("SERIE", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                Text("Acción", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f))
            }
        }

        items(listaSeries) { item ->
            Row(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${item.id}", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Text(item.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                IconButton(
                    onClick = {
                        navController.navigate("serieVer/${item.id}")
                        Log.i("SERIES_APP", "Ver serie ID: ${item.id}")
                    },
                    modifier = Modifier.weight(0.15f)
                ) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Editar")
                }
                IconButton(
                    onClick = {
                        navController.navigate("serieDel/${item.id}")
                        Log.i("SERIES_APP", "Eliminar serie ID: ${item.id}")
                    },
                    modifier = Modifier.weight(0.15f)
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

// 🔹 EDITAR O CREAR SERIE
@Composable
fun ContenidoSerieEditar(navController: NavHostController, servicio: SerieApiService, pid: Int = 0) {
    val context = LocalContext.current
    var id = pid
    var name by remember { mutableStateOf("") }
    var release_date by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var grabar by remember { mutableStateOf(false) }

    if (id != 0) {
        LaunchedEffect(id) {
            try {
                val objSerie = servicio.selectSerie(id.toString())
                name = objSerie.body()?.name ?: ""
                release_date = objSerie.body()?.release_date ?: ""
                rating = objSerie.body()?.rating?.toString() ?: "0"
                category = objSerie.body()?.category ?: ""
            } catch (e: Exception) {
                Log.e("SERIES_APP", "Error al cargar serie: ${e.message}")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(value = id.toString(), onValueChange = {}, label = { Text("ID (solo lectura)") }, readOnly = true)
        TextField(value = name, onValueChange = { name = it }, label = { Text("Name:") })
        TextField(value = release_date, onValueChange = { release_date = it }, label = { Text("Release Date:") })
        TextField(value = rating, onValueChange = { rating = it }, label = { Text("Rating:") })
        TextField(value = category, onValueChange = { category = it }, label = { Text("Category:") })

        Button(
            onClick = {
                if (name.isBlank() || release_date.isBlank() || rating.isBlank() || category.isBlank()) {
                    Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                } else {
                    grabar = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text("Guardar Cambios", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    if (grabar) {
        val safeRating = rating.toIntOrNull() ?: 0
        val objSerie = SerieModel(id, name, release_date, safeRating, category)
        val contextRef = context.applicationContext

        // ✅ Llamadas API + Toast desde helper function
        LaunchedEffect(objSerie) {
            try {
                if (id == 0) {
                    servicio.insertSerie(objSerie)
                    showToast(contextRef, "Serie registrada correctamente")
                } else {
                    servicio.updateSerie(id.toString(), objSerie)
                    showToast(contextRef, "Serie actualizada correctamente")
                }
                navController.navigate("series")
            } catch (e: Exception) {
                showToast(contextRef, "Error al guardar: ${e.message}")
                Log.e("SERIES_APP", "Error guardando: ${e.message}")
            }
        }

        grabar = false
    }
}

// 🔹 ELIMINAR SERIE
// 🔹 ELIMINAR SERIE (mejorado con detalle y estilo)
@Composable
fun ContenidoSerieEliminar(navController: NavHostController, servicio: SerieApiService, id: Int) {
    var showDialog by remember { mutableStateOf(true) }
    var borrar by remember { mutableStateOf(false) }
    var serie by remember { mutableStateOf<SerieModel?>(null) }
    val context = LocalContext.current.applicationContext

    // 🔸 Cargar detalles de la serie antes de confirmar
    LaunchedEffect(id) {
        try {
            val response = servicio.selectSerie(id.toString())
            serie = response.body()
        } catch (e: Exception) {
            Log.e("SERIES_APP", "Error cargando serie: ${e.message}")
            showToast(context, "Error cargando serie")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Confirmar Eliminación",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    if (serie != null) {
                        Text("¿Seguro que desea eliminar esta serie?", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "ID: ${serie!!.id}\nTítulo: ${serie!!.name}",
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = Color(0xFF424242)
                        )
                    } else {
                        Text("Cargando detalles...", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    onClick = {
                        showDialog = false
                        borrar = true
                    }
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575)),
                    onClick = {
                        showDialog = false
                        navController.navigate("series")
                    }
                ) {
                    Text("Cancelar", color = Color.White)
                }
            }
        )
    }

    // 🔸 Ejecutar eliminación con retroalimentación visual
    if (borrar) {
        LaunchedEffect(Unit) {
            try {
                servicio.deleteSerie(id.toString())
                showToast(context, "Serie eliminada: ${serie?.name ?: id}")
            } catch (e: Exception) {
                showToast(context, "Error al eliminar: ${e.message}")
                Log.e("SERIES_APP", "Error al eliminar: ${e.message}")
            }
            navController.navigate("series")
        }
        borrar = false
    }
}


// ✅ Helper seguro para mostrar Toasts fuera del contexto composable
fun showToast(context: android.content.Context, message: String) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
