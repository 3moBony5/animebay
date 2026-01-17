package io.animebay.stream.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScheduleAnime(
    val name: String,
    val imageUrl: String,
    val animeUrl: String,
    val description: String?
) : Parcelable

@Parcelize
data class DailySchedule(
    val day: String,
    val animes: List<ScheduleAnime>
) : Parcelable
