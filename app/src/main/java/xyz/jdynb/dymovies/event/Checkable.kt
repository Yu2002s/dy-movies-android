package xyz.jdynb.dymovies.event

interface Checkable {

  fun toggle(toggleMode: Boolean)

  fun checkAll(isChecked: Boolean = true)

  fun reverseCheck()

  fun refresh() {}

  fun delete() {}
}