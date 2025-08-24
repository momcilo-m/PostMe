package com.momcilo.postme.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.momcilo.postme.data.entities.Marker
import kotlinx.coroutines.tasks.await

class DeliveryRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
)
{
    suspend fun createDelivery(delivery: Marker): Result<Boolean>
    {
        return try{
            val user =  FirebaseAuth.getInstance().currentUser

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

            val user =  FirebaseAuth.getInstance().currentUser

            if (user == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val doc = db.collection("delivery").document(id).get().await()
            if (doc.exists() && (doc.getString("deliverer")?.isEmpty() == true)) {
                val update = doc.reference.update("deliverer", user.uid).await()
                Result.success(true)
            } else {
                Result.success(false)
            }
        }
        catch (e: Exception)
        {
            Result.failure(e);
        }

    }

//    suspend fun finishDelivery(): Result<Boolean>
//    {
//
//    }
}