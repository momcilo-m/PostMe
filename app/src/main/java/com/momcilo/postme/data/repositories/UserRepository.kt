package com.momcilo.postme.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.momcilo.postme.data.entities.User
import kotlinx.coroutines.tasks.await
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.StorageReference


class UserRepository(
    private val auth: FirebaseAuth,
    private val db: DatabaseReference,
    private val storageDb: StorageReference
) {

    suspend fun registerUserWithEmail(user: User, password: String,photo:Uri?): Result<FirebaseUser?> {
        return try {
            val res = auth.createUserWithEmailAndPassword(user.email,password).await()
            val uid = res.user?.uid ?: throw Exception("User not registered")

            if(photo!=null)
            {
                val ref = storageDb.child("images/${user.email}-${System.currentTimeMillis()}.jpg")

                ref.putFile(photo).await()

                val downloadUrl = ref.downloadUrl.await()

                user.photo = downloadUrl.toString()
            }

            db.child("users").child(uid).setValue(user).await()
            Result.success(res.user)
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    suspend fun sendLocation(loc: GeoPoint)
    {
        val userId = auth.currentUser?.uid ?: return

        val userRef = db.child("users").child(userId)

        val locationMap = mapOf(
            "latitude" to loc.latitude,
            "longitude" to loc.longitude
        )
        userRef.child("location").setValue(locationMap).await()
    }

    suspend fun loginUserWithEmail(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User not found")
            val user = db.child("users").child(firebaseUser.uid).get().await()
            user.getValue(User::class.java)
            Result.success(convertResponse(user))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isLoggedIn(): Result<User>
    {
        return try {
            val firebaseUser = auth.currentUser ?: throw Exception("User not logged in");
            val user = db.child("users").child(firebaseUser.uid).get().await()
            user.getValue(User::class.java)
            Result.success(convertResponse(user))
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    fun logout(): Result<Boolean>
    {
        return try {
            auth.signOut();
            Result.success(true);
        }
        catch (e: Exception)
        {
            Result.failure(e);
        }

    }

    private fun convertResponse(user: DataSnapshot): User
    {
        return User(
            email = user.child("email").value.toString(),
            username = user.child("username").value.toString(),
            name = user.child("name").value.toString(),
            phone = user.child("phone").value.toString(),
            photo = user.child("photo").value.toString(),
            points = user.child("points").value.toString().toInt()
        )
    }

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    fun trackUser()
    {
        db.child("users")
            .orderByChild("points")
            .limitToLast(15)
            .addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val usersList = mutableListOf<User>()
                for (userSnap in snapshot.children) {
                    val user = userSnap.getValue(User::class.java)
                    user?.let { usersList.add(it) }
                }

                val sorted = usersList.sortedByDescending { it.points }

                _users.value = sorted
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
            }
        })

    }

//    suspend fun getUsers(): Result<List<User>>
//    {
//        return try {
//
//            var userDoc = db.child("users")
//                //.orderByChild("points")
//                .get().await();
//
//            var users = userDoc.children.mapNotNull { user -> user.getValue(User::class.java) }.sortedByDescending { it.points }
//
//            Result.success(users);
//        }
//        catch (e: Exception)
//        {
//            Result.failure(e);
//        }
//
//    }

    suspend fun sendPoints(username: String, points: Int): Result<Boolean>
    {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not auth"))

            val userSnapshot = db.child("users").child(uid).get().await()
            val user = convertResponse(userSnapshot)

            if(user.points >= points)
            {
                val receiverSnapshot = db.child("users").orderByChild("name").equalTo(username).get().await()

                if (receiverSnapshot.exists()) {
                    val receiverChild = receiverSnapshot.children.first()
                    val receiver = convertResponse(receiverChild)
                    val receiverUid = receiverChild.key

                    Log.d("POINTS","RECEIVER $receiverUid")

                    val updates = hashMapOf<String, Any>(
                        "/users/$uid/points" to (user.points - points),
                        "/users/$receiverUid/points" to (receiver.points + points)
                    )

                    db.updateChildren(updates).await()
                    Result.success(true);
                }
                else
                    Result.failure(Exception("User not found"));
            }
            else
            {
                Result.failure(Exception("You don't have enough points"));
            }

        }
        catch (_: Exception)
        {
            Result.failure(Exception("Something went wrong with send points"));
        }
    }
}