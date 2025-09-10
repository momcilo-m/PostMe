package com.momcilo.postme.data.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.momcilo.postme.data.entities.User
import kotlinx.coroutines.tasks.await
import android.location.Location


class UserRepository(
    private val auth: FirebaseAuth,
    private val db: DatabaseReference
) {

    suspend fun registerUserWithEmail(user: User, password: String): Result<FirebaseUser?> {
        return try {
            val res = auth.createUserWithEmailAndPassword(user.email,password).await()
            val uid = res.user?.uid ?: throw Exception("User not registered")

            db.child("users").child(uid).setValue(user).await()
            Result.success(res.user)
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    suspend fun sendLocation(loc:Location)
    {
        val userId = auth.currentUser?.uid ?: return

        val userRef = db.child("users").child(userId)

        val locationMap = mapOf(
            "latitude" to loc.latitude,
            "longitude" to loc.longitude
        )

        // Postavi lokaciju pod korisnika
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
            email = user.child("email").toString(),
            username = user.child("username").toString(),
            name = user.child("name").toString(),
            phone = user.child("phone").toString()
        )
    }
}