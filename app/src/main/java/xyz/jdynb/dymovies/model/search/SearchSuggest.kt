package xyz.jdynb.dymovies.model.search

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

@Keep
@Serializable
data class SearchSuggest(
  @SerialName("name")
  @Column(nullable = false)
  val name: String = "",
  @SerialName("updateAt")
  @Column(nullable = false)
  val updateAt: Long = System.currentTimeMillis()
): LitePalSupport() {

  val id: Long = 0

  fun update() {
    update(id)
  }
}
