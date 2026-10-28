package com.example.ciudadalerta.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ciudadalerta.R
import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Reporte

/** Etiqueta de color según el estado del reporte. */
@Composable
fun EstadoBadge(estado: EstadoReporte, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.colorScheme
    val (fondo, texto) = when (estado) {
        EstadoReporte.PENDIENTE -> colores.errorContainer to colores.onErrorContainer
        EstadoReporte.EN_REVISION -> colores.tertiaryContainer to colores.onTertiaryContainer
        EstadoReporte.RESUELTO -> colores.primaryContainer to colores.onPrimaryContainer
    }
    Surface(color = fondo, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = stringResource(estado.labelRes()),
            color = texto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Tarjeta de reporte reutilizada en "Mis reportes" (ciudadano) y en la lista del administrador. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteCard(reporte: Reporte, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(reporte.categoria.labelRes()),
                    fontWeight = FontWeight.Bold
                )
                EstadoBadge(reporte.estatus)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = reporte.descripcion, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.label_fecha_hora, reporte.fecha, reporte.hora),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
