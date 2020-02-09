package org.monicahq.phoebe.auth

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import org.monicahq.phoebe.api.RestApi
import org.monicahq.phoebe.data.LoginDataSource
import org.monicahq.phoebe.data.Result
import org.monicahq.phoebe.ui.login.LoginActivity

class AccountAuthenticator(private var context: Context, private val dataSource: LoginDataSource) : AbstractAccountAuthenticator(context) {

    companion object {
        val accountType: String = "org.monicahq.phoebe"
        val authTokenType: String = "normal"
    }

        /**
     * Ask the authenticator for a localized label for the given authTokenType.
     * @param authTokenType the authTokenType whose label is to be returned, will never be null
     * @return the localized label of the auth token type, may be null if the type isn't known
     */
    override fun getAuthTokenLabel(authTokenType: String?): String {
        return "Monica"
    }

    /**
     * Checks that the user knows the credentials of an account.
     * @param response to send the result back to the AccountManager, will never be null
     * @param account the account whose credentials are to be checked, will never be null
     * @param options a Bundle of authenticator-specific options, may be null
     * @return a Bundle result or null if the result is to be returned via the response. The result
     * will contain either:
     *
     *  *  [AccountManager.KEY_INTENT], or
     *  *  [AccountManager.KEY_BOOLEAN_RESULT], true if the check succeeded, false otherwise
     *  *  [AccountManager.KEY_ERROR_CODE] and [AccountManager.KEY_ERROR_MESSAGE] to
     * indicate an error
     *
     * @throws NetworkErrorException if the authenticator could not honor the request due to a
     * network error
     */
    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle {

        val accountManager = AccountManager.get(context)
        val token = accountManager.peekAuthToken(account, authTokenType)
        val url = accountManager.getUserData(account, "url")

        val api = RestApi.getApi(url).setToken(token)

        val meResponse = api.meApi.me().execute()

        val bundle = Bundle()
        bundle.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, meResponse.isSuccessful)

        return bundle
    }

    /**
     * Update the locally stored credentials for an account.
     * @param response to send the result back to the AccountManager, will never be null
     * @param account the account whose credentials are to be updated, will never be null
     * @param authTokenType the type of auth token to retrieve after updating the credentials,
     * may be null
     * @param options a Bundle of authenticator-specific options, may be null
     * @return a Bundle result or null if the result is to be returned via the response. The result
     * will contain either:
     *
     *  *  [AccountManager.KEY_INTENT], or
     *  *  [AccountManager.KEY_ACCOUNT_NAME] and [AccountManager.KEY_ACCOUNT_TYPE] of
     * the account whose credentials were updated, or
     *  *  [AccountManager.KEY_ERROR_CODE] and [AccountManager.KEY_ERROR_MESSAGE] to
     * indicate an error
     *
     * @throws NetworkErrorException if the authenticator could not honor the request due to a
     * network error
     */
    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Gets an authtoken for an account.
     *
     * If not `null`, the resultant [Bundle] will contain different sets of keys
     * depending on whether a token was successfully issued and, if not, whether one
     * could be issued via some [android.app.Activity].
     *
     *
     * If a token cannot be provided without some additional activity, the Bundle should contain
     * [AccountManager.KEY_INTENT] with an associated [Intent]. On the other hand, if
     * there is no such activity, then a Bundle containing
     * [AccountManager.KEY_ERROR_CODE] and [AccountManager.KEY_ERROR_MESSAGE] should be
     * returned.
     *
     *
     * If a token can be successfully issued, the implementation should return the
     * [AccountManager.KEY_ACCOUNT_NAME] and [AccountManager.KEY_ACCOUNT_TYPE] of the
     * account associated with the token as well as the [AccountManager.KEY_AUTHTOKEN]. In
     * addition [AbstractAccountAuthenticator] implementations that declare themselves
     * `android:customTokens=true` may also provide a non-negative [ ][.KEY_CUSTOM_TOKEN_EXPIRY] long value containing the expiration timestamp of the expiration
     * time (in millis since the unix epoch), tokens will be cached in memory based on
     * application's packageName/signature for however long that was specified.
     *
     *
     * Implementers should assume that tokens will be cached on the basis of account and
     * authTokenType. The system may ignore the contents of the supplied options Bundle when
     * determining to re-use a cached token. Furthermore, implementers should assume a supplied
     * expiration time will be treated as non-binding advice.
     *
     *
     * Finally, note that for `android:customTokens=false` authenticators, tokens are cached
     * indefinitely until some client calls [ ][AccountManager.invalidateAuthToken].
     *
     * @param response to send the result back to the AccountManager, will never be null
     * @param account the account whose credentials are to be retrieved, will never be null
     * @param xauthTokenType the type of auth token to retrieve, will never be null
     * @param options a Bundle of authenticator-specific options. It always contains
     * [AccountManager.KEY_CALLER_PID] and [AccountManager.KEY_CALLER_UID]
     * fields which will let authenticator know the identity of the caller.
     * @return a Bundle result or null if the result is to be returned via the response.
     * @throws NetworkErrorException if the authenticator could not honor the request due to a
     * network error
     */
    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account,
        xauthTokenType: String?,
        options: Bundle?
    ): Bundle {
        val am = AccountManager.get(context)

        var authToken = am.peekAuthToken(account, xauthTokenType)
        var expiry = ""

        if (TextUtils.isEmpty(authToken)) {
            val result = dataSource.execute(account.name, am.getPassword(account)).get()

            if (result is Result.Success) {
                authToken = result.data.token
                expiry = result.data.expires_in
            }
        }

        if (!TextUtils.isEmpty(authToken)) {
            val result = Bundle()
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            if (!TextUtils.isEmpty(expiry)) {
                result.putString(KEY_CUSTOM_TOKEN_EXPIRY, expiry)
            }

            return result
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our LoginActivity.
        val intent = Intent(context, LoginActivity::class.java)

        intent.putExtra(accountType, accountType)
        intent.putExtra(authTokenType, authTokenType)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)

        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)

        return bundle
    }

    /**
     * Checks if the account supports all the specified authenticator specific features.
     * @param response to send the result back to the AccountManager, will never be null
     * @param account the account to check, will never be null
     * @param features an array of features to check, will never be null
     * @return a Bundle result or null if the result is to be returned via the response. The result
     * will contain either:
     *
     *  *  [AccountManager.KEY_INTENT], or
     *  *  [AccountManager.KEY_BOOLEAN_RESULT], true if the account has all the features,
     * false otherwise
     *  *  [AccountManager.KEY_ERROR_CODE] and [AccountManager.KEY_ERROR_MESSAGE] to
     * indicate an error
     *
     * @throws NetworkErrorException if the authenticator could not honor the request due to a
     * network error
     */
    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Returns a Bundle that contains the Intent of the activity that can be used to edit the
     * properties. In order to indicate success the activity should call response.setResult()
     * with a non-null Bundle.
     * @param response used to set the result for the request. If the Constants.INTENT_KEY
     * is set in the bundle then this response field is to be used for sending future
     * results if and when the Intent is started.
     * @param accountType the AccountType whose properties are to be edited.
     * @return a Bundle containing the result or the Intent to start to continue the request.
     * If this is null then the request is considered to still be active and the result should
     * sent later using response.
     */
    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Adds an account of the specified accountType.
     * @param response to send the result back to the AccountManager, will never be null
     * @param accountType the type of account to add, will never be null
     * @param authTokenType the type of auth token to retrieve after adding the account, may be null
     * @param requiredFeatures a String array of authenticator-specific features that the added
     * account must support, may be null
     * @param options a Bundle of authenticator-specific options. It always contains
     * [AccountManager.KEY_CALLER_PID] and [AccountManager.KEY_CALLER_UID]
     * fields which will let authenticator know the identity of the caller.
     * @return a Bundle result or null if the result is to be returned via the response. The result
     * will contain either:
     *
     *  *  [AccountManager.KEY_INTENT], or
     *  *  [AccountManager.KEY_ACCOUNT_NAME] and [AccountManager.KEY_ACCOUNT_TYPE] of
     * the account that was added, or
     *  *  [AccountManager.KEY_ERROR_CODE] and [AccountManager.KEY_ERROR_MESSAGE] to
     * indicate an error
     *
     * @throws NetworkErrorException if the authenticator could not honor the request due to a
     * network error
     */
    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        val intent = Intent(context, LoginActivity::class.java)

        intent.putExtra(accountType, accountType)
        intent.putExtra(authTokenType, authTokenType)
        intent.putExtra("is_adding_new_account", true)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)

        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)

        return bundle
    }
}
