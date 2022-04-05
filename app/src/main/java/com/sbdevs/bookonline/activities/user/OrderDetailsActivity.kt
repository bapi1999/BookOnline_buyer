package com.sbdevs.bookonline.activities.user

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.ProductActivity
import com.sbdevs.bookonline.databinding.ActivityOrderDetailsBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.othercalss.TimeDateAgo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap

class OrderDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDetailsBinding
    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private lateinit var productImage: ImageView

    private lateinit var productId: String

    private lateinit var orderID: String
    private var sellerID: String = ""

    private lateinit var productName: String
    private lateinit var imageUrl: String
    val gone = View.GONE
    val visible = View.VISIBLE

    //Rating lay out ================================

    private var ALL_READY_REVIEWED = false
    lateinit var ratingBar: RatingBar
    lateinit var reviewInput: TextInputLayout

    var rating5: Long = 0
    var rating4: Long = 0
    var rating3: Long = 0
    var rating2: Long = 0
    var rating1: Long = 0
    var totalRatingsNumber = 0L
    var onlinePayment = false

    private lateinit var buyerName: String
    //Rating lay out ================================


    private val loadingDialog = LoadingDialog()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        orderID = intent.getStringExtra("orderID")!!

        loadingDialog.show(supportFragmentManager, "show")

        lifecycleScope.launch(Dispatchers.IO) {
            getMyOrder(orderID)


        }


        productImage = binding.lay1.productImage


        ratingBar = binding.layRating.rateNowContainer
        reviewInput = binding.layRating.reviewInput

        ratingBar.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, fl, b ->
                Toast.makeText(this, " rating${ratingBar.rating.toLong()}", Toast.LENGTH_SHORT)
                    .show()
            }


    }

    override fun onStart() {
        super.onStart()

        binding.cancelOrderBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                cancelOrder(orderID, sellerID)
            }

        }
        binding.returnOrderBtn.setOnClickListener {
            returnOrder(orderID, sellerID)
        }

        binding.lay1.viewDetails.setOnClickListener {
            val productIntent = Intent(this, ProductActivity::class.java)
            productIntent.putExtra("productId", productId)
            startActivity(productIntent)
        }


        binding.layRating.submitBtn.setOnClickListener {
            loadingDialog.show(supportFragmentManager, "show")
            checkAllDetails()
        }


    }

    private suspend fun getMyOrder(orderID: String) {

        val lay1 = binding.lay1
        val lay2 = binding.lay2


        val orderRef = firebaseFirestore.collection("ORDERS").document(orderID)

        orderRef.get()
            .addOnSuccessListener {

                if (it.exists()) {
                    val productThumbnail = it.get("productThumbnail").toString().trim()
                    imageUrl = productThumbnail
                    val title = it.get("productTitle").toString()
                    productName = title

                    val pricePerUnit = it.get("PRICE_SELLING_UNIT").toString()
//                    val price = it.get("price").toString()
                    val shippingCharge = it.get("PRICE_SHIPPING_CHARGE").toString()
                    val priceTotal = it.get("PRICE_TOTAL").toString()

                    val orderedQty = it.getLong("ordered_Qty")!!
                    val status = it.get("status").toString()
                    onlinePayment = it.getBoolean("Online_payment")!!

                    val productIdDB = it.get("productId").toString()
                    val tracKingId = it.get("ID_Of_Tracking").toString()
                    sellerID = it.get("ID_Of_SELLER").toString()

                    val isOrderCanceled = it.getBoolean("is_order_canceled")!!
                    val orderCanceledBy = it.get("order_canceled_by").toString()
                    val cancellationReason = it.get("cancellation_reason").toString()

                    val orderTime = it.getTimestamp("Time_ordered")!!.toDate()
                    val acceptedTime = it.getTimestamp("Time_accepted")
                    val packedTime = it.getTimestamp("Time_packed")
                    val shippedTime = it.getTimestamp("Time_shipped")
                    val deliveredTime = it.getTimestamp("Time_delivered")
                    val returnedTime = it.getTimestamp("Time_returned")
                    val canceledTime = it.getTimestamp("Time_canceled")
                    val returnPeriod = it.getLong("Time_period")!!.toLong()
                    val address: MutableMap<String, Any> =
                        it.get("address") as MutableMap<String, Any>

                    val daysAgo = TimeDateAgo().msToTimeAgo(this@OrderDetailsActivity, orderTime)

                    productId = productIdDB




                    when (status) {
                        "new" -> {
                            binding.cancelOrderBtn.isEnabled = true
                            binding.cancelOrderBtn.visibility = visible
                            binding.returnOrderBtn.visibility = gone
                            binding.orderRatingContainer.visibility = gone
                            orderNew(orderTime)


                        }

                        "accepted" -> {
                            binding.cancelOrderBtn.isEnabled = true
                            binding.cancelOrderBtn.visibility = visible
                            binding.returnOrderBtn.visibility = gone
                            binding.orderRatingContainer.visibility = gone
                            val acceptT = acceptedTime!!.toDate()

                            orderNew(orderTime)
                            orderAccepted(acceptT)

                        }
                        "shipped" -> {
                            binding.cancelOrderBtn.visibility = gone
                            binding.returnOrderBtn.visibility = gone
                            binding.orderRatingContainer.visibility = gone

                            binding.linearLayout10.visibility = View.INVISIBLE

                            val acceptT = acceptedTime!!.toDate()
                            //val packT = packedTime!!.toDate()
                            val shipT = shippedTime!!.toDate()


                            orderNew(orderTime)
                            orderAccepted(acceptT)
                            //orderPacked(packT)
                            orderShipped(shipT)
                        }
                        "delivered" -> {
                            binding.cancelOrderBtn.visibility = gone
                            binding.returnOrderBtn.visibility = visible
                            binding.orderRatingContainer.visibility = visible

                            val acceptT = acceptedTime!!.toDate()
                            //val packT = packedTime!!.toDate()
                            val shipT = shippedTime!!.toDate()
                            val deliverT = deliveredTime!!.toDate()

                            orderNew(orderTime)
                            orderAccepted(acceptT)
                            //orderPacked(packT)
                            orderShipped(shipT)
                            orderDelivered(deliverT)

                            getUsername()
                            getProductRating(productId)
                            isReviewed(productId)
                        }

                        "returned" -> {
                            val returnT = returnedTime!!.toDate()
                            binding.cancelOrderBtn.visibility = gone
                            binding.returnOrderBtn.visibility = gone
                        }
                        else -> {

                            binding.cancelOrderBtn.visibility = gone
                            binding.returnOrderBtn.visibility = gone
                        }
                    }

                    if (isOrderCanceled) {
                        val cancelT = canceledTime!!.toDate()
                        binding.cancelOrderBtn.visibility = gone
                        binding.returnOrderBtn.visibility = gone
                        binding.orderTrackContainer.visibility = gone
                        orderCanceled(cancelT, orderCanceledBy, cancellationReason)
                        binding.statusTxt.text = "Canceled"
                        binding.cancellationContainer.visibility = visible
                    } else {
                        binding.cancellationContainer.visibility = gone
                        binding.statusTxt.text = status
                    }


                    binding.orderIdTxt.text = orderID
                    binding.trakingIdTxt.text = tracKingId
                    binding.orderedDateText.text = daysAgo

                    Glide.with(this@OrderDetailsActivity).load(productThumbnail)
                        .placeholder(R.drawable.as_square_placeholder)
                        .into(productImage)

                    lay1.titleTxt.text = title
                    lay1.priceTxt.text = priceTotal
                    lay1.productQuantity.text = orderedQty.toString()


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

                    lay2.buyerName.text = buyerName
                    lay2.buyerAddress.text = addressBuilder.toString()
                    lay2.buyerAddressType.text = buyerAddressType
                    lay2.buyerTownAndPin.text = townPinBuilder.toString()
                    lay2.buyerState.text = buyerState
                    lay2.buyerPhone.text = buyerPhone

                    loadingDialog.dismiss()

                } else {
                    binding.orderScroll.visibility = gone
                    binding.linearLayout10.visibility = gone
                    loadingDialog.dismiss()
                }
            }
            .addOnFailureListener {
                binding.orderScroll.visibility = gone
                binding.linearLayout10.visibility = gone
                loadingDialog.dismiss()
                Log.e("Get Order details", "faild: ${it.message}")
            }.await()


    }

    private fun orderNew(orderTime: Date) {

        binding.lay3.orderDate.text = getDateTime(orderTime)

        binding.lay3.orderImageButton.backgroundTintList = AppCompatResources
            .getColorStateList(this@OrderDetailsActivity, R.color.amber_600)
        binding.lay3.orderImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)
    }

    private fun orderAccepted(acceptTime: Date) {
        binding.lay3.acceptDate.text = getDateTime(acceptTime)

        binding.lay3.acceptImageButton.backgroundTintList = AppCompatResources
            .getColorStateList(this@OrderDetailsActivity, R.color.successGreen)
        binding.lay3.acceptImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)
    }

    private fun orderShipped(shippedTime: Date) {
        binding.lay3.shippedDate.text = getDateTime(shippedTime)
        binding.lay3.shippedImageButton.backgroundTintList = AppCompatResources
            .getColorStateList(this@OrderDetailsActivity, R.color.blueLink)
        binding.lay3.shippedImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)
    }

    private fun orderDelivered(deliveredTime: Date) {

        binding.lay3.deliveredDate.text = getDateTime(deliveredTime)
        binding.lay3.deliveredImageButton.backgroundTintList = AppCompatResources
            .getColorStateList(this@OrderDetailsActivity, R.color.indigo_900)
        binding.lay3.deliveredImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)
    }

    private fun orderCanceled(deliveredTime: Date, orderCanceledBy: String, reason: String) {

        binding.lay0.cancellationReason.text = reason
        binding.lay0.cancellationTime.text = getDateTime(deliveredTime)
        binding.lay0.cancellationText.text = "Order is canceled by $orderCanceledBy"
    }


    private suspend fun cancelOrder(orderID: String, sellerID: String) {


        val cancelMap: MutableMap<String, Any> = HashMap()
        cancelMap["status"] = "canceled"
        cancelMap["is_order_canceled"] = true
        cancelMap["order_canceled_by"] = "Customer"
        cancelMap["Time_canceled"] = FieldValue.serverTimestamp()

        val orderRef = firebaseFirestore.collection("USERS")
            .document(sellerID)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("ORDERS")
            .document(orderID)
        orderRef.update(cancelMap)
            .addOnSuccessListener {
                sendNotification(productName, imageUrl, "canceled", sellerID, orderID)
                if (onlinePayment){
                    sendRefundRequest()
                }

            }.await()


        val cancelT = Date()
        binding.cancelOrderBtn.visibility = gone
        binding.returnOrderBtn.visibility = gone
        binding.orderTrackContainer.visibility = gone
        binding.statusTxt.text = "Canceled"

        binding.lay0.cancellationTime.text = TimeDateAgo().msToTimeAgo(this, cancelT)
        binding.lay0.cancellationText.text = "Order is canceled by customer"

    }

    private fun sendRefundRequest(){
        val refundMap: MutableMap<String, Any> = HashMap()
        refundMap["Buyer_Id"] = user!!.uid
        refundMap["Time"] = FieldValue.serverTimestamp()
        refundMap["Money_refunded"]=false
        refundMap["Order_id"] = orderID

        firebaseFirestore.collection("REFUND_REQUEST").add(refundMap)
            .addOnSuccessListener {  }
    }

    private fun sendProductReturnRequest(){
        val refundMap: MutableMap<String, Any> = HashMap()
        refundMap["Buyer_Id"] = user!!.uid
        refundMap["Time"] = FieldValue.serverTimestamp()
        refundMap["requested_to_delivery_partner"]=false
        refundMap["Order_id"] = orderID

        firebaseFirestore.collection("PRODUCT_RETURN_REQUEST").add(refundMap)
            .addOnSuccessListener {  }
    }

    private fun returnOrder(orderID: String, sellerID: String) {
        val returnMap: MutableMap<String, Any> = HashMap()
        returnMap["status"] = "returned"
        returnMap["Time_returned"] = FieldValue.serverTimestamp()

        val orderRef = firebaseFirestore.collection("USERS")
            .document(sellerID)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("ORDERS")
            .document(orderID)

        orderRef.update(returnMap).addOnSuccessListener {
            sendProductReturnRequest()
        }
    }

    private fun sendNotification(
        productName: String,
        url: String,
        status: String,
        sellerID: String,
        orderId: String,
    ) {
        val ref = firebaseFirestore.collection("USERS")
            .document(sellerID)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("NOTIFICATION")

        val notificationMap: MutableMap<String, Any> = HashMap()
        notificationMap["date"] = FieldValue.serverTimestamp()
        notificationMap["description"] = "$status: $productName"
        notificationMap["image"] = url
        notificationMap["order_id"] = orderId
        notificationMap["buyer_id"] = user!!.uid
        notificationMap["seen"] = false
//

        ref.add(notificationMap)
            .addOnSuccessListener {

            }.addOnFailureListener {
                Log.e("get buyer notification", "${it.message}")
            }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDateTime(date: Date): String? {
        return try {
            val sdf = SimpleDateFormat("dd MMMM yyyy hh:mm a")
            //val netDate = Date(s.toLong() * 1000)
            sdf.format(date)
        } catch (e: Exception) {
            e.toString()
        }
    }


//TODO---  RATING LAYOUT #################################################################################################
//TODO==================================================================================================================


    private fun isReviewed(productID: String) {
        firebaseFirestore.collection("PRODUCTS")
            .document(productID)
            .collection("PRODUCT_REVIEW")
            .document(user!!.uid)
            .get().addOnSuccessListener {
                if (it.exists()) {
                    ALL_READY_REVIEWED = true
                    val ratings = it.getLong("rating")!!.toLong()
                    val review = it.getString("review").toString()

                    ratingBar.rating = ratings.toFloat()
                    if (review.isNullOrEmpty()) {
                        Log.w("review:", "Empty")
                    } else {
                        reviewInput.editText?.setText(review)
                    }

                } else {
                    ALL_READY_REVIEWED = false
                }
            }.addOnFailureListener {
                ALL_READY_REVIEWED = false
            }
    }


    private fun getProductRating(productID: String) {
        firebaseFirestore.collection("PRODUCTS")
            .document(productID).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    rating5 = it.getLong("rating_Star_5")!!
                    rating4 = it.getLong("rating_Star_4")!!
                    rating3 = it.getLong("rating_Star_3")!!
                    rating2 = it.getLong("rating_Star_2")!!
                    rating1 = it.getLong("rating_Star_1")!!
                    totalRatingsNumber = it.getLong("rating_total")!!
                } else {
                    binding.lay1.viewDetails.isEnabled = false
                    binding.lay1.viewDetails.backgroundTintList =
                        AppCompatResources.getColorStateList(this, R.color.grey_600)
                }


            }
    }


    private fun getUsername() {
        firebaseFirestore.collection("USERS").document(user!!.uid)
            .addSnapshotListener { value, error ->
                error?.let {
                    Log.e("Load user name", "failed: ${it.message}")
                    return@addSnapshotListener
                }
                value?.let {
                    val email = it.getString("email").toString()
                    val name = it.getString("name").toString()
                    buyerName = if (name == "") {
                        email
                    } else {
                        name
                    }
                }
            }
    }


    private fun checkRatingBar(): Boolean {
        val rating = ratingBar.rating.toInt()
        return rating != 0
    }


    private fun checkAllDetails() {

        if (!checkRatingBar()) {
            binding.layRating.linearLayout2.backgroundTintList =
                AppCompatResources.getColorStateList(this, R.color.red_600)
            loadingDialog.dismiss()
        } else {

            binding.layRating.linearLayout2.backgroundTintList =
                AppCompatResources.getColorStateList(this, R.color.white)

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {

                    updateProductReting()
                    createProductRatingDoc()
                }
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                }


            }


        }
    }


    private suspend fun updateProductReting() {
        val productMap: MutableMap<String, Any> = HashMap()

        when (val thisRating = ratingBar.rating.toInt()) {
            5 -> {
                productMap["rating_Star_$thisRating"] = rating5 + 1
            }
            4 -> {
                productMap["rating_Star_$thisRating"] = rating4 + 1
            }
            3 -> {
                productMap["rating_Star_$thisRating"] = rating3 + 1
            }
            2 -> {
                productMap["rating_Star_$thisRating"] = rating2 + 1
            }
            1 -> {
                productMap["rating_Star_$thisRating"] = rating1 + 1
            }

        }
        productMap["rating_total"] = totalRatingsNumber + 1
        productMap["rating_avg"] = calculateRating().toString()
        firebaseFirestore.collection("PRODUCTS").document(productId)
            .update(productMap).await()
    }


    private fun calculateRating(): Float {

        var total = 0L

        when (val thisRating = ratingBar.rating.toInt()) {
            5 -> {
                total = (rating5 + 1) * 5 + rating4 * 4 + rating3 * 3 + rating2 * 2 + rating1 * 1
            }
            4 -> {
                total = rating5 * 5 + (rating4 + 1) * 4 + rating3 * 3 + rating2 * 2 + rating1 * 1
            }
            3 -> {
                total = rating5 * 5 + rating4 * 4 + (rating3 + 1) * 3 + rating2 * 2 + rating1 * 1
            }
            2 -> {
                total = rating5 * 5 + rating4 * 4 + rating3 * 3 + (rating2 + 1) * 2 + rating1 * 1
            }
            1 -> {
                total = rating5 * 5 + rating4 * 4 + rating3 * 3 + rating2 * 2 + (rating1 + 1) * 1
            }
        }

        val avgRating: Float = (total.toFloat()) / (totalRatingsNumber.toFloat() + 1)

        return avgRating

    }


    private fun createProductRatingDoc() {
        val ratingMap: MutableMap<String, Any> = HashMap()
        val review = reviewInput.editText?.text.toString()
        ratingMap["buyer_name"] = buyerName
        ratingMap["rating"] = ratingBar.rating.toLong()
        ratingMap["review_Date"] = FieldValue.serverTimestamp()
        ratingMap["buyer_ID"] = user!!.uid

        if (review.isEmpty()) {
            ratingMap["is_review_available"] = false
            ratingMap["review"] = ""
        } else {
            ratingMap["is_review_available"] = true
            ratingMap["review"] = review
        }


        firebaseFirestore.collection("PRODUCTS")
            .document(productId).collection("PRODUCT_REVIEW")
            .document(user!!.uid)
            .set(ratingMap)
    }


//TODO---  RATING LAYOUT #################################################################################################


}