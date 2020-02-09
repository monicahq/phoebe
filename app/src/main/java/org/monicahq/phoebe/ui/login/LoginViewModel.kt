package org.monicahq.phoebe.ui.login

import android.util.Patterns
import androidx.lifecycle.*
import org.monicahq.phoebe.R
import org.monicahq.phoebe.data.LoginRepository

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult: MutableLiveData<LoginResult> by lazy {
        MutableLiveData<LoginResult>()
    }
    val loginResult: LiveData<LoginResult> = _loginResult

    init {
        if (loginRepository.isLoggedIn) {
            _loginResult.value = LoginResult(success = loginRepository.user)
        } else {
            _loginResult.value = null
        }
    }

    fun login(username: String, password: String) {
        loginRepository.login(username, password)
    }

    fun logout() {
        _loginResult.value = null
        loginRepository.logout()
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }
}
