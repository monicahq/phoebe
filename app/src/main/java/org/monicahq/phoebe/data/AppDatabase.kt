package org.monicahq.phoebe.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import org.monicahq.phoebe.BuildConfig
import org.monicahq.phoebe.data.dao.ContactDao
import org.monicahq.phoebe.data.model.Contact

interface RoomDatabaseFactory<T> {
    fun getInstance(context: Context, scope: CoroutineScope): T
    fun switchToInMemory(context: Context)
}

@Database(entities = [Contact::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao

    companion object : RoomDatabaseFactory<AppDatabase> {

        @Volatile
        var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of SampleDatabase.
         *
         * @param context The context.
         * @return The singleton instance of SampleDatabase.
         */
        override fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            BuildConfig.DATABASE_NAME
                        )
                            // .addCallback(ContactsDatabaseCallback(scope))
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }

        /**
         * Switches the internal implementation with an empty in-memory database.
         *
         * @param context The context.
         */
        @VisibleForTesting
        override fun switchToInMemory(context: Context) {
            INSTANCE = Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            ).build()
        }
    }

/*
    private class ContactsDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE.let { database ->
                scope.launch {
                    populateDatabase(database!!.contactDao())
                }
            }
        }

        suspend fun populateDatabase(contactDao: ContactDao) {
            // Delete all content here.
            contactDao.deleteAll()

            ContactsDataSource(loginRepository.user!!) { result ->
                if (result != null && result is Result.Success) {
                    contactDao.insertAll(result.data)
                }
            }.execute()

        }
    }

 */
}
