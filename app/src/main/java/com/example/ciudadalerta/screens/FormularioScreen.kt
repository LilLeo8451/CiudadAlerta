package com.example.ciudadalerta.screens // Ajusta a tu paquete real

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ciudadalerta.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(viewModel: ReportViewModel = viewModel()) {
    val categoria by viewModel.categoria.collectAsState()
    val descripcion by viewModel.descripcion.collectAsState()
    val ubicacionConfirmada by viewModel.ubicacionConfirmada.collectAsState()
    val hora by viewModel.hora.collectAsState()

    val opcionesCategoria = listOf("Bache", "Alumbrado", "Basura")

    var mostrarReloj by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Nuevo Reporte", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Tipo de problema:")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            opcionesCategoria.forEach { opcion ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (categoria == opcion),
                        onClick = { viewModel.categoria.value = opcion }
                    )
                    Text(text = opcion)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { viewModel.descripcion.value = it },
            label = { Text("Descripción (Ej. Bache profundo)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Hora de avistamiento: $hora")
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { mostrarReloj = true }) {
                Text("Elegir Hora")
            }
        }

        if (mostrarReloj) {
            AlertDialog(
                onDismissRequest = { mostrarReloj = false },
                confirmButton = {
                    TextButton(onClick = {
                        val horaFormateada = "${timePickerState.hour}:${timePickerState.minute.toString().padStart(2, '0')}"
                        viewModel.hora.value = horaFormateada
                        mostrarReloj = false
                    }) { Text("Aceptar") }
                },
                text = { TimePicker(state = timePickerState) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = ubicacionConfirmada,
                onCheckedChange = { viewModel.ubicacionConfirmada.value = it }
            )
            Text("Confirmar que estoy en el lugar del incidente")
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.guardarReporte() },
            modifier = Modifier.fillMaxWidth(),
            enabled = descripcion.isNotBlank() && ubicacionConfirmada
        ) {
            Text("Guardar Reporte")
        }
    }
}