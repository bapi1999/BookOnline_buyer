package com.sbdevs.bookonline.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.MyOrderAdapter
import com.sbdevs.bookonline.databinding.FragmentMyOrderBinding
import com.sbdevs.bookonline.models.CartModel


class MyOrderFragment : Fragment() {
    private var _binding:FragmentMyOrderBinding?=null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser


    var dbOrderList:ArrayList<MutableMap<String,Any>> = ArrayList()
    lateinit var adapter:MyOrderAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyOrderBinding.inflate(inflater, container, false)

        if (user != null){
            getAllMyOrder()
        }else{
            binding.emptyContainer.visibility = View.VISIBLE
            binding.ordersRecycler.visibility = View.GONE
        }




        val recyclerView = binding.ordersRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = MyOrderAdapter(dbOrderList)
        recyclerView.adapter = adapter

        return binding.root
    }

    private fun getAllMyOrder(){

        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_ORDERS").get().addOnSuccessListener {
                val x = it.get("order_list")

                if (x != null){
                    dbOrderList = x as ArrayList<MutableMap<String,Any>>
                    adapter.list = dbOrderList
                    adapter.notifyDataSetChanged()


                }else{
                    binding.emptyContainer.visibility = View.VISIBLE
                    binding.ordersRecycler.visibility = View.GONE
                    Log.d("MyOrder","No order foung")
                }
            }.addOnFailureListener {
                Toast.makeText(context,it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }


}