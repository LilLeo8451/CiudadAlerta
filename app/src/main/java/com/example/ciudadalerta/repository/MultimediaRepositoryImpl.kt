package com.example.ciudadalerta.repository

import com.example.ciudadalerta.data.local.dao.MultimediaDao
import com.example.ciudadalerta.data.local.entity.MultimediaEntity
import com.example.ciudadalerta.data.mapper.toDomain
import com.example.ciudadalerta.data.mapper.toEntity
import com.example.ciudadalerta.model.Multimedia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MultimediaRepositoryImpl(
    private val dao: MultimediaDao
) : MultimediaRepository {

    override fun observarPorReporte(reporteId: Int): Flow<List<Multimedia>> =
        dao.observarPorReporte(reporteId).map { lista -> lista.map { it.toDomain() } }

    override suspend fun agregar(reporteId: Int, uri: String): Int =
        dao.insertar(MultimediaEntity(reporteId = reporteId, uri = uri)).toInt()

    override suspend fun eliminar(multimedia: Multimedia) {
        dao.eliminar(multimedia.toEntity())
    }
}