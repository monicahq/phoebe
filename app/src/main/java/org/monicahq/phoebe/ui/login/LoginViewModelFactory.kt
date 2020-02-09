package org.monicahq.phoebe.ui.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.monicahq.phoebe.data.LoginDataSource
import org.monicahq.phoebe.data.LoginRepository

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class LoginViewModelFactory(private val activity: Activity) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(
                LoginRepository(
                    LoginDataSource(activity), activity
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
