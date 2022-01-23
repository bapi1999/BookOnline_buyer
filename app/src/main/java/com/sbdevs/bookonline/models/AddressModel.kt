package com.sbdevs.bookonline.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddressModel (

    val name:String = "",
    val address1:String = "",
    val address2:String = "",
    val address_type:String = "",
    val city_vill:String = "",
    val pincode:String = "",
    val buyerState:String = "",
    val phone:String = ""
) : Parcelable