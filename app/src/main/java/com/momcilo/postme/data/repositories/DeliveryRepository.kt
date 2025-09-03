package com.momcilo.postme.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.momcilo.postme.data.entities.Marker
import kotlinx.coroutines.tasks.await
import kotlin.contracts.Returns

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


//    suspend fun finishDelivery(): Result<Boolean>
//    {
//
//    }
}