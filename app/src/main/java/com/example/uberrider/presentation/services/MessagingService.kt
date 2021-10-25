package com.example.uberrider.presentation.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.uberrider.R
import com.example.uberrider.domain.usecases.UpdateTokenService
import com.example.uberrider.presentation.StartingActivity
import com.example.uberrider.presentation.model.DriverResponseEvent
import com.example.uberrider.presentation.util.Constants.ACCEPT_RIDER_REQUEST
import com.example.uberrider.presentation.util.Constants.DECLINE_RIDER_REQUEST
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject
import kotlin.random.Random

private const val CHANNEL_ID = "prom_channel"

@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main)

    @Inject
    lateinit var updateToken: UpdateTokenService
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        serviceScope.launch {
            updateToken.invoke(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        when {
            remoteMessage.data["title"] == DECLINE_RIDER_REQUEST -> {
                EventBus.getDefault().postSticky(

                    DriverResponseEvent(
                        driverId = remoteMessage.data["driverId"]!!,
                        response = remoteMessage.data["title"]!!
                    )

                )
            }
            remoteMessage.data["title"] == ACCEPT_RIDER_REQUEST -> {
                EventBus.getDefault().postSticky(
                    DriverResponseEvent(
                        driverId = remoteMessage.data["driverId"]!!,
                        response = remoteMessage.data["title"]!!,
                        phoneNumber = remoteMessage.data["phoneNumber"]!!,
                        driverName = remoteMessage.data["driverName"]!!,
                        driverLatitude = remoteMessage.data["latitude"]!!.toDouble(),
                        driverLongitude = remoteMessage.data["longitude"]!!.toDouble(),
                        profilePhoto = remoteMessage.data["photoUrl"]!!

                    )
                )
            }
            else -> {

                EventBus.getDefault().postSticky(
                    DriverResponseEvent(
                        response = "Driver arrived.",
                    )
                )
                val intent = Intent(this, StartingActivity::class.java)
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notificationId = Random.nextInt()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel(notificationManager)
                }

                val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)

                val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(remoteMessage.data["title"])
                    //                .setContentText(remoteMessage.data["message"])
                    .setSmallIcon(R.drawable.car_icon)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setContentIntent(pendingIntent)
                    .build()

                notificationManager.notify(notificationId, notification)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "messaging_channel"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {

            description = "Channel for products promotions"
            enableLights(true)
            lightColor = Color.BLACK

        }

        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceJob.cancel()
    }
}