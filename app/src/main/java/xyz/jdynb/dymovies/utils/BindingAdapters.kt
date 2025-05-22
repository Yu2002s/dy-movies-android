package xyz.jdynb.dymovies.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.drake.brv.utils.bindingAdapter
import xyz.jdynb.dymovies.R
import xyz.jdynb.dymovies.model.download.Download
import xyz.jdynb.dymovies.utils.cache.ColorDrawableCache
import xyz.jdynb.dymovies.view.RoundImageView

object BindingAdapters {

  private val fadeTransition = DrawableTransitionOptions.withCrossFade()

  @JvmStatic
  @BindingAdapter("imageUrl")
  fun loadImage(view: ImageView, url: String?) {
    if (!url.isNullOrEmpty()) {
      val randomColor = ColorUtils.generateRandomVibrantColor()
      val colorDrawable = ColorDrawableCache.getColorDrawable(randomColor)
      Glide.with(view.context)
        .load(url)
        .placeholder(colorDrawable)
        .transition(fadeTransition)
        .into(view)
    }
  }

  @JvmStatic
  @BindingAdapter("imageUrl")
  fun loadImage(view: RoundImageView, url: String?) {
    val randomColor = ColorUtils.generateRandomVibrantColor()
    val colorDrawable = ColorDrawableCache.getColorDrawable(randomColor)
    if (!url.isNullOrEmpty()) {
      Glide.with(view.context)
        .load(url)
        .centerCrop()
        .placeholder(colorDrawable)
        .transition(fadeTransition)
        .into(view)
    } else {
      view.setImageDrawable(colorDrawable)
    }
  }

  @JvmStatic
  @BindingAdapter("android:src") // Use the standard android:src attribute
  fun setImageResource(view: ImageView, resourceId: Int) {
    if (resourceId != 0) { // Check for a valid resource ID
      view.setImageResource(resourceId)
    }
  }

  @JvmStatic
  @BindingAdapter("drawableStartCompat")
  fun setDrawableStart(view: TextView, resourceId: Int) {
    if (resourceId != 0) {
      view.setCompoundDrawablesWithIntrinsicBounds(
        ContextCompat.getDrawable(
          view.context,
          resourceId
        ), null, null, null
      )
    }
  }

  @JvmStatic
  @BindingAdapter("htmlText")
  fun setHtmlText(tv: TextView, htmlContent: String?) {
    if (!htmlContent.isNullOrEmpty()) {
      tv.text = HtmlCompat.fromHtml(htmlContent, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }
  }

  @JvmStatic
  @BindingAdapter("time")
  fun setTimeText(tv: TextView, time: Long) {
    tv.text = time.getTime()
  }

  @JvmStatic
  @BindingAdapter("realTime")
  fun setRealTime(tv: TextView, time: Long) {
    tv.text = time.getRelTime()
  }

  @JvmStatic
  @BindingAdapter("isSelected")
  fun setChecked(view: View, isSelected: Boolean) {
    view.isSelected = isSelected
  }

  @JvmStatic
  @BindingAdapter("visible")
  fun setVisible(view: View, isVisible: Boolean) {
    view.isVisible = isVisible
  }

  @JvmStatic
  @BindingAdapter("videoSrc")
  fun setVideoSource(view: ImageView, download: Download) {
    if (!download.isCompleted) {
      view.setImageResource(R.drawable.baseline_download_for_offline_24)
      return
    }
    val randomColor = ColorUtils.generateRandomVibrantColor()
    val colorDrawable = ColorDrawableCache.getColorDrawable(randomColor)
    Glide.with(view)
      .load(download.downloadPath)
      .centerCrop()
      .placeholder(colorDrawable)
      .transition(fadeTransition)
      .into(view)
  }

  @JvmStatic
  @BindingAdapter("models")
  fun setModels(view: RecyclerView, models: List<Any>) {
    view.bindingAdapter.models = models
  }
}