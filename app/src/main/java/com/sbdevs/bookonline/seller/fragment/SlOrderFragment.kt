package com.sbdevs.bookonline.seller.fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.FragmentSlOrderBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.seller.activities.SlAddProductActivity
import com.sbdevs.bookonline.seller.activities.SlEarningActivity
import com.sbdevs.bookonline.seller.adapters.SellerOrderAdapter
import com.sbdevs.bookonline.seller.models.SellerOrderModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class SlOrderFragment : Fragment(), SellerOrderAdapter.OrderItemClickListener {

    private var _binding: FragmentSlOrderBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

//    private var paginateSellerOrderList: MutableList<SellerOrderModel> = ArrayList()
    private lateinit var sellerOrderAdapter: SellerOrderAdapter
    private lateinit var recyclerView: RecyclerView

    private var sellerOrderList: MutableList<SellerOrderModel> = ArrayList()
//    private var canceledOrderLst: MutableList<SellerOrderModel> = ArrayList()

    private var statusString: String = "new"

    private val loadingDialog = LoadingDialog()

    private var lastResult: DocumentSnapshot? = null
    private lateinit var times: Timestamp
    private var isReachLast: Boolean = false

    val gone = View.GONE
    val visible = View.VISIBLE

    private var divident = 0
    private var extra = 0
    private var lowerPoint = 0
    private var upperPoint = 9
    lateinit var reasons: Array<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlOrderBinding.inflate(inflater, container, false)

        val bottomBar = binding.bottomBar
        bottomBar.orderIcon.setImageResource(R.drawable.ic_order_icon_2_fill)
        bottomBar.productIcon.setImageResource(R.drawable.ic_outline_shopping_cart_24)
        bottomBar.earningIcon.setImageResource(R.drawable.ic_outline_payments_24)
        bottomBar.profileIcon.setImageResource(R.drawable.ic_account_circle_outline_24)



        loadingDialog.show(childFragmentManager, "SHow")
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            getOrders()
        }




        recyclerView = binding.orderRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)

        sellerOrderAdapter = SellerOrderAdapter(sellerOrderList, this)
        recyclerView.adapter = sellerOrderAdapter

        reasons = resources.getStringArray(R.array.order_cancel_reasons)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomBar = binding.bottomBar


        bottomBar.productContainer.setOnClickListener {
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.main_frame_layout, SlMyProductFragment())
                addToBackStack("sl_product")
            }
        }

        bottomBar.addProductContainer.setOnClickListener {
            val newIntent = Intent(requireContext(), SlAddProductActivity::class.java)
            startActivity(newIntent)
            bottomBar.addProductContainer.isClickable = false
        }


        bottomBar.earningContainer.setOnClickListener {
            val newIntent = Intent(requireContext(), SlEarningActivity::class.java)
            startActivity(newIntent)
            bottomBar.earningContainer.isClickable = false
        }

        bottomBar.profileContainer.setOnClickListener {
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.main_frame_layout, SlProfileFragment())
                addToBackStack("sl_profile")
            }
        }





        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (isReachLast) {
                        Log.w("Query item", "Last item is reached already")
                        binding.progressBar2.visibility = View.GONE
                    } else {

                        when (statusString) {

                            "new" -> {
                                binding.progressBar2.visibility = View.VISIBLE
                                getOrders()
                            }

                            "shipped" -> {
                                binding.progressBar2.visibility = View.VISIBLE
                                getOrderByTags("shipped","Time_shipped")
                            }

                            "delivered" -> {
                                binding.progressBar2.visibility = View.VISIBLE
                                getOrderByTags("delivered","Time_delivered")
                            }

                            "canceled" -> {
                                binding.progressBar2.visibility = View.VISIBLE
                                getCanceledOrders()
                            }

                        }

                    }
                }
            }
        })



        binding.orderTypeRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioButton1 -> {
                    statusString = "new"
                    orderTypeChangingMethod()
                    getOrders()
                }
                R.id.radioButton2 -> {
                    statusString = "shipped"
                    orderTypeChangingMethod()
                    getOrderByTags("shipped","Time_shipped")
                }
                R.id.radioButton3 -> {
                    statusString = "delivered"
                    orderTypeChangingMethod()
                    getOrderByTags("delivered","Time_delivered")
                }

                R.id.radioButton4 -> {
                    statusString = "canceled"
                    orderTypeChangingMethod()
                    getCanceledOrders()
                }

            }
        }


    }

    override fun onResume() {
        super.onResume()
        binding.bottomBar.earningContainer.isClickable = true
        binding.bottomBar.addProductContainer.isClickable = true

    }



    private fun getOrders() {

        val resultList: ArrayList<SellerOrderModel> = ArrayList()
        resultList.clear()

        val query: Query = firebaseFirestore.collection("ORDERS")
            .whereEqualTo("ID_Of_SELLER", user!!.uid)
            .whereIn("status", listOf("new","accepted","packed"))
            .orderBy("Time_ordered", Query.Direction.ASCENDING)


        query.limit(10).get().addOnSuccessListener {

            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()) {
                for (item in allDocumentSnapshot) {
                    val orderId = item.id
                    val imageUrl = item.getString("productThumbnail").toString()
                    val productName = item.getString("productTitle").toString()
                    val statusString = item.getString("status").toString()
                    val orderedQty = item.getLong("ordered_Qty")!!
                    val price = item.getLong("PRICE_TOTAL")!!
                    val buyerId = item.getString("ID_Of_BUYER").toString()
                    val onlinePayment: Boolean = item.getBoolean("Online_payment")!!
                    val orderTime: Date = item.getTimestamp("Time_ordered")!!.toDate()
                    val acceptedTime = item.getTimestamp("Time_accepted")?.toDate()
                    val packedTime = item.getTimestamp("Time_packed")?.toDate()
                    val shippedTime = item.getTimestamp("Time_shipped")?.toDate()
                    val deliveredTime = item.getTimestamp("Time_delivered")?.toDate()
                    val returnedTime = item.getTimestamp("Time_returned")?.toDate()
                    val canceledTime = item.getTimestamp("Time_canceled")?.toDate()
                    val address: MutableMap<String, Any> = item.get("address") as MutableMap<String, Any>

                    resultList.add(
                        SellerOrderModel(
                            orderId, imageUrl, productName, statusString, buyerId, orderedQty,
                            price, onlinePayment, address, orderTime, acceptedTime, packedTime,
                            shippedTime, deliveredTime, returnedTime, canceledTime
                        )
                    )
                }

                sellerOrderList.addAll(resultList)

                if (sellerOrderList.isEmpty()) {
                    binding.emptyContainer.visibility = visible
                    binding.orderRecycler.visibility = gone
                } else {
                    binding.emptyContainer.visibility = gone
                    binding.orderRecycler.visibility = visible

                    sellerOrderAdapter.list = sellerOrderList

                    if (lastResult == null ){
                        sellerOrderAdapter.notifyItemRangeInserted(0,resultList.size)
                    }else{
                        sellerOrderAdapter.notifyItemRangeInserted(sellerOrderList.size-1,resultList.size)
                    }

                    val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                    lastResult = lastR
                    times = lastR.getTimestamp("Time_ordered")!!
                }

                isReachLast = allDocumentSnapshot.size < 10

            } else {
                isReachLast = true
                if (sellerOrderList.isEmpty()) {
                    binding.emptyContainer.visibility = visible
                    binding.orderRecycler.visibility = gone
                }
            }

            binding.progressBar2.visibility = gone
            loadingDialog.dismiss()
        }.addOnFailureListener {
            Log.e("Load accepted orders", "${it.message}")
            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone

        }
    }


    private fun getOrderByTags(status:String, orderTimeType:String){

        val resultList :ArrayList<SellerOrderModel> = ArrayList()
        resultList.clear()

        val query: Query = if (lastResult == null){
            firebaseFirestore
                .collection("ORDERS")
                .whereEqualTo("ID_Of_SELLER",user!!.uid)
                .whereEqualTo("status",status)
                .orderBy(orderTimeType, Query.Direction.DESCENDING)
        }else{
            firebaseFirestore.collection("ORDERS")
                .whereEqualTo("ID_Of_SELLER",user!!.uid)
                .whereEqualTo("status",status)
                .orderBy(orderTimeType,Query.Direction.DESCENDING)
                .startAfter(times)
        }

        query.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){
                for (item in allDocumentSnapshot){

                    val orderId = item.id
                    val imageUrl =  item.getString("productThumbnail").toString()
                    val productName =  item.getString("productTitle").toString()
                    val statusString =  item.getString("status").toString()
                    val orderedQty =  item.getLong("ordered_Qty")!!
                    val price =  item.getLong("PRICE_TOTAL")!!
                    val buyerId =  item.getString("ID_Of_BUYER").toString()
                    val onlinePayment: Boolean = item.getBoolean("Online_payment")!!
                    val orderTime: Date = item.getTimestamp("Time_ordered")!!.toDate()
                    val acceptedTime= item.getTimestamp("Time_accepted")?.toDate()
                    val packedTime= item.getTimestamp("Time_packed")?.toDate()
                    val shippedTime= item.getTimestamp("Time_shipped")?.toDate()
                    val deliveredTime= item.getTimestamp("Time_delivered")?.toDate()
                    val returnedTime= item.getTimestamp("Time_returned")?.toDate()
                    val canceledTime= item.getTimestamp("Time_canceled")?.toDate()
                    val address:MutableMap<String,Any> = item.get("address")!! as MutableMap<String,Any>

                    resultList.add(SellerOrderModel(orderId,imageUrl,productName,statusString, buyerId,orderedQty,
                        price,onlinePayment,address,orderTime,acceptedTime,packedTime,
                        shippedTime,deliveredTime,returnedTime,canceledTime))

                }
                sellerOrderList.addAll(resultList)

                if (sellerOrderList.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.orderRecycler.visibility = gone
                }else{
                    binding.emptyContainer.visibility = gone
                    binding.orderRecycler.visibility = visible

                    sellerOrderAdapter.list = sellerOrderList

                    if (lastResult == null ){
                        sellerOrderAdapter.notifyItemRangeInserted(0,resultList.size)
                    }else{
                        sellerOrderAdapter.notifyItemRangeInserted(sellerOrderList.size-1,resultList.size)
                    }

                    val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                    lastResult = lastR
                    times = lastR.getTimestamp(orderTimeType)!!
                }

                isReachLast = allDocumentSnapshot.size < 10

            }else{
                isReachLast = true
                if (sellerOrderList.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.orderRecycler.visibility = gone
                }

            }

            binding.progressBar2.visibility = gone
            loadingDialog.dismiss()
        }.addOnFailureListener {
            Log.e("Load orders","${it.message}")
            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone
        }
    }



    private fun getCanceledOrders(){

        val resultList :ArrayList<SellerOrderModel> = ArrayList()
        resultList.clear()

        val query: Query = if (lastResult == null){

            firebaseFirestore
                .collection("ORDERS")
                .whereEqualTo("ID_Of_SELLER",user!!.uid)
                .whereEqualTo("is_order_canceled",true)
                .orderBy("Time_canceled", Query.Direction.DESCENDING)
        }else{
            firebaseFirestore
                .collection("ORDERS")
                .whereEqualTo("ID_Of_SELLER",user!!.uid)
                .whereEqualTo("is_order_canceled",true)
                .orderBy("Time_canceled", Query.Direction.DESCENDING)
                .startAfter(times)
        }



        query.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){
                for (item in allDocumentSnapshot){
                    val orderId = item.id
                    val imageUrl =  item.getString("productThumbnail").toString()
                    val productName =  item.getString("productTitle").toString()
                    val statusString =  item.getString("status").toString()
                    val orderedQty =  item.getLong("ordered_Qty")!!
                    val price =  item.getLong("PRICE_TOTAL")!!
                    val buyerId =  item.getString("ID_Of_BUYER").toString()
                    val onlinePayment: Boolean = item.getBoolean("Online_payment")!!
                    val orderTime: Date = item.getTimestamp("Time_ordered")!!.toDate()
                    val acceptedTime= item.getTimestamp("Time_accepted")?.toDate()
                    val packedTime= item.getTimestamp("Time_packed")?.toDate()
                    val shippedTime= item.getTimestamp("Time_shipped")?.toDate()
                    val deliveredTime= item.getTimestamp("Time_delivered")?.toDate()
                    val returnedTime= item.getTimestamp("Time_returned")?.toDate()
                    val canceledTime= item.getTimestamp("Time_canceled")?.toDate()
                    val address:MutableMap<String,Any> = item.get("address") as MutableMap<String,Any>

                    resultList.add(SellerOrderModel(orderId,imageUrl,productName,statusString, buyerId,orderedQty,
                        price,onlinePayment,address,orderTime,acceptedTime,packedTime,
                        shippedTime,deliveredTime,returnedTime,canceledTime))
                }

                sellerOrderList.addAll(resultList)

                if (sellerOrderList.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.orderRecycler.visibility = gone
                }else{
                    binding.emptyContainer.visibility = gone
                    binding.orderRecycler.visibility = visible
                    sellerOrderAdapter.list = sellerOrderList

                    if (lastResult == null ){
                        sellerOrderAdapter.notifyItemRangeInserted(0,resultList.size)
                    }else{
                        sellerOrderAdapter.notifyItemRangeInserted(sellerOrderList.size-1,resultList.size)
                    }

                    val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                    lastResult = lastR
                    times = lastR.getTimestamp("Time_canceled")!!
                }

                isReachLast = allDocumentSnapshot.size < 10
            }else{
                isReachLast = true
                if (sellerOrderList.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.orderRecycler.visibility = gone
                }
            }

            binding.progressBar2.visibility = gone
            loadingDialog.dismiss()
        }.addOnFailureListener {
            Log.e("Load orders","${it.message}")
            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone
        }

    }

