package com.momcilo.postme.data.entities

import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryDataEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.firestore.DocumentSnapshot

class GeoListener(
    private val onNewObject: (DocumentSnapshot) -> Unit
): GeoQueryDataEventListener {

    override fun onDataEntered(
        dataSnapshot: DataSnapshot?,
        location: GeoLocation?
    ) {
        TODO("Not yet implemented")
    }

    override fun onDataExited(dataSnapshot: DataSnapshot?) {
        TODO("Not yet implemented")
    }

    override fun onDataMoved(
        dataSnapshot: DataSnapshot?,
        location: GeoLocation?
    ) {
        TODO("Not yet implemented")
    }

    override fun onDataChanged(
        dataSnapshot: DataSnapshot?,
        location: GeoLocation?
    ) {
        TODO("Not yet implemented")
    }

    override fun onGeoQueryReady() {
        TODO("Not yet implemented")
    }

    override fun onGeoQueryError(error: DatabaseError?) {
        TODO("Not yet implemented")
    }


}