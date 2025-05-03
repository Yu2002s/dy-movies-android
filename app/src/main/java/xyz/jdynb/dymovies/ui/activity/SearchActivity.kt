package xyz.jdynb.dymovies.ui.activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.MenuProvider
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.setDifferModels
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import com.drake.net.utils.scopeLife
import com.drake.net.utils.scopeNetLife
import com.drake.net.utils.withDefault
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.litepal.LitePal
import org.litepal.extension.find
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.base.BaseActivity
import xyz.jdynb.dymovies.config.Api
import xyz.jdynb.dymovies.databinding.ActivitySearchBinding
import xyz.jdynb.dymovies.databinding.ItemListSuggestBinding
import xyz.jdynb.dymovies.ui.fragment.search.SearchFragment
import xyz.jdynb.dymovies.model.search.SearchSuggest
import xyz.jdynb.dymovies.model.vod.VodDetail
import xyz.jdynb.dymovies.model.vod.VodProvider
import xyz.jdynb.dymovies.model.vod.VodType

class SearchActivity : BaseActivity() {

  companion object {

    private val TAG = SearchActivity::class.java.simpleName

  }

  private lateinit var binding: ActivitySearchBinding

  private val inputManager by lazy {
    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  }

  private val mHandler = Handler(Looper.getMainLooper())

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

    val fragments = mutableListOf<Fragment>()
    val types = mutableListOf(VodType(name = "全部"))

    scopeNetLife {
      binding.suggestRv.models = withDefault {
        querySuggests()
      }
      val vodProviders = Get<MutableList<VodProvider>>(Api.VOD_PROVIDER).await()
        .also {
          val vodProvider = VodProvider(name = "全部")
          vodProvider.value = ""
          it.add(0, vodProvider)
        }
      val result = Get<List<VodType>>(Api.VOD_TYPE_ALL).await()
      val allTypes = result.flatMap { it.children }// .filter { it.pid != null }
      fragments.add(SearchFragment.newInstance(VodType(children = allTypes), vodProviders))
      types.addAll(result)
      fragments.addAll(result.map {
        SearchFragment.newInstance(it, vodProviders)
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
            binding.suggestRv.setDifferModels(
              querySuggests().also {
                Log.d(TAG, "suggests: $it")
              }
            )
          }
        } else {
          querySearchIndex()
        }
        return true
      }

      override fun onQueryTextSubmit(query: String): Boolean {
        if (fragments.isEmpty()) {
          return false
        }
        val text = query.trim()
        if (text.isNotEmpty()) {
          scope(Dispatchers.Default) {
            SearchSuggest(text).saveOrUpdate("name = ?", text)
          }
          Log.d(TAG, "search: $text")
          inputManager.hideSoftInputFromWindow(
            binding.editSearch.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
          )
          binding.editSearch.clearFocus()
          fragments.forEachIndexed { index, fragment ->
            if (fragment is SearchFragment) {
              Log.d(TAG, "index: $index, keyword: $query")
              fragment.keyword = query
              if (index == vp.currentItem) {
                fragment.refresh()
              }
            }
          }
          return true
        }
        return false
      }
    })
  }

  private fun querySearchIndex() {
    mHandler.removeCallbacks(searchIndexRunnable)
    mHandler.postDelayed(searchIndexRunnable, 300)
  }

  private val searchIndexRunnable = Runnable {
    scope {
      binding.indexRv.setDifferModels(Get<List<String>>(Api.VOD_SEARCH_INDEX) {
        setQuery("keyword", binding.editSearch.query.toString())
      }.await())
    }
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

  private suspend fun querySuggests() = withDefault {
    LitePal.limit(100)
      .order("updateAt desc").find<SearchSuggest>()
  }
}