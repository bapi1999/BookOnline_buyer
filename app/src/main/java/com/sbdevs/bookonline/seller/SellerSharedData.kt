package com.sbdevs.bookonline.seller

import com.google.firebase.firestore.DocumentSnapshot
import com.sbdevs.bookonline.seller.models.MyProductModel
import com.sbdevs.bookonline.seller.models.SellerOrderModel

class SellerSharedData {

    companion object{
        //todo -- ORDER ------------------------------------
        var newSellerOrderList:MutableList<SellerOrderModel> = ArrayList()
        var shippedOrderList:MutableList<SellerOrderModel> = ArrayList()
        var deliveredOrderList:MutableList<SellerOrderModel> = ArrayList()
        var canceledOrderList:MutableList<SellerOrderModel> = ArrayList()

        var newOrderCache = false
        var shippedOrderCache = false
        var deliveredOrderCache = false
        var canceledOrderCache = false

        var newOrderLastResult: DocumentSnapshot? = null
        var shippedLastResult: DocumentSnapshot? = null
        var deliveredLastResult: DocumentSnapshot? = null
        var canceledLastResult: DocumentSnapshot? = null

        //todo -- PRODUCT --------------------------------------

        var allProductList:ArrayList<MyProductModel> = ArrayList()
        var outOfStockList:ArrayList<MyProductModel> = ArrayList()
        var hiddenProductList:ArrayList<MyProductModel> = ArrayList()



        //todo -- address verify -------------------------------
        var isSellerVerified = false
        var isAddressVerified = false
        var isBusinessDetailAdded = false

    }

}