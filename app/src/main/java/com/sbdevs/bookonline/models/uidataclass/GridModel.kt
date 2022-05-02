package com.sbdevs.bookonline.models.uidataclass

import java.util.*
import kotlin.collections.ArrayList

data class GridModel (
    val productId:String = "",
    val book_title:String="",
    val productImage_List: MutableList<String> =ArrayList(),
    val price_original:Long = 0L,
    val price_selling:Long =0L,
    var PRODUCT_ADDED_ON:Date = Date()
        )