package com.example.ciudadalerta.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ciudadalerta.R

// Mismos cortes que WindowSizeClass de Material: < 600dp, 600-839dp, >= 840dp
enum class TamanoPantalla { COMPACTA, MEDIANA, EXPANDIDA }

@Composable
fun rememberTamanoPantalla(): TamanoPantalla {
    val ancho = LocalConfiguration.current.screenWidthDp
    return when {
        ancho < 600 -> TamanoPantalla.COMPACTA
        ancho < 840 -> TamanoPantalla.MEDIANA
        else -> TamanoPantalla.EXPANDIDA
    }
}

// Centra el contenido con un ancho máximo para que no se estire en pantallas grandes
@Composable
fun ContenidoCentrado(
    anchoMaximo: Dp = 640.dp,
    contenido: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier
                .widthIn(max = anchoMaximo)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            contenido()
        }
    }
}

// Lista a la izquierda y detalle a la derecha
@Composable
fun DosPaneles(
    izquierda: @Composable () -> Unit,
    derecha: @Composable () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight()
        ) { izquierda() }
        VerticalDivider()
        Box(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight()
        ) { derecha() }
    }
}

// Panel derecho cuando todavía no se elige un reporte
@Composable
fun PanelSinSeleccion() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Description,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.msg_selecciona_reporte),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
