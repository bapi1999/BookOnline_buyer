package com.sbdevs.bookonline.fragments.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.adapters.user.MyOrderAdapter
import com.sbdevs.bookonline.databinding.FragmentMyOrderBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import kotlin.collections.ArrayList


class MyOrderFragment : Fragment() {
    private var _binding:FragmentMyOrderBinding?=null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser


    var dbOrderList:ArrayList<MutableMap<String,Any>> = ArrayList()

    val paginateOrderList: ArrayList<MutableMap<String,Any>> = ArrayList()

    lateinit var adapter: MyOrderAdapter
    private lateinit var orderRecycler:RecyclerView

    private val loadingDialog = LoadingDialog()

    private var lastResult: DocumentSnapshot? =null
    private lateinit var times: Timestamp

    private var divident = 0
    private var extra = 0
    private var lowerPoint = 0
    private var upperPoint=9

    private var st = ""

    private val numArray = arrayOf(1,2,3,9,11,13,25,37,99, 110, 132)


    private var isReachLast:Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyOrderBinding.inflate(inflater, container, false)
        loadingDialog.show(childFragmentManager,"Show")

        if (user != null){
            getAllMyOrder()
        }else{
            binding.emptyContainer.visibility = View.VISIBLE
            binding.ordersRecycler.visibility = View.GONE
            loadingDialog.dismiss()
        }




        orderRecycler = binding.ordersRecycler
        orderRecycler.layoutManager = LinearLayoutManager(context)

        adapter = MyOrderAdapter(dbOrderList)
        orderRecycler.adapter = adapter

        //paginateData()

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        orderRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)


                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (isReachLast){
                        Log.w("Query item","Last item is reached already")
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(),"ended by true",Toast.LENGTH_SHORT).show()

                    }else{


                        if (dbOrderList.size == paginateOrderList.size){
                            Log.e("last query", "reached last")
                            isReachLast = true
                            binding.progressBar.visibility = View.GONE

                        }

                        else{
                            binding.progressBar.visibility = View.VISIBLE
                            paginateData(dbOrderList,divident,extra)
                        }

                    }

                }

            }

        })

    }

    private fun getAllMyOrder(){

        firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("USER_DATA")
            .document("MY_ORDERS")
            .get().addOnSuccessListener {
                val x = it.get("order_list")

                if (x != null){
                    dbOrderList = x as ArrayList<MutableMap<String,Any>>

                    if (dbOrderList.isEmpty()){

                        binding.emptyContainer.visibility = View.VISIBLE
                        binding.ordersRecycler.visibility = View.GONE

                    }else{

                        dbOrderList.reverse()

                        val totalItem = dbOrderList.size
                        divident = totalItem/10
                        extra = totalItem%10

                        st += "$totalItem => div= $divident / mod= $extra \n"

                        if (totalItem <= 10){
                            adapter.list = dbOrderList
                            adapter.notifyDataSetChanged()
                            isReachLast = true


                        }else{
                            paginateData(dbOrderList,divident,extra)
                        }
                    }

                }else{
                    binding.emptyContainer.visibility = View.VISIBLE
                    binding.ordersRecycler.visibility = View.GONE
                    Log.d("MyOrder","No order foung")
                }
                loadingDialog.dismiss()
            }.addOnFailureListener {
                Toast.makeText(context,it.message.toString(), Toast.LENGTH_SHORT).show()
                loadingDialog.dismiss()
                binding.emptyContainer.visibility = View.VISIBLE
                binding.ordersRecycler.visibility = View.GONE
            }
    }

    private fun paginateData( orderList:ArrayList<MutableMap<String,Any>>,divCount:Int,extra:Int){

        val kh: ArrayList<MutableMap<String,Any>> = ArrayList()

        if (divCount == 0 && extra > 0){
            for (i in lowerPoint until (lowerPoint+extra)){
                kh.add(orderList[i])
            }

            paginateOrderList.addAll(kh)
            adapter.list = paginateOrderList
            adapter.notifyItemRangeInserted(lowerPoint,extra)


//            st += "method 1 ${orderList.size} => div= $divCount / mod= $extra / listsize = ${kh.size} \n"

        }else if (divCount>0){

            for (i in lowerPoint .. upperPoint){
                kh.add(orderList[i])
            }
            paginateOrderList.addAll(kh)
            adapter.list = paginateOrderList
            adapter.notifyItemRangeInserted(lowerPoint,10)

            lowerPoint+=10
            upperPoint+=10

            divident-=1

//            st += "method 2 ${orderList.size} => div= $divCount / mod= $extra / listsize = ${kh.size} \n"
        }else{
            Toast.makeText(requireContext()," divident gone -ve",Toast.LENGTH_SHORT).show()
            Log.w("divident","divident cant be -ve")
        }

        binding.textView66.text = st
        binding.progressBar.visibility = View.GONE


    }


}