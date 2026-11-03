package com.example.ciudadalerta.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ciudadalerta.R
import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.viewmodel.ReportViewModel

/**
 * Detalle de un reporte.
 * - Ciudadano dueño del reporte (mientras esté PENDIENTE): puede editarlo y eliminarlo.
 * - Administrador: puede cambiar el estado y eliminar el reporte.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleReporteScreen(
    reporteId: Int,
    esAdmin: Boolean,
    usuarioActualId: Int,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    viewModel: ReportViewModel = viewModel(factory = ReportViewModel.Factory)
) {
    val reportes by viewModel.reportes.collectAsState()
    val reporte = reportes.find { it.id == reporteId }
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_volver)
                )
            }
            Text(text = stringResource(R.string.title_detalle), style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (reporte == null) {
            Text(text = stringResource(R.string.msg_no_encontrado))
        } else {
            val esDuenoYPendiente = !esAdmin &&
                    reporte.usuarioId == usuarioActualId &&
                    reporte.estatus == EstadoReporte.PENDIENTE
            val puedeEditar = esDuenoYPendiente
            val puedeEliminar = esAdmin || esDuenoYPendiente

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(reporte.categoria.labelRes()),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        EstadoBadge(reporte.estatus)
                    }
                    HorizontalDivider()
                    InfoFila(R.string.label_descripcion_detalle, reporte.descripcion)
                    InfoFila(R.string.label_fecha, reporte.fecha)
                    InfoFila(R.string.label_hora_simple, reporte.hora)
                    InfoFila(
                        R.string.label_ubicacion,
                        stringResource(if (reporte.ubicacionConfirmada) R.string.valor_si else R.string.valor_no)
                    )
                    val autor by produceState(initialValue = "—", reporte.usuarioId) {
                        value = viewModel.nombreAutor(reporte.usuarioId)
                    }
                    InfoFila(R.string.label_reportado_por, autor)
                }
            }

            if (esAdmin) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.label_cambiar_estado),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                val estados = EstadoReporte.entries
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    estados.forEachIndexed { index, estado ->
                        SegmentedButton(
                            selected = reporte.estatus == estado,
                            onClick = { viewModel.cambiarEstado(reporte.id, estado) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = estados.size),
                            icon = {},
                            label = {
                                Text(
                                    text = stringResource(estado.labelRes()),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }

            if (puedeEditar || puedeEliminar) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (puedeEditar) {
                        OutlinedButton(onClick = onEditar, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_editar))
                        }
                    }
                    if (puedeEliminar) {
                        OutlinedButton(
                            onClick = { confirmarEliminar = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_eliminar))
                        }
                    }
                }
            }

            if (confirmarEliminar) {
                AlertDialog(
                    onDismissRequest = { confirmarEliminar = false },
                    title = { Text(stringResource(R.string.dialog_eliminar_titulo)) },
                    text = { Text(stringResource(R.string.dialog_eliminar_mensaje)) },
                    confirmButton = {
                        TextButton(onClick = {
                            confirmarEliminar = false
                            viewModel.eliminarReporte(reporte.id)
                            onVolver()
                        }) { Text(stringResource(R.string.btn_eliminar)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmarEliminar = false }) {
                            Text(stringResource(R.string.btn_cancelar))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun InfoFila(@StringRes etiqueta: Int, valor: String) {
    Column {
        Text(
            text = stringResource(etiqueta),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = valor, style = MaterialTheme.typography.bodyLarge)
    }
}
