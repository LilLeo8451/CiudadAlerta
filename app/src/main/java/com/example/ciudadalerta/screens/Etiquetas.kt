package com.example.ciudadalerta.screens

import androidx.annotation.StringRes
import com.example.ciudadalerta.R
import com.example.ciudadalerta.model.CategoriaReporte
import com.example.ciudadalerta.model.EstadoReporte
import com.example.ciudadalerta.model.Rol
import com.example.ciudadalerta.viewmodel.AuthError

@StringRes
fun CategoriaReporte.labelRes(): Int = when (this) {
    CategoriaReporte.BACHE -> R.string.cat_bache
    CategoriaReporte.ALUMBRADO -> R.string.cat_alumbrado
    CategoriaReporte.BASURA -> R.string.cat_basura
    CategoriaReporte.FUGA_AGUA -> R.string.cat_fuga_agua
}

@StringRes
fun EstadoReporte.labelRes(): Int = when (this) {
    EstadoReporte.PENDIENTE -> R.string.estado_pendiente
    EstadoReporte.EN_REVISION -> R.string.estado_en_revision
    EstadoReporte.RESUELTO -> R.string.estado_resuelto
}

@StringRes
fun Rol.labelRes(): Int = when (this) {
    Rol.CIUDADANO -> R.string.rol_ciudadano
    Rol.ADMIN -> R.string.rol_admin
}

@StringRes
fun AuthError.mensajeRes(): Int = when (this) {
    AuthError.CAMPOS_VACIOS -> R.string.error_campos_vacios
    AuthError.CREDENCIALES_INVALIDAS -> R.string.error_credenciales
    AuthError.CORREO_INVALIDO -> R.string.error_correo_invalido
    AuthError.CORREO_YA_REGISTRADO -> R.string.error_correo_registrado
    AuthError.PASSWORD_CORTA -> R.string.error_password_corta
    AuthError.PASSWORDS_NO_COINCIDEN -> R.string.error_passwords_no_coinciden
}
