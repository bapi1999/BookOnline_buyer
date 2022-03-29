package com.sbdevs.bookonline.fragments.user

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.FragmentPaymentBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.models.user.CartModel
import com.sbdevs.bookonline.othercalss.*
import com.sbdevs.bookonline.othercalss.Constants.Companion.BASE_URL
import com.sbdevs.bookonline.othercalss.Constants.Companion.CONTENT_TYPE_FCM
import com.sbdevs.bookonline.othercalss.Constants.Companion.SERVER_KEY
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

const val TOPIC = "/topics/myTopic2"

class PaymentFragment : Fragment() {
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private val database = Firebase.database

    var receivedList: ArrayList<CartModel> = ArrayList()

    var selecter = 0
    private val loadingDialog = LoadingDialog()
    var warnings: Int = 0

    var orderedItem: Int = 0
    var totalAmount:Int = 0

    private lateinit var payOnline: LinearLayout
    private lateinit var cashOnDelivery: LinearLayout
    private lateinit var confirmBtn:Button

    private lateinit var address: MutableMap<String, Any>
    private var buyerToken = ""
    var count = 0
    var fromTo = -1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)


        payOnline = binding.linearLayout
        cashOnDelivery = binding.linearLayout22
        confirmBtn = binding.confirmButton
        confirmBtn.isEnabled = false
        confirmBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(), R.color.grey_600)

        val miniAddress = binding.miniAddres

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        val intent = requireActivity().intent
        totalAmount = intent.getIntExtra("total_amount", 0)
        binding.totalAmount.text = "$totalAmount/-"
        fromTo = intent.getIntExtra("From_To", -1)

        address = intent.getSerializableExtra("address") as MutableMap<String, Any>

        val buyerName: String = address["name"].toString()
        val buyerAddress1: String = address["address1"].toString()
        val buyerAddress2: String = address["address2"].toString()
        val buyerAddressType: String = address["address_type"].toString()
        val buyerTown: String = address["city_vill"].toString()
        val buyerPinCode: String = address["pincode"].toString()
        val buyerState: String = address["state"].toString()
        val buyerPhone: String = address["phone"].toString()

//        if (buyerName.equals(null) and buyerPhone.equals(null)) {
//            binding.confirmButton.isEnabled = false
//            binding.addressLay.visibility = View.GONE
//            binding.noAddress.visibility = View.VISIBLE
//        } else {
//            binding.confirmButton.isEnabled = true
//            binding.addressLay.visibility = View.VISIBLE
//            binding.noAddress.visibility = View.GONE
//        }

        val addressBuilder = StringBuilder()
        addressBuilder.append(buyerAddress1).append(", ").append(buyerAddress2)

        val townPinBuilder = StringBuilder()
        townPinBuilder.append(buyerTown).append(", ").append(buyerPinCode)

        binding.miniAddres.buyerName.text = buyerName
        binding.miniAddres.buyerAddress.text = addressBuilder.toString()
        binding.miniAddres.buyerAddressType.text = buyerAddressType
        binding.miniAddres.buyerTownAndPin.text = townPinBuilder.toString()
        binding.miniAddres.buyerState.text = buyerState
        binding.miniAddres.buyerPhone.text = buyerPhone

        receivedList = intent.getParcelableArrayListExtra<Parcelable>("productList") as ArrayList<CartModel>

        getMyToken()



        return binding.root

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        payOnline.setOnClickListener {
            payOnline.backgroundTintList = AppCompatResources.getColorStateList(requireContext(), R.color.amber_500)
            cashOnDelivery.backgroundTintList = AppCompatResources.getColorStateList(requireContext(), R.color.grey_400)
            selecter = 1

            confirmBtn.isEnabled = true
            confirmBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(), R.color.amber_600)


        }

        cashOnDelivery.setOnClickListener {
            payOnline.backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.grey_400)
            cashOnDelivery.backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.amber_500)
            selecter = 2

            confirmBtn.isEnabled = true
            confirmBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(), R.color.amber_600)
        }



        confirmBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager, "Show")
            Toast.makeText(requireContext(),"clicked",Toast.LENGTH_SHORT).show()

            when (selecter) {
                1 -> {
//
                    // PayTM task and firebase task
                    lifecycleScope.launch(Dispatchers.Main) {
                        delay(1000)
                        loadingDialog.dismiss()
                    }
                }
                2 -> {

                    lifecycleScope.launch(Dispatchers.IO) {

                        checkAllOrderMethods(receivedList, address)

                        if (fromTo == 1){
                            deleteProductFromCart()
                        }else{
                            Log.i("from","Buy now")
                        }


//                        withContext(Dispatchers.Main) {
//                            val action = PaymentFragmentDirections.actionPaymentFragmentToCongratulationFragment(
//                                warnings,
//                                orderedItem
//                            )
//                            findNavController().navigate(action)
//                        }
                    }
                }
                else -> {
                    loadingDialog.dismiss()
                    Toast.makeText(context, "Select payment method", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


    private suspend fun checkAllOrderMethods (
        list: ArrayList<CartModel>,
        address: MutableMap<String, Any>
    )   {
        for (item in list) {

            firebaseFirestore.collection("PRODUCTS")
                .document(item.productId).get()
                .addOnSuccessListener {

                    val stockQty = it.getLong("in_stock_quantity")!!.toLong()
                    val docname: String = generateDocName()
                    val orderQuantity = item.orderQuantity
                    val itemSoldSoFar = it.getLong("number_of_item_sold")!!.toLong()
                    val productReturnAvailable = it.getBoolean("product_return_available")!!
                    when {
                        stockQty >= orderQuantity -> {
                            val newQty = stockQty - orderQuantity
                            val itemSoldNow = itemSoldSoFar + orderQuantity
                            orderedItem++
                            // update product
                            updateProductStock(item.productId, newQty, itemSoldNow)
                            // create order

                            lifecycleScope.launch(Dispatchers.IO) {
                                createOrders(
                                    item.url,
                                    item.title,
                                    item.productId,
                                    item.sellerId,
                                    orderQuantity,
                                    docname,
                                    address,
                                    item.priceSelling,
                                    item.deliveryCharge,
                                    productReturnAvailable
                                )
                            }


                        }
                        stockQty in 1L until orderQuantity -> {
                            orderedItem++
                            val newQty = 0L
                            val itemSoldNow = itemSoldSoFar + stockQty

                            updateProductStock(item.productId, newQty, itemSoldNow)

                            lifecycleScope.launch (Dispatchers.IO){
                                createOrders(
                                    item.url,
                                    item.title,
                                    item.productId,
                                    item.sellerId,
                                    stockQty,
                                    docname,
                                    address,
                                    item.priceSelling,
                                    item.deliveryCharge,
                                    productReturnAvailable
                                )
                            }



                        }
                        stockQty == 0L -> {
                            Toast.makeText(context, "Some Product just got out of stock now", Toast.LENGTH_SHORT).show()
                            warnings = 1
                            // Don't update product
                            // Don't create order
                        }
                    }
                }.addOnFailureListener {
                    Log.e("check all orders error","${it.message}")
                }.await()
        }
    }

    private fun updateProductStock(productId: String, newQty: Long, itemSoldNow: Long) {

        val productMap: MutableMap<String, Any> = HashMap()

        productMap["in_stock_quantity"] = newQty
        productMap["number_of_item_sold"] = itemSoldNow

        firebaseFirestore.collection("PRODUCTS").document(productId).update(productMap)
    }

    private suspend  fun createOrders(
        thumbnail: String,
        title: String,
        productId: String,
        sellerId: String,
        orderQty: Long,
        docName: String,
        address: MutableMap<String, Any>,
        unitSellingPrice: Long,
        shippingCharge:Long,
        productReturnAvailable:Boolean
        ) = CoroutineScope(Dispatchers.IO).launch {
        val productMap: MutableMap<String, Any> = HashMap()
        productMap["productThumbnail"] = thumbnail
        productMap["productTitle"] = title
        productMap["productId"] = productId.trim()

        productMap["PRICE_SELLING_UNIT"] = unitSellingPrice //UNIT PRICE = x
        productMap["PRICE_SELLING_TOTAL"] = unitSellingPrice*orderQty // x * QTY  = y
        productMap["PRICE_SHIPPING_CHARGE"] = shippingCharge// SHIPPING CHARGE  = z
        productMap["PRICE_TOTAL"] = (unitSellingPrice*orderQty)+shippingCharge // total  = y+z
        productMap["BUYER_TOKEN"] = buyerToken
        productMap["ID_Of_BUYER"] = user!!.uid
        productMap["ID_Of_SELLER"] = sellerId
        productMap["ID_Of_Tracking"] = "Not Available yet"
        productMap["ID_Of_ORDER"] = generateOrderID()

        productMap["ordered_Qty"] = orderQty
        productMap["already_paid"]=false
        productMap["status"] = "new"
        //todo- All status must be in lowercase
        productMap["is_order_canceled"] = false
        productMap["address"] = address
        productMap["Time_ordered"] = FieldValue.serverTimestamp()

        if (productReturnAvailable){
            productMap["Time_period"] = 7L
        }else{
            productMap["Time_period"] = 0L
        }

        //security fault. use firebase function for better security
        //   |
        //   V
        productMap["eligible_for_credit"] = false
        productMap["already_credited"] = false


        firebaseFirestore.collection("ORDERS")
            .document(docName).set(productMap).addOnSuccessListener {
                notifySeller(thumbnail, title, orderQty, docName, sellerId)
                getSellerToken( title, orderQty, sellerId)
            }.addOnFailureListener {
                Log.e("Create order error ","${it.message}")
            }.await()

    }



    private suspend fun deleteProductFromCart() {

        SharedDataClass.dbCartList.clear()

        val updates = hashMapOf<String, Any>(
            "cart_list" to FieldValue.delete()
        )
        firebaseFirestore.collection("USERS").document(user!!.uid)
            .collection("USER_DATA").document("MY_CART")
            .update(updates)
            .addOnSuccessListener {
                Log.i("remove item from cart", "success")
            }.addOnFailureListener {
                Log.e("remove item from cart", "failed: ${it.message}")
            }.await()
    }

    private fun generateDocName(): String {

        val timeString = LocalDateTime.now().toString()
        val userString = user!!.uid.substring(0, 10)
        val randomString: String = UUID.randomUUID().toString().substring(0, 5)
        val docBuilder: StringBuilder = StringBuilder()
        docBuilder.append(timeString).append(userString).append(randomString)
        val docName = docBuilder.toString().replace(".", "_").replace("-", "_").replace(":", "_")
        return docName
    }

    private fun generateOrderID(): String {
        val timeString = LocalDateTime.now().toString()
        val userString = user!!.uid
        val randomString: String = UUID.randomUUID().toString().substring(0, 2)
        val docBuilder: StringBuilder = StringBuilder()
        docBuilder.append(timeString).append(userString).append(randomString)
        val docName = docBuilder.toString().replace(".", "").replace("-", "").replace(":", "")
        return docName
    }

    private fun notifySeller(
        thumbnail: String, title: String,
        orderQuantity: Long, docName: String, sellerId: String
    ) {

        val description = "( $orderQuantity ) product named ( $title ) has been ordered"

        val notificationMap: MutableMap<String, Any> = HashMap()
        notificationMap["date"] = FieldValue.serverTimestamp()
        notificationMap["description"] =description;

        notificationMap["image"] = thumbnail
        notificationMap["order_id"] = docName
        notificationMap["seen"] = false

        firebaseFirestore.collection("USERS").document(sellerId)
            .collection("NOTIFICATIONS").add(notificationMap)
            .addOnSuccessListener {
                Log.i("Notify Seller", "successful")

            }
            .addOnFailureListener {
                Log.e("Notify Seller", "${it.message}")
            }


    }

    private fun getSellerToken(title: String, orderQuantity: Long, sellerId: String ){
        database.getReference("Tokens").child(sellerId).get().addOnSuccessListener {snapShot ->
            val sellerToken = snapShot.value.toString()
            val description = "( $orderQuantity ) product named ( $title ) has been ordered"
            val title = "New Order"

            if(title.isNotEmpty() && description.isNotEmpty() && sellerToken.isNotEmpty()) {
                sendNotificationStep1(title,description)
            }
        }.addOnFailureListener {
            Log.e(" getSellerToken error1", it.message.toString())
        }

    }


    private fun getMyToken(){
        database.getReference("Tokens").child(user!!.uid).get().addOnSuccessListener{
            buyerToken = it.toString()
        }.addOnFailureListener {
            Log.e(" getMyToken error1", it.message.toString())
        }
    }



    //TODO-<<<<<<<<<<<<<=================  SEND NOTIFICATION ==============================================
    private fun sendNotificationStep1(title:String,message:String){
        val topic = "/topics/Enter_your_topic_name" //topic has to match what the receiver subscribed to

        val notification = JSONObject()
        val notifcationBody = JSONObject()

        try {
            notifcationBody.put("title", "Enter_title")
            notifcationBody.put("message", message)   //Enter your notification message
            notification.put("to", buyerToken)
            notification.put("data", notifcationBody)
            Log.e("TAG", "try")
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: " + e.message)
        }

        sendNotificationStep2(notification)

    }

    private fun sendNotificationStep2(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST,BASE_URL, notification,
            Response.Listener<JSONObject> { response ->
                Log.i("TAG", "onResponse: $response")

            },
            Response.ErrorListener {
                Toast.makeText(requireContext(), "Request error", Toast.LENGTH_LONG).show()
                Log.i("TAG", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = SERVER_KEY
                params["Content-Type"] = CONTENT_TYPE_FCM
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(requireContext())
    }

    //TODO-==============================  SEND NOTIFICATION ======>>>>>>>>>>>>>>>>>>>>>>>>>>>>>



}