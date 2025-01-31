package xyz.jdynb.dymovies.model.user

data class UserForm(
  val email: String = "",
  val code: String = "",
) {
  var emailError: Int? = null
  var codeError: Int? = null

  var disableCodeBtn = true
}
