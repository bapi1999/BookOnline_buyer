package com.sbdevs.bookonline.models

import java.util.*

data class NotificationModel(
    val date: Date =  Date(),
    val description:String = "",
    val image:String = "",
    val order_id:String = "",
    val seen:Boolean = false
    )