package com.sbdevs.bookonline.models

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class MyDonationModel (
    val Time_donate_request:Date = Date(),
    val Time_donate_received:Date = Date(),
    val total_qty:Long = 0,
    val Donor_Id:String = "",
    val total_point:Long = 0,
    val item_List:MutableList<MutableMap<String, Any>> = ArrayList(),
    val is_received:Boolean = false,
        )