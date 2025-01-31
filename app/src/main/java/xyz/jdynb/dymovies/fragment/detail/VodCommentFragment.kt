package xyz.jdynb.dymovies.fragment.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.dividerSpace
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import com.drake.net.utils.scopeDialog
import kotlinx.coroutines.delay
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.FragmentVodCommentBinding
import xyz.jdynb.dymovies.model.page.Page
import xyz.jdynb.dymovies.model.vod.VodComment
import xyz.jdynb.dymovies.model.vod.VodReply

class VodCommentFragment : Fragment() {

  private var _binding: FragmentVodCommentBinding? = null
  private val binding get() = _binding!!

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

    ViewCompat.setOnApplyWindowInsetsListener(binding.bottom) {v, insets ->
      v.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom)
      insets
    }

    val id = 1000//requireArguments().getInt("id")

    binding.commentRv.dividerSpace(20, DividerOrientation.VERTICAL).setup {
      addType<VodComment>(R.layout.item_list_comment)
      addType<VodReply>(R.layout.item_list_reply)

      R.id.comment_item.onClick {

      }

      R.id.reply_item.onClick {

      }
    }

    binding.page.onRefresh {
      scope {
        val result = Get<Page<VodComment>>(Api.VOD_COMMENTS + "/$id") {
          addQuery("page", index)
        }.await()
        addData(result.data) {
          itemCount < result.total
        }
      }
    }.showLoading()

    binding.submit.setOnClickListener { v ->
      scopeDialog {
        delay(2000)
      }.finally {
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}