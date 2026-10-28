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
    viewModel: ReportViewModel = viewModel(factory = ReportViewModel.Factory)
) {
    val todos by viewModel.reportes.collectAsState()
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

        if (mios.isEmpty()) {
            Text(text = stringResource(R.string.empty_mis_reportes))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(mios, key = { it.id }) { reporte ->
                    ReporteCard(reporte = reporte, onClick = { onReporteClick(reporte.id) })
                }
            }
        }
    }
}
