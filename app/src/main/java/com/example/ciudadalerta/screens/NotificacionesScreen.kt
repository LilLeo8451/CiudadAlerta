package com.example.ciudadalerta.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ciudadalerta.R
import com.example.ciudadalerta.viewmodel.NotificacionItem
import com.example.ciudadalerta.viewmodel.NotificacionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificacionesScreen(
    notificacionViewModel: NotificacionViewModel,
    onAbrirReporte: (Int) -> Unit
) {
    val items by notificacionViewModel.items.collectAsState()
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val hayNoLeidas = items.any { !it.notificacion.leida }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.title_notificaciones),
            style = MaterialTheme.typography.headlineMedium
        )

        if (hayNoLeidas) {
            TextButton(
                onClick = { notificacionViewModel.marcarTodasLeidas() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.btn_marcar_todas))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (items.isEmpty()) {
            Text(text = stringResource(R.string.empty_notificaciones))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items, key = { it.notificacion.id }) { item ->
                    NotificacionCard(
                        item = item,
                        fecha = formatoFecha.format(Date(item.notificacion.fecha)),
                        onClick = {
                            notificacionViewModel.marcarLeida(item.notificacion.id)
                            onAbrirReporte(item.notificacion.reporteId)
                        },
                        onEliminar = { notificacionViewModel.eliminar(item.notificacion.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificacionCard(
    item: NotificacionItem,
    fecha: String,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    val notificacion = item.notificacion
    val categoria = item.categoria
    val estado = stringResource(notificacion.estadoNuevo.labelRes())
    val texto = if (categoria != null) {
        stringResource(R.string.notif_texto, stringResource(categoria.labelRes()), estado)
    } else {
        stringResource(R.string.notif_texto_sin_categoria, estado)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notificacion.leida) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = if (notificacion.leida) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = texto,
                    fontWeight = if (notificacion.leida) FontWeight.Normal else FontWeight.Bold
                )
                Text(text = fecha, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.cd_eliminar_notificacion)
                )
            }
        }
    }
}