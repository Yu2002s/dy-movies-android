package xyz.jdynb.dymovies.model.vod


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.annotation.Keep
import androidx.databinding.BaseObservable
import com.drake.brv.item.ItemExpand
import xyz.jdynb.dymovies.model.user.User
import kotlin.math.truncate

@Keep
@Serializable
data class VodComment(
    @SerialName("id")
    var id: Int = 0,
    @SerialName("user")
    var user: User = User(),
    @SerialName("detailId")
    val detailId: Int = 0,
    @SerialName("content")
    var content: String = "",
    @SerialName("createAt")
    var createAt: String = "",
    @SerialName("replyList")
    var replyList: List<VodReply> = listOf()
): BaseObservable(), ItemExpand {

    override var itemGroupPosition: Int = 0
    /**
     * 展开状态
     */
    override var itemExpand: Boolean = true

    override fun getItemSublist(): List<Any?> {
        return replyList
    }
}