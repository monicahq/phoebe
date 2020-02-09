package org.monicahq.phoebe.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import org.monicahq.phoebe.data.model.Contact

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts")
    fun getAll(): LiveData<List<Contact>>

    @Query("SELECT * FROM contacts WHERE id IN (:contactIds)")
    fun loadAllByIds(contactIds: IntArray): List<Contact>

    @Query("SELECT * FROM contacts WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): Contact

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(contacts: List<Contact>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(contact: Contact)

    @Delete
    fun delete(contact: Contact)

    @Query("DELETE FROM contacts")
    fun deleteAll()
}
