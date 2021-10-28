package com.sbdevs.bookonline.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
class CartModel (
    val productId:String = "",

    val url:String ="",
    val title:String = "",
    val price:String = "",
    val inStock:Boolean = false,
    val stock:Long = 0L,
    val offerPrice:String ="",
    val quantity:Long = 0L
        )  : Parcelable