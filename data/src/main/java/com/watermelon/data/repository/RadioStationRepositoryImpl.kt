package com.watermelon.data.repository

import com.watermelon.data.local.dao.RadioStationDao
import com.watermelon.data.local.entity.toDomain
import com.watermelon.data.local.entity.toEntity
import com.watermelon.domain.model.RadioStation
import com.watermelon.domain.repository.RadioStationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioStationRepositoryImpl @Inject constructor(
    private val dao: RadioStationDao
) : RadioStationRepository {

    override fun getFavoriteStations(): Flow<List<RadioStation>> {
        return dao.getFavoriteStations().map { list -> list.map { it.toDomain() } }
    }

    override fun getRecentStations(): Flow<List<RadioStation>> {
        return dao.getRecentStations().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addFavorite(station: RadioStation) {
        dao.insertStation(station.toEntity("favorite"))
    }

    override suspend fun removeFavorite(stationUuid: String) {
        dao.removeFavorite(stationUuid)
    }

    override suspend fun recordRecentlyPlayed(station: RadioStation) {
        dao.insertStation(station.toEntity("recent"))
        dao.trimRecentTo(50)
    }

    override fun isFavorite(stationUuid: String): Flow<Boolean> {
        return dao.isFavorite(stationUuid)
    }
}
