package com.momcilo.postme.data.states

import com.momcilo.postme.data.entities.User

sealed class AuthState {

    object Loading : AuthState()

    data class Authenticated(
        val user: User
    ): AuthState()

    object Unauthenticated: AuthState()

    data class Error(
        val code:Int? = null,
        val message: String
    ): AuthState()

    data class RegistrationSuccess(
        val userId: String,
        val email: String,
        val needsEmailVerification: Boolean
    ) : AuthState()

    data class RegistrationFailed(
        val email: String,
        val message:String,
        val code:Int? = null
    ) : AuthState()
}