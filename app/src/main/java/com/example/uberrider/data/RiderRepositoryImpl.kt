package com.example.uberrider.data

import android.location.Location
import android.net.Uri
import android.util.Log
import com.example.uberrider.data.model.DriverDto
import com.example.uberrider.data.model.RequestDriverDataDto
import com.example.uberrider.data.model.RiderDto
import com.example.uberrider.data.model.TokenDto
import com.example.uberrider.data.util.Constants.DEFAULT_DISTANCE
import com.example.uberrider.data.util.Constants.DEFAULT_USER_IMAGE
import com.example.uberrider.data.util.Constants.DRIVERS_INFO_REFERENCE
import com.example.uberrider.data.util.Constants.DRIVERS_LOCATION_REFERENCE
import com.example.uberrider.data.util.Constants.DRIVERS_TOKENS_REFERENCE
import com.example.uberrider.data.util.Constants.LIMIT_RANGE
import com.example.uberrider.data.util.Constants.RIDERS_INFO_REFERENCE
import com.example.uberrider.data.util.Constants.RIDERS_LOCATION_REFERENCE
import com.example.uberrider.data.util.Constants.RIDERS_TOKENS_REFERENCE
import com.example.uberrider.data.util.DriverMapper
import com.example.uberrider.data.util.RiderMapper
import com.example.uberrider.domain.RiderRepository
import com.example.uberrider.domain.model.Driver
import com.example.uberrider.domain.model.Rider
import com.example.uberrider.util.Result
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RiderRepositoryImpl(
    private val riderMapper: RiderMapper,
    private val driverMapper: DriverMapper,
    private val api: NotificationApi
) : RiderRepository {

    private val auth = Firebase.auth
    private var userId: String? = auth.currentUser?.uid
    private val riders = Firebase.database.getReference(RIDERS_INFO_REFERENCE)
    private val drivers = Firebase.database.getReference(DRIVERS_INFO_REFERENCE)
    private val ridersLocation = Firebase.database.getReference(RIDERS_LOCATION_REFERENCE)
    private val driversLocation = Firebase.database.getReference(DRIVERS_LOCATION_REFERENCE)
    private val ridersTokens = Firebase.database.getReference(RIDERS_TOKENS_REFERENCE)
    private val driversTokens = Firebase.database.getReference(DRIVERS_TOKENS_REFERENCE)
    private var currentUserRef: DatabaseReference? = auth.currentUser?.let {
        Firebase.database.getReference(RIDERS_LOCATION_REFERENCE).child(it.uid)
    }

    private val storage = Firebase.storage
    private val onLineRef = Firebase.database.getReference(".info/connected")
    private val geoFire = GeoFire(ridersLocation)
    private lateinit var driversGeoFire: GeoFire
    private lateinit var geoQuery: GeoQuery

    private val availableDrivers = HashMap<String, Driver>()
    private val availableDriversFlow = MutableStateFlow(availableDrivers)

    private val _driverDisconnection = MutableStateFlow("")

    private var distance = DEFAULT_DISTANCE

    private val declineDrivers = mutableSetOf<String>()

    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                currentUserRef?.onDisconnect()?.removeValue()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            //Do nothing
        }
    }

    private val TAG = "RIDERS_REPOSITORY_IMPL"
    private var listenerValueAddition = 0
    private var userKnowAboutConnectionState = false

    override suspend fun register(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String
    ): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid!!
                val user = RiderDto(fullName = fullName, email = email, phoneNumber = phoneNumber)
                riders.child(uid).setValue(user).await()

                userId = auth.currentUser?.uid
                currentUserRef = Firebase.database.getReference(RIDERS_LOCATION_REFERENCE)
                    .child(auth.currentUser?.uid!!)
                Result.Success(data = true, "Registration completed successfully")
            }
        } catch (e: Exception) {
            return Result.Error(data = false, errorMessage = e.message!!)
        }
    }

    override suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                auth.signInWithEmailAndPassword(email, password).await()

                userId = auth.currentUser?.uid
                currentUserRef = Firebase.database.getReference(RIDERS_LOCATION_REFERENCE)
                    .child(auth.currentUser?.uid!!)
                Result.Success(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(data = null, errorMessage = e.message!!)
        }
    }

    override suspend fun getUserConnectionStatus(): Result<Boolean> {
        return try {

            withContext(Dispatchers.IO) {
                val connectionStatus = auth.currentUser

                connectionStatus?.let {
                    Result.Success(data = true)
                } ?: Result.Success(data = false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(data = null, errorMessage = e.message!!)
        }
    }

    override suspend fun resetPassword(email: String): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                auth.sendPasswordResetEmail(email).await()

                Result.Success(data = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(data = null, errorMessage = e.message!!)
        }
    }

    @ExperimentalCoroutinesApi
    override fun connectRider(latitude: Double, longitude: Double): Flow<Result<Boolean>> =
        callbackFlow {
            if (listenerValueAddition == 0) {
                onLineRef.addValueEventListener(valueEventListener)
                listenerValueAddition++
            }

            geoFire.setLocation(
                auth.currentUser?.uid,
                GeoLocation(latitude, longitude)
            ) { key, error ->
                if (error == null) {
                    if (!userKnowAboutConnectionState) {
                        trySend(Result.Success(true))
                        userKnowAboutConnectionState = true
                    }
                } else {
                    Log.e(TAG, "$error")
                    trySend(
                        Result.Error(
                            data = false,
                            errorMessage = "We have a technical problem please open the app later."
                        )
                    )
                }
            }

            awaitClose()
        }

    override fun disconnectRider() {
        onLineRef.removeEventListener(valueEventListener)
        geoFire.removeLocation(userId)
    }

    override suspend fun getRiderProfile(): Result<Rider> {
        return try {

            withContext(Dispatchers.IO) {
                val driverDto = riders.child(auth.currentUser?.uid!!).get().await()
                    .getValue(RiderDto::class.java)

                val driver = riderMapper.mapToDomainModel(driverDto!!)

                Result.Success(data = driver)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(data = null, errorMessage = "We have a technical problem please try later")
        }
    }

    private suspend fun updateProfileImage(uri: Uri) = withContext(Dispatchers.IO) {
        val riderDto =
            riders.child(auth.currentUser?.uid!!).get().await().getValue(RiderDto::class.java)

        if (riderDto?.profilePhoto != DEFAULT_USER_IMAGE) {
            storage.getReferenceFromUrl(riderDto?.profilePhoto!!).delete().await()
        }

        storage.reference.child("driversImages/${auth.currentUser?.uid}").putFile(uri)
            .await().metadata?.reference?.downloadUrl?.await()
    }

    override suspend fun editProfile(
        currentEmail: String,
        password: String,
        updatedEmail: String,
        phoneNumber: String,
        uri: String?
    ): Result<Boolean> {
        return try {

            val imageUrl = uri?.let {
                val imageUri = Uri.parse(it)

                updateProfileImage(imageUri).toString()
            }

            val map = mutableMapOf<String, Any>(
                "email" to updatedEmail,
                "phoneNumber" to phoneNumber
            )

            imageUrl?.let {
                map["profilePhoto"] = it
            }

            withContext(Dispatchers.IO) {

                val credential = EmailAuthProvider.getCredential(currentEmail, password)

                auth.currentUser?.reauthenticate(credential)?.await()

                riders.child(auth.currentUser?.uid!!).updateChildren(map).await()
                auth.currentUser!!.updateEmail(updatedEmail).await()

                Result.Success(data = true)

            }

        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(data = null, e.message!!)
        }
    }

    override fun logout() {
        auth.signOut()
    }

    override suspend fun updateTokenService(token: String) {
        withContext(Dispatchers.IO) {
            auth.currentUser?.let {
                ridersTokens.child(it.uid)
                    .setValue(TokenDto(token))
                    .await()
            }
        }
    }

    override suspend fun updateTokenActivity() {
        withContext(Dispatchers.IO) {
            val token = FirebaseMessaging.getInstance().token.await()
            updateTokenService(token)
        }
    }

    @ExperimentalCoroutinesApi
    override fun queryDrivers(
        cityName: String,
        latitude: Double,
        longitude: Double,
        loadDrivers: () -> Unit
    ) {
        driversGeoFire = GeoFire(driversLocation.child(cityName))
        geoQuery = driversGeoFire.queryAtLocation(GeoLocation(latitude, longitude), distance)

        geoQuery.removeAllListeners()

        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String?, location: GeoLocation?) {

                if (!availableDrivers.contains(key)) {
                    drivers.child(key!!).get().addOnSuccessListener {
                        val driverDto = it.getValue(DriverDto::class.java)
                        driverDto?.longitude = longitude
                        driverDto?.latitude = latitude
                        driverDto?.key = key

                        val driver = driverMapper.mapToDomainModel(driverDto!!)

                        availableDrivers[key] = driver
                        checkDriverConnection(key, cityName)
                        availableDriversFlow.value = availableDrivers

                        Log.e(TAG, availableDrivers.toString())
                    }
                }
            }

            override fun onKeyExited(key: String?) {

            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
                availableDrivers[key]?.latitude = location?.latitude!!
                availableDrivers[key]?.longitude = location.longitude
            }

            override fun onGeoQueryReady() {
                if (distance < LIMIT_RANGE) {
                    distance++
                    loadDrivers()
                } else {
                    distance = DEFAULT_DISTANCE
                    availableDriversFlow.value = availableDrivers
                }

//                Log.e(TAG,availableDrivers.toString())
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                Log.e(TAG, error?.message!!)
            }

        })
    }

    override fun getAvailableDrivers(): Flow<HashMap<String, Driver>> {
        return availableDriversFlow
    }

    @ExperimentalCoroutinesApi
    override fun checkDriverConnection(key: String, city: String) {
        val driverLocation = driversLocation.child(city)
            .child(key)

        driverLocation.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    _driverDisconnection.value = key

                    Log.e(TAG, key)
                    availableDrivers.remove(key)
                    declineDrivers.remove(key)
                    driverLocation.removeEventListener(this)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Do nothing
            }

        })

    }

    override fun listenToDriverDisconnection(): Flow<String> {
        return _driverDisconnection
    }

    override suspend fun searchForDrivers(
        latitude: Double,
        longitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double
    ): Result<Driver> {

        val riderCoordinates = Location("")
        val driverCoordinates = Location("")

        riderCoordinates.latitude = latitude
        riderCoordinates.longitude = longitude

        return try {
            if (availableDrivers.size > 0) {
                val driver = withContext(Dispatchers.Default) {

                    availableDrivers.filter {
                        !declineDrivers.contains(it.value.key)
                    }.minByOrNull {

                        driverCoordinates.latitude = it.value.latitude
                        driverCoordinates.longitude = it.value.longitude

                        riderCoordinates.distanceTo(driverCoordinates)


                    }?.value
                }

                driver?.let {
                    val token = withContext(Dispatchers.IO) {
                        driversTokens.child(driver.key).get().await().getValue(TokenDto::class.java)
                    }

                    sendRequestToDriver(
                        token?.token!!,
                        latitude,
                        longitude,
                        destinationLatitude,
                        destinationLongitude
                    )

                    Result.Success(driver)
                } ?: Result.Error(null, "No driver accepted your request.")
            } else {
                Result.Error(null, "There is no connected drivers at the moment.")
            }

        } catch (e: Exception) {

            Log.e(TAG, "ERROR")
            Result.Error(null, "We have a technical issue, please try later.")
        }
    }

    private suspend fun sendRequestToDriver(
        token: String,
        riderLatitude: Double,
        riderLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double
    ) {

        try {
            val riderToken = withContext(Dispatchers.IO) {
                ridersTokens.child(userId!!).get().await().getValue(TokenDto::class.java)?.token
            }

            val riderDto = withContext(Dispatchers.IO) {
                riders.child(userId!!).get().await()
                    .getValue(RiderDto::class.java)
            }

            val data = HashMap<String, String>()
            data["title"] = "Rider request"
            data["message"] = "A person near you needs a ride."
            data["riderLatitude"] = riderLatitude.toString()
            data["riderLongitude"] = riderLongitude.toString()
            data["riderToken"] = riderToken!!
            data["phoneNumber"] = riderDto!!.phoneNumber
            data["destinationLatitude"] = destinationLatitude.toString()
            data["destinationLongitude"] = destinationLongitude.toString()

            val notificationData = RequestDriverDataDto(
                token,
                data
            )
            api.sendNotification(notificationData)
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
        }
    }

    override fun addToDeclineDrivers(driverId: String) {
        declineDrivers.add(driverId)
    }
}