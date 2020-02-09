package org.monicahq.phoebe.data

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import org.monicahq.phoebe.auth.AccountAuthenticator
import org.monicahq.phoebe.data.model.LoggedInUser
import java.lang.ref.WeakReference

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(private val dataSource: LoginDataSource, private val activity: Context) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null

    val isLoggedIn: Boolean
        get() = user != null

    private var accountListAdapterListener: WeakReference<AccountListAdapterListener>? = null

    init {
        try {
            if (activity is AccountListAdapterListener) {
                accountListAdapterListener = WeakReference(activity as AccountListAdapterListener)
            }
            getToken()
        } catch (e: Exception) {
            user = null
        }
    }

    private fun getToken() {
        val accountManager = AccountManager.get(activity)
        val accounts = accountManager.getAccountsByType(AccountAuthenticator.accountType)
        if (accounts.count() > 0) {
            val account = accounts[0]

            val token = accountManager.peekAuthToken(account, AccountAuthenticator.authTokenType)

            val id = accountManager.getUserData(account, "id").toInt()
            val name = accountManager.getUserData(account, "name")
            val expiresIn = accountManager.getUserData(account, "expires_in")
            val url = accountManager.getUserData(account, "url")

            user = LoggedInUser(id, name, account.name, token, expiresIn, url)
        } else {
            if (accountListAdapterListener != null) {
                val listener = accountListAdapterListener!!.get()
                listener?.createAccount()
            }
        }
    }

    /*
    private class OnTokenAcquired(val repository: LoginRepository) : AccountManagerCallback<Bundle> {

        override fun run(result: AccountManagerFuture<Bundle>) {
            // Get the result of the operation from the AccountManagerFuture.
            val bundle: Bundle = result.result

            val launch: Intent? = bundle.get(AccountManager.KEY_INTENT) as? Intent
            if (launch != null) {
                repository.context.startActivityForResult(launch, 0)
            }
            else {
                // The token is a named value in the bundle. The name of the value
                // is stored in the constant AccountManager.KEY_AUTHTOKEN.
                val token = bundle.getString(AccountManager.KEY_AUTHTOKEN, "")

                // Not the best but we initialize it without all properties
                repository.user = LoggedInUser(token, "", "", "")
            }
        }
    }

     */

    fun logout() {
        setLoggedInUser("", "", null)
    }

    fun login(username: String, password: String) {
        // handle login
        dataSource.execute(username, password)

        /*
        if (result is Result.Success) {
            setLoggedInUser(username, password, result.data)
        }
        return result

         */
    }

    fun setLoggedInUser(username: String, password: String, loggedInUser: LoggedInUser?) {
        user = loggedInUser
        try {

            val accountManager = AccountManager.get(activity)
            if (loggedInUser != null) {
                val account = Account(username, AccountAuthenticator.accountType)

                if (accountManager.addAccountExplicitly(account, password, Bundle())) {
                    accountManager.setAuthToken(
                        account,
                        AccountAuthenticator.authTokenType,
                        loggedInUser.token
                    )

                    accountManager.setUserData(account, "id", loggedInUser.id.toString())
                    accountManager.setUserData(account, "name", loggedInUser.name)
                    accountManager.setUserData(account, "expires_in", loggedInUser.expires_in)
                    accountManager.setUserData(account, "url", loggedInUser.url)

                    accountManager.notifyAccountAuthenticated(account)
                }
            } else {
                val accounts = accountManager.getAccountsByType(AccountAuthenticator.accountType)
                accountManager.removeAccountExplicitly(accounts[0])
            }
        } catch (e: Exception) {
            user = null
        }
    }

    interface AccountListAdapterListener {
        fun createAccount()
    }
}
