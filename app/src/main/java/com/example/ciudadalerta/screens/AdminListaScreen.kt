package com.example.ciudadalerta.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ciudadalerta.R
import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminListaScreen(
    onReporteClick: (Int) -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val reportes by viewModel.reportesFiltrados.collectAsState()
    val filtro by viewModel.filtroCategoria.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = stringResource(R.string.title_admin_reportes), style = MaterialTheme.typography.headlineMedium)
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

        Text(
            text = stringResource(R.string.label_total_reportes, reportes.size),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (reportes.isEmpty()) {
            Text(text = stringResource(R.string.empty_reportes_filtro))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reportes, key = { it.id }) { reporte ->
                    ReporteCard(reporte = reporte, onClick = { onReporteClick(reporte.id) })
                }
            }
        }
    }
}
