package com.example.ciudadalerta.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ciudadalerta.R
import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(usuarioId: Int, viewModel: ReportViewModel = viewModel(factory = ReportViewModel.Factory)) {
    val categoria by viewModel.categoria.collectAsState()
    val descripcion by viewModel.descripcion.collectAsState()
    val ubicacionConfirmada by viewModel.ubicacionConfirmada.collectAsState()
    val hora by viewModel.hora.collectAsState()

    val context = LocalContext.current
    val mensajeGuardado = stringResource(R.string.msg_reporte_guardado)

    var mostrarReloj by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = stringResource(R.string.title_new_report), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(R.string.label_tipo_problema))
        CategoriaReporte.entries.forEach { opcion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = categoria == opcion,
                        onClick = { viewModel.categoria.value = opcion },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = categoria == opcion, onClick = null)
                Text(
                    text = stringResource(opcion.labelRes()),
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { viewModel.descripcion.value = it },
            label = { Text(stringResource(R.string.label_descripcion)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.label_hora_avistamiento, hora),
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { mostrarReloj = true }) {
                Text(stringResource(R.string.btn_elegir_hora))
            }
        }

        if (mostrarReloj) {
            AlertDialog(
                onDismissRequest = { mostrarReloj = false },
                confirmButton = {
                    TextButton(onClick = {
                        val h = timePickerState.hour.toString().padStart(2, '0')
                        val m = timePickerState.minute.toString().padStart(2, '0')
                        viewModel.hora.value = "$h:$m"
                        mostrarReloj = false
                    }) { Text(stringResource(R.string.btn_aceptar)) }
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
            Text(stringResource(R.string.label_confirmar_ubicacion))
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.guardarReporte(usuarioId)
                Toast.makeText(context, mensajeGuardado, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = descripcion.isNotBlank() && ubicacionConfirmada
        ) {
            Text(stringResource(R.string.btn_save))
        }
    }
}
