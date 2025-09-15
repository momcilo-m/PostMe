package com.momcilo.postme.data.repositories

import android.location.Location
import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
//import com.firebase.geofire.GeoQueryDataEventListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.momcilo.postme.data.entities.Marker
import kotlinx.coroutines.tasks.await
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.listeners.GeoQueryDataEventListener
import kotlin.Result


class DeliveryRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
)
{
    private val geoFireStore: GeoFirestore = GeoFirestore(db.collection("delivery"))
    var userLocation = GeoPoint(0.0,0.0)
    private val geoQuery = geoFireStore.queryAtLocation(userLocation, 1.0)

    fun distanceBetween(g1:GeoPoint, g2: GeoPoint): Float
    {
        var res = FloatArray(1)
        Location.distanceBetween(
            g1.latitude,
            g1.longitude,
            g2.latitude,
            g2.longitude,
            res
        )

        return res[0];
    }

    fun createDelivery(delivery: Marker): Result<Boolean>
    {
        return try{
            val user =  auth.currentUser

            if (user == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            delivery.user = user.uid.toString()

            //Hah lokacija
            var loc = GeoLocation(delivery.address.latitude,delivery.address.longitude)
            var hsh = GeoFireUtils.getGeoHashForLocation(loc)
            delivery.address.hash = hsh;


            loc = GeoLocation(delivery.position.latitude,delivery.position.longitude)
            hsh = GeoFireUtils.getGeoHashForLocation(loc)
            delivery.position.hash = hsh;
            delivery.g = hsh;
            delivery.l = GeoPoint(delivery.position.latitude, delivery.position.longitude)


            db.collection("delivery").add(delivery)
                .addOnSuccessListener { ref->
                    delivery.id = ref.id.toString()
                    Result.success(true)
                }
                .addOnFailureListener {e->
                    throw e
                }

            Result.success(false);
        }
        catch (e: Exception)
        {
            Result.failure(e);
        }
    }

    suspend fun takeDelivery(id:String): Result<Boolean>
    {
        return try {

            val user =  auth.currentUser

            if (user == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val doc = db.collection("delivery").document(id).get().await()

            if (!doc.exists()) {
               throw Exception("Delivery doesn't exist")
           }
            else if((doc.getString("deliverer")!= null || doc.getString("user") == user.uid))
            {
                throw Exception("Delivery are taken or you can't take your delivery")
            }

            val addressMap = doc.get("position") as Map<*, *>
            val lat = (addressMap["latitude"] as Number).toDouble()
            val lng = (addressMap["longitude"] as Number).toDouble()

            val res= this@DeliveryRepository.distanceBetween(GeoPoint(lat,lng), userLocation);

            if(res >= 250)
                throw Exception("You are far away for take delivery")

            doc.reference.update(
                mapOf(
                    "deliverer" to user.uid,
                    "status" to "delivery",
                )
            ).await()
            Result.success(true)


        }
        catch (e: Exception)
        {
            Result.failure(e);
        }

    }

    suspend fun finishDelivery(id: String)
    :Result<Boolean>
    {
        return try {
            val user =  auth.currentUser

            if (user == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val doc = db.collection("delivery").document(id).get().await()

            if (!doc.exists()) {
                throw Exception("Delivery doesn't exist")
            }
            else if(doc.getString("deliverer") != user.uid)
            {
                throw Exception("You are not responsible for this delivery")
            }

            val addressMap = doc.get("address") as Map<*, *>
            val lat = (addressMap["latitude"] as Number).toDouble()
            val lng = (addressMap["longitude"] as Number).toDouble()

            val res= this@DeliveryRepository.distanceBetween(GeoPoint(lat,lng), userLocation);

            if(res >= 250)
                return Result.failure(Exception("You are far away for finish delivery"));

            doc.reference.update(
                mapOf(
                    "deliverer" to "",
                    "status" to "delivered",
                )
            ).await()


            //Bodovanje korisnika



            Result.success(true)

        }
        catch (e: Exception)
        {
            return Result.failure(e);
        }
    }

    //Init
    suspend fun loadPendingDeliveries():Result<List<Marker>>
    {
        return try {
            val docs = db.collection("delivery").whereEqualTo("status","Pending").get().await()
            val markers = docs.documents.mapNotNull { it.toObject(Marker::class.java)?.copy(id = it.id) }
            Result.success(markers);
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    //Filter
    suspend fun loadFilteredDeliveries(
        user: String?,
        radius:String?,
        status:String?
    ):List<Marker>
    {
        var query:Query = db.collection("delivery")

        if (user != null && user!="") {
            query = query.whereEqualTo("user", user)
        }

        if (status != null && status !="") {
            query = query.whereEqualTo("status", status)
        }

        val res = if(radius!=null)
        {
            val center = GeoLocation(37.4219983,-122.084);
            val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radius.toDouble())
            val matchingDocs = mutableListOf<DocumentSnapshot>()

            for (b in bounds) {
                val snap =
                    query
                    .orderBy("position.hash")
                    .startAt(b.startHash)
                    .endAt(b.endHash)
                    .get()
                    .await()
                for (doc in snap.documents) {
                    val loc = doc.get("position") as? Map<*, *>
                    val lat = loc?.get("latitude") as? Double ?: continue
                    val lng = loc.get("longitude") as? Double ?: continue

                    val distance = GeoFireUtils.getDistanceBetween(center, GeoLocation(lat, lng))
                    if (distance <= radius.toDouble()) {
                        matchingDocs.add(doc)
                    }
                }
            }
            matchingDocs
        }
        else
        {
            query.get().await().documents
        }


        return res.mapNotNull { it.toObject(Marker::class.java)?.copy(id = it.id) }
    }


    //Notifikacija kada je objekat u blizini
    fun startGeoQuery(onNewObject: (Marker) -> Unit)
    {
        geoQuery.addGeoQueryDataEventListener(object : GeoQueryDataEventListener {

            override fun onDocumentChanged(
                documentSnapshot: DocumentSnapshot,
                location: GeoPoint
            ) {
                Log.d("HAKUNA","PROMENA");
            }

            override fun onDocumentEntered(
                documentSnapshot: DocumentSnapshot,
                location: GeoPoint
            )
            {
                val marker = documentSnapshot.toObject(Marker::class.java)?.copy(id = documentSnapshot.id)

                if(marker!=null)
                    onNewObject(marker)
            }

            override fun onDocumentExited(documentSnapshot: DocumentSnapshot) {
                Log.d("HAKUNA","IZASO");
            }

            override fun onDocumentMoved(
                documentSnapshot: DocumentSnapshot,
                location: GeoPoint
            ) {
                TODO("Not yet implemented")
            }

            override fun onGeoQueryError(exception: Exception) {
                Log.d("HAKUNA","GRESKA MAKAR");
            }

            override fun onGeoQueryReady() {
                Log.d("HAKUNA","KRECEMO");
            }

        })
    }

    fun updateQueryCenter(newLocation: GeoPoint) {
        geoQuery?.center = newLocation
        userLocation = newLocation;
    }


    suspend fun loadDeliveryToFinish():Result<List<Marker>>
    {
        return try {
            val user =  auth.currentUser

            if (user == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val id = user.uid.toString();
            val docs = db.collection("delivery").whereNotEqualTo("status","Pending").whereEqualTo("deliverer",id).get().await()
            val markers = docs.documents.mapNotNull { it.toObject(Marker::class.java)?.copy(id = it.id) }

            Result.success(markers);
        }
        catch (e: Exception)
        {
            Result.failure(e);
        }
    }



}