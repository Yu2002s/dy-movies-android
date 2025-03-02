package xyz.jdynb.dymovies.fragment.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.databinding.FragmentRecommendBinding
import xyz.jdynb.dymovies.model.vod.VodVideo

class VodRecommendFragment: Fragment() {

  private var _binding: FragmentRecommendBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentRecommendBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val models = listOf(
      VodVideo(1, 1, "第一集", "url1"),
      VodVideo(2, 2, "第二集", "url2"),
      VodVideo(3, 3, "第三集", "url3"),
      VodVideo(4, 4, "第四集", "url4"),
    )

    binding.rv1.setup(models)
    binding.rv2.setup(models)
  }

  private fun RecyclerView.setup(models: List<VodVideo>) {
    linear().setup {
      singleMode = true
      addType<VodVideo>(R.layout.item_list_selection)
      onChecked { position, checked, allChecked ->
        val model = getModel<VodVideo>(position)
        model.isChecked = checked
        model.notifyChange()
        Log.d("jdy", "rv2 -> onChecked: $position, $checked")
      }
      R.id.item.onClick {
        // setChecked(adapterPosition, true)
        binding.rv1.bindingAdapter.setChecked(adapterPosition, true)
        binding.rv2.bindingAdapter.setChecked(adapterPosition, true)
      }
    }.models = models
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}