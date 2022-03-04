package com.sbdevs.bookonline.activities.donation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.donate.AllDonationsAdapter
import com.sbdevs.bookonline.adapters.donate.MyDonationAdapter
import com.sbdevs.bookonline.databinding.ActivityAllDonationBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.models.MyDonationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AllDonationActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAllDonationBinding

    private var firebaseFirestore = Firebase.firestore
    private var user = Firebase.auth.currentUser

    private var donationList: MutableList<MyDonationModel> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private var donationAdapter: AllDonationsAdapter = AllDonationsAdapter(donationList)

    private val gone = View.GONE
    private val visible = View.VISIBLE

    private val loadingDialog = LoadingDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllDonationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.allDonationRecycler
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = donationAdapter


        loadingDialog.show(supportFragmentManager,"show")

        lifecycleScope.launch(Dispatchers.IO) {
            getAllDonation()
        }

        binding.donateButton.setOnClickListener {
            val donateIntent = Intent(this,AddContributionActivity::class.java)
            startActivity(donateIntent)
        }

    }
    private suspend fun getAllDonation(){
        firebaseFirestore.collection("DONATIONS")
            .whereEqualTo("Donor_Id",user!!.uid)
            .whereEqualTo("is_received",true)
            .orderBy("Time_donate_received")
            .limit(10L).get()
            .addOnSuccessListener {

                donationList = it.toObjects(MyDonationModel::class.java)


                if (donationList.isEmpty()){
                    recyclerView.visibility = gone
                }
                else{
                    recyclerView.visibility = visible
                    donationAdapter.list = donationList
                    donationAdapter.notifyDataSetChanged()

                }
                loadingDialog.dismiss()
            }
            .addOnFailureListener {
                Log.e("get all donation", "${it.message}")
                loadingDialog.dismiss()
            }.await()
    }
}