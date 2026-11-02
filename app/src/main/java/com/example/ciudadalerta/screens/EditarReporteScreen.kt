package com.example.ciudadalerta.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.ciudadalerta.model.Reporte
import com.example.ciudadalerta.viewmodel.ReportViewModel

@Composable
fun EditarReporteScreen(
    reporteId: Int,
    onVolver: () -> Unit,
    viewModel: ReportViewModel = viewModel(factory = ReportViewModel.Factory)
) {
    val reportes by viewModel.reportes.collectAsState()
    val reporte = reportes.find { it.id == reporteId }

    val context = LocalContext.current
    val mensajeActualizado = stringResource(R.string.msg_reporte_actualizado)

    if (reporte == null) {
        // Mientras Room entrega el reporte
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        FormularioEdicion(
            reporte = reporte,
            onGuardar = { actualizado ->
                viewModel.actualizarReporte(actualizado)
                Toast.makeText(context, mensajeActualizado, Toast.LENGTH_SHORT).show()
                onVolver()
            },
            onCancelar = onVolver
        )
    }
}

@Composable
private fun FormularioEdicion(
    reporte: Reporte,
    onGuardar: (Reporte) -> Unit,
    onCancelar: () -> Unit
) {
    // Los valores iniciales salen del reporte; rememberSaveable conserva lo escrito al girar la pantalla
    var categoria by rememberSaveable { mutableStateOf(reporte.categoria) }
    var descripcion by rememberSaveable { mutableStateOf(reporte.descripcion) }
    var ubicacionConfirmada by rememberSaveable { mutableStateOf(reporte.ubicacionConfirmada) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancelar) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_volver)
                )
            }
            Text(
                text = stringResource(R.string.title_editar_reporte),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(R.string.label_tipo_problema))
        CategoriaReporte.entries.forEach { opcion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = categoria == opcion,
                        onClick = { categoria = opcion },
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
            onValueChange = { descripcion = it },
            label = { Text(stringResource(R.string.label_descripcion)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = ubicacionConfirmada, onCheckedChange = { ubicacionConfirmada = it })
            Text(stringResource(R.string.label_confirmar_ubicacion))
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onGuardar(
                    reporte.copy(
                        categoria = categoria,
                        descripcion = descripcion.trim(),
                        ubicacionConfirmada = ubicacionConfirmada
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = descripcion.isNotBlank() && ubicacionConfirmada
        ) {
            Text(stringResource(R.string.btn_guardar_cambios))
        }
        TextButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.btn_cancelar))
        }
    }
}