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
import com.google.android.gms.ads.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.adapters.user.MyOrderAdapter
import com.sbdevs.bookonline.databinding.FragmentMyOrderBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.models.user.MyOrderModel
import kotlin.collections.ArrayList


class MyOrderFragment : Fragment() {
    private var _binding: FragmentMyOrderBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser


    var orderList: ArrayList<MyOrderModel> = ArrayList()


    lateinit var adapter: MyOrderAdapter
    private lateinit var orderRecycler: RecyclerView

    private val loadingDialog = LoadingDialog()

    private var lastResult: DocumentSnapshot? = null
    private lateinit var times: Timestamp
    private var isReachLast: Boolean = false
    private val gone = View.GONE
    private val visible = View.VISIBLE
    private lateinit var bannerAdView:AdView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyOrderBinding.inflate(inflater, container, false)
        loadingDialog.show(childFragmentManager, "Show")

        if (user != null) {
            getMyOrders()
        } else {
            binding.emptyContainer.visibility = View.VISIBLE
            binding.ordersRecycler.visibility = View.GONE
            loadingDialog.dismiss()
        }




        orderRecycler = binding.ordersRecycler
        orderRecycler.layoutManager = LinearLayoutManager(context)

        adapter = MyOrderAdapter(orderList)
        orderRecycler.adapter = adapter

        //paginateData()

        MobileAds.initialize(requireContext()) {}
        bannerAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        bannerAdView.loadAd(adRequest)

        bannerAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                Log.e("Banner","Ad loaded successfully")
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                Log.e("Banner load Failed","${adError.message}")
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        orderRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)


                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (isReachLast) {
                        Log.w("Query item", "Last item is reached already")
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "ended by true", Toast.LENGTH_SHORT).show()

                    } else {
                        binding.progressBar.visibility = View.VISIBLE
                        getMyOrders()

                    }

                }

            }

        })

    }

    override fun onPause() {
        super.onPause()
        bannerAdView.pause()
    }

    override fun onResume() {
        super.onResume()
        bannerAdView.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        bannerAdView.destroy()
    }

    private fun getMyOrders() {
        val resultList: MutableList<MyOrderModel> = ArrayList()
        resultList.clear()
        val query: Query = if (lastResult == null) {
            firebaseFirestore.collection("ORDERS")
                .whereEqualTo("ID_Of_BUYER", user!!.uid)
                .orderBy("Time_ordered", Query.Direction.DESCENDING)
        } else {
            firebaseFirestore.collection("ORDERS")
                .whereEqualTo("ID_Of_BUYER", user!!.uid)
                .orderBy("Time_ordered", Query.Direction.DESCENDING)
                .startAfter(times)
        }

        query.limit(10L).get()
            .addOnSuccessListener {
                val allDocumentSnapshot = it.documents
                if (allDocumentSnapshot.isNotEmpty()) {
                    for (item in allDocumentSnapshot) {
                        val orderId = item.id
                        val productThumbnail = item.get("productThumbnail").toString()
                        val productTitle = item.get("productTitle").toString()
                        val orderTime = item.getTimestamp("Time_ordered")!!.toDate()
                        val price = item.getLong("PRICE_TOTAL")!!.toLong()
                        val orderedQty = item.getLong("ordered_Qty")!!
                        val status = item.get("status").toString()
                        resultList.add(
                            MyOrderModel(
                                orderId,
                                productThumbnail,
                                productTitle,
                                orderTime,
                                price,
                                orderedQty,
                                status
                            )
                        )
                    }

                    isReachLast = allDocumentSnapshot.size < 10

                } else {
                    isReachLast = true
                }

                orderList.addAll(resultList)

                if (orderList.isEmpty()) {
                    orderRecycler.visibility = gone
                    binding.progressBar.visibility = gone
                    binding.emptyContainer.visibility = visible
                } else {
                    orderRecycler.visibility = visible
                    binding.progressBar.visibility = visible
                    binding.emptyContainer.visibility = gone

                    adapter.list = orderList

                    if (lastResult == null) {
                        adapter.notifyItemRangeInserted(0, resultList.size)
                    } else {
                        adapter.notifyItemRangeInserted(orderList.size - 1, resultList.size)
                    }

                    val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                    lastResult = lastR
                    times = lastR.getTimestamp("Time_ordered")!!

                    binding.progressBar.visibility = View.GONE
                }

                loadingDialog.dismiss()

            }.addOnFailureListener {
                Log.e("get my orders", "${it.message}")
                loadingDialog.dismiss()
            }
    }

    private fun oLD_METHODS() {
//        //TODO-OLD_METHOD===================================================================================================================
//        private fun getAllMyOrder(){
//
//            firebaseFirestore.collection("USERS")
//                .document(user!!.uid).collection("USER_DATA")
//                .document("MY_ORDERS")
//                .get().addOnSuccessListener {
//                    val x = it.get("order_list")
//
//                    if (x != null){
//                        dbOrderList = x as ArrayList<MutableMap<String,Any>>
//
//                        if (dbOrderList.isEmpty()){
//
//                            binding.emptyContainer.visibility = View.VISIBLE
//                            binding.ordersRecycler.visibility = View.GONE
//
//                        }else{
//
//                            dbOrderList.reverse()
//
//                            val totalItem = dbOrderList.size
//                            divident = totalItem/10
//                            extra = totalItem%10
//
//                            st += "$totalItem => div= $divident / mod= $extra \n"
//
//                            if (totalItem <= 10){
//                                adapter.list = dbOrderList
//                                adapter.notifyDataSetChanged()
//                                isReachLast = true
//
//
//                            }else{
//                                paginateData(dbOrderList,divident,extra)
//                            }
//                        }
//
//                    }else{
//                        binding.emptyContainer.visibility = View.VISIBLE
//                        binding.ordersRecycler.visibility = View.GONE
//                        Log.d("MyOrder","No order foung")
//                    }
//                    loadingDialog.dismiss()
//                }.addOnFailureListener {
//                    Toast.makeText(context,it.message.toString(), Toast.LENGTH_SHORT).show()
//                    loadingDialog.dismiss()
//                    binding.emptyContainer.visibility = View.VISIBLE
//                    binding.ordersRecycler.visibility = View.GONE
//                }
//        }
//        private fun paginateData( orderList:ArrayList<MutableMap<String,Any>>,divCount:Int,extra:Int){
//
//            val kh: ArrayList<MutableMap<String,Any>> = ArrayList()
//
//            if (divCount == 0 && extra > 0){
//                for (i in lowerPoint until (lowerPoint+extra)){
//                    kh.add(orderList[i])
//                }
//
//                paginateOrderList.addAll(kh)
//                adapter.list = paginateOrderList
//                adapter.notifyItemRangeInserted(lowerPoint,extra)
//
//
////            st += "method 1 ${orderList.size} => div= $divCount / mod= $extra / listsize = ${kh.size} \n"
//
//            }else if (divCount>0){
//
//                for (i in lowerPoint .. upperPoint){
//                    kh.add(orderList[i])
//                }
//                paginateOrderList.addAll(kh)
//                adapter.list = paginateOrderList
//                adapter.notifyItemRangeInserted(lowerPoint,10)
//
//                lowerPoint+=10
//                upperPoint+=10
//
//                divident-=1
//
////            st += "method 2 ${orderList.size} => div= $divCount / mod= $extra / listsize = ${kh.size} \n"
//            }else{
//                Toast.makeText(requireContext()," divident gone -ve",Toast.LENGTH_SHORT).show()
//                Log.w("divident","divident cant be -ve")
//            }
//
//            binding.textView66.text = st
//            binding.progressBar.visibility = View.GONE
//
//
//        }
////TODO-OLD_METHOD===================================================================================================================

    }

}