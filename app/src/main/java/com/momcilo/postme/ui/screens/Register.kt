package com.momcilo.postme.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.momcilo.postme.ui.viewModels.UserViewModel

@Composable
fun RegisterScreen(userViewModel: UserViewModel)
{
    Column {
        Title();
        RegisterFields(viewModel = userViewModel);
        Submit(viewModel = userViewModel)
    }

}

@Composable
fun Title(modifier: Modifier = Modifier)
{
    Text(text="Create account")
}

@Composable
fun RegisterFields(modifier: Modifier= Modifier, viewModel:UserViewModel)
{

    Column {
        TextField(
            value = viewModel.name,
            label = { Text("Enter your name") },
            onValueChange = {newText -> viewModel.name = newText},
        )
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
        TextField(
            value = viewModel.phone,
            label = { Text("Enter your phone") },
            onValueChange = {newText -> viewModel.phone = newText},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            singleLine = true
        )
    }
}

@Composable
fun Submit(viewModel:UserViewModel)
{
    Button(onClick = { viewModel.register() }) {
        Text("Register");
    }
}
