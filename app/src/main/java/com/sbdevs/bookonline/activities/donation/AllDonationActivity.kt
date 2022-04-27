package com.sbdevs.bookonline.activities.donation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.ads.*
import com.sbdevs.bookonline.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.adapters.donate.AllDonationsAdapter
import com.sbdevs.bookonline.databinding.ActivityAllDonationBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.fragments.register.LoginDialogFragment
import com.sbdevs.bookonline.models.MyDonationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AllDonationActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAllDonationBinding

    private var firebaseFirestore = Firebase.firestore
    private val firebaseDatabase = Firebase.database.reference

    private var donationList: MutableList<MyDonationModel> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private var donationAdapter: AllDonationsAdapter = AllDonationsAdapter(donationList)

    private val gone = View.GONE
    private val visible = View.VISIBLE
    private val loadingDialog = LoadingDialog()

    private var nativeAdLayout: NativeAdLayout? = null
    private var adView: LinearLayout? = null
    private var nativeAd: NativeAd? = null

    private val loginDialog = LoginDialogFragment()


    private val TAG = "AllDonationActivity".javaClass.simpleName





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllDonationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.allDonationRecycler
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = donationAdapter

        val actionBar = binding.toolbar
        setSupportActionBar(actionBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize the Audience Network SDK
        AudienceNetworkAds.initialize(this);
        val adId = resources.getString(R.string.fb_native_ad)
        nativeAd =  NativeAd(this, adId)
        loadingDialog.show(supportFragmentManager,"show")
        lifecycleScope.launch(Dispatchers.IO) {
            loadNativeAd()
            getAllDonation()
            getDonationData()
        }


            binding.donateFabBtn.setOnClickListener {
                val currentUser= Firebase.auth.currentUser
                if (currentUser != null) {
                val donateIntent = Intent(this, AddContributionActivity::class.java)
                startActivity(donateIntent)
                }else{
                    loginDialog.show(supportFragmentManager, "custom login dialog")
                }
            }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadNativeAd() {


        val nativeAdListener =object : NativeAdListener {
            override fun onError(p0: Ad?, p1: AdError?) {
                Log.e("Ads load error","$p1 \n message: ${p1!!.errorMessage}")
                loadingDialog.dismiss()
            }

            override fun onAdLoaded(p0: Ad?) {
                // Race condition, load() called again before last ad was displayed
                if (nativeAd == null || nativeAd != p0) {
                    return;
                }
                // Inflate Native Ad into Container
                inflateAd(nativeAd!!);
            }

            override fun onAdClicked(p0: Ad?) {
                Log.d("Ads","Clicked")
            }

            override fun onLoggingImpression(p0: Ad?) {
                Log.e("Ads","Log Impression")
                loadingDialog.dismiss()
            }

            override fun onMediaDownloaded(p0: Ad?) {
                Log.d("Ads","MediaDownload")
            }


        }

        // Request an ad
        nativeAd!!.loadAd(
            nativeAd!!.buildLoadAdConfig()
                .withAdListener(nativeAdListener)
                .build());
    }

    private fun inflateAd(nativeAd: NativeAd) {
        nativeAd.unregisterView()

        // Add the Ad view into the ad container.
        nativeAdLayout = binding.nativeAdContainer
        val inflater = LayoutInflater.from(this)
        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        adView = inflater.inflate(R.layout.ad_native_f_1, nativeAdLayout, false) as LinearLayout

        nativeAdLayout!!.addView(adView)

        // Add the AdOptionsView
        val adChoicesContainer = adView!!.findViewById<LinearLayout>(R.id.ad_choices_container)
        val adOptionsView = AdOptionsView(this, nativeAd, nativeAdLayout)
        adChoicesContainer.removeAllViews()
        adChoicesContainer.addView(adOptionsView, 0)

        // Create native UI using the ad metadata.

        // Create native UI using the ad metadata.
        val nativeAdIcon: MediaView = adView!!.findViewById(R.id.native_ad_icon)
        val nativeAdTitle = adView!!.findViewById<TextView>(R.id.native_ad_title)
        val nativeAdMedia: MediaView = adView!!.findViewById(R.id.native_ad_media)
        val nativeAdSocialContext = adView!!.findViewById<TextView>(R.id.native_ad_social_context)
        val nativeAdBody = adView!!.findViewById<TextView>(R.id.native_ad_body)
//        val sponsoredLabel = adView!!.findViewById<TextView>(R.id.native_ad_sponsored_label)
        val nativeAdCallToAction = adView!!.findViewById<Button>(R.id.native_ad_call_to_action)

        // Set the Text.
        nativeAdTitle.text = nativeAd.advertiserName
        nativeAdBody.text = nativeAd.adBodyText
        nativeAdSocialContext.text = nativeAd.adSocialContext
        nativeAdCallToAction.visibility = if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction.text = nativeAd.adCallToAction
//        sponsoredLabel.text = nativeAd.sponsoredTranslation

        // Create a list of clickable views
        val clickableViews: MutableList<View> = ArrayList()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(adView, nativeAdMedia, nativeAdIcon,clickableViews)
    }



    private suspend fun getAllDonation(){
        firebaseFirestore.collection("DONATIONS")
            .whereEqualTo("is_received",true)
            .orderBy("Time_donate_received",Query.Direction.DESCENDING)
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
//                loadingDialog.dismiss()
            }
            .addOnFailureListener {
                Log.e("get all donation", "${it.message}")
//                loadingDialog.dismiss()
            }.await()
    }

    private fun getDonationData(){
        firebaseDatabase.child("Donation").get()
            .addOnSuccessListener {
                val benefitedPeople:Long = it.child("benefited_people").value as Long
                val donatedItems:Long = it.child("donated_items").value as Long
                val donations:Long = it.child("donations").value as Long

                binding.textView980.text = donatedItems.toString()
                binding.textView90.text = benefitedPeople.toString()

                binding.textView90.visibility = visible
                binding.textView980.visibility = visible
                binding.progressBar6.visibility = gone
                binding.progressBar7.visibility = gone

            }.addOnFailureListener {
                Log.e("Get Strip Normal error :","${it.message}")
                binding.textView90.visibility = gone
                binding.textView980.visibility = gone
            }

    }



}