package com.momcilo.postme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momcilo.postme.ui.viewModels.MapViewModel
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.google.type.LatLng
import com.momcilo.postme.data.entities.Marker
import com.momcilo.postme.data.entities.Position
import com.momcilo.postme.ui.viewModels.DeliveryViewModel


@Composable
fun HomeScreen(
    vm: DeliveryViewModel,
    map: MapViewModel,
    nav: NavHostController
)
{
    var selectedDelivery by remember { mutableStateOf<Marker?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(vm.markers) { delivery ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { selectedDelivery = delivery },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(text = "Title: ${delivery.title}")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Description: ${delivery.description}")
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Status: ${delivery.status}")
                            Spacer(modifier = Modifier.height(2.dp))
                            Button(onClick = {vm.finishDelivery(delivery.id)}) {
                                Text("Finish delivery")
                            }
                        }
                    }
                }
            }
        }


    selectedDelivery?.let { delivery ->
        AlertDialog(
            onDismissRequest = { selectedDelivery = null },
            title = { Text("Delivery's detail") },
            text = {
                Column {
                    Text("Title: ${delivery.title}")
                    Text("Description: ${delivery.description}")
                    Text("Mobile: ${delivery.status}")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    map.showDeliveryLocation(Position(), delivery.address)
                    nav.navigate("map")
                }) {
                    Text("Show on Map")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDelivery = null }) {
                    Text("Close")
                }
            }
        )
    }
}

/*


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            DeliveriesBox(vm,"Your deliveries","Show on map")
            Spacer(modifier = Modifier.height(20.dp))
            DeliveriesBox(vm,"Deliveries to finish","Finish delivery")
        }
    }

 */



@Composable
fun DeliveriesBox(
    vm: MapViewModel,
    title:String,
    btnText:String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .heightIn(min = 100.dp, max = 300.dp)
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(vm.markers) { marker ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = marker.title,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                text = marker.status,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = { /* akcija */ },
                            modifier = Modifier
                                .height(36.dp)
                                .defaultMinSize(minWidth = 120.dp)
                        ) {
                            Text(
                                text = btnText,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

