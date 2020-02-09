package org.monicahq.phoebe.ui.login

import org.monicahq.phoebe.data.model.LoggedInUser

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUser? = null,
    val error: Int? = null
)
