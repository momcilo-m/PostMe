package com.momcilo.postme.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.momcilo.postme.ui.viewModels.UserViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter


@Composable
fun LeaderboardScreen(vm: UserViewModel) {

    val listState = rememberLazyListState()


    LazyColumn(state = listState) {
        itemsIndexed(vm.users.value){ index,user ->
            LeaderboardItem(
                rank = index+1,
                username = user.username,
                score = user.points,
                imageUrl = user.photo
            )
//            if (index == users.lastIndex - 2) {
//                viewModel.loadNextPage()
//            }
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, username: String, score: Int, imageUrl: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rank",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )

        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = username, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Text(
            text = "$score pts",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
