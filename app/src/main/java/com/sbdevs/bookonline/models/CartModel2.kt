package com.sbdevs.bookonline.models

data class CartModel2(
    val productId:String = "",
    val stockQuantity:Long = 0L,
    val orderQtyinty:Long = 0L,
    val state:Int = -1
//state: 0= already stock out 1= less than OrderQty so Stock out 2 = greater than orderQty

)