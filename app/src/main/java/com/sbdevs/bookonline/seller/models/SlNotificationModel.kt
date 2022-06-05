package com.sbdevs.bookonline.seller.models

import java.util.*

data class SlNotificationModel(
    val NOTIFICATION_CODE:Long = 0L,
    val date: Date =  Date(),
    val description:String = "",
    val image:String = "",
    val order_id:String = "",
    val seen:Boolean = false
    )