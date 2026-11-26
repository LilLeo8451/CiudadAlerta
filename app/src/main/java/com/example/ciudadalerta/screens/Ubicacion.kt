package com.example.ciudadalerta.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ciudadalerta.R
import com.example.ciudadalerta.util.Coordenadas
import com.example.ciudadalerta.util.formatearCoordenadas
import com.example.ciudadalerta.util.tienePermisoUbicacion

// Botón para tomar la ubicación del GPS (pide el permiso si hace falta)
@Composable
fun SelectorUbicacion(
    ubicacion: Coordenadas?,
    buscando: Boolean,
    error: Boolean,
    onObtener: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var permisoDenegado by rememberSaveable { mutableStateOf(false) }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        permisoDenegado = resultado.values.none { it }
        if (!permisoDenegado) onObtener()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.label_ubicacion_reporte))
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                if (tienePermisoUbicacion(context)) {
                    onObtener()
                } else {
                    pedirPermiso.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            enabled = !buscando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (buscando) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.MyLocation, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(
                    if (ubicacion == null) R.string.btn_usar_ubicacion else R.string.btn_actualizar_ubicacion
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        when {
            ubicacion != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatearCoordenadas(ubicacion.latitud, ubicacion.longitud),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            permisoDenegado -> Text(
                text = stringResource(R.string.msg_permiso_ubicacion),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            error -> Text(
                text = stringResource(R.string.msg_error_ubicacion),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Abre la ubicación en Google Maps (o la app de mapas que haya)
@Composable
fun BotonVerEnMapa(latitud: Double, longitud: Double, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            val geo = Uri.parse("geo:$latitud,$longitud?q=$latitud,$longitud")
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, geo))
            } catch (e: ActivityNotFoundException) {
                // Sin app de mapas: se abre en el navegador
                val web = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitud,$longitud")
                context.startActivity(Intent(Intent.ACTION_VIEW, web))
            }
        },
        modifier = modifier
    ) {
        Icon(Icons.Filled.Map, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.btn_ver_mapa))
    }
}
