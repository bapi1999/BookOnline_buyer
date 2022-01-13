package com.sbdevs.bookonline.othercalss

import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SharedDataClass {
    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    var cartNumber:Int = 0


    fun getCartListForOptionMenu(textView: TextView) {
        if(user != null){
            firebaseFirestore.collection("USERS").document(user.uid)
                .collection("USER_DATA")
                .document("MY_CART").get().addOnSuccessListener {
                    val x = it.get("cart_list")
                    if (x != null) {
                        val fbCartList = x as ArrayList<MutableMap<String, Any>>
                        if (fbCartList.size <= 0) {
                            textView.visibility = View.GONE
                        } else {
                            textView.visibility = View.VISIBLE
                            cartNumber = fbCartList.size
                            textView.text = cartNumber.toString()
                        }
                    } else {
                        textView.visibility = View.GONE
                    }
                }.addOnFailureListener {
                    Log.w("CartList","${it.message}")
                }

        }else{
            Log.w("CartList","User not logged in")
        }

    }



}