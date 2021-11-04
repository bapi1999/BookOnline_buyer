package com.sbdevs.bookonline.othercalss

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.adapters.SearchQueryAdapter

class FireStoreData {

    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = FirebaseAuth.getInstance()

    private var searchList:ArrayList<String> = ArrayList()
    private lateinit var searchAdapter: SearchQueryAdapter //= SearchQueryAdapter(searchList,0,1)


//    var list:ArrayList<MutableMap<String, Any>> = ArrayList()

    fun getFirebaseCartList(context:Context,list:ArrayList<MutableMap<String, Any>>){

        firebaseFirestore.collection("USERS").document(firebaseAuth.currentUser!!.uid)
            .collection("USER_DATA").document("MY_CART")
            .get().addOnCompleteListener {
                if (it.isSuccessful){
                    val x = it.result?.get("cart_list")
                    if (x ==null){
                        Toast.makeText(context,"Failed", Toast.LENGTH_SHORT).show()
                    }else{
//                        list = x
                    }
                }
            }

    }

    fun getProductnameList(recyclerView:RecyclerView){
        firebaseFirestore.collection("PRODUCT_FILTER").document("FILTER_1").get().addOnSuccessListener {
            searchList  = it.get("LIST1") as ArrayList<String>
//            searchAdapter.list =searchList
            searchAdapter.notifyDataSetChanged()
            //searchAdapter = SearchQueryAdapter(searchList)
            recyclerView.adapter = searchAdapter
        }
    }

    fun getUsername(textView: TextView){
        firebaseFirestore.collection("USERS").document(firebaseAuth.currentUser!!.uid).get()
            .addOnSuccessListener {
                if (it.exists()){
                    val email = it.getString("email").toString()
                    val name = it.getString("name").toString()
                    if (name == ""){
                        textView.text = email
                    }else{
                        textView.text = name
                    }

                }
            }
    }


}