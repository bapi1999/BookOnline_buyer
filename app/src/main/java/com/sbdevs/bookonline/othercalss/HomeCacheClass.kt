package com.sbdevs.bookonline.othercalss

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.sbdevs.bookonline.models.uidataclass.GridModel
import com.sbdevs.bookonline.models.uidataclass.SliderModel
import com.sbdevs.bookonline.models.uidataclass.TopCategoryModel

class HomeCacheClass {

    companion object{
        private val firebaseDatabase = SharedDataClass.database


        var isSliderImageAvailable:Boolean = false
        var sliderModelList:ArrayList<SliderModel> = ArrayList()

        var isCategoryAvailable:Boolean = false
        var categoryList:ArrayList<TopCategoryModel> = ArrayList()

        var isNewArrivalReachLast = false
        var isNewArrivalExist= false
        var lastResult: DocumentSnapshot? =null
        lateinit var times: Timestamp
        var newArrivalProductList:ArrayList<GridModel> = ArrayList()




    }
}