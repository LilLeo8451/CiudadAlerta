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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
 * - Ciudadano (esAdmin = false): solo lectura.
 * - Administrador (esAdmin = true): además puede cambiar el estado del reporte.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleReporteScreen(
    reporteId: Int,
    esAdmin: Boolean,
    onVolver: () -> Unit,
    viewModel: ReportViewModel = viewModel()
) {
    val reportes by viewModel.reportes.collectAsState()
    val reporte = reportes.find { it.id == reporteId }

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
                    InfoFila(R.string.label_reportado_por, viewModel.nombreAutor(reporte.usuarioId))
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
