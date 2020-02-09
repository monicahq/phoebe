package org.monicahq.phoebe.auth

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.monicahq.phoebe.data.LoginDataSource

class AuthenticatorService : Service() {
    private var authenticator: AccountAuthenticator? = null

    override fun onCreate() {
        authenticator = AccountAuthenticator(this, LoginDataSource(this))
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    override fun onBind(intent: Intent?): IBinder {
        return authenticator!!.iBinder
    }
}
