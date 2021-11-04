package com.sbdevs.bookonline.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
class CartModel (
    val productId:String = "",
    val sellerId:String = "",
    val url:String ="",
    val title:String = "",
    val price:String = "",
    val inStock:Boolean = false,
    val stockQty:Long = 0L,
    val offerPrice:String ="",
    val orderQuantity:Long = 0L
        )  : Parcelable