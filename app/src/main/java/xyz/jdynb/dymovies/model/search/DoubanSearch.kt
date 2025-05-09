package xyz.jdynb.dymovies.model.search

import kotlinx.serialization.Serializable

@Serializable
data class DoubanSearch(
  val subjects: DouBanSubjectItems
)

@Serializable
data class DouBanSubjectItems(
  val items: List<DoubanSubjectItem> = listOf()
)
