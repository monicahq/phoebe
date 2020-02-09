package org.monicahq.phoebe

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.monicahq.phoebe.data.AppDatabase
import org.monicahq.phoebe.data.dao.ContactDao
import org.monicahq.phoebe.data.model.Contact
import java.io.IOException
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ApplicationProvider
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

/**
 * This is not meant to be a full set of tests. For simplicity, most of your samples do not
 * include tests. However, when building the Room, it is helpful to make sure it works before
 * adding the UI.
 */

@RunWith(AndroidJUnit4::class)
class WordDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var contactDao: ContactDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        contactDao = db.contactDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetContact() = runBlocking {
        val contact = getContact(0, "firstName", "lastName")
        contactDao.insert(contact)
        val allContacts = contactDao.getAll().waitForValue()
        assertEquals(allContacts[0], contact)
    }

    @Test
    @Throws(Exception::class)
    fun getAllWords() = runBlocking {
        val contact1 = getContact(0, "firstName", "lastName")
        contactDao.insert(contact1)
        val contact2 = getContact(1, "firstName", "lastName")
        contactDao.insert(contact2)
        val allContacts = contactDao.getAll().waitForValue()
        assertEquals(allContacts[0], contact1)
        assertEquals(allContacts[1], contact2)
    }

    @Test
    @Throws(Exception::class)
    fun deleteAll() = runBlocking {
        val contact1 = getContact(0, "firstName", "lastName")
        contactDao.insert(contact1)
        val contact2 = getContact(1, "firstName", "lastName")
        contactDao.insert(contact2)
        contactDao.deleteAll()
        val allContacts = contactDao.getAll().waitForValue()
        assertTrue(allContacts.isEmpty())
    }

    private fun getContact(id: Int, firstName: String, lastName: String): Contact {
        return Contact(id, firstName, lastName, "", "", isPartial = false, isDead = false)
    }
}
