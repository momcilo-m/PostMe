package com.momcilo.postme.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import com.momcilo.postme.ui.viewModels.UserViewModel

@Composable
fun LoginScreen(
    viewModel:UserViewModel
)
{
    Column {
        Title();
        LoginFields(viewModel);
        Register();
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

        Register();
    }
}

@Composable
fun Register()
{
    Text("You don't have account?")
}


