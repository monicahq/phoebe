package org.monicahq.phoebe.data

import org.monicahq.phoebe.api.RestApi
import org.monicahq.phoebe.data.model.LoggedInUser
import android.os.AsyncTask
import org.monicahq.phoebe.data.model.Contact
import java.io.IOException

class ContactsDataSource(private val user: LoggedInUser, private val callback: (Result<List<Contact>>?) -> Unit) : AsyncTask<Void, Int, Result<List<Contact>>>() {

    override fun doInBackground(vararg params: Void?): Result<List<Contact>> {
        try {

            val api = RestApi.getApi(user.url).setToken(user.token)

            val response = api.contacts.index(null, null, null, null).execute()
            val result = if (response.isSuccessful) {
                val list = response.body()!!.data.map { contact ->
                    Contact(
                        contact.id,
                        contact.first_name,
                        contact.last_name,
                        contact.nickname,
                        contact.gender,
                        contact.is_partial,
                        contact.is_dead
                    )
                }
                Result.Success(list)
            } else {
                Result.Error(IOException(response.message()))
            }

            callback(result)

            return result
        } catch (e: Throwable) {
            return Result.Error(IOException("Error", e))
        }
    }
}
