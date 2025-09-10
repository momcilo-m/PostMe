package com.momcilo.postme.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.momcilo.postme.ui.viewModels.UserViewModel

@Composable
fun LoginScreen(
    viewModel: UserViewModel,
    goToRegister: ()->Unit
)
{
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
        )

        TextField(
            value = viewModel.password,
            label = { Text("Enter your password") },
            onValueChange = {newText -> viewModel.password = newText}
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