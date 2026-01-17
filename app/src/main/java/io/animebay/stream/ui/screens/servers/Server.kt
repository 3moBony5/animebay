package io.animebay.stream.ui.screens.servers // تأكد من أن الحزمة صحيحة

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Server(
    val name: String,
    val embedUrl: String, // <-- هذا هو التغيير الرئيسي
    val quality: String
) : Parcelable
