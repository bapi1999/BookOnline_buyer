package com.sbdevs.bookonline.fragments.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.MainActivity
import com.sbdevs.bookonline.activities.user.ProceedOrderActivity
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
    var outOfStockItemList:ArrayList<CartModel> = ArrayList()


    private val loadingDialog = LoadingDialog()

    var selecter = 0
    var warnings: Int = 0
    var st = ""
    var totalAmount:Int = 0
    var coinDiscountPrice = 0
    var fromTo = -1
    private var myCoins =0L
    private var newCoins =0L
    private var payWithCoin =false
    private lateinit var coinImage:ImageView

    private lateinit var payOnline: LinearLayout
    private lateinit var cashOnDelivery: LinearLayout
    private lateinit var confirmBtn:Button

    private lateinit var address: MutableMap<String, Any>

    private val gone = View.GONE
    private val visible = View.VISIBLE



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
        val lay2 = binding.lay2
        coinImage = binding.imageView14

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)



        val intent = requireActivity().intent
        totalAmount = intent.getIntExtra("total_amount", 0)
        val deliveryCharge = intent.getIntExtra("deliveryCharge", 0)
        val netSellingPrice = intent.getIntExtra("netSellingPrice", 0)

        binding.totalAmount.text = "$totalAmount/-"
        fromTo = intent.getIntExtra("From_To", -1)
        warnings = intent.getIntExtra("changInQuantity", 0)

        if(fromTo == 1){
            binding.payWithCoinBtn.visibility = gone
        }else{
            binding.payWithCoinBtn.visibility = visible
            getMyCoin()
        }

        address = intent.getSerializableExtra("address") as MutableMap<String, Any>

        val buyerName: String = address["name"].toString()
        val buyerAddress1: String = address["address1"].toString()
        val buyerAddress2: String = address["address2"].toString()
        val buyerAddressType: String = address["address_type"].toString()
        val buyerTown: String = address["city_vill"].toString()
        val buyerPinCode: String = address["pincode"].toString()
        val buyerState: String = address["state"].toString()
        val buyerPhone: String = address["phone"].toString()


        val addressBuilder = StringBuilder()
        addressBuilder.append(buyerAddress1).append(", ").append(buyerAddress2)

        val townPinBuilder = StringBuilder()
        townPinBuilder.append(buyerTown).append(", ").append(buyerPinCode)

        miniAddress.buyerName.text = buyerName
        miniAddress.buyerAddress.text = addressBuilder.toString()
        miniAddress.buyerAddressType.text = buyerAddressType
        miniAddress.buyerTownAndPin.text = townPinBuilder.toString()
        miniAddress.buyerState.text = buyerState
        miniAddress.buyerPhone.text = buyerPhone

        lay2.amountToPay.text = totalAmount.toString()
        lay2.deliveryCharge.text = deliveryCharge.toString()
        lay2.discountContainer.visibility = View.GONE
        lay2.totalSellingPrice.text = netSellingPrice.toString()

        receivedList = intent.getParcelableArrayListExtra<Parcelable>("productList") as ArrayList<CartModel>
        outOfStockItemList = intent.getParcelableArrayListExtra<Parcelable>("OutOfStockProductList") as ArrayList<CartModel>

        Log.e("size of ","RL-${receivedList.size} / OOSL-${outOfStockItemList.size}")
        if (outOfStockItemList.size !=0){
            st += "${outOfStockItemList.size} product is Out Of Stock \n"

        }

        when{
            warnings == 0->{
                Log.i("Qty change","0")
            }
            warnings == 1->{
                st += "Order quantity has been changed : $warnings product\n "
            }
            warnings > 1->{
                st += "Order quantity has been changed : $warnings products \n"
            }

        }
        binding.errorMessageText.text = st

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBackMethod()
            }
        })


        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.backBtn1.setOnClickListener {
            goBackMethod()
        }

        binding.backBtn2.setOnClickListener {
            goBackMethod()
        }



        payOnline.setOnClickListener {
            payOnline.backgroundTintList = AppCompatResources.getColorStateList(requireContext(), R.color.amber_500)
            cashOnDelivery.backgroundTintList = AppCompatResources.getColorStateList(requireContext(), R.color.grey_400)
            selecter = 1

            binding.imageView15.setImageResource(R.drawable.ic_check_box_24)
            binding.imageView15.imageTintList = AppCompatResources.getColorStateList(requireContext(),R.color.amber_600)
            binding.imageView16.setImageResource(R.drawable.ic_baseline_check_box_outline_blank_24)
            binding.imageView16.imageTintList = AppCompatResources.getColorStateList(requireContext(),R.color.grey_800)


            confirmBtn.isEnabled = true
            confirmBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(), R.color.amber_600)


        }

        cashOnDelivery.setOnClickListener {
            payOnline.backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.grey_400)
            cashOnDelivery.backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.amber_500)
            selecter = 2

            binding.imageView15.setImageResource(R.drawable.ic_baseline_check_box_outline_blank_24)
            binding.imageView15.imageTintList = AppCompatResources.getColorStateList(requireContext(),R.color.grey_800)
            binding.imageView16.setImageResource(R.drawable.ic_check_box_24)
            binding.imageView16.imageTintList = AppCompatResources.getColorStateList(requireContext(),R.color.amber_600)

            confirmBtn.isEnabled = true
            confirmBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(), R.color.amber_600)
        }



        confirmBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager, "Show")

            when (selecter) {
                1 -> {

                    // PayTM task and firebase task
                    loadingDialog.dismiss()
                    Toast.makeText(context, "Online Payment is currently unavailable", Toast.LENGTH_SHORT).show()
//                    lifecycleScope.launch(Dispatchers.Main) {
//                        delay(1000)
//                        loadingDialog.dismiss()
//                    }

                }
                2 -> {

                    lifecycleScope.launch(Dispatchers.IO) {

                        checkAllOrderMethods(receivedList, address)

                        if (fromTo == 1){
                            //From Cart
                            deleteProductFromCart()
                        }else{
                            //From BuyNow
                            if (payWithCoin){
                                updateMyCoins()
                            }else{
                                Log.i("nothing","todo")
                            }
                        }

                        delay(2000L)

                        withContext(Dispatchers.Main) {
                            val action = PaymentFragmentDirections.actionPaymentFragmentToCongratulationFragment(
                                receivedList.size
                            )
                            findNavController().navigate(action)
                        }
                    }
                }
                else -> {
                    loadingDialog.dismiss()
                    Toast.makeText(context, "Select payment method", Toast.LENGTH_SHORT).show()
                }
            }
        }



        binding.payWithCoinBtn.setOnClickListener {
            if (payWithCoin){
                payWithCoin = false

                binding.lay2.amountToPay.text = totalAmount.toString()
                binding.totalAmount.text = "$totalAmount/-"
                binding.payWithCoinBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.grey_100)
                Glide.with(requireContext()).load(R.drawable.as_coin_not_select).into(coinImage)
                binding.lay2.additionalDiscountContainer.visibility = gone

            }else{
                payWithCoin = true
                binding.payWithCoinBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.amber_100)
                Glide.with(requireContext()).load(R.drawable.as_coin_selected).into(coinImage)

                var additionalDiscount = 0
                when {
                    totalAmount>myCoins -> {
                        coinDiscountPrice = (totalAmount - myCoins).toInt()
                        newCoins = 0
                        additionalDiscount = myCoins.toInt()
                    }
                    totalAmount<myCoins -> {
                        coinDiscountPrice = 0
                        newCoins = myCoins - totalAmount
                        additionalDiscount = totalAmount
                    }

                }

                binding.lay2.amountToPay.text = coinDiscountPrice.toString()
                binding.lay2.additionalDiscountContainer.visibility = visible
                binding.lay2.additionalDiscount.text = "-${additionalDiscount}"
                binding.totalAmount.text = "$coinDiscountPrice/-"
            }

        }


    }

    private fun goBackMethod(){

        lifecycleScope.launch(Dispatchers.IO){
            undoProductQtyChange(receivedList)
            delay(1000L)
            withContext(Dispatchers.Main){
                requireActivity().finish()
            }
        }
    }




    private suspend fun checkAllOrderMethods (list: ArrayList<CartModel>, address: MutableMap<String, Any>)   {
        for (item in list) {
            val docname: String = generateDocName()
            lifecycleScope.launch (Dispatchers.IO){
                createOrders(
                    item.url,
                    item.title,
                    item.productId,
                    item.sellerId,
                    item.orderQuantity,
                    docname,
                    address,
                    item.priceSelling,
                    item.deliveryCharge,
                    item.returnPolicy,
                    item.SELLER_PROFIT
                )
                getSellerToken( item.title, item.orderQuantity, item.sellerId)
            }
        }
    }




    private  suspend fun undoProductQtyChange(list: ArrayList<CartModel>){

        for ((i, item) in list.withIndex()) {

            firebaseFirestore.collection("PRODUCTS")
                .document(item.productId).get()
                .addOnSuccessListener {

                    val stockQty = it.getLong("in_stock_quantity")!!.toLong()
                    val orderQuantity = item.orderQuantity
                    val itemSoldSoFar = it.getLong("number_of_item_sold")!!.toLong()

                    val newQty:Long = stockQty + orderQuantity
                    val itemSoldNow = itemSoldSoFar - orderQuantity
                    updateProductStock(item.productId, newQty, itemSoldNow)


                }.addOnFailureListener {
                    Log.e("check all orders error","${it.message}")
                }.await()

        }

    }

    private fun updateProductStock(productId: String, newQty: Long, itemSoldNow: Long) {

        val productMap: MutableMap<String, Any> = HashMap()

        productMap["in_stock_quantity"] = newQty
        productMap["number_of_item_sold"] = itemSoldNow

        firebaseFirestore.collection("PRODUCTS")
            .document(productId).set(productMap, SetOptions.merge())
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
        productReturnAvailable:String,
        sellerProfit:String
        ) = CoroutineScope(Dispatchers.IO).launch {
        val productMap: MutableMap<String, Any> = HashMap()
        productMap["productThumbnail"] = thumbnail
        productMap["productTitle"] = title
        productMap["productId"] = productId.trim()

        productMap["PRICE_SELLING_UNIT"] = unitSellingPrice //UNIT PRICE = x
        productMap["PRICE_SELLING_TOTAL"] = unitSellingPrice*orderQty // x * QTY  = y
        productMap["PRICE_SHIPPING_CHARGE"] = shippingCharge// SHIPPING CHARGE  = z
        productMap["PRICE_TOTAL"] = (unitSellingPrice*orderQty)+shippingCharge // total  = y+z
        productMap["ID_Of_BUYER"] = user!!.uid
        productMap["ID_Of_SELLER"] = sellerId
        productMap["ID_Of_Tracking"] = "Not Available yet"
        productMap["ID_Of_ORDER"] = generateOrderID()

        productMap["ordered_Qty"] = orderQty
        productMap["Online_payment"]=false
        productMap["status"] = "new"
        //todo- All status must be in lowercase
        productMap["is_order_canceled"] = false
        productMap["address"] = address
        productMap["Time_ordered"] = FieldValue.serverTimestamp()
        productMap["SELLER_PROFIT"] = sellerProfit

        if (productReturnAvailable == "No Replacement Policy"){
            productMap["Time_period"] = 0L
        }else{
            productMap["Time_period"] = 7L
        }

        //security fault. use firebase function for better security
        //   |
        //   V
        productMap["eligible_for_credit"] = false
        productMap["already_credited"] = false


        firebaseFirestore.collection("ORDERS")
            .document(docName).set(productMap).addOnSuccessListener {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {

                    notifySeller(thumbnail, title, orderQty, docName, sellerId)

                }

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

    private fun getMyCoin(){

        firebaseFirestore.collection("USERS")
            .document(user!!.uid).get()
            .addOnSuccessListener {
                myCoins = it.getLong("my_donation_coins")!!.toLong()
                if (myCoins <= 0L){
                    binding.payWithCoinBtn.visibility = gone
                }else{
                    binding.payWithCoinBtn.visibility = visible
                    binding.myCoin.text = "$myCoins c"
                    var discount = 0
                    when {
                        totalAmount>myCoins -> {
                            discount = (totalAmount - myCoins).toInt()
                        }
                        totalAmount<myCoins -> {
                            discount = 0
                        }
                    }
                    binding.coinDisCountPrice.text = discount.toString()
                    binding.realPrice.text = totalAmount.toString()
                }
            }
            .addOnFailureListener {
                Log.e("Get MyCoin","${it.message}")
                binding.payWithCoinBtn.visibility = gone
            }

    }

    private fun updateMyCoins(){
        val coin = hashMapOf(
            "my_donation_coins" to newCoins
        )

        firebaseFirestore.collection("USERS")
            .document(user!!.uid).set(coin, SetOptions.merge())
//            .addOnSuccessListener {  }
//            .addOnFailureListener {  }
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
            .collection("SELLER_NOTIFICATIONS").add(notificationMap)
            .addOnSuccessListener {
                Log.i("Notify Seller", "successful")

            }
            .addOnFailureListener {
                Log.e("Notify Seller", "${it.message}")
            }


    }

    private suspend fun getSellerToken(productName: String, orderQuantity: Long, sellerId: String ){
        val description = "($orderQuantity) product named ( $productName ) has been ordered"
        val title = "New Order"

        database.getReference("Seller_Tokens").child(sellerId).get()
            .addOnSuccessListener {snapShot ->
            val sellerToken = snapShot.value.toString()

            if(title.isNotEmpty() && description.isNotEmpty() && sellerToken.isNotEmpty()) {
                sendNotificationStep1(title,description,sellerToken)
            }
        }.addOnFailureListener {
            Log.e(" getSellerToken error1", it.message.toString())
        }.await()

    }





    //TODO-<<<<<<<<<<<<<=================  SEND NOTIFICATION ==============================================
    private fun sendNotificationStep1(title:String,message:String,sellerToken:String){
        val topic = "/topics/Enter_your_topic_name" //topic has to match what the receiver subscribed to

        val notification = JSONObject()
        val notifcationBody = JSONObject()

        try {
            notifcationBody.put("title", title)
            notifcationBody.put("message", message)   //Enter your notification message
            notification.put("to", sellerToken)
            notification.put("data", notifcationBody)
            Log.e("sendNotificationStep1", "try")
        } catch (e: JSONException) {
            Log.e("sendNotificationStep1", "exception: ${e.message}")
        }

        sendNotificationStep2(notification)

    }

    private fun sendNotificationStep2(notification: JSONObject) {
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST,BASE_URL, notification,
            Response.Listener<JSONObject> { response ->
                Log.i("sendNotificationStep2", "onResponse: $response")

            },
            Response.ErrorListener {
                Toast.makeText(requireContext(), "Request error", Toast.LENGTH_LONG).show()
                Log.i("sendNotificationStep2", "onErrorResponse: Didn't work")
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