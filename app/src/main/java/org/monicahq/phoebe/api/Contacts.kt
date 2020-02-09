package org.monicahq.phoebe.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Contacts {
    @GET("api/contacts")
    fun index(
        @Query("limit") limit: Int?,
        @Query("page") page: Int?,
        @Query("query") query: String?,
        @Query("sort") sort: String?
    ):
        Call<ContactIndexResponse>
}

class ContactIndexResponse(val data: List<Contact>)

class Contact(
    val id: Int,
    val first_name: String?,
    val last_name: String?,
    val nickname: String?,
    val gender: String?,
    val gender_type: String?,
    val is_partial: Boolean,
    val is_dead: Boolean,
    val account: Account,
    val created_at: String,
    val updated_at: String
)

class Account(val id: Int)
