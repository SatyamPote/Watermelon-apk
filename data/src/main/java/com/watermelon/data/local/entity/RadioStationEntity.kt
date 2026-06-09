package com.watermelon.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.watermelon.domain.model.RadioStation

@Entity(tableName = "radio_stations")
data class RadioStationEntity(
    @PrimaryKey val stationUuid: String,
    val name: String?,
    val url: String?,
    val favicon: String?,
    val country: String?,
    val countrycode: String?,
    val language: String?,
    val tags: String?,
    val bitrate: Int = 0,
    val votes: Int = 0,
    @ColumnInfo(index = true) val actionType: String, // "favorite", "recent"
    val timestamp: Long = System.currentTimeMillis()
)

fun RadioStationEntity.toDomain(): RadioStation = RadioStation(
    stationuuid = stationUuid,
    name = name,
    url = url,
    urlResolved = null,
    homepage = null,
    favicon = favicon,
    country = country,
    countrycode = countrycode,
    language = language,
    tags = tags,
    bitrate = bitrate,
    votes = votes
)

fun RadioStation.toEntity(actionType: String): RadioStationEntity = RadioStationEntity(
    stationUuid = stationuuid ?: "${name}_${url}",
    name = name,
    url = url,
    favicon = favicon,
    country = country,
    countrycode = countrycode,
    language = language,
    tags = tags,
    bitrate = bitrate,
    votes = votes,
    actionType = actionType,
    timestamp = System.currentTimeMillis()
)
