package com.example.uberrider.data

import com.example.uberrider.data.model.RequestDriverDataDto
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAA4DtwOnY:APA91bE6_bbMifIoPctcaR7K_B7CjtJmav3tx2U3Sa8W6cYRE6lt_dG4FJGR-Yvr_kefXT-cFBeAPPzs33r_pnzXJ0sWbjFuGJQjLflrRCkzd_MjwRZvQWKckGZD_EKj1pezCJhdjjAk"
    )
    @POST("send")
    suspend fun sendNotification(@Body notificationDataDto: RequestDriverDataDto): ResponseBody
}