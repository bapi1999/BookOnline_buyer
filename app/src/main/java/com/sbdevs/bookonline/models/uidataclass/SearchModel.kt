package com.sbdevs.bookonline.models.uidataclass

data class SearchModel (
    val productId:String = "",
    val productName:String="",
    val url:String ="",
    val priceOriginal:Long = 0L,
    val priceSelling:Long =0L,
    val stockQty:Long = 0L,
    val avgRating:String = "",
    val totalRatings:Long = 0L,

        )