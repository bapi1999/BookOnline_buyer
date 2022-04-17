package com.sbdevs.bookonline.models

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class MyDonationModel (
    var Time_donate_request:Date = Date(),
    var Donor_Id:String = "",
    var total_qty:Long = 0,
    var total_point:Long = 0,
    var is_received:Boolean = false,
    var item_List:MutableList<MutableMap<String, Any>> = ArrayList()

        )