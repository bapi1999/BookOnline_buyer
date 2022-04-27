package com.sbdevs.bookonline.othercalss

import android.util.Log
import com.sbdevs.bookonline.models.uidataclass.SliderModel
import com.sbdevs.bookonline.models.uidataclass.TopCategoryModel

class HomeCacheClass {

    companion object{
        private val firebaseDatabase = SharedDataClass.database


        var isSliderImageAvailable:Boolean = false
        var sliderModelList:ArrayList<SliderModel> = ArrayList()

        var isCategoryAvailable:Boolean = false
        var categoryList:ArrayList<TopCategoryModel> = ArrayList()




    }
}