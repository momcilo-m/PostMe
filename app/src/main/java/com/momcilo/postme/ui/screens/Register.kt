package com.momcilo.postme.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.momcilo.postme.data.entities.Position
import com.momcilo.postme.ui.viewModels.UserViewModel
import com.momcilo.postme.R

@Composable
fun RegisterScreen(userViewModel: UserViewModel)
{
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center)
    {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileImage(userViewModel.imageUri)
            Text(text="Create account")
            RegisterFields(viewModel = userViewModel);
            Submit(viewModel = userViewModel)
        }
    }


}

@Composable
fun TitleReg(modifier: Modifier = Modifier)
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

@Composable
fun ProfileImage(imageUri:MutableState<Uri?>)
{
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUri.value = uri
        }
    }

    val imageModel: Any = imageUri.value ?: R.mipmap.profile

    AsyncImage(
        model = imageModel,
        contentDescription = "Profile Image",
        modifier = Modifier.size(120.dp).clip(CircleShape).clickable{picker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )},
        contentScale = ContentScale.Crop)
}