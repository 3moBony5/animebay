package io.animebay.stream.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DownloadLink(
    val host: String,
    val url: String,
    val quality: String
) : Parcelable
