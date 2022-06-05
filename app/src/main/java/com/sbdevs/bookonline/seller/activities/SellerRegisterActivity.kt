package com.sbdevs.bookonline.seller.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.ProductZoomImageAdapter
import com.sbdevs.bookonline.databinding.ActivitySellerRegisterBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.othercalss.SharedDataClass
import com.sbdevs.bookonline.seller.adapters.SellerIntroAdapter
import com.sbdevs.bookonline.seller.models.IntroModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SellerRegisterActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySellerRegisterBinding

    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth
    private val loadingDialog = LoadingDialog()

    private lateinit var intropager:ViewPager2
    val list:ArrayList<IntroModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intropager = binding.introPager


        list.add(IntroModel(R.drawable.seller_intro_1,"Sell your used, refurbished, or new books here without any complexity"))
        list.add(IntroModel(R.drawable.seller_intro_2,"Receive orders from every part of India and follow the simple steps to fulfill the orders"))
        list.add(IntroModel(R.drawable.seller_intro_3,"No need for a GST number to sell"))

        val adapter = SellerIntroAdapter(list)
        intropager.adapter = adapter
        binding.dotsIndicator.setViewPager2(intropager)

        binding.sellerRegisterBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO){
                loadingDialog.show(supportFragmentManager,"show")
                retrieveUserToken()
                createPaths()
            }
        }
    }



    private suspend  fun createPaths(){


        val timstamp1 = FieldValue.serverTimestamp()
        val userMap: MutableMap<String, Any> = HashMap()
        userMap["Is_seller"] = true
        userMap["seller_register_date"] = timstamp1
        userMap["TotalSeals"] = 0L
        userMap["TotalProfit"] = 0L
        userMap["OrdersDelivered"] = 0L
        userMap["OrdersCanceled"] = 0L
        userMap["new_notification_seller"] = timstamp1
        userMap["LastDeliveredOrderTime"] = timstamp1
        userMap["LastCanceledOrderTime"] = timstamp1
        userMap["LastProductAddedTime"] = timstamp1
        userMap["LastTimeSealsChecked"] = timstamp1

        val earningMap: MutableMap<String, Any> = HashMap()
        earningMap["current_amount"] = 0L


        val businessDetailsMap: MutableMap<String, Any> = HashMap()
        businessDetailsMap["Business_name"] = ""
        businessDetailsMap["Business_type"] = ""
        businessDetailsMap["Business_profile"] = ""
        businessDetailsMap["about_business"] = ""
        businessDetailsMap["Is_BusinessDetail_Added"] = false
        businessDetailsMap["is_address_verified"] = false
        businessDetailsMap["hide_address_contact"] = true

        val bankDetailsMap: MutableMap<String, Any> = HashMap()
        bankDetailsMap["UPI_id"] =""
        bankDetailsMap["Is_BankDetail_Added"] = false

        val welcomeNoti: String = getString(R.string.welcome_notification_for_seller)
        val notificationMap: MutableMap<String, Any> = HashMap()
        notificationMap["date"] = FieldValue.serverTimestamp()
        notificationMap["description"] = welcomeNoti
        notificationMap["image"] = ""
        notificationMap["NOTIFICATION_CODE"]=0L
        notificationMap["order_id"] = ""
        notificationMap["seen"] = false

        val currentUser = firebaseAuth.currentUser!!.uid

        val docRef = firebaseFirestore.collection("USERS").document(currentUser)
        docRef.set(userMap, SetOptions.merge()).await()

        docRef.collection("SELLER_NOTIFICATIONS").add(notificationMap).await()

        val sellerRef = docRef.collection("SELLER_DATA")
        sellerRef.document("BANK_DETAILS").set(bankDetailsMap).await()
        sellerRef.document("BUSINESS_DETAILS").set(businessDetailsMap).await()
        sellerRef.document("MY_EARNING").set(earningMap).await()

        withContext(Dispatchers.Main){
            Toast.makeText(this@SellerRegisterActivity, "Successfully Registered", Toast.LENGTH_SHORT).show()
            SharedDataClass.isSeller = true
            val newIntent = Intent(this@SellerRegisterActivity,SellerMainActivity::class.java)
            startActivity(newIntent)
            finish()
            loadingDialog.dismiss()
        }


    }


    private fun retrieveUserToken(){

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token:String = task.result
                val userId:String = FirebaseAuth.getInstance().currentUser!!.uid

                FirebaseDatabase.getInstance().getReference("Tokens")
                    .child(userId)
                    .setValue(token).addOnSuccessListener {
                        Log.d("Token:", "saved")
                    }.addOnFailureListener {
                        Log.e("Token:", "${it.message}")
                    }

            }else{
                Log.e("error","${task.exception?.message}")
            }

        }

    }


}