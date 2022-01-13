package com.sbdevs.bookonline.models.uidataclass

data class SearchModel (
    val productId:String = "",
    val book_title:String="",
    val product_thumbnail:String ="",
    val price_original:Long = 0L,
    val price_selling:Long =0L,
    val in_stock_quantity:Long = 0L,
    val rating_avg:String = "",
    val rating_total:Long = 0L,
    val book_condition:String = "",
    val book_type:String = "",
    val book_printed_ON:Long = 0L

        )