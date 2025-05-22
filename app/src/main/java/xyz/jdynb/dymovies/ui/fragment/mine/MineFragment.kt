package xyz.jdynb.dymovies.ui.fragment.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.base.BaseFragment
import xyz.jdynb.dymovies.config.SPConfig
import xyz.jdynb.dymovies.databinding.FragmentMineBinding
import xyz.jdynb.dymovies.model.BingImageResponse
import xyz.jdynb.dymovies.model.MineModel
import xyz.jdynb.dymovies.model.ui.setupActionList
import xyz.jdynb.dymovies.ui.activity.DownloadActivity
import xyz.jdynb.dymovies.ui.activity.FeedbackActivity
import xyz.jdynb.dymovies.ui.activity.SettingActivity
import xyz.jdynb.dymovies.ui.activity.VodHistoryActivity
import xyz.jdynb.dymovies.utils.SpUtils.get
import xyz.jdynb.dymovies.utils.SpUtils.put
import xyz.jdynb.dymovies.utils.startActivity

class MineFragment : BaseFragment() {

  companion object {

    private const val BING_HOST = "https://cn.bing.com"
  }

  private var _binding: FragmentMineBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mine, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.actionRv.setupActionList {
      when (it.id) {
        "download" -> startActivity<DownloadActivity>()
        "history" -> startActivity<VodHistoryActivity>()
        "feedback" -> startActivity<FeedbackActivity>()
        "setting" -> startActivity<SettingActivity>()
      }
    }

    val mineModel = MineModel()
    mineModel.cover = SPConfig.MINE_COVER.get<String?>(null)
    binding.m = mineModel

    scopeNetLife {
      val result =
        Get<BingImageResponse>("https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1").await()
      val cover = BING_HOST + result.images.getOrNull(0)?.url
      SPConfig.MINE_COVER put cover
      mineModel.cover = cover
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

}