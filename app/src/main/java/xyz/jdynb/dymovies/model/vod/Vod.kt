package xyz.jdynb.dymovies.model.vod


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.annotation.Keep

@Keep
@Serializable
data class Vod(
    @SerialName("id")
    var id: Int = 0,
    @SerialName("name")
    var name: String = "",
    @SerialName("tid")
    var tid: Int = 0,
    @SerialName("pic")
    var pic: String = "",
    @SerialName("note")
    var note: String = "",
    @SerialName("updateTime")
    var updateTime: String = "",
    @SerialName("flag")
    var flag: String = ""
)