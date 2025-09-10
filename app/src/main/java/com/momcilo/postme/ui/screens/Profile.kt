package com.momcilo.postme.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.momcilo.postme.ui.viewModels.UserViewModel

@Composable
fun ProfileScreen(
    viewModel:UserViewModel
)
{

    Column {
        Text("Ime Prezime")
        Text("momcilo@momcilo.rs")
        Button(onClick = {viewModel.logout()}) {
            Text("Logout!")
        }
    }


}
