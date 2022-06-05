package com.sbdevs.bookonline.seller.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivitySellerMainBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.models.NotificationModel
import com.sbdevs.bookonline.othercalss.SharedDataClass
import com.sbdevs.bookonline.seller.SellerSharedData
import com.sbdevs.bookonline.seller.fragment.SlOrderFragment
import com.sbdevs.bookonline.seller.models.SlNotificationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class SellerMainActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySellerMainBinding

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private lateinit var notificationBadgeText: TextView
    private lateinit var timeStamp: Timestamp
    private var notificationList:List<SlNotificationModel> = ArrayList()
    private val loadingDialog = LoadingDialog()

    private val gone = View.GONE
    private val visible = View.VISIBLE
    private val inVisible = View.INVISIBLE

    private lateinit var addBusinessDetailsBtn: Button
    private lateinit var warningMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar = binding.toolbar
        setSupportActionBar(actionBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        notificationBadgeText  = binding.layNotify.notificationBadgeCounter

        addBusinessDetailsBtn= binding.noBusinessLay.button2
        warningMessage=binding.noBusinessLay.textView3
        addBusinessDetailsBtn.setOnClickListener {
            val newIntent = Intent(this,SlBusinessDetailsActivity::class.java)
            startActivity(newIntent)
        }

        lifecycleScope.launch(Dispatchers.Main) {
            isUserVerified()
            getTimeStamp()
        }

        supportFragmentManager.beginTransaction().replace(R.id.main_frame_layout,SlOrderFragment()).commit()
    }


    override fun onStart() {
        super.onStart()

        binding.layNotify.notificationBadgeContainerLay.setOnClickListener {

            updateNotificationForOptionMenu()
            notificationBadgeText.visibility = View.GONE

            val notificationIntent = Intent(this,SellerNotificationActivity::class.java)
            startActivity(notificationIntent)

        }



    }



    override fun onResume() {
        super.onResume()
        if (SellerSharedData.isBusinessDetailAdded){
            if ( SellerSharedData.isAddressVerified){
                binding.noBusinessContainer.visibility = gone
                binding.mainFrameLayout.visibility = visible
            }else{
                binding.noBusinessContainer.visibility = visible
                binding.mainFrameLayout.visibility = inVisible
                addBusinessDetailsBtn.visibility = gone
                val st = getString(R.string.seller_address_not_verified)
                val stBuilder: StringBuilder = StringBuilder()
                val st2 = "You are not a verified seller yet."
                stBuilder.append(st2).append(" ").append(st)
                warningMessage.text = stBuilder.toString()
            }

        }else{
            SellerSharedData.isBusinessDetailAdded = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getNotificationForOptionMenu(timeStamp1:Timestamp,textView: TextView) {

        val ref = firebaseFirestore.collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_NOTIFICATIONS")
            .whereGreaterThan("date",timeStamp1)

        ref.addSnapshotListener { value, error ->
            error?.let {
                Log.e("Notification","can not load notification",it.cause)
                textView.visibility = View.GONE
            }

            value?.let {

                notificationList = it.toObjects(SlNotificationModel::class.java)

                if (notificationList.isEmpty()){
                    textView.visibility = View.GONE
                }else{
                    textView.visibility = View.VISIBLE
                    textView.text = notificationList.size.toString()
                }
            }


        }

    }

    private fun updateNotificationForOptionMenu() {
        Log.e("click","auto clicked")
        if (user!= null){
            val ref = firebaseFirestore.collection("USERS").document(user.uid)

            val newDate =Date()

            val fixedTimestamp:Timestamp = Timestamp(newDate)

            val notiMAp: MutableMap<String, Any> = HashMap()
            notiMAp["new_notification_seller"] = fixedTimestamp

            ref.update(notiMAp).addOnSuccessListener {
                timeStamp = fixedTimestamp
            }
        }


    }

    private suspend fun getTimeStamp(){
        firebaseFirestore.collection("USERS").document(user!!.uid)
            .get().addOnSuccessListener {
                timeStamp = it.getTimestamp("new_notification_seller")!! as Timestamp

                getNotificationForOptionMenu(timeStamp,notificationBadgeText)

            }.addOnFailureListener {
                Log.e("get Notification time","${it.message}")
            }.await()
    }


    private fun isUserVerified(){
        firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")
            .document("BUSINESS_DETAILS").get().addOnSuccessListener {
                val isBusinessAdded = it.getBoolean("Is_BusinessDetail_Added")!!
                val isVerified = it.getBoolean("is_address_verified")!!


                val st2 = "You are not a verified seller yet."
                val stBuilder: StringBuilder = StringBuilder()
                if (isBusinessAdded){
                    SellerSharedData.isBusinessDetailAdded = true

                    if (isVerified){
                        binding.noBusinessContainer.visibility = gone
                        binding.mainFrameLayout.visibility = visible
                        SellerSharedData.isAddressVerified = true
                    }else{
                        binding.noBusinessContainer.visibility = visible
                        binding.mainFrameLayout.visibility = inVisible
                        addBusinessDetailsBtn.visibility = gone
                        val st = getString(R.string.seller_address_not_verified)

                        stBuilder.append(st2).append(" ").append(st)
                        warningMessage.text = stBuilder.toString()
                        SellerSharedData.isAddressVerified = false
                    }

                    SellerSharedData.isSellerVerified = true

                }else{
                    SellerSharedData.isBusinessDetailAdded = false

                    binding.noBusinessContainer.visibility = visible
                    binding.mainFrameLayout.visibility = inVisible
                    val st = getString(R.string.you_are_not_a_verified_seller)
                    warningMessage.text = st
                    addBusinessDetailsBtn.visibility = visible
                    SellerSharedData.isSellerVerified = false

                }


            }

    }



}