package org.monicahq.phoebe.data

import androidx.lifecycle.LiveData
import org.monicahq.phoebe.data.dao.ContactDao
import org.monicahq.phoebe.data.model.Contact

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class ContactsRepository(private val contactsDao: ContactDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allContacts: LiveData<List<Contact>> = contactsDao.getAll()

    suspend fun insert(contact: Contact) {
        contactsDao.insert(contact)
    }
}
