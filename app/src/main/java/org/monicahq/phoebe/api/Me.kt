package org.monicahq.phoebe.api

import retrofit2.Call
import retrofit2.http.GET

class UserResponseData(val data: UserResponse)
class UserResponse(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val email: String,
    val timezone: String
)

interface MeApi {
    @GET("api/me")

    fun me():
        Call<UserResponseData>
}
