package com.momcilo.postme.ui.viewModels

import android.net.Uri
import android.util.Log
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
import com.momcilo.postme.data.cache.MarkerCache
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class UserViewModel(
    private val context: PostMeApplication,
    private val userRepo: UserRepository
): ViewModel() {

    var name by mutableStateOf("");
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var phone by mutableStateOf("")
    var imageUri = mutableStateOf<Uri?>("android.resource://${this.context.packageName}/${R.mipmap.profile}".toUri())

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    var currentUser by mutableStateOf(User());

    //Buffer za snack bar
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        viewModelScope.launch {
            userRepo.isLoggedIn()
            .onSuccess { user->
                _authState.value = AuthState.Authenticated(user);
                currentUser = user;
                Log.d("LOGINUSER","AUTH SI NA STARTU")
            }
            .onFailure { e->
                _authState.value = AuthState.Unauthenticated
                Log.d("LOGINUSER","NISI AUTH NA STARTU")
            }

            userRepo.trackUser()

            userRepo.users.observeForever { list ->
                _users.value = list
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        userRepo.users.removeObserver{}
    }

    fun register()
    {
        val user = User(username = name, name=name, email = email, phone = phone)
        viewModelScope.launch {
            _authState.value = AuthState.Loading;
            val res = userRepo.registerUserWithEmail(user,password,imageUri.value);
            res.onSuccess {user->
                _authState.value = AuthState.RegistrationSuccess(user?.uid ?: "", user?.email ?: email, false);
                clearUserInput()
            }

            res.onFailure {error ->
                _authState.value = AuthState.RegistrationFailed(email,error.localizedMessage?:"Account not created");
                _toastEvent.emit(error.localizedMessage ?: "Error while register")
            }
        }
    }
    fun login()
    {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val res = userRepo.loginUserWithEmail(email,password)

            res.onSuccess {user->
                Log.d("LOGINUSER","PROSO JE U VM")
                _authState.value = AuthState.Authenticated(user)
                currentUser = user;
            }

            res.onFailure{e->
                Log.d("LOGINUSER","NIJE PROSO U VM")
                _authState.value = AuthState.Unauthenticated
                _toastEvent.emit(e.localizedMessage ?: "Error while login")
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

            MarkerCache.clear()
            clearUserInput()
        }
    }

    fun clearUserInput()
    {
        name = ""
        email = ""
        password = ""
        phone = ""
        imageUri.value = "android.resource://${context.packageName}/${R.mipmap.profile}".toUri()
    }


    fun transferPoints(name: String, points:Int=0)
    {
        if(points == 0)
            return;

        viewModelScope.launch {
            val res = userRepo.sendPoints(name,points)

            res.onSuccess {
                _toastEvent.emit("Points are successfully send")
            }

            res.onFailure {e->
                _toastEvent.emit(e.localizedMessage ?: "Error while sending points")
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