package xyz.jdynb.dymovies.utils

import android.graphics.Color
import java.util.Random

object ColorUtils {

  private val random = Random()

  fun generateRandomVibrantColor(): Int {
    // Generate random hue (0-360)
    val hue = random.nextInt(361)
    // Set saturation to a high value (e.g., 0.8 to 1.0) for vibrancy
    val saturation = 0.8f + random.nextFloat() * 0.2f
    // Set lightness to a mid-range value (e.g., 0.4 to 0.8) to avoid very dark or very light colors
    val lightness = 0.4f + random.nextFloat() * 0.4f
    // Convert HSL to RGB
    return hslToRgb(hue, saturation, lightness)
  }

  private fun hslToRgb(hue: Int, saturation: Float, lightness: Float): Int {
    val h = hue / 360f
    val s = saturation
    val l = lightness

    val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
    val p = 2 * l - q

    val r = hueToRgb(p, q, h + 1f / 3f)
    val g = hueToRgb(p, q, h)
    val b = hueToRgb(p, q, h - 1f / 3f)

    return Color.rgb((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
  }

  private fun hueToRgb(p: Float, q: Float, t: Float): Float {
    var tempT = t
    if (tempT < 0f) tempT += 1f
    if (tempT > 1f) tempT -= 1f
    if (tempT < 1f / 6f) return p + (q - p) * 6f * tempT
    if (tempT < 1f / 2f) return q
    if (tempT < 2f / 3f) return p + (q - p) * (2f / 3f - tempT) * 6f
    return p
  }
}