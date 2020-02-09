package org.monicahq.phoebe.ui

import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.monicahq.phoebe.R
import org.monicahq.phoebe.auth.AccountAuthenticator
import org.monicahq.phoebe.data.ContactsAdapter
import org.monicahq.phoebe.data.LoginDataSource
import org.monicahq.phoebe.data.LoginRepository

class MainActivity : LoginRepository.AccountListAdapterListener, FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loginRepository = LoginRepository(LoginDataSource(this), this)

        if (loginRepository.isLoggedIn) {
            setContentView(R.layout.main_activity)
        }
    }

    override fun createAccount() {
        val accountManager = AccountManager.get(this)
        accountManager.addAccount(
            AccountAuthenticator.accountType,
            AccountAuthenticator.authTokenType,
            null,
            null,
            this,
            { future ->
                if (future != null) {
                    // Get the result of the operation from the AccountManagerFuture.
                    // val result: Bundle = future.result
                    // val name = result.getString(AccountManager.KEY_ACCOUNT_NAME)

                    setContentView(R.layout.main_activity)
                    //redirect()
                }
            },
            null
        )
    }

    private fun redirect() {
        val intent = Intent(this, BasicActivity::class.java)

        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)

        this.startActivity(intent)
    }
}
