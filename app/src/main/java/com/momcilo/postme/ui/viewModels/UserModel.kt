package com.momcilo.postme.ui.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.momcilo.postme.data.entities.User
import com.momcilo.postme.data.repositories.UserRepository
import com.momcilo.postme.data.states.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val userRepo: UserRepository): ViewModel() {

    var name by mutableStateOf("");
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var phone by mutableStateOf("")

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        viewModelScope.launch {
            val res = userRepo.isLoggedIn()

            res.onSuccess { user->
                _authState.value = AuthState.Authenticated(user);
            }

            res.onFailure {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun register()
    {
        val user = User(username = name, name=name, email = email, phone = phone)
        viewModelScope.launch {
            val res = userRepo.registerUserWithEmail(user,password);

            res.onSuccess {user->
                _authState.value = AuthState.RegistrationSuccess(user?.uid ?: "", user?.email ?: email, false);
            }

            res.onFailure {error ->
                _authState.value = AuthState.RegistrationFailed(email,error.localizedMessage?:"Account not created");
            }
        }
    }
    fun login()
    {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val res = userRepo.loginUserWithEmail(email,password)

            res.onSuccess {user->
                _authState.value = AuthState.Authenticated(user)
            }

            res.onFailure{e->
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
}

class UserViewModelFactory(private val repository: UserRepository): ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(UserViewModel::class.java))
        {
            return UserViewModel(repository) as T;
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}