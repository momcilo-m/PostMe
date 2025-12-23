package com.momcilo.postme.data.repositories

import android.location.Location
import android.util.Log
import androidx.lifecycle.asFlow
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.Timestamp
//import com.firebase.geofire.GeoQueryDataEventListener
import org.imperiumlabs.geofirestore.GeoQuery
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.maps.GeoApiContext
import com.momcilo.postme.data.cache.MarkerCache
import com.momcilo.postme.data.entities.Marker
import com.momcilo.postme.data.entities.TempLoc
import com.momcilo.postme.data.entities.UserDelivery
import kotlinx.coroutines.tasks.await
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.listeners.GeoQueryDataEventListener
import kotlin.Result
import com.google.maps.DirectionsApi
import com.google.maps.model.LatLng
import com.google.maps.model.TravelMode
import com.momcilo.postme.data.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeliveryRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val userDb: DatabaseReference,
    private val locationRepository: LocationRepository,

)
{
    private val geoFireStore: GeoFirestore = GeoFirestore(db.collection("delivery"))

    val userLocation: Flow<Location> = this.locationRepository.location.asFlow().filterNotNull()
    private var geoQuery: GeoQuery? = null

    suspend fun initQuery()
    {
        try {
            val loc = userLocation.firstOrNull()
            loc?.let {
                geoQuery = geoFireStore.queryAtLocation(
                    GeoPoint(it.latitude, it.longitude),
                    0.250
                )
            }
        } catch (e: Exception) {
        }
    }


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

    fun distancePath(path: List<UserDelivery>): Double
    {
        var total = 0.0;

        for(i in 0 until path.size-1)
        {
            var p1 = path[i].location;
            var p2 = path[i+1].location;

            total+= distanceBetween(GeoPoint(p1.latitude,p1.longitude), GeoPoint(p2.latitude,p2.longitude));
        }

        return total;
    }

    fun createDelivery(delivery: Marker): Result<Marker>
    {
        return try{
            val user =  auth.currentUser

            if (user == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            delivery.user = user.uid.toString()

            //Hash lokacija
            var loc = GeoLocation(delivery.address.latitude,delivery.address.longitude)
            var hsh = GeoFireUtils.getGeoHashForLocation(loc)
            delivery.address.hash = hsh;


            loc = GeoLocation(delivery.position.latitude,delivery.position.longitude)
            hsh = GeoFireUtils.getGeoHashForLocation(loc)
            delivery.position.hash = hsh;
            delivery.g = hsh;
            delivery.l = GeoPoint(delivery.position.latitude, delivery.position.longitude)


            val docRef = db.collection("delivery").document()
            delivery.id = docRef.id

            docRef.set(delivery)
                .addOnSuccessListener {
                    Result.success(true)
                }

            Result.success(delivery);
        }
        catch (e: Exception)
        {
            //Log.d("LAST",e.localizedMessage ?: "aa")
            Result.failure(e);
        }
    }

    suspend fun takeDelivery(id:String): Result<Boolean>
    {
        return try {

            Log.d("FIN_DEL", "UZIMA SE ${id}")

            val user =  auth.currentUser

            if (user == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val doc = db.collection("delivery").document(id).get().await()

            if (!doc.exists()) {
               throw Exception("Delivery doesn't exist")
            }
            else if(doc.getString("deliverer")!= "" )
            {
                Log.d("FIN_DEL", doc.getString("deliverer").toString())
                throw Exception("Delivery are taken")
            }
            else if(doc.getString("user") == user.uid)
            {
                throw Exception("You can't take your delivery")
            }

            val addressMap = doc.get("position") as Map<*, *>
            val lat = (addressMap["latitude"] as Number).toDouble()
            val lng = (addressMap["longitude"] as Number).toDouble()

            var userLoc = userLocation.first();

            val res= this@DeliveryRepository.distanceBetween(GeoPoint(lat,lng),locationToGeoPoint(userLoc));

            if(res >= 250)
                throw Exception("You are far away for take delivery")

            doc.reference.update(
                mapOf(
                    "deliverer" to user.uid,
                    "takeAt" to Timestamp.now(),
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
            val delivery = doc.toObject(Marker::class.java)?.copy(id = doc.id)

            if (!doc.exists()) {
                throw Exception("Delivery doesn't exist")
            }
            else if(delivery?.deliverer != user.uid)
            {
                throw Exception("You are not responsible for this delivery")
            }

            var address = delivery.address;
            var position = delivery.position

            var userLoc = userLocation.first();
            val res= this@DeliveryRepository.distanceBetween(GeoPoint(address.latitude,address.longitude), locationToGeoPoint(userLoc));

            if(res >= 250)
                return Result.failure(Exception("You are far away for finish delivery"));


            //Racunanje bodova za delivery


            //Predjena kilometraza
            val pathDoc = userDb.child("user-delivery").child("${user.uid}-${id}").get().await()
            val path = pathDoc.children.mapNotNull {child -> child.getValue(UserDelivery::class.java)}
            var distance = distancePath(path);


            //Optimalna kilometraza i vreme
            var optimal = getRoute(LatLng(position.latitude,position.longitude),LatLng(address.latitude,address.longitude))

            val optimalDistance = optimal["distanceMeters"]
            val optimalTime = optimal["durationSeconds"]

            Log.d("SCORE","${optimalTime?.times(1000L)} ms : $optimalDistance m -- $distance m : ${Timestamp.now().toDate().time - delivery.takeAt.toDate().time}")

            //Vremena se konvertuju u ms
            var score = calculateScore(optimalDistance ?: 0, (optimalTime ?: 0) * 1000L, distance.toLong(), Timestamp.now().toDate().time - delivery.takeAt.toDate().time);
            Log.d("SCORE","$score")

            //Upisivanje u bazu da je dostava gotova
            doc.reference.update(
                mapOf(
                    "deliverer" to "",
                    "status" to "delivered",
                )
            ).await()

            var currentScore = userDb.child("users").child(delivery.deliverer).child("points").get().await().getValue(Int::class.java);

            userDb.child("users").child(delivery.deliverer).child("points").setValue((currentScore?:0) + score)

            Result.success(true)

        }
        catch (e: Exception)
        {
            return Result.failure(e);
        }
    }

    suspend fun getRoute(source: LatLng, dest: LatLng): Map<String, Long> {
        return withContext(Dispatchers.IO) {
            val context = GeoApiContext.Builder()
                .apiKey("AIzaSyCDAjqtcy9m_gKwT5OVagFSt4_L0I-lExU")
                .build()

            val req = DirectionsApi.newRequest(context)
                .origin(source)
                .destination(dest)
                .mode(TravelMode.DRIVING)

            val res = req.await()

            val distance = res.routes[0].legs[0].distance.inMeters
            val duration = res.routes[0].legs[0].duration.inSeconds

            mapOf(
                "distanceMeters" to distance,
                "durationSeconds" to duration
            )
        }
    }

    fun calculateScore(
        optimalDistance: Long,
        optimalTime: Long,
        realDistance: Long,
        realTime: Long
    ): Double
    {
        var distancePercentage = 1;
        var timePercentage = 1;
        var base = 10.0;

        //Penali za odugovlacenje
        distancePercentage =
            if(realDistance < optimalDistance)
                1;
            else
                ((realDistance * 100 / optimalDistance).toInt() - 100)/100;

        //Penali za kasnjenje
        timePercentage =
            if(realTime < optimalTime)
                1
            else
                ((realTime * 100 / optimalTime).toInt() - 100)/100;

        if (timePercentage == 1 && distancePercentage == 1)
            return base + (realDistance/1000.0) + (realTime/1000.0/60.0);

        val calcTime = (realTime / 1000.0 / 60.0) - (realTime / 1000.0 / 60.0) * timePercentage
        val calcDistance = (realDistance / 1000.0) - (realDistance / 1000.0) * distancePercentage

        return base + calcTime + calcDistance
    }

    //Pocetne dostave na mapi
    suspend fun loadPendingDeliveries():Result<List<Marker>>
    {
        return try {
            val docs = db.collection("delivery").whereEqualTo("status","pending").get().await()
            val markers = docs.documents.mapNotNull { it.toObject(Marker::class.java)?.copy(id = it.id) }//.filter { it.status == "pending" }
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
            val usrsh = userDb.child("users").orderByChild("name").equalTo(user).get().await()
            val usr = usrsh.children.firstOrNull()

            if(usr == null)
                throw Exception("User Not found");

            val userName = usr.key

            Log.d("FILTER",userName.toString())

            query = query.whereEqualTo("user", userName.toString())
        }

        query = if (status != null && status !="") {
            query.whereEqualTo("status", status)
        } else
            query.whereNotEqualTo("status","delivered")

        val res = if(radius!=null && radius!="")
        {
            var userLoc = userLocation.first();
            val center = GeoLocation(userLoc.latitude,userLoc.longitude);
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

    //Slanje lokacije za user-delivery
    suspend fun sendLocationDelivery(location:GeoPoint) {

        if(auth.currentUser == null)
            return;

        val updates = mutableMapOf<String, Any>()

        if(MarkerCache.send.isEmpty())
        {
            Log.d("DEL","NEMA NISTA ZA SLANJE")
            return;
        }
        else
        {
            Log.d("DEL","IMA STA ZA SLANJE")
        }

        for (marker in MarkerCache.send) {
            val path = "user-delivery/${auth.currentUser!!.uid}-${marker.id}/${System.currentTimeMillis()}"
            val value = UserDelivery(
                user = marker.user,
                delivery = marker.id,
                location = TempLoc(location.latitude, location.longitude)
            )
            updates[path] = value
            Log.d("DEL",marker.id)
        }

        userDb.updateChildren(updates).await()
    }

    //Notifikacija kada je objekat u blizini
    fun startGeoQuery(onNewObject: (Marker) -> Unit)
    {
        geoQuery?.addGeoQueryDataEventListener(object : GeoQueryDataEventListener {

            override fun onDocumentChanged(
                documentSnapshot: DocumentSnapshot,
                location: GeoPoint
            )
            {

            }

            override fun onDocumentEntered(
                documentSnapshot: DocumentSnapshot,
                location: GeoPoint
            )
            {

                val marker = documentSnapshot.toObject(Marker::class.java)?.copy(id = documentSnapshot.id)
                Log.d("GEOQUERY","ENTER ${marker?.title}")
                if(marker!=null)
                    onNewObject(marker)
            }

            override fun onDocumentExited(documentSnapshot: DocumentSnapshot) {
                Log.d("GEOQUERY","EXIT")
            }

            override fun onDocumentMoved(documentSnapshot: DocumentSnapshot, location: GeoPoint) {
                Log.d("GEOQUERY","MOVE")
            }

            override fun onGeoQueryError(exception: Exception) {
                Log.d("GEOQUERY","ERROR")
            }

            override fun onGeoQueryReady() {
                //Log.d("GEOQUERY","READY")
            }

        })
    }

    fun updateQueryCenter(newLocation: GeoPoint) {
        geoQuery?.center = newLocation
    }

    suspend fun loadDeliveryToFinish():Result<List<Marker>>
    {
        return try {
            val user =  auth.currentUser

            if (user == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val id = user.uid.toString();
            val docs = db.collection("delivery").whereNotEqualTo("status","pending").whereEqualTo("deliverer",id).get().await()
            val markers = docs.documents.mapNotNull { it.toObject(Marker::class.java)?.copy(id = it.id) }

            Result.success(markers);
        }
        catch (e: Exception)
        {
            Result.failure(e);
        }
    }

    fun locationToGeoPoint(loc: Location): GeoPoint
    {
        return GeoPoint(loc.latitude,loc.longitude);
    }

}