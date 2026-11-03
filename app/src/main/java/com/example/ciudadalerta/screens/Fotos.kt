package com.example.ciudadalerta.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.example.ciudadalerta.R

/**
 * Botones "Tomar foto" (cámara) y "Galería" (selector de fotos del sistema).
 * No necesitan permisos: la cámara la abre otra app y el selector de fotos no pide acceso a la galería.
 */
@Composable
fun BotonesAgregarFoto(
    crearUriCamara: () -> Uri,
    onFotoElegida: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    // Se guarda para saber dónde quedó la foto cuando la cámara regrese (sobrevive a girar la pantalla)
    var uriCamara by rememberSaveable { mutableStateOf<Uri?>(null) }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        if (tomada) uriCamara?.let(onFotoElegida)
        uriCamara = null
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(onFotoElegida)
    }

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = {
                val uri = crearUriCamara()
                uriCamara = uri
                camara.launch(uri)
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.PhotoCamera, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.btn_tomar_foto))
        }
        OutlinedButton(
            onClick = {
                galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.btn_galeria))
        }
    }
}

/**
 * Fila horizontal de miniaturas. Al tocar una se ve en grande.
 * @param fotos lo que Coil sabe cargar: Uri o String ("file:///...").
 * @param onQuitar si no es null, cada miniatura muestra una X para quitarla.
 */
@Composable
fun GaleriaFotos(
    fotos: List<Any>,
    modifier: Modifier = Modifier,
    onQuitar: ((Int) -> Unit)? = null
) {
    var fotoAmpliada by remember { mutableStateOf<Any?>(null) }

    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(fotos) { index, foto ->
            Box {
                AsyncImage(
                    model = foto,
                    contentDescription = stringResource(R.string.cd_foto_reporte),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { fotoAmpliada = foto }
                )
                if (onQuitar != null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { onQuitar(index) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cd_quitar_foto),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    fotoAmpliada?.let { foto ->
        Dialog(onDismissRequest = { fotoAmpliada = null }) {
            AsyncImage(
                model = foto,
                contentDescription = stringResource(R.string.cd_foto_reporte),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { fotoAmpliada = null }
            )
        }
    }
}
