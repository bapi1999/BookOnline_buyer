package com.sbdevs.bookonline.models

data class CartModel2(
    val productId:String = "",
    val url:String ="",
    val title:String = "",
    val price:String = "",
    val offerPrice:String ="",
    val quantity:Long = 0L

)