//TODO - Important method, do not delete----------------------------------------------
//    private fun paginateData(sellerOrderList: MutableList<SellerOrderModel>) {
//        val totalItem = sellerOrderList.size
//        divident = totalItem / 10
//        extra = totalItem % 10
//        val kh: MutableList<SellerOrderModel> = ArrayList()
//        if (divident == 0 && extra > 0) {
//            for (i in lowerPoint until (lowerPoint + extra)) {
//                kh.add(sellerOrderList[i])
//            }
//            paginateSellerOrderList.addAll(kh)
//            sellerOrderAdapter.list = paginateSellerOrderList
//            sellerOrderAdapter.notifyItemRangeInserted(lowerPoint, extra)
//        } else if (divident > 0) {
//            for (i in lowerPoint..upperPoint) {
//                kh.add(sellerOrderList[i])
//            }
//            paginateSellerOrderList.addAll(kh)
//            sellerOrderAdapter.list = paginateSellerOrderList
//            sellerOrderAdapter.notifyItemRangeInserted(lowerPoint, 10)
//            lowerPoint += 10
//            upperPoint += 10
//            divident -= 1
//        } else {
//            Log.w("divident", "divident cant be -ve")
//        }
//        binding.progressBar2.visibility = gone
//    }


    private fun orderTypeChangingMethod() {
        sellerOrderList.clear()
        lowerPoint = 0
        upperPoint = 9
        divident = 0
        extra = 0
        sellerOrderAdapter.notifyDataSetChanged()
        lastResult = null
        isReachLast = false
    }


    private fun updateOrder(orderId: String, status: String) {

        val orderMap: MutableMap<String, Any> = HashMap()
        orderMap["status"] = status
        orderMap["Time_$status"] = FieldValue.serverTimestamp()

        firebaseFirestore
            .collection("ORDERS")
            .document(orderId).update(orderMap)
            .addOnSuccessListener {
                Log.i("$status order", "successful")
                loadingDialog.dismiss()
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Log.e("$status order", "${it.message}")
                Toast.makeText(requireContext(), "Failed to update order", Toast.LENGTH_LONG).show()
            }

    }




    override fun acceptClickListener(position: Int) {

        loadingDialog.show(childFragmentManager, "show")
        val documentId = sellerOrderList[position].documentId
        sellerOrderList[position].status = "accepted"
        sellerOrderList[position].Time_accepted = Date()
        sellerOrderAdapter.notifyItemChanged(position)
        updateOrder(documentId, "accepted")

    }

    override fun shipClickListener(position: Int) {
        loadingDialog.show(childFragmentManager, "show")
        val documentId = sellerOrderList[position].documentId
        updateOrder(documentId, "shipped")
        sellerOrderList.removeAt(position)
        sellerOrderAdapter.notifyItemRemoved(position)
    }

    override fun cancelClickListener(position: Int) {
        val orderId = sellerOrderList[position].documentId
        val onlinePayment= sellerOrderList[position].onlinePayment
        val buyerId = sellerOrderList[position].buyerId
        dialogOption(orderId,onlinePayment,buyerId,position)
    }

    private fun dialogOption(documentId: String,onlinePayment:Boolean,buyerId:String,position: Int) {
        val qtyDialog = Dialog(requireContext())
//        qtyDialog.window!!.setBackgroundDrawable(
//            ColorDrawable(Color.argb(100, 0, 0, 0))
//            //AppCompatResources.getDrawable(requireContext(), R.drawable.s_shape_bg_2)
//        )
        qtyDialog.requestWindowFeature(1)

        qtyDialog.setContentView(R.layout.sle_order_cancellation_lay_1)
        qtyDialog.setCancelable(true)
        qtyDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        qtyDialog.show()

        val radioGroup: RadioGroup = qtyDialog.findViewById(R.id.reason_radio_group)
        val submitBtn: Button = qtyDialog.findViewById(R.id.submit_btn)
        val txt: TextView = qtyDialog.findViewById(R.id.textView91)

        val radioReason1:RadioButton = qtyDialog.findViewById(R.id.reason1)
        val radioReason2:RadioButton = qtyDialog.findViewById(R.id.reason2)
        val radioReason3:RadioButton = qtyDialog.findViewById(R.id.reason3)
        val radioReason4:RadioButton = qtyDialog.findViewById(R.id.reason4)

        radioReason1.text = reasons[0]
        radioReason2.text = reasons[1]
        radioReason3.text = reasons[2]
        radioReason4.text = reasons[3]

        var reason = reasons[0]

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {

                R.id.reason1 -> {
                    reason = reasons[0]
                }
                R.id.reason2 -> {
                    reason = reasons[1]
                }
                R.id.reason3 -> {
                    reason = reasons[2]
                }
                R.id.reason4 -> {
                    reason = reasons[3]
                }


            }
        }

        submitBtn.setOnClickListener {
            txt.text = reason
            cancelOrder(documentId,reason,onlinePayment,buyerId,position)
            qtyDialog.dismiss()
        }

    }

    private fun cancelOrder(documentId: String, reason: String,onlinePayment:Boolean,buyerId: String,position: Int) {

        val orderMap: MutableMap<String, Any> = HashMap()
        orderMap["status"] = "canceled"
        orderMap["is_order_canceled"] = true
        orderMap["order_canceled_by"] = "seller"
        orderMap["cancellation_reason"] = reason
        orderMap["Time_canceled"] = FieldValue.serverTimestamp()


        firebaseFirestore
            .collection("ORDERS")
            .document(documentId).update(orderMap)
            .addOnSuccessListener {
                if (onlinePayment){
                    sendRefundRequest(documentId, buyerId )
                }
                Log.i("canceled order", "successful")
                loadingDialog.dismiss()
                sellerOrderList.removeAt(position)
                sellerOrderAdapter.notifyItemRemoved(position)
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Log.e("canceled order", "${it.message}")
            }

    }

    private fun sendRefundRequest(documentId: String,buyerId:String){
        val refundMap: MutableMap<String, Any> = HashMap()
        refundMap["Buyer_Id"] = buyerId
        refundMap["Time"] = FieldValue.serverTimestamp()
        refundMap["Money_refunded"]=false
        refundMap["Order_doc_id"] = documentId

        firebaseFirestore.collection("REFUND_REQUEST").add(refundMap)
            .addOnSuccessListener {  }
    }


}