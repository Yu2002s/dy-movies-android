package xyz.jdynb.dymovies.adapter;

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlin.reflect.KClass

/**
 * 通用的 ViewPager2 adapter
 */
class CommonViewPagerAdapter(
  fm: FragmentManager,
  lifecycle: Lifecycle,
  private val fragments: List<KClass<out Fragment>>
) :
FragmentStateAdapter(fm, lifecycle) {
    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
      .javaObjectType.getConstructor().newInstance()
}