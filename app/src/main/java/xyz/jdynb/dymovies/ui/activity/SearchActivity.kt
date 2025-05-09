package xyz.jdynb.dymovies.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setDifferModels
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import com.drake.net.utils.scopeNetLife
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import org.litepal.LitePal
import org.litepal.extension.find
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.ActivitySearchBinding
import xyz.jdynb.dymovies.databinding.ItemListSuggestBinding
import xyz.jdynb.dymovies.model.search.SearchSuggest
import xyz.jdynb.dymovies.model.vod.VodProvider
import xyz.jdynb.dymovies.model.vod.VodType
import xyz.jdynb.dymovies.ui.fragment.search.SearchFragment
import xyz.jdynb.dymovies.ui.fragment.search.SearchVodFragment
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SearchActivity : BaseActivity() {

  companion object {

    private val TAG = SearchActivity::class.java.simpleName

  }

  private lateinit var binding: ActivitySearchBinding

  private val inputManager by lazy {
    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  }

  private val mHandler = Handler(Looper.getMainLooper())

  @SuppressLint("NotifyDataSetChanged")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    binding.toolbar.setNavigationOnClickListener {
      finish()
      overridePendingTransition(0, 0)
    }
    // addMenuProvider(this)
    binding.editSearch.post {
      binding.editSearch.requestFocus()
    }

    binding.suggestRv.setup {
      addType<SearchSuggest>(R.layout.item_list_suggest)

      R.id.chip.onClick {
        binding.editSearch.setQuery(getModel<SearchSuggest>().name, true)
      }

      onBind {
        getBinding<ItemListSuggestBinding>().chip.setOnCloseIconClickListener {
          scope(Dispatchers.Default) {
            getModel<SearchSuggest>().delete()
          }
          mutable.removeAt(layoutPosition)
          notifyItemRemoved(layoutPosition)
        }
      }
    }

    binding.indexRv.setup {
      addType<String>(R.layout.item_list_index)

      R.id.search_index.onClick {
        binding.editSearch.setQuery(getModel<String>(), true)
      }
    }

    val tab = binding.searchTab
    val vp = binding.searchVp

    val fragments = mutableListOf<SearchFragment>(/*SearchParseFragment()*/)
    val types = mutableListOf(/*VodType(name = "解析"), */VodType(name = "全部"))

    scopeNetLife {
      binding.suggestRv.models = querySuggests()
      val vodProviders = Get<MutableList<VodProvider>>(Api.VOD_PROVIDER).await()
      val vodProvider = VodProvider(name = "全部")
      vodProvider.value = ""
      vodProviders.add(0, vodProvider)
      val result = Get<List<VodType>>(Api.VOD_TYPE_ALL).await()
      val allTypes = result.flatMap { it.children }
      fragments.add(SearchVodFragment.newInstance(VodType(children = allTypes), vodProviders))
      types.addAll(result)
      fragments.addAll(result.map {
        SearchVodFragment.newInstance(it, vodProviders)
      })
      vp.adapter?.notifyDataSetChanged()
    }

    vp.adapter = SearchPageAdapter(supportFragmentManager, lifecycle, fragments)

    TabLayoutMediator(tab, vp) { t, position ->
      t.text = types[position].name
    }.attach()

    binding.editSearch.setOnQueryTextFocusChangeListener { v, hasFocus ->
      val isEmpty = (v as SearchView).query.isEmpty()
      binding.suggestRv.isVisible = isEmpty
      binding.searchVp.isVisible = !hasFocus && !isEmpty
      binding.searchTab.isVisible = !hasFocus && !isEmpty
      binding.indexRv.isVisible = hasFocus
    }

    binding.editSearch.setOnQueryTextListener(object : OnQueryTextListener {

      override fun onQueryTextChange(newText: String?): Boolean {
        val isBlank = newText.isNullOrBlank()
        Log.d(TAG, "queryTextChange: $newText")
        binding.suggestRv.isVisible = isBlank
        binding.indexRv.isVisible = !isBlank && binding.editSearch.hasFocus()
        if (isBlank) {
          Log.d(TAG, "onSearchChanged: $newText")
          scope {
            binding.suggestRv.setDifferModels(querySuggests())
          }
        } else {
          querySearchIndex()
        }
        return true
      }

      override fun onQueryTextSubmit(query: String): Boolean {
        if (fragments.size <= 1) {
          return false
        }
        val text = query.trim()
        if (text.isNotEmpty()) {
          scope(Dispatchers.Default) {
            SearchSuggest(text).saveOrUpdate("name = ?", text)
          }
          hideSoftInputMethod()
          Log.d(TAG, "search: $text")
          notifyCurrentFragmentSearch(fragments, text)
          return true
        }
        return false
      }
    })
  }

  /**
   * 通知当前 fragment 进行搜索
   */
  private fun notifyCurrentFragmentSearch(fragments: List<SearchFragment>, keyword: String) {
    fragments.forEachIndexed { index, searchFragment ->
      searchFragment.keyword = keyword
      if (index == binding.searchVp.currentItem) {
        searchFragment.search()
      }
    }
  }

  /**
   * 查询搜索所需的搜索索引
   */
  private fun querySearchIndex() {
    mHandler.removeCallbacks(searchIndexRunnable)
    mHandler.postDelayed(searchIndexRunnable, 300)
  }

  /**
   * 搜索索引运行的任务
   */
  private val searchIndexRunnable = Runnable {
    // 发起网络请求从后台获取搜索索引
    scope {
      binding.indexRv.setDifferModels(Get<List<String>>(Api.VOD_SEARCH_INDEX) {
        setQuery("keyword", binding.editSearch.query.toString())
      }.await())
    }
  }

  /**
   * 查询历史的搜索建议
   */
  private suspend fun querySuggests() = suspendCoroutine {
    it.resume(
      LitePal.limit(100)
        .order("updateAt desc").find<SearchSuggest>()
    )
  }

  /**
   * 隐藏软件输入法并让搜索框失焦
   */
  private fun hideSoftInputMethod() {
    inputManager.hideSoftInputFromWindow(
      binding.editSearch.windowToken,
      InputMethodManager.HIDE_NOT_ALWAYS
    )
    binding.editSearch.clearFocus()
  }

  override fun onDestroy() {
    super.onDestroy()
    mHandler.removeCallbacksAndMessages(null)
  }

  private class SearchPageAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    private val fragments: List<Fragment>
  ) : FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]
  }
}