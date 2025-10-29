package com.example.lab10_maxi.view

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lab10_maxi.data.SerieApiService
import com.example.lab10_maxi.data.SerieModel

@Composable
fun ContenidoSeriesListado(navController: NavHostController, servicio: SerieApiService) {
    val listaSeries: SnapshotStateList<SerieModel> = remember { mutableStateListOf() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val listado = servicio.selectSeries()
            listaSeries.clear()
            listado.forEach { listaSeries.add(it) }
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
                Text("Accion", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f))
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
                        Log.e("SERIE-VER", "ID = ${item.id}")
                    },
                    modifier = Modifier.weight(0.15f)
                ) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Ver")
                }
                IconButton(
                    onClick = {
                        navController.navigate("serieDel/${item.id}")
                        Log.e("SERIE-DEL", "ID = ${item.id}")
                    },
                    modifier = Modifier.weight(0.15f)
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

@Composable
fun ContenidoSerieEditar(navController: NavHostController, servicio: SerieApiService, pid: Int = 0) {
    val context = LocalContext.current
    var id = pid
    var name by remember { mutableStateOf<String?>("") }
    var release_date by remember { mutableStateOf<String?>("") }
    var rating by remember { mutableStateOf<String?>("") }
    var category by remember { mutableStateOf<String?>("") }
    var grabar by remember { mutableStateOf(false) }

    if (id != 0) {
        LaunchedEffect(id) {
            val objSerie = servicio.selectSerie(id.toString())
            name = objSerie.body()?.name ?: ""
            release_date = objSerie.body()?.release_date ?: ""
            rating = objSerie.body()?.rating?.toString() ?: "0"
            category = objSerie.body()?.category ?: ""
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(value = id.toString(), onValueChange = {}, label = { Text("ID (solo lectura)") }, readOnly = true)
        TextField(value = name ?: "", onValueChange = { name = it }, label = { Text("Name:") })
        TextField(value = release_date ?: "", onValueChange = { release_date = it }, label = { Text("Release Date:") })
        TextField(value = rating ?: "", onValueChange = { rating = it }, label = { Text("Rating:") })
        TextField(value = category ?: "", onValueChange = { category = it }, label = { Text("Category:") })
        Button(onClick = {
            if ((name ?: "").isBlank() || (release_date ?: "").isBlank() || (rating ?: "").isBlank() || (category ?: "").isBlank()) {
                Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                grabar = true
            }
        }) { Text("Grabar", fontSize = 16.sp) }
    }

    if (grabar) {
        val safeRating = try { (rating ?: "0").toInt() } catch (e: Exception) { 0 }
        val objSerie = SerieModel(id, name ?: "", release_date ?: "", safeRating, category ?: "")
        LaunchedEffect(objSerie) {
            try {
                if (id == 0)
                    servicio.insertSerie(objSerie)
                else
                    servicio.updateSerie(id.toString(), objSerie)
                // limpiar campos después de guardar
                name = ""
                release_date = ""
                rating = ""
                category = ""
            } catch (e: Exception) {
                Log.e("SERIES_APP", "Error guardando: ${e.message}")
            }
            navController.navigate("series")
        }
        grabar = false
    }
}

@Composable
fun ContenidoSerieEliminar(navController: NavHostController, servicio: SerieApiService, id: Int) {
    var showDialog = true
    var borrar = false

    if (showDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Está seguro de eliminar la Serie?") },
            confirmButton = {
                Button(onClick = { showDialog = false; borrar = true }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false; navController.navigate("series") }) {
                    Text("Cancelar")
                }
            }
        )
    }
    if (borrar) {
        LaunchedEffect(Unit) {
            try {
                servicio.deleteSerie(id.toString())
                Log.i("SERIES_APP", "Serie eliminada ID: $id")
            } catch (e: Exception) {
                Log.e("SERIES_APP", "Error al eliminar: ${e.message}")
            }
            navController.navigate("series")
        }
        borrar = false
    }
}
