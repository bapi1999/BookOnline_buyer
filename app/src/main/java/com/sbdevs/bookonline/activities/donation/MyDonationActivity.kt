package com.sbdevs.bookonline.activities.donation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.donate.MyDonationAdapter
import com.sbdevs.bookonline.databinding.ActivityMyDonationBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.models.MyDonationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MyDonationActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMyDonationBinding

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private var donationList: MutableList<MyDonationModel> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private var donationAdapter: MyDonationAdapter = MyDonationAdapter(donationList)

    private var lastResult: DocumentSnapshot? =null
    private lateinit var times: Timestamp
    private var isReachLast:Boolean = false

    private val gone = View.GONE
    private val visible =View.VISIBLE

    private var status = false
    private var orderByString = "Time_donate_request"

    private lateinit var donorBadge:ImageView
    private lateinit var donorLevel:TextView
    private lateinit var minPoint:TextView
    private lateinit var currentPoint:TextView
    private lateinit var maxPoint:TextView
    private lateinit var levelProgress:ProgressBar


    private val loadingDialog = LoadingDialog()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyDonationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.myDonationRecycler
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = donationAdapter

        donorBadge = binding.donorBadge
        donorLevel = binding.donorLevel
        minPoint = binding.minPoint
        currentPoint = binding.currentPoint
        maxPoint = binding.maxPoint
        levelProgress = binding.levelProgress



        loadingDialog.show(supportFragmentManager,"show")

        getUsername()
        lifecycleScope.launch(Dispatchers.IO) {
            getRequestedDonations(status,orderByString)

        }

    }


    override fun onStart() {
        super.onStart()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    // end scrolling: do what you want here and after calling the function change the value of boolean

                    if (isReachLast){
                        Log.w("Query item","Last item is reached already")
                        binding.progressBar.visibility = View.GONE
                        //Log.e("last query", "${lastResult.toString()}")
                    }else{
                        binding.progressBar.visibility = View.VISIBLE
                        //Log.e("last query", "${lastResult.toString()}")

                    }
                }
            }
        })

        binding.orderTypeRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){

                R.id.radioButton1->{
                    status = false
                    orderByString = "Time_donate_request"
                    changeOrderMethod(status,orderByString)
                }
                R.id.radioButton2->{
                    status = true
                    orderByString = "Time_donate_received"
                    changeOrderMethod(status,orderByString)
                }

            }
        }

    }



    private fun changeOrderMethod(status:Boolean, orderTimeType:String){

        donationList.clear()
        donationAdapter.notifyDataSetChanged()
        lastResult = null
        isReachLast = false

        loadingDialog.show(supportFragmentManager,"Show")
        lifecycleScope.launch(Dispatchers.IO) {
            getRequestedDonations(status,orderTimeType)
        }
    }


    private suspend fun getRequestedDonations(receivedStatus:Boolean, orderTimeType:String){
        val query:Query = if (lastResult == null){
            firebaseFirestore.collection("DONATIONS")
                .whereEqualTo("Donor_Id",user!!.uid)
                .whereEqualTo("is_received",receivedStatus)
                .orderBy(orderTimeType)
        }else{
            firebaseFirestore.collection("DONATIONS")
                .whereEqualTo("Donor_Id",user!!.uid)
                .whereEqualTo("is_received",receivedStatus)
                .orderBy(orderTimeType)
                .startAfter(times)
        }

        query.limit(10L).get()
            .addOnSuccessListener {
                val allDocumentSnapshot = it.documents

                isReachLast = if (allDocumentSnapshot.isNotEmpty()){
                    allDocumentSnapshot.size < 10 // limit is 10
                }else{
                    true
                }
                val list:MutableList<MyDonationModel> = it.toObjects(MyDonationModel::class.java)
                donationList.addAll(list)

                if (donationList.isEmpty()){
                    recyclerView.visibility = gone
                    binding.progressBar.visibility = gone
                    binding.textView94.visibility = visible
                }
                else{
                    recyclerView.visibility = visible
//                    binding.progressBar.visibility = visible
                    binding.textView94.visibility = gone

                    donationAdapter.list = donationList

                    if (lastResult == null ){
                        donationAdapter.notifyItemRangeInserted(0,list.size)
                    }else{
                        donationAdapter.notifyItemRangeInserted(donationList.size-1,list.size)
                    }


//Todo- new approach =================================================================
                    if (allDocumentSnapshot.isNotEmpty()){
                        val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                        lastResult = lastR
                        times = lastR.getTimestamp(orderTimeType)!!
                    }
//Todo- new approach =================================================================

                    binding.progressBar.visibility = gone
                }

                loadingDialog.dismiss()
            }
            .addOnFailureListener {
                Log.e("get my donation", "${it.message}")
                loadingDialog.dismiss()
            }.await()
    }

//total_donation_qty

    private fun getUsername() {
        if (user != null){
            firebaseFirestore.collection("USERS").document(user!!.uid)
                .get().addOnSuccessListener {
                    val totalQty:Long = it.getLong("total_donation_qty")!!.toLong()
                    val totalPoint = it.getLong("total_donation_point")!!.toLong()
                    when {
                        totalQty <10 -> {
                            //donor badge image is created
                            donorLevel.text = "Level 0"
                            minPoint.text = "0"
                            maxPoint.text = "10"
                            currentPoint.text = "${totalQty}/"
                            levelProgress.max = 10
                            levelProgress.progress = totalQty.toInt()

                        }
                        totalQty in 10..49 -> {
                            //donor badge image is created
                            donorLevel.text = "Level 1"
                            minPoint.text = "10"
                            maxPoint.text = "50"
                            currentPoint.text = "${totalQty}/"
                            levelProgress.max = 50
                            levelProgress.progress = totalQty.toInt()
                        }
                        totalQty in 50..199 -> {
                            //donor badge image is created
                            donorLevel.text = "Level 2"
                            minPoint.text = "50"
                            maxPoint.text = "200"
                            currentPoint.text = "${totalQty}/"
                            levelProgress.max = 200
                            levelProgress.progress = totalQty.toInt()
                        }
                        totalQty in 200..499 -> {
                            donorLevel.text = "Level 3"
                            minPoint.text = "200"
                            maxPoint.text = "500"
                            currentPoint.text = "${totalQty}/"
                            levelProgress.max = 500
                            levelProgress.progress = totalQty.toInt()
                        }
                        totalQty in 500..1499 -> {
                            donorLevel.text = "Level 4"
                            minPoint.text = "500"
                            maxPoint.text = "1500"
                            currentPoint.text = "${totalQty}/"
                            levelProgress.max = 1500
                            levelProgress.progress = totalQty.toInt()
                        }
                        totalQty in 1500..4999 -> {
                            donorLevel.text = "Level 5"
                            minPoint.text = "1500"
                            maxPoint.text = "5000"
                            currentPoint.text = "${totalQty}/"
                            levelProgress.max = 5000
                            levelProgress.progress = totalQty.toInt()
                        }
                        totalQty in 5000..9999 -> {
                            donorLevel.text = "Level 6"
                            minPoint.text = "5000"
                            maxPoint.text = "10000"
                            currentPoint.text = "${totalQty}/"
                            levelProgress.max = 10000
                            levelProgress.progress = totalQty.toInt()
                        }
                        totalQty >10000 -> {
                            donorLevel.text = "Level 7"
                            minPoint.text = "10000"
                            maxPoint.text = "100000"
                            currentPoint.text = "${totalQty}/"
                            levelProgress.max = 100000
                            levelProgress.progress = totalQty.toInt()
                        }

                    }

                }
        }else{
            //textView.text = getString(R.string.you_aren_t_logged_in)
        }

    }

}