package com.example.ciudadalerta.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ciudadalerta.R

// Aviso cuando no se pudo contactar la Web API
@Composable
fun AvisoSinConexion(
    visible: Boolean,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = visible, modifier = modifier) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
            ) {
                Icon(Icons.Filled.CloudOff, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.msg_sin_conexion),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onReintentar) { Text(stringResource(R.string.btn_reintentar)) }
            }
        }
    }
}

// Deslizar hacia abajo para sincronizar (el contenido debe ser un LazyColumn)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenedorRefrescable(
    sincronizando: Boolean,
    onRefrescar: () -> Unit,
    modifier: Modifier = Modifier,
    contenido: @Composable BoxScope.() -> Unit
) {
    PullToRefreshBox(
        isRefreshing = sincronizando,
        onRefresh = onRefrescar,
        modifier = modifier,
        content = contenido
    )
}
