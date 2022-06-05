package com.sbdevs.bookonline.seller.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.adapters.NotificationAdapter
import com.sbdevs.bookonline.databinding.FragmentNotificationBinding
import com.sbdevs.bookonline.models.NotificationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivitySellerNotificationBinding
import com.sbdevs.bookonline.fragments.LoadingDialog

class SellerNotificationActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySellerNotificationBinding

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    lateinit var notificationAdapter: NotificationAdapter
    private var notificationList: MutableList<NotificationModel> = ArrayList()

    var notificationDocIdList: ArrayList<String> = ArrayList()

    private val loadingDialog = LoadingDialog()
    private lateinit var notificationRecycler: RecyclerView

    private var lastResult: DocumentSnapshot? = null
    private lateinit var times: Timestamp
    private var isReachLast: Boolean = false
    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar = binding.toolbar
        setSupportActionBar(actionBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadingDialog.show(supportFragmentManager, "Show")

        if (user != null) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    getNotificationFormDB()
                }
                withContext(Dispatchers.Main) {
                    delay(1000)
                    loadingDialog.dismiss()
                }


            }
        } else {
            binding.emptyContainer.visibility = View.VISIBLE
            binding.notificationRecycler.visibility = View.GONE
            loadingDialog.dismiss()
        }




        notificationRecycler = binding.notificationRecycler
        notificationRecycler.layoutManager = LinearLayoutManager(this)

        notificationAdapter = NotificationAdapter(notificationList)
        notificationRecycler.adapter = notificationAdapter


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }



    override fun onStart() {
        super.onStart()

        notificationRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_RIGHT) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (isReachLast) {
                        Log.e("Query item", "Last item is reached already")

                    } else {

                        Log.e("last query", "${lastResult.toString()}")
                        binding.progressBar.visibility = View.VISIBLE
                        getNotificationFormDB()
                    }

                }
            }



        })
    }


    private fun getNotificationFormDB() {

        val newNotiList: MutableList<NotificationModel> = ArrayList()
        newNotiList.clear()

        val query = if (lastResult == null) {
            firebaseFirestore.collection("USERS").document(user!!.uid)
                .collection("SELLER_NOTIFICATIONS")
                .orderBy("date", Query.Direction.DESCENDING)
        } else {
            firebaseFirestore.collection("USERS").document(user!!.uid)
                .collection("SELLER_NOTIFICATIONS")
                .orderBy("date", Query.Direction.DESCENDING)
                .startAfter(times)
        }


        query.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents


            if (allDocumentSnapshot.isNotEmpty()) {


                for (item in allDocumentSnapshot) {

                    val notificationId = item.id
                    val date = item.getTimestamp("date")!!.toDate()
                    val description: String = item.getString("description").toString()
                    val image: String = item.getString("image").toString()
                    val order_id: String = item.getString("order_id")!!
                    val seen: Boolean = item.getBoolean("seen")!!

                    newNotiList.add(
                        NotificationModel(
                            notificationId,
                            date,
                            description,
                            image,
                            order_id,
                            seen
                        )
                    )


                }
                isReachLast = allDocumentSnapshot.size < 10 // limit is 10


            } else {
                isReachLast = true
            }





            notificationList.addAll(newNotiList)

            if (notificationList.isEmpty()) {
                binding.emptyContainer.visibility = View.VISIBLE
                binding.notificationRecycler.visibility = View.GONE
            } else {
                binding.emptyContainer.visibility = View.GONE
                binding.notificationRecycler.visibility = View.VISIBLE

                notificationAdapter.list = notificationList


                if (lastResult == null) {
                    notificationAdapter.notifyItemRangeInserted(0, newNotiList.size)
                } else {
                    notificationAdapter.notifyItemRangeInserted(notificationList.size - 1, newNotiList.size)
                }

//Todo- new approach =================================================================
                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("date")!!

            }

            loadingDialog.dismiss()
            binding.progressBar.visibility = View.GONE

        }.addOnFailureListener {
            Log.e("NotificationFragment", "${it.message}")
            loadingDialog.dismiss()
            binding.progressBar.visibility = View.GONE
        }

    }

}