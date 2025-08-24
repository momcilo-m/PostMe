package com.momcilo.postme.ui.screens

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.momcilo.postme.ui.viewModels.MapViewModel


@SuppressLint("MissingPermission")
@Composable
fun MapScreen(locVM: MapViewModel)
{
    //Start location
    val lat = locVM.location.value?.latitude ?: 42.0;
    val lng = locVM.location.value?.longitude ?: 42.0;

    //Map settings
    val cameraPositionState = rememberCameraPositionState() { position = CameraPosition.fromLatLngZoom(LatLng(lat,lng), 100f) }
    var uiSettings by remember { mutableStateOf(MapUiSettings()) }
    var properties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL, isTrafficEnabled = false, isBuildingEnabled = false, isMyLocationEnabled = true))
    }

    //Dialog and Marker state
    var showDialog by remember { mutableStateOf(false) }
    var showPickLocation by remember { mutableStateOf(false) }
    var markerLocation by remember {mutableStateOf(LatLng(15.0,14.0))}

    var tempLocation by remember { mutableStateOf(LatLng(0.0,0.0)) }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings,
        onMapClick = { click->
            if(showPickLocation)
            {
                locVM.address.value = "${click.latitude} ${click.longitude}"
                tempLocation = LatLng(click.latitude,click.longitude)
            }
        },
        onMapLongClick = {click->
            markerLocation = LatLng(click.latitude, click.longitude)
            showDialog = true
            tempLocation = LatLng(0.0,0.0)
        }
    )
    {
        if(!showPickLocation)
            locVM.markers.forEach {marker->
                MarkerInfoWindowContent(
                    state = MarkerState(position = marker.position),
                    title = marker.title,
                )
                {
                    Text( "Korisnik ${marker.title} je postavio ovu porudzbinu. Adresa dostave je ${marker.address}")
                }
            }

        if(showPickLocation)
            Marker(
                state = MarkerState(position = tempLocation)
            )
    }

    if(showDialog) AddMarker(
        onDismiss = { showDialog = false },
        onCreate = { n: String, a: String, d: String ->
            locVM.addMarker(
                n,
                a,
                d,
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
}

@Composable
fun AddMarker(
    onDismiss:()-> Unit,
    onCreate:(String, String, String)->Unit,
    addAddress:()->Unit,

    title: MutableState<String>,
    address: MutableState<String>,
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
                    value = address.value,
                    onValueChange = { address.value = it },
                    label = { Text("Address in LatLng or Textual") },
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
            Button(onClick = { onCreate(title.value,description.value,address.value) }) {
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
