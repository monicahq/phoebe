package org.monicahq.phoebe.api

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers

class LoginOauthResponse(val access_token: String, val expires_in: String)

interface OAuthApi {
    @FormUrlEncoded
    @Headers("No-Authentication: true")
    @POST("oauth/login")

    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ):
        Call<LoginOauthResponse>
}
