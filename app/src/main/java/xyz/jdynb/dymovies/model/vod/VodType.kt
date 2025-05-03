package xyz.jdynb.dymovies.model.vod


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.drake.brv.BR

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

    fun isChild() = pid == null || children.isEmpty()

    @get:Bindable
    var isChecked = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.checked)
        }

}