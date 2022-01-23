package com.sbdevs.bookonline.othercalss

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.HomeAdapter
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.models.HomeModel
import com.sbdevs.bookonline.models.uidataclass.SearchModel

class SharedDataClass {




    companion object {

        var cartNumber:Int = 0
        @SuppressLint("StaticFieldLeak")
        private val firebaseFirestore = Firebase.firestore
        private val user = Firebase.auth.currentUser

        var currentACtivity = 1 // 1 -> MainActivity, 2->ProductActivity
        var product_id = ""
        var newLogin = false

        var uiViewLIst:MutableList<HomeModel> = ArrayList()
        var lastResult : DocumentSnapshot ?=null
        var lastIndex : Long =-1L
        var isReachLast:Boolean = false
        var homeAdapter:HomeAdapter = HomeAdapter(uiViewLIst)
        var dbCartList:ArrayList<MutableMap<String, Any>> = ArrayList()
        private var counter1 = 0


        var dbWishList:ArrayList<String> = ArrayList()



        fun getCartListForOptionMenu() {
            if(user != null){
                firebaseFirestore.collection("USERS").document(user.uid)
                    .collection("USER_DATA")
                    .document("MY_CART").get().addOnSuccessListener {
                        val x = it.get("cart_list")

                        if (x != null) {
                            dbCartList = x as ArrayList<MutableMap<String, Any>>
                            if (dbCartList.isEmpty()) {
                                Log.w("CartList","empty")
                                //textView.visibility = View.GONE
                            } else {
                                //textView.visibility = View.VISIBLE
                                cartNumber = dbCartList.size
                                //textView.text = dbCartList.size.toString()
                            }
                        }
//                        else {
//                            textView.visibility = View.GONE
//                        }

                    }.addOnFailureListener {
                        Log.w("CartList","${it.message}")
                    }

            }else{
                //textView.visibility = View.GONE
                Log.w("CartList","User not logged in")
            }

        }

//        fun getCartList() {
//
//            firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
//                .document("MY_CART").get().addOnSuccessListener {
//
//                    val x = it.get("cart_list")
//
//                    if (x != null) {
//                        dbCartList = x as ArrayList<MutableMap<String, Any>>
//
//                    } else {
//                        Log.w("CartList", "Cart list not found")
//                    }
//
//                }.addOnFailureListener {
//                    Log.e("CartList", "${it.message}",it.cause)
//                }
//
//        }



        fun getHomePageData(progressBar:ProgressBar,dialog: LoadingDialog){

            if(isReachLast){
                progressBar.visibility = View.GONE
            }else{
                progressBar.visibility = View.VISIBLE
                val query:Query = if (lastResult==null){
                    firebaseFirestore.collection("HOMEPAGE")
                        .orderBy("index", Query.Direction.ASCENDING)
                }else{
                    firebaseFirestore.collection("HOMEPAGE")
                        .orderBy("index", Query.Direction.ASCENDING)
                        .startAfter(lastIndex)
                }

                query.limit(5L)
                    .get().addOnSuccessListener{

                        val allDocumentSnapshot = it.documents

                        if (allDocumentSnapshot.isNotEmpty()){

                            isReachLast = allDocumentSnapshot.size < 5 // limit is 5
                            val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                            lastResult = lastR
                            lastIndex = lastR.getLong("index")!!
                            //times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!

                        }
                        else{
                            isReachLast = true
                        }




                        val homeElementList = it.toObjects(HomeModel::class.java)

                        uiViewLIst.addAll(homeElementList)


                        if (uiViewLIst.isEmpty()){
//                       searchRecycler.visibility = gone
//                       binding.progressBar2.visibility = gone
//                       binding.noResultFoundText.visibility = visible
                            Log.e("Home List", " empty list")
                        }else{
//                       searchRecycler.visibility = visible
//                       binding.progressBar2.visibility = visible
//                       binding.noResultFoundText.visibility = gone

                            homeAdapter.homeModelList =uiViewLIst
//                       homeAdapter.notifyDataSetChanged()


                            if (counter1 ==0 ){
                                homeAdapter.notifyItemRangeInserted(0, uiViewLIst.size)
                            }else{
                                homeAdapter.notifyItemRangeInserted(uiViewLIst.size-1,homeElementList.size)
                            }

                            //recyclerView.adapter = homeAdapter
                            progressBar.visibility = View.GONE
                            counter1 = 1
                        }
                        dialog.dismiss()

                    }.addOnFailureListener{
                        Log.e("HomeFragment","Failed to load home ${it.message}",it.cause)
                        progressBar.visibility = View.GONE
                        dialog.dismiss()
                    }

            }

        }


        fun getWishList(){
            if (user!= null){
                firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
                    .document("MY_WISHLIST")
                    .get().addOnSuccessListener {

                        val x = it.get("wish_list")

                        if (x != null) {
                            val wishList = x as ArrayList<String>
                            if (wishList.isNotEmpty()){
                                dbWishList = x
                            }

                        } else {
                            Log.w("WishList", "No wish list found")
                        }


                    }
                    .addOnFailureListener{
                        Log.e("WishList", "${it.message}",it.cause)
                    }
            }else{
                Log.w("WishList","User not logged in")
            }




        }



    }









}