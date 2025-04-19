package xyz.jdynb.dymovies.model.vod


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.annotation.Keep

/**
 * 影片一些基本的信息，用于列表页展示的数据
 */
@Keep
@Serializable
data class Vod(
    /**
     * 影片 id
     */
    @SerialName("id")
    var id: Int = 0,
    /**
     * 影片名称
     */
    @SerialName("name")
    var name: String = "",
    /**
     * 影片类型 id
     */
    @SerialName("tid")
    var tid: Int = 0,
    /**
     * 影片封面
     */
    @SerialName("pic")
    var pic: String = "",
    /**
     * 影片配置信息
     */
    @SerialName("note")
    var note: String = "",
    /**
     * 更新时间
     */
    @SerialName("updateTime")
    var updateTime: String = "",
    /**
     * 影片标识 （采集源的标识）
     */
    @SerialName("flag")
    var flag: String = ""
)