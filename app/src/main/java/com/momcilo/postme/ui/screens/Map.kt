package com.momcilo.postme.ui.screens

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Point
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.momcilo.postme.R
import com.momcilo.postme.data.entities.Marker
import com.momcilo.postme.data.entities.Position
import com.momcilo.postme.ui.viewModels.LocationViewModel
import com.momcilo.postme.ui.viewModels.MapViewModel


@SuppressLint("MissingPermission")
@Composable
fun MapScreen(locVM: MapViewModel,lVm: LocationViewModel)
{
    //Start location
    val lat = lVm.location.value?.latitude ?: 0.0;
    val lng = lVm.location.value?.longitude ?: 0.0;

    //Map settings
    val cameraPositionState = rememberCameraPositionState() { position = CameraPosition.fromLatLngZoom(LatLng(lat,lng), 100f) }
    var uiSettings by remember { mutableStateOf(MapUiSettings()) }
    var properties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL, isTrafficEnabled = false, isBuildingEnabled = false, isMyLocationEnabled = true))
    }

    //Dialog and Marker state
    var showDialog by remember { mutableStateOf(false) }
    var showPickLocation by remember { mutableStateOf(false) }
    var showFilter by remember {mutableStateOf(false)}
    var markerLocation by remember {mutableStateOf(Position(0.0,0.0))}

    var tempLocation by remember { mutableStateOf(LatLng(0.0,0.0)) }

    Box(modifier = Modifier.fillMaxSize())
    {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings,
            onMapClick = { click->
                if(showPickLocation)
                {
                    locVM.address.value = Position(click.latitude,click.longitude)
                    tempLocation = LatLng(click.latitude,click.longitude)
                }
            },
            onMapLongClick = {click->
                markerLocation = Position(click.latitude, click.longitude)
                showDialog = true
                tempLocation = LatLng(0.0,0.0)
            }
        )
        {
            if(!showPickLocation)
                locVM.markers.forEach {marker->
                    MarkerInfoWindowContent(
                        state = MarkerState(position = marker.position.toLatLng()),
                        title = marker.title,
                        onInfoWindowLongClick =
                            {
                                locVM.takeDelivery(marker.id)
                            }
                    )
                    {
                        Column {
                            Text( "Korisnik ${marker.user} je postavio ovu porudzbinu")
                            Button(onClick = {})
                            {
                                Text("Lociraj Dostavu")
                            }
                        }

                    }
                }

            if(showPickLocation)
                Marker(
                    state = MarkerState(position = tempLocation)
                )
        }

        if(showDialog) AddMarker(
            onDismiss = { showDialog = false },
            onCreate = { name: String, address: Position, desc: String ->
                locVM.addMarker(
                    name,
                    address,
                    desc,
                    markerLocation
                ); showDialog = false
            },
            addAddress = {showDialog=false; showPickLocation=true},
            title = locVM.title,
            address = locVM.address,
            description = locVM.description
        )

        if(showPickLocation) PickLocation(
            onClick = {showPickLocation=false;showDialog=true}
        )

        if(!showFilter)
            Button(
                onClick = {
                    showFilter = true
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
            ) {
                Text("Filter Map")
            }

        if(showFilter)
            FilterMap(
                username = locVM.filterUser,
                radius = locVM.filterRadius,
                status = locVM.filterStatus,
                filter = {
                    locVM.loadDeliveryWithFilter(locVM.filterUser.value,locVM.filterRadius.value,locVM.filterStatus.value);
                    showFilter = false;
                }
            )
    }


}

//Dialog for add Delivery
@Composable
fun AddMarker(
    onDismiss:()-> Unit,
    onCreate:(String, Position, String)->Unit,
    addAddress:()->Unit,

    title: MutableState<String>,
    address: MutableState<Position>,
    description: MutableState<String>
)
{

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Add Package") },
        text = {
            Column {
                OutlinedTextField(
                    value = title.value,
                    onValueChange = { title.value = it },
                    label = { Text("Title") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = "${address.value.latitude}, ${address.value.longitude}",
                    onValueChange = { input ->
                        val parts = input.split(",")
                        if (parts.size == 2) {
                            val lat = parts[0].trim().toDoubleOrNull()
                            val lng = parts[1].trim().toDoubleOrNull()
                            if (lat != null && lng != null) {
                                address.value = Position(lat, lng)
                            }
                        }
                    },
                    label = { Text("Address (Lat, Lng)") },
                    singleLine = true
                )
                Button(onClick = addAddress)
                {
                    Text("Pick Address on a Map")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description.value,
                    onValueChange = { description.value = it },
                    label = { Text("Description") },
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(title.value,address.value,description.value,) }) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("X")
            }
        }
    )
}

//Dialog for destination address Delivery
@Composable
fun PickLocation(
   onClick:()->Unit
)
{
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(onClick) {
            Text("Pick Location")
        }
        Button(onClick) {
            Text("Cancel")
        }
    }
}

//Dialog for map filter

@Composable
fun FilterMap(
    username: MutableState<String>,
    radius: MutableState<String>,
    status: MutableState<String>,
    filter:()->Unit
)
{
    Column {
        OutlinedTextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = { Text("Username") },
            singleLine = true
        )

        OutlinedTextField(
            value = radius.value,
            onValueChange = { radius.value = it },
            label = { Text("Radius") },
            singleLine = true
        )

        OutlinedTextField(
            value = status.value,
            onValueChange = { status.value = it },
            label = { Text("Status") },
            singleLine = true
        )

        Button(onClick = filter) {
            Text("Filter")
        }
    }
}
