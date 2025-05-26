package xyz.jdynb.dymovies.ui.fragment.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.Post
import com.drake.net.utils.scope
import com.drake.net.utils.scopeDialog
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.FragmentVodCommentBinding
import xyz.jdynb.dymovies.dialog.VodCommentDialog
import xyz.jdynb.dymovies.model.page.Page
import xyz.jdynb.dymovies.model.result.SimpleResult
import xyz.jdynb.dymovies.model.vod.VodComment
import xyz.jdynb.dymovies.model.vod.VodReply
import xyz.jdynb.dymovies.utils.fitNavigationBar
import xyz.jdynb.dymovies.utils.showToast

class VodCommentFragment : Fragment() {

  companion object {
    private val TAG = VodCommentFragment::class.java.simpleName
  }


  private var _binding: FragmentVodCommentBinding? = null
  private val binding get() = _binding!!

  private lateinit var vodCommentDialog: VodCommentDialog
  private var detailId = 0

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_vod_comment, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.bottom.fitNavigationBar(defaultBottom = 20)

    detailId = requireArguments().getInt("id")

    binding.commentRv.dividerSpace(20, DividerOrientation.VERTICAL).setup {
      addType<VodComment>(R.layout.item_list_comment)
      addType<VodReply>(R.layout.item_list_reply)

      R.id.comment_item.onClick {
        val vodComment = getModel<VodComment>()
        Log.i(TAG, "vodComment: $vodComment")
        showCommentDialog(vodComment.user.id, vodComment.id)
      }

      R.id.reply_item.onClick {
        val vodReply = getModel<VodReply>()
        Log.i(TAG, "vodReply: $vodReply")
        showCommentDialog(vodReply.fromUser.id, vodReply.commentId)
      }
    }

    binding.page.onRefresh {
      scope {
        val result = Get<Page<VodComment>>(Api.VOD_COMMENTS + "/$detailId") {
          addQuery("page", index)
        }.await()
        addData(result.data) {
          itemCount < result.total
        }
      }
    }.showLoading()

    binding.submit.setOnClickListener { _ ->
      val content = binding.inputComment.editText!!.text.toString().trim()
      if (content.isEmpty()) {
        "请输入评论内容".showToast()
        return@setOnClickListener
      }
      binding.inputComment.editText!!.setText("")
      submitComment(content)
    }
  }

  private fun showCommentDialog(toUid: Int? = null, commentId: Int? = null) {
    if (!::vodCommentDialog.isInitialized) {
      vodCommentDialog = VodCommentDialog(requireContext())
      vodCommentDialog.onSubmit = { content ->
        submitComment(content, toUid, commentId)
      }
    }
    vodCommentDialog.show()
  }

  private fun submitComment(content: String, toUid: Int? = null, commentId: Int? = null) {
    scopeDialog {
      val result = Post<SimpleResult>(Api.VOD_COMMENTS) {
        json(
          "detailId" to detailId,
          "toUid" to toUid,
          "commentId" to commentId,
          "content" to content
        )
      }.await()
      result.msg.showToast()
      //TODO: 楼中楼回复增量更新待实现
      binding.page.refreshing()
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}