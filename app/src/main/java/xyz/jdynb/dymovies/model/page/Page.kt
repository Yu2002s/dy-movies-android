package xyz.jdynb.dymovies.model.page


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.annotation.Keep

@Keep
@Serializable
data class Page<T>(
    @SerialName("currentPage")
    var currentPage: Int = 0,
    @SerialName("total")
    var total: Int = 0,
    @SerialName("lastPage")
    var lastPage: Int = 0,
    @SerialName("data")
    var `data`: List<T> = listOf()
)