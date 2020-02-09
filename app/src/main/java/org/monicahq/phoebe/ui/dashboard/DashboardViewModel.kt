package org.monicahq.phoebe.ui.dashboard

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.monicahq.phoebe.data.*
import org.monicahq.phoebe.data.model.Contact

class DashboardViewModel(application: Application, private val loginRepository: LoginRepository) : AndroidViewModel(application) {

    // The ViewModel maintains a reference to the repository to get data.
    private val repository: ContactsRepository
    // LiveData gives us updated words when they change.
    val contacts: LiveData<List<Contact>>

    init
    {
        val contactsDao = AppDatabase.getInstance(application, viewModelScope).contactDao()
        repository = ContactsRepository(contactsDao)
        contacts = repository.allContacts

        if (loginRepository.user != null) {
            ContactsDataSource(loginRepository.user!!) { result ->
                if (result != null && result is Result.Success) {
                    contactsDao.insertAll(result.data)
                }
            }.execute()
        }
    }

    /**
     * The implementation of insert() in the database is completely hidden from the UI.
     * Room ensures that you're not doing any long running operations on
     * the main thread, blocking the UI, so we don't need to handle changing Dispatchers.
     * ViewModels have a coroutine scope based on their lifecycle called
     * viewModelScope which we can use here.
     */
    fun insert(contact: Contact) = viewModelScope.launch {
        repository.insert(contact)
    }

}
