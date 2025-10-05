package com.momcilo.postme.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.momcilo.postme.ui.viewModels.UserViewModel

@Composable
fun LoginScreen(
    viewModel: UserViewModel,
    goToRegister: ()->Unit
)
{
    val context = LocalContext.current
    val toastEvent = viewModel.toastEvent.collectAsState(initial = null)

    LaunchedEffect(toastEvent.value) {
        toastEvent.value?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center)
    {
        Column {
            Title();
            LoginFields(viewModel);
            Register(goToRegister);
        }
    }
}

@Composable
fun Title()
{
    Text(text = "Login")
}


@Composable
fun LoginFields(viewModel:UserViewModel)
{

    Column {
        TextField(
            value = viewModel.email,
            label = { Text("Enter your email") },
            onValueChange = {newText -> viewModel.email = newText},
            singleLine = true
        )

        TextField(
            value = viewModel.password,
            label = { Text("Enter your password") },
            onValueChange = {newText -> viewModel.password = newText},
            singleLine = true
        )

        Button(onClick = { viewModel.login() }) {
            Text("Login")
        }
    }

}

@Composable
fun Register(goToRegister:()-> Unit)
{
    Text(
        modifier = Modifier.clickable{goToRegister()},
        text="You don't have account?"
    )
}