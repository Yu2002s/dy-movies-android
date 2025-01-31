package xyz.jdynb.dymovies.model.vod

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.annotation.Keep

@Keep
@Serializable
data class SchedulerJob(
    @SerialName("key")
    var key: String = "",
    @SerialName("name")
    var name: String = "",
    @SerialName("status")
    var status: Int = 0,
    @SerialName("statusStr")
    var statusStr: String = "",
    @SerialName("children")
    var children: List<Children> = listOf()
) {
    @Keep
    @Serializable
    data class Children(
        @SerialName("key")
        var key: String = "",
        @SerialName("name")
        var name: String = "",
        @SerialName("group")
        var group: String = "",
        @SerialName("cron")
        var cron: String = "",
        @SerialName("jobClass")
        var jobClass: String = "",
        @SerialName("desc")
        var desc: String = "",
        @SerialName("host")
        var host: String = "",
        @SerialName("status")
        var status: Int = 0,
        @SerialName("statusStr")
        var statusStr: String = ""
    )
}