package com.sbdevs.bookonline.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.models.HomeModel

class HomeViewModel (): ViewModel() {

    private var uiViewLIst = MutableLiveData<MutableList<HomeModel>>()
    private val firebaseFirestore = Firebase.firestore

    val sds: MutableLiveData<MutableList<HomeModel>> get()  = uiViewLIst

    fun getHomePageData(){

        firebaseFirestore.collection("HOMEPAGE")
            .orderBy("index", Query.Direction.ASCENDING)
            .limit(5L)
            .get().addOnSuccessListener{

                uiViewLIst = it.toObjects(HomeModel::class.java) as MutableLiveData<MutableList<HomeModel>>


            }.addOnFailureListener{
                Log.e("HomeFragment","Failed to load home ${it.message}",it.cause)
            }
    }

    fun getList():MutableLiveData<MutableList<HomeModel>>{
        return uiViewLIst
    }


}