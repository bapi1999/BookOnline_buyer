package com.sbdevs.bookonline.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
class CartModel (
    val productId:String = "",
    val sellerId:String = "",
    val url:String ="",
    val title:String = "",

    val priceOriginal:Long = 0L,
    val priceSelling:Long =0L,

    val stockQty:Long = 0L,
    var orderQuantity:Long = 0L
        )  : Parcelable