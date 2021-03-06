package com.sbdevs.bookonline.activities.donation

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.ads.*
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
import java.util.*


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

    private var orderByString = "Time_donate_request"

    private lateinit var donorBadge:ImageView
    private lateinit var donorLevel:TextView
    private lateinit var minPoint:TextView
    private lateinit var currentPoint:TextView
    private lateinit var maxPoint:TextView
    private lateinit var levelProgress:ProgressBar


    private val loadingDialog = LoadingDialog()

    private var nativeAdLayout: NativeAdLayout? = null
    private var adView: LinearLayout? = null
    private var nativeBannerAd: NativeBannerAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyDonationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        AudienceNetworkAds.initialize(this);
        val adId = resources.getString(R.string.fb_native_ad)
        nativeBannerAd =  NativeBannerAd(this, adId)

        recyclerView = binding.myDonationRecycler
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = donationAdapter

        donorBadge = binding.donorBadge
        donorLevel = binding.donorLevel
        minPoint = binding.minPoint
        currentPoint = binding.currentPoint
        maxPoint = binding.maxPoint
        levelProgress = binding.levelProgress


        val actionBar = binding.toolbar
        setSupportActionBar(actionBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        loadingDialog.show(supportFragmentManager,"show")

        getUsername()
        loadNativeAd()

        lifecycleScope.launch(Dispatchers.IO) {
            getRequestedDonations(orderByString)

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
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


    }


    private fun loadNativeAd() {


        val nativeAdListener =object : NativeAdListener {
            override fun onError(p0: Ad?, p1: AdError?) {
                Log.e("Ads load error","$p1 \n message: ${p1!!.errorMessage}")
                loadingDialog.dismiss()
            }

            override fun onAdLoaded(p0: Ad?) {
                // Race condition, load() called again before last ad was displayed
                if (nativeBannerAd == null || nativeBannerAd != p0) {
                    return;
                }
                // Inflate Native Ad into Container
                inflateAd(nativeBannerAd!!);
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
        nativeBannerAd!!.loadAd(
            nativeBannerAd!!.buildLoadAdConfig()
                .withAdListener(nativeAdListener)
                .build());
    }

    private fun inflateAd(nativeAd: NativeBannerAd) {
        nativeAd.unregisterView()

        // Add the Ad view into the ad container.
        nativeAdLayout = binding.nativeAdContainer
        val inflater = LayoutInflater.from(this)
        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        adView = inflater.inflate(R.layout.ad_native_f_2_small, nativeAdLayout, false) as LinearLayout

        nativeAdLayout!!.addView(adView)

        // Add the AdOptionsView
        val adChoicesContainer = adView!!.findViewById<RelativeLayout>(R.id.ad_choices_container)
        val adOptionsView = AdOptionsView(this, nativeAd, nativeAdLayout)
        adChoicesContainer.removeAllViews()
        adChoicesContainer.addView(adOptionsView, 0)

        // Create native UI using the ad metadata.

        // Create native UI using the ad metadata.
        val nativeAdIcon: MediaView = adView!!.findViewById(R.id.native_icon_view)
        val nativeAdTitle = adView!!.findViewById<TextView>(R.id.native_ad_title)
        val nativeAdSocialContext = adView!!.findViewById<TextView>(R.id.native_ad_social_context)
        val sponsoredLabel = adView!!.findViewById<TextView>(R.id.native_ad_sponsored_label)
        val nativeAdCallToAction = adView!!.findViewById<Button>(R.id.native_ad_call_to_action)

        // Set the Text.
        nativeAdTitle.text = nativeAd.advertiserName
        nativeAdSocialContext.text = nativeAd.adSocialContext
        nativeAdCallToAction.visibility = if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction.text = nativeAd.adCallToAction
        sponsoredLabel.text = nativeAd.sponsoredTranslation

        // Create a list of clickable views
        val clickableViews: MutableList<View> = ArrayList()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(adView, nativeAdIcon,clickableViews)
    }





    private suspend fun getRequestedDonations(orderTimeType:String){
        val newList:MutableList<MyDonationModel> = ArrayList()

        val query:Query = if (lastResult == null){
            firebaseFirestore.collection("DONATIONS")
                .whereEqualTo("Donor_Id",user!!.uid)
                .orderBy(orderTimeType,Query.Direction.DESCENDING)
        }else{
            firebaseFirestore.collection("DONATIONS")
                .whereEqualTo("Donor_Id",user!!.uid)
                .orderBy(orderTimeType,Query.Direction.DESCENDING)
                .startAfter(times)
        }

        query.limit(10L).get()
            .addOnSuccessListener {
                val allDocumentSnapshot = it.documents

                 if (allDocumentSnapshot.isNotEmpty()){
                     isReachLast =allDocumentSnapshot.size < 10 // limit is 10

                     for (item in allDocumentSnapshot){
                         val timeRequest:Date = item.getTimestamp("Time_donate_request")!!.toDate()
                         val donorId:String = item.getString("Donor_Id")!!
                         val totalQty:Long = item.getLong("total_qty")!!
                         val totalPoint:Long = item.getLong("total_point")!!
                         val isReceived:Boolean = item.getBoolean("is_received")!!
                         val itemList:MutableList<MutableMap<String, Any>> = item.get("item_List") as MutableList<MutableMap<String, Any>>
                         newList.add(MyDonationModel(timeRequest,donorId,totalQty,totalPoint,isReceived,itemList))
                     }

                     donationList.addAll(newList)

                     if (donationList.isEmpty()){
                         recyclerView.visibility = gone
                         binding.progressBar.visibility = gone
                         binding.textView94.visibility = visible
                     }
                     else{
                         recyclerView.visibility = visible
                         binding.textView94.visibility = gone

                         donationAdapter.list = donationList

                         if (lastResult == null ){
                             donationAdapter.notifyItemRangeInserted(0,newList.size)
                         }else{
                             donationAdapter.notifyItemRangeInserted(donationList.size-1,newList.size)
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
                }else{
                     isReachLast = true
                }


//                loadingDialog.dismiss()
            }
            .addOnFailureListener {
                Log.e("get my donation", "${it.message}")
//                loadingDialog.dismiss()
            }.await()
    }

//total_donation_qty

    private fun getUsername() {
        if (user != null){
            firebaseFirestore.collection("USERS").document(user!!.uid)
                .get().addOnSuccessListener {
                    val totalQty:Long = it.getLong("total_donation_qty")!!.toLong()
                    val totalPoint = it.getLong("total_donation_point")!!.toLong()
                    val usablePoint = it.getLong("my_donation_coins")!!.toLong()
                    binding.totalPoint.text = totalPoint.toString()
                    binding.totalItem.text = "$totalQty items"
                    binding.usablePoint.text = "$usablePoint"


                    when {
                        totalPoint <100 -> {
                            //donor badge image is created
                            donorLevel.text = "Level 0"
                            minPoint.text = "0"
                            maxPoint.text = "100"
                            currentPoint.text = "${totalPoint}/"
                            levelProgress.max = 100
                            levelProgress.progress = totalPoint.toInt()
                            donorBadge.setImageResource(R.drawable.ic_slide1)
                            donorBadge.imageTintList = AppCompatResources.getColorStateList(this,R.color.grey_400)

                        }
                        totalPoint in 100..499 -> {
                            //donor badge image is created
                            donorLevel.text = "Level 1"
                            minPoint.text = "100"
                            maxPoint.text = "500"
                            currentPoint.text = "${totalPoint}/"
                            levelProgress.max = 500
                            levelProgress.progress = totalPoint.toInt()
                            donorBadge.setImageResource(R.drawable.ic_slide1)

                        }
                        totalPoint in 500..1999 -> {
                            //donor badge image is created
                            donorLevel.text = "Level 2"
                            minPoint.text = "500"
                            maxPoint.text = "2000"
                            currentPoint.text = "${totalPoint}/"
                            levelProgress.max = 2000
                            levelProgress.progress = totalPoint.toInt()
                            donorBadge.setImageResource(R.drawable.ic_slide2)

                        }
                        totalPoint in 2000..4999 -> {
                            donorLevel.text = "Level 3"
                            minPoint.text = "2000"
                            maxPoint.text = "5000"
                            currentPoint.text = "${totalPoint}/"
                            levelProgress.max = 5000
                            levelProgress.progress = totalPoint.toInt()
                            donorBadge.setImageResource(R.drawable.ic_slide3)

                        }
                        totalPoint in 5000..14999 -> {
                            donorLevel.text = "Level 4"
                            minPoint.text = "5000"
                            maxPoint.text = "15000"
                            currentPoint.text = "${totalPoint}/"
                            levelProgress.max = 15000
                            levelProgress.progress = totalPoint.toInt()
                            donorBadge.setImageResource(R.drawable.ic_slide4)

                        }
                        totalPoint in 15000..49990 -> {
                            donorLevel.text = "Level 5"
                            minPoint.text = "15000"
                            maxPoint.text = "50000"
                            currentPoint.text = "${totalPoint}/"
                            levelProgress.max = 50000
                            levelProgress.progress = totalPoint.toInt()
                            donorBadge.setImageResource(R.drawable.ic_slide5)

                        }
                        totalPoint in 50000..99999 -> {
                            donorLevel.text = "Level 6"
                            minPoint.text = "50000"
                            maxPoint.text = "100000"
                            currentPoint.text = "${totalPoint}/"
                            levelProgress.max = 100000
                            levelProgress.progress = totalPoint.toInt()
                            donorBadge.setImageResource(R.drawable.ic_slide6)

                        }
                        totalPoint >100000 -> {
                            donorLevel.text = "Level 7"
                            minPoint.text = "100000"
                            maxPoint.text = "1000000"
                            currentPoint.text = "${totalPoint}/"
                            levelProgress.max = 1000000
                            levelProgress.progress = totalPoint.toInt()
                        }

                    }

                }
        }else{
            //textView.text = getString(R.string.you_aren_t_logged_in)
        }

    }

}