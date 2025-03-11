package xyz.jdynb.dymovies.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
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
import xyz.jdynb.dymovies.fragment.search.SearchFragment
import xyz.jdynb.dymovies.model.search.SearchSuggest
import xyz.jdynb.dymovies.model.vod.VodDetail
import xyz.jdynb.dymovies.model.vod.VodType

class SearchActivity : BaseActivity(), MenuProvider {

  companion object {

    private val TAG = SearchActivity::class.java.simpleName

  }

  private lateinit var binding: ActivitySearchBinding

  private lateinit var searchView: SearchView

  private val inputManager by lazy {
    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  }

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

    val tab = binding.searchTab
    val vp = binding.searchVp

    val types = mutableListOf(VodType(name = "全部"))
    val fragments = mutableListOf<Fragment>(SearchFragment.newInstance(0))

    scopeNetLife {
      binding.suggestRv.models = withDefault {
        querySuggests()
      }
      val result = Get<List<VodType>>(Api.VOD_TYPE_PARENT).await()
      types.addAll(result)
      fragments.addAll(result.map {
        SearchFragment.newInstance(it.id)
      })
      vp.adapter?.notifyItemRangeInserted(1, types.size)
    }

    vp.adapter = SearchPageAdapter(supportFragmentManager, lifecycle, fragments)

    TabLayoutMediator(tab, vp) { t, position ->
      t.text = types[position].name
    }.attach()

    binding.editSearch.setOnQueryTextFocusChangeListener { _, hasFocus ->
      binding.suggestRv.isVisible = hasFocus
      binding.searchVp.isVisible = !hasFocus
      binding.searchTab.isVisible = !hasFocus
    }

    binding.editSearch.setOnQueryTextListener(object : OnQueryTextListener {

      override fun onQueryTextChange(newText: String?): Boolean {
        val isBlank = newText.isNullOrBlank()
        if (isBlank) {
          Log.d(TAG, "onSearchChanged: $newText")
          scope {
            binding.suggestRv.setDifferModels(
              querySuggests().also {
                Log.d(TAG, "suggests: $it")
              }
            )
          }
        }
        return true
      }

      override fun onQueryTextSubmit(query: String): Boolean {
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

  override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    menuInflater.inflate(R.menu.menu_search, menu)
    val searchItem = menu.findItem(R.id.search)
    searchItem.expandActionView()
    searchView = searchItem.actionView as SearchView
    searchView.setIconifiedByDefault(false)
    searchView.isSubmitButtonEnabled = true
    searchView.queryHint = getString(R.string.searchbar_hint)

  }

  override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
    return true
  }

  override fun onDestroy() {
    super.onDestroy()
    // removeMenuProvider(this)
  }
}