package xyz.jdynb.dymovies.model.vod


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable

@Keep
@Serializable
data class VodType(
    @SerialName("id")
    var id: Int = 0,
    @SerialName("pid")
    var pid: Int? = null,
    @SerialName("name")
    var name: String = "",
    @SerialName("flag")
    var flag: String = "",
    @SerialName("children")
    var children: List<VodType> = emptyList()
): BaseObservable() {

    var isChecked = false

}