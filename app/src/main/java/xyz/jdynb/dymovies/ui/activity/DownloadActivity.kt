package xyz.jdynb.dymovies.ui.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.drake.brv.BindingAdapter
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.net.utils.scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.litepal.LitePal
import org.litepal.extension.find
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.databinding.ActivityDownloadBinding
import xyz.jdynb.dymovies.databinding.ItemListDownloadBinding
import xyz.jdynb.dymovies.download.DownloadListener
import xyz.jdynb.dymovies.download.DownloadService
import xyz.jdynb.dymovies.model.download.Download
import xyz.jdynb.dymovies.model.download.DownloadStatus
import xyz.jdynb.dymovies.model.vod.VodDetail
import xyz.jdynb.dymovies.utils.formatBytes
import xyz.jdynb.dymovies.utils.showToast
import java.util.Objects

class DownloadActivity : BaseActivity(), ServiceConnection, DownloadListener, MenuProvider {

  companion object {

    private const val TAG = "DownloadActivity"

  }

  private val binding by lazy {
    ActivityDownloadBinding.inflate(layoutInflater)
  }

  private var _downloadService: DownloadService? = null
  private val downloadService get() = _downloadService!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    bindService(Intent(this, DownloadService::class.java), this, Context.BIND_AUTO_CREATE)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    addMenuProvider(this, this)

    binding.rv.setup {
      setAnimation(AnimationType.SCALE)
      addType<Download>(R.layout.item_list_download)
      R.id.sw.onClick {
        val download = getModel<Download>()
        downloadService.resumeOrPauseDownload(download)
      }

      onFastClick(R.id.item, R.id.cb) {
        val model = getModel<Download>()
        if (!toggleMode && it == R.id.item) {
          if (model.status != DownloadStatus.COMPLETED) {
            downloadService.resumeOrPauseDownload(model)
            return@onFastClick
          }
          // 打开播放
          SimpleVideoActivity.actionStart(model.downloadPath, model.name)
          return@onFastClick
        }
        val checked = model.isChecked
        setChecked(layoutPosition, !checked)
      }

      R.id.item.onLongClick {
        if (!toggleMode) {
          toggle()
          setChecked(layoutPosition, true)
        }
      }

      onChecked { position, checked, _ ->
        val model = getModel<Download>(position)
        model.isChecked = checked
      }

      onToggle { position, toggleMode, _ ->
        // 刷新列表显示选择按钮
        val model = getModel<Download>(position)
        model.isVisibleCheck = toggleMode
        changeListEditable(this)
      }
    }

    binding.rvl.onRefresh {
      scope {
        val offset = (index - 1) * 10
        val downloads = withContext(Dispatchers.IO) {
          LitePal.offset(offset).limit(10).order("updateAt desc").find<Download>()
            .onEach { download ->
              download.status = downloadService.getDownloadStatus(download)
            }
        }
        addData(downloads)
      }
    }

    checkPermission()
  }

  private fun checkAll(isChecked: Boolean) {
    val bindingAdapter = binding.rv.bindingAdapter
    if (!bindingAdapter.toggleMode) {
      bindingAdapter.toggle()
    }
    bindingAdapter.checkedAll(isChecked)
  }

  private fun reverseCheck() {
    binding.rv.bindingAdapter.checkedReverse()
  }

  @Suppress("UNCHECKED_CAST")
  private fun delete() {
    val bindingAdapter = binding.rv.bindingAdapter
    val checkedItems = bindingAdapter.checkedPosition
    Log.d(TAG, "checkedItems: $checkedItems")
    checkedItems.sortedDescending().forEach { index ->
      val download = (bindingAdapter.models as List<Download>)[index]
      // 暂时在主线程删除
      downloadService.removeDownload(download)
      bindingAdapter.mutable.removeAt(index)
      bindingAdapter.notifyItemRemoved(index)
    }
    if (bindingAdapter.models.isNullOrEmpty()) {
      invalidateMenu()
    }
    toggle(false)
  }

  private fun toggle(toggleMode: Boolean = !binding.rv.bindingAdapter.toggleMode) {
    binding.rv.bindingAdapter.toggle(toggleMode)
  }

  /** 改变编辑状态 */
  private fun changeListEditable(adapter: BindingAdapter) {
    val toggleMode = adapter.toggleMode
    // val checkedCount = adapter.checkedCount
    invalidateMenu()

    // 如果取消管理模式则取消全部已选择
    if (!toggleMode) adapter.checkedAll(false)
  }

  @Suppress("UNCHECKED_CAST")
  override fun onDownload(download: Download) {
    val models = binding.rv.models ?: return
    models as List<Download>
    val index = models.indexOf(download)
    runOnUiThread {
      if (index != -1) {
        models[index].apply {
          status = download.status
          currentByte = download.currentByte
          progress = download.progress
        }
      }
    }
  }

  override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
    _downloadService = (service as DownloadService.DownloadBinder).getService()
    _downloadService?.let {
      it.downloadListener = this
      binding.rvl.showLoading()
    }
  }

  override fun onServiceDisconnected(name: ComponentName?) {
    _downloadService = null
  }

  override fun onInsetChanged(statusBarHeight: Int, navigationBarHeight: Int): Boolean {
    binding.savePath.updatePadding(bottom = navigationBarHeight + 10)
    return false
  }

  private fun checkPermission() {
    // android 11以上
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      if (!Environment.isExternalStorageManager()) {
        "请授权访问存储空间以下载文件".showToast()
        startActivity(
          Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            "package:$packageName".toUri()
          )
        )
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    _downloadService?.downloadListener = null
    unbindService(this)
    _downloadService = null
  }

  override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    menuInflater.inflate(R.menu.menu_check, menu)
  }

  override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
    when (menuItem.itemId) {
      R.id.edit -> toggle()
      R.id.delete -> delete()
      R.id.check_all -> {
        menuItem.isChecked = !menuItem.isChecked
        checkAll(menuItem.isChecked)
      }

      R.id.reverse_check -> reverseCheck()
    }
    return true
  }

  override fun onPrepareMenu(menu: Menu) {
    super.onPrepareMenu(menu)
    val editItem = menu.findItem(R.id.edit)
    val bindingAdapter = binding.rv.bindingAdapter
    editItem.icon = ContextCompat.getDrawable(
      this,
      if (bindingAdapter.toggleMode) R.drawable.baseline_edit_off_24 else R.drawable.baseline_mode_edit_24
    )
  }
}