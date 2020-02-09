package org.monicahq.phoebe.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import org.monicahq.phoebe.R
import org.monicahq.phoebe.data.model.LoggedInUser
import org.monicahq.phoebe.databinding.LoginFragmentBinding
import org.monicahq.phoebe.ui.afterTextChanged

class Login2Fragment : Fragment() {

    private val loginViewModel: LoginViewModel by activityViewModels { LoginViewModelFactory(activity!!) }

    private var _binding: LoginFragmentBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val b get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginViewModel.loginFormState.observe(viewLifecycleOwner, Observer { loginState ->
            // disable login button unless both username / password is valid
            b.login.isEnabled = loginState.isDataValid

            if (loginState?.usernameError != null) {
                b.username.error = getString(loginState.usernameError)
            }
            if (loginState?.passwordError != null) {
                b.password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(viewLifecycleOwner, Observer { loginResult ->
            b.loading.visibility = View.GONE
            if (loginResult?.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult?.success != null) {
                updateUiWithUser(loginResult.success)
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            loginViewModel.logout()
            // activity?.finish()
        }

        b.username.afterTextChanged {
            loginViewModel.loginDataChanged(
                b.username.text.toString(),
                b.password.text.toString()
            )
        }

        b.password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    b.username.text.toString(),
                    b.password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            b.username.text.toString(),
                            b.password.text.toString()
                        )
                }
                false
            }

            b.login.setOnClickListener {
                b.loading.visibility = View.VISIBLE
                loginViewModel.login(b.username.text.toString(), b.password.text.toString())
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUser) {
        val welcome = getString(R.string.welcome)
        val displayName = model.name
        // TODO : initiate successful logged in experience
        Toast.makeText(
            context,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show()
    }
}
