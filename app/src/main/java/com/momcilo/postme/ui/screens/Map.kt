package com.momcilo.postme.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Point
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.asFlow
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.momcilo.postme.R
import com.momcilo.postme.data.entities.Position
import com.momcilo.postme.ui.viewModels.MapViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@SuppressLint("MissingPermission")
@Composable
fun MapScreen(locVM: MapViewModel)
{
    val location by locVM.userLocation.asFlow().collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0,0.0), 15f)
    }

    fun animateCamera(position: LatLng)
    {
        Log.d("MARKER","VRACANJE ${position.latitude}, ${position.longitude}")
        if(position.latitude != 0.0 && position.longitude !=0.0) {
            scope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(position.latitude, position.longitude),
                        15f
                    )
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            locVM.cameraSet = false;
        }
    }

    LaunchedEffect(location) {
        if(locVM.deliveryLocation != LatLng(0.0,0.0) && locVM.showMarker)
        {
            Log.d("MAPDEBUG","PRE ANIM MARKER "+cameraPositionState.position.toString())
            Log.d("MAPDEBUG","PRE ANIM MARKER "+locVM.deliveryLocation.toString())
            Log.d("MAPDEBUG","PRE ANIM MARKER "+location.toString())
            Log.d("MAPDEBUG","PRE ANIM MARKER "+!locVM.cameraSet)

            //Log.d("MAPDEBUG",locVM.deliveryLocation.latitude.toString());
            animateCamera(locVM.deliveryLocation)
            locVM.cameraSet = true;
            locVM.showMarker = false;
        }
        else if(location != null && !locVM.cameraSet)
        {
            Log.d("MAPDEBUG","PRE ANIM USER "+cameraPositionState.position.toString())
            Log.d("MAPDEBUG","PRE ANIM USER "+locVM.deliveryLocation.toString())
            Log.d("MAPDEBUG","PRE ANIM USER "+location.toString())
            Log.d("MAPDEBUG","PRE ANIM USER "+!locVM.cameraSet)
            //Log.d("MAPDEBUG", location!!.latitude.toString());
            location?.let {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude),
                        15f
                    )
                )
            }

            locVM.cameraSet = true;
        }

    }

    val context = LocalContext.current
    val toastEvent = locVM.toastEvent.collectAsState(initial = null)

    LaunchedEffect(toastEvent.value) {
        toastEvent.value?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

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
    //var deliveryLocation by remember { mutableStateOf(LatLng(0.0,0.0)) }


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
            },
        )
        {
            val pendingDelivery = bitmapDescriptorFromVector(context, R.drawable.baseline_inventory_24)
            val deliveryIcon = bitmapDescriptorFromVector(context, R.drawable.baseline_local_shipping_24)

            if(!showPickLocation)
                locVM.markers.forEach {marker->
                    MarkerInfoWindowContent(
                        state = MarkerState(position = marker.position.toLatLng()),
                        title = marker.title,
                        onInfoWindowLongClick =
                        {
                            locVM.takeDelivery(marker.id)
                        },
                        onInfoWindowClick = {
                            locVM.showDeliveryLocation(marker.position,marker.address)
                            animateCamera(LatLng(marker.address.latitude, marker.address.longitude))
                        },
                        icon = pendingDelivery
                    )
                    {
                        Column {
                            Text( "Description: ${marker.description}")
                            Text("Status :${marker.status}")
                            Text("DateTime: 2025-09-18 18:52")
                        }

                    }
                }

            //Odabir Lokacije za dostavu
            if(showPickLocation && tempLocation.latitude != 0.0 && tempLocation.longitude != 0.0)
                Marker(
                    state = MarkerState(position = tempLocation)
                )

            //Prikaz lokacije za dostavu
            if(locVM.deliveryLocation.latitude != 0.0 && locVM.deliveryLocation.longitude != 0.0)
                Marker(
                    icon = deliveryIcon,
                    state = MarkerState(position = locVM.deliveryLocation),
                    onClick = {
                        locVM.deliveryLocation = LatLng(0.0,0.0);
                        animateCamera(locVM.returnLocation)
                        true
                    }
                )
        }

        //Dijalog za dodavanje markera
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

        //Dijalog za odabir lokacije
        if(showPickLocation) PickLocation(
            onClick = {showPickLocation=false;showDialog=true}
        )

        //Dugme za filter mape
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

        //Dijalog za filtriranje Mape
        if(showFilter)
            FilterMap(
                username = locVM.filterUser,
                radius = locVM.filterRadius,
                status = locVM.filterStatus,
                filter = {
                    locVM.loadDeliveryWithFilter(locVM.filterUser.value,locVM.filterRadius.value,locVM.filterStatus.value);
                    showFilter = false;
                },
                cancel = {
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
            Button(onClick = { onCreate(title.value, address.value, description.value) }) {
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
    filter:()->Unit,
    cancel:()->Unit
)
{
    Column(modifier = Modifier
        .fillMaxWidth()
        .background(color = Color.White, shape = RoundedCornerShape(4.dp)),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        Row {
            Button(onClick = filter) {
                Text("Filter")
            }
            Spacer(modifier = Modifier.size(10.dp))
            Button(onClick = cancel) {
                Text("Cancel")
            }
        }

    }
}


fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!
    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)

    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}