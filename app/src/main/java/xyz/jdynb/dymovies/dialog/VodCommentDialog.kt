package xyz.jdynb.dymovies.dialog

import android.content.Context
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.databinding.DialogVodCommentBinding
import xyz.jdynb.dymovies.utils.showToast

class VodCommentDialog(context: Context) : BottomSheetDialog(context) {

  private lateinit var binding: DialogVodCommentBinding

  lateinit var onSubmit: (content: String) -> Unit

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.inflate(
      layoutInflater,
      R.layout.dialog_vod_comment,
      null,
      false
    )
    setContentView(binding.root)

    binding.btnSend.setOnClickListener {
      val content = binding.inputComment.editText!!.text.toString().trim()
      if (content.isEmpty()) {
        "请输入评论内容".showToast()
        return@setOnClickListener
      }
      onSubmit(content)
      binding.inputComment.editText?.setText("")
      dismiss()
    }
  }

  override fun onStart() {
    super.onStart()
    binding.inputComment.editText?.requestFocus()
  }
}