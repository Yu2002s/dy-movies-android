package xyz.jdynb.dymovies.model.app


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.annotation.Keep

@Keep
@Serializable
data class Update(
    @SerialName("id")
    var id: Int = 0,
    @SerialName("versionName")
    var versionName: String = "",
    @SerialName("versionCode")
    var versionCode: Int = 0,
    @SerialName("content")
    var content: String = "",
    @SerialName("status")
    var status: Int = 0,
    @SerialName("isForce")
    var isForce: Int = 0,
    @SerialName("url")
    var url: String = "",
    @SerialName("createAt")
    var createAt: String = ""
)