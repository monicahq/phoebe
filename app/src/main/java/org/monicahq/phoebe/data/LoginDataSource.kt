package org.monicahq.phoebe.data

import android.content.Context
import android.os.AsyncTask
import org.monicahq.phoebe.BuildConfig
import org.monicahq.phoebe.api.RestApi
import org.monicahq.phoebe.data.model.LoggedInUser
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */

class LoginDataSource(context: Context) : AsyncTask<String, Int, Result<LoggedInUser>>() {

    private var mWeakContext: WeakReference<Context>? = null
    private var mListener: WeakReference<OnAuthenticatorTaskListener>? = null

    init {
        mWeakContext = WeakReference(context.applicationContext)
        if (context is OnAuthenticatorTaskListener) {
            mListener = WeakReference(context as OnAuthenticatorTaskListener)
        }
    }

    override fun doInBackground(vararg params: String?): Result<LoggedInUser> {
        try {

            val api = RestApi.getApi(BuildConfig.APP_URL)

            val loginResponse = api.oauthApi.login(params[0]!!, params[1]!!).execute()
            if (loginResponse.isSuccessful && loginResponse.headers()["Content-Type"]!!.startsWith("text/html")) {
                return Result.Redirect(loginResponse.raw().body!!.string())
            } else if (loginResponse.isSuccessful) {
                val accessToken = loginResponse.body()!!.access_token
                val expiresIn = loginResponse.body()!!.expires_in

                val user: LoggedInUser = try {
                    val meResponse = api.setToken(accessToken).meApi.me().execute()
                    if (meResponse.isSuccessful) {
                        val userResponse = meResponse.body()!!.data
                        val id = userResponse.id
                        val name = userResponse.first_name + " " + userResponse.last_name
                        val email = userResponse.email
                        LoggedInUser(id, name, email, accessToken, expiresIn, api.Url)
                    } else {
                        LoggedInUser(0, "", params[0]!!, accessToken, expiresIn, api.Url)
                    }
                } catch (e: Throwable) {
                    // ignore
                    LoggedInUser(0, "", params[0]!!, accessToken, expiresIn, api.Url)
                }

                return Result.Success(user)
            } else {
                return Result.Error(IOException(loginResponse.message()))
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    override fun onPostExecute(result: Result<LoggedInUser>) {
        val listener = mListener!!.get()
        listener?.onAuthenticatorTaskCallback(result)
    }

    /*
     * Interface to retrieve data from recognition task
     */
    interface OnAuthenticatorTaskListener {
        fun onAuthenticatorTaskCallback(result: Result<LoggedInUser>?)
    }
}
