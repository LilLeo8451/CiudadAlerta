package com.example.ciudadalerta.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ciudadalerta.R
import com.example.ciudadalerta.viewmodel.ReportViewModel

@Composable
fun ListaScreen(
    usuarioId: Int,
    onReporteClick: (Int) -> Unit,
    reporteSeleccionado: Int? = null,
    viewModel: ReportViewModel = viewModel(factory = ReportViewModel.Factory)
) {
    val todos by viewModel.reportes.collectAsState()
    val sincronizando by viewModel.sincronizando.collectAsState()
    val sinConexion by viewModel.sinConexion.collectAsState()
    val mios = remember(todos, usuarioId) {
        todos.filter { it.usuarioId == usuarioId }.sortedByDescending { it.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = stringResource(R.string.title_my_reports), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        AvisoSinConexion(visible = sinConexion, onReintentar = viewModel::sincronizar)

        ContenedorRefrescable(
            sincronizando = sincronizando,
            onRefrescar = viewModel::sincronizar,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (mios.isEmpty()) {
                    item { Text(text = stringResource(R.string.empty_mis_reportes)) }
                }
                items(mios, key = { it.id }) { reporte ->
                    ReporteCard(
                        reporte = reporte,
                        onClick = { onReporteClick(reporte.id) },
                        seleccionado = reporte.id == reporteSeleccionado
                    )
                }
            }
        }
    }
}
