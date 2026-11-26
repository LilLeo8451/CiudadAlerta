package com.example.ciudadalerta.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.viewmodel.ReporteConAutor
import com.example.ciudadalerta.viewmodel.ReportesCiudadViewModel

// Todos los reportes de la ciudad (ciudadano y admin)
@Composable
fun ReportesCiudadScreen(
    usuarioActualId: Int,
    onReporteClick: (Int) -> Unit,
    viewModel: ReportesCiudadViewModel = viewModel(factory = ReportesCiudadViewModel.Factory)
) {
    val reportes by viewModel.reportes.collectAsState()
    val filtro by viewModel.filtroCategoria.collectAsState()
    val sincronizando by viewModel.sincronizando.collectAsState()
    val sinConexion by viewModel.sinConexion.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = stringResource(R.string.title_reportes_ciudad), style = MaterialTheme.typography.headlineMedium)
        Text(
            text = stringResource(R.string.subtitle_reportes_ciudad),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = filtro == null,
                    onClick = { viewModel.seleccionarCategoria(null) },
                    label = { Text(stringResource(R.string.filtro_todos)) }
                )
            }
            items(CategoriaReporte.entries) { categoria ->
                FilterChip(
                    selected = filtro == categoria,
                    onClick = { viewModel.seleccionarCategoria(categoria) },
                    label = { Text(stringResource(categoria.labelRes())) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        AvisoSinConexion(visible = sinConexion, onReintentar = viewModel::sincronizar)

        Text(
            text = stringResource(R.string.label_total_reportes, reportes.size),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        ContenedorRefrescable(
            sincronizando = sincronizando,
            onRefrescar = viewModel::sincronizar,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (reportes.isEmpty()) {
                    item { Text(text = stringResource(R.string.empty_reportes_ciudad)) }
                }
                items(reportes, key = { it.reporte.id }) { item ->
                    ReporteCiudadCard(
                        item = item,
                        esMio = item.reporte.usuarioId == usuarioActualId,
                        onClick = { onReporteClick(item.reporte.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReporteCiudadCard(item: ReporteConAutor, esMio: Boolean, onClick: () -> Unit) {
    val reporte = item.reporte
    val autor = when {
        esMio -> stringResource(R.string.label_autor_tu)
        item.autor != null -> item.autor
        else -> stringResource(R.string.label_autor_ciudadano, reporte.usuarioId)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(reporte.categoria.labelRes()), fontWeight = FontWeight.Bold)
                EstadoBadge(reporte.estatus)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = reporte.descripcion, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = autor,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = stringResource(R.string.label_fecha_hora, reporte.fecha, reporte.hora),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
