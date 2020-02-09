package org.monicahq.phoebe.data.model

import java.io.Serializable

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val id: Int,
    val name: String,
    val email: String,
    val token: String,
    val expires_in: String,
    val url: String
) : Serializable
