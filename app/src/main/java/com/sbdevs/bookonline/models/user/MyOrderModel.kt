package com.sbdevs.bookonline.models.user

import com.google.firebase.Timestamp
import java.util.*

data class MyOrderModel (
    val orderId:String = "",
    val productThumbnail:String = "",
    val productTitle:String = "",
    val orderTime:Date = Date(),
    val price :Long = 0,
    val orderedQty:Long = 0,
    val status:String=""
        )