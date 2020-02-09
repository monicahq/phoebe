package org.monicahq.phoebe.ui.login

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.monicahq.phoebe.R
import org.monicahq.phoebe.auth.AccountAuthenticator
import org.monicahq.phoebe.data.LoginDataSource
import org.monicahq.phoebe.data.Result
import org.monicahq.phoebe.data.model.LoggedInUser

class LoginActivity : LoginDataSource.OnAuthenticatorTaskListener, AppCompatActivity() { // AccountAuthenticatorActivity

    private var mAccountAuthenticatorResponse: AccountAuthenticatorResponse? = null
    private var mResultBundle: Bundle? = null

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    private fun setAccountAuthenticatorResult(result: Bundle?) {
        mResultBundle = result
    }

    /**
     * Retrieves the AccountAuthenticatorResponse from either the intent of the icicle, if the
     * icicle is non-zero.
     * @param icicle the save instance data of this Activity, may be null
     */
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        mAccountAuthenticatorResponse = intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse!!.onRequestContinued()
        }
        setContentView(R.layout.login_activity)
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    override fun finish() {
        if (mAccountAuthenticatorResponse != null) { // send the result bundle back if set, otherwise send an error.
            if (mResultBundle != null) {
                mAccountAuthenticatorResponse!!.onResult(mResultBundle)
            } else {
                mAccountAuthenticatorResponse!!.onError(
                    AccountManager.ERROR_CODE_CANCELED,
                    "canceled"
                )
            }
            mAccountAuthenticatorResponse = null
        }
        super.finish()
    }

    override fun onAuthenticatorTaskCallback(result: Result<LoggedInUser>?) {
        if (result is Result.Success) {

            setLoggedInUser(result.data)

            val bundle = Bundle()
            bundle.putString(AccountManager.KEY_ACCOUNT_NAME, result.data.email)
            bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.accountType)
            bundle.putString(AccountManager.KEY_AUTHTOKEN, result.data.token)

            setAccountAuthenticatorResult(bundle)
        }

        finish()
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser?) {
        try {

            val accountManager = AccountManager.get(this)
            if (loggedInUser != null) {
                val account = Account(loggedInUser.email, AccountAuthenticator.accountType)

                val bundle = Bundle()
                bundle.putString("id", loggedInUser.id.toString())
                bundle.putString("name", loggedInUser.name)
                bundle.putString("expires_in", loggedInUser.expires_in)
                bundle.putString("url", loggedInUser.url)

                // We never store the password
                if (accountManager.addAccountExplicitly(account, "", bundle)) {
                    accountManager.setAuthToken(
                        account,
                        AccountAuthenticator.authTokenType,
                        loggedInUser.token
                    )

                    /*
                    accountManager.setUserData(account, "id", loggedInUser.id.toString())
                    accountManager.setUserData(account, "name", loggedInUser.name)
                    accountManager.setUserData(account, "expires_in", loggedInUser.expires_in)
                    accountManager.setUserData(account, "url", loggedInUser.url)

                     */

                    accountManager.notifyAccountAuthenticated(account)
                }
            } else {
                val accounts = accountManager.getAccountsByType(AccountAuthenticator.accountType)
                accountManager.removeAccountExplicitly(accounts[0])
            }
        } catch (e: Exception) {
        }
    }
}
