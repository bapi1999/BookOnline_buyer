package com.sbdevs.bookonline.othercalss

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.models.uidataclass.SliderModel

class SharedDataClass {




    companion object {

        val database = Firebase.database("https://ecommerceapp2-ui-db-home.asia-southeast1.firebasedatabase.app/").reference


        var orderCameFrom = 0
        //1 = buy now / 2 = cart

        var cartNumber:Int = 0
        @SuppressLint("StaticFieldLeak")
        private val firebaseFirestore = Firebase.firestore
        private val user = Firebase.auth.currentUser
        var currentACtivity = 1 // 1 -> MainActivity, 2->ProductActivity
        var product_id = ""
        var newLogin = false

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