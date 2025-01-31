package xyz.jdynb.dymovies.utils.cache

import android.graphics.drawable.ColorDrawable
import androidx.collection.LruCache

object ColorDrawableCache {
  private const val MAX_CACHE_SIZE = 50 // Adjust as needed
  private val colorDrawableCache = LruCache<Int, ColorDrawable>(MAX_CACHE_SIZE)

  fun getColorDrawable(color: Int): ColorDrawable {
    var colorDrawable = colorDrawableCache.get(color)
    if (colorDrawable == null) {
      colorDrawable = ColorDrawable(color)
      colorDrawableCache.put(color, colorDrawable)
    }
    return colorDrawable
  }
}