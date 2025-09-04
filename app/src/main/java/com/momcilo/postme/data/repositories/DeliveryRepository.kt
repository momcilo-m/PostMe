package com.momcilo.postme.data.repositories

import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.momcilo.postme.data.entities.Marker
import com.momcilo.postme.data.entities.Position
import kotlinx.coroutines.tasks.await
import kotlin.Result

class DeliveryRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
)
{
    suspend fun createDelivery(delivery: Marker): Result<Boolean>
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
               throw Exception("Sorry but delivery doesn't exist")
            }else if((doc.getString("deliverer")!= null || doc.getString("deliverer") == user.uid))
            {
                throw Exception("Delivery are taken or you can't take your delivery")
            }
            else {
//                doc.reference.update(
//                    mapOf(
//                        "deliverer" to user.uid,
//                        "status" to "delivery",
//                    )
//                ).await()
                Result.success(true)
            }

        }
        catch (e: Exception)
        {
            Result.failure(e);
        }

    }


    suspend fun loadDeliveries():Result<List<Marker>>
    {
        return try {
            val docs = db.collection("delivery").get().await()
            val markers = docs.documents.mapNotNull { it.toObject(Marker::class.java)?.copy(id = it.id) }
            Result.success(markers);
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }


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

//    suspend fun finishDelivery(): Result<Boolean>
//    {
//
//    }
}