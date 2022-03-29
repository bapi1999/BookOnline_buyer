package com.sbdevs.bookonline.models.user

data class CartMapModel (
    val Seller_id:String = "",
    val products:MutableList<String> = ArrayList(),
    val quantities:MutableList<Long> = ArrayList(),
    val color:String = "",
    val instance:Int = 0
        )