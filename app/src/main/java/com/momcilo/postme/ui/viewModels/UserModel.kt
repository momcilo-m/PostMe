package com.momcilo.postme.ui.viewModels

import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.momcilo.postme.R
import com.momcilo.postme.activities.PostMeApplication
import com.momcilo.postme.data.entities.User
import com.momcilo.postme.data.repositories.UserRepository
import com.momcilo.postme.data.states.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class UserViewModel(
    private val context: PostMeApplication,
    private val userRepo: UserRepository
): ViewModel() {

    var name by mutableStateOf("");
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var phone by mutableStateOf("")

//    private val _imageUri  = mutableStateOf<Uri?>(null)
//    val imageUri: MutableState<Uri?> = _imageUri;
    var imageUri = mutableStateOf<Uri?>("android.resource://${this.context.packageName}/${R.mipmap.profile}".toUri())

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    var currentUser by mutableStateOf(User());


    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        viewModelScope.launch {
            userRepo.isLoggedIn()
            .onSuccess { user->
                _authState.value = AuthState.Authenticated(user);
                currentUser = user;
                Log.d("AUTH",user.email)
                Log.d("AUTH",user.name)
                Log.d("AUTH",user.photo)
            }
            .onFailure {
                _authState.value = AuthState.Unauthenticated
            }

            userRepo.getUsers()
                .onSuccess { data->
                    _users.value += data
                }
                .onFailure {

                }

        }
    }

    fun register()
    {
        val user = User(username = name, name=name, email = email, phone = phone)
        viewModelScope.launch {
            val res = userRepo.registerUserWithEmail(user,password,imageUri.value);

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
                currentUser = user;
            }

            res.onFailure{e->
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun logout()
    {
        viewModelScope.launch {
            val result = userRepo.logout();

            result.onSuccess {
                _authState.value = AuthState.Unauthenticated
                currentUser = User();
            }
        }
    }

    fun getUsers()
    {
        viewModelScope.launch {
            userRepo.getUsers()
                .onSuccess { data->
                    _users.value += data
                }
                .onFailure {

                }
        }
    }

}

class UserViewModelFactory(private val app: PostMeApplication,private val repository: UserRepository): ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(UserViewModel::class.java))
        {
            return UserViewModel(app,repository) as T;
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}