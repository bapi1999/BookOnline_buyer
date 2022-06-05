package com.sbdevs.bookonline.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlin.collections.ArrayList

class NotificationFragment : Fragment() {
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)

        loadingDialog.show(childFragmentManager, "Show")

        if (user != null) {
            viewLifecycleOwner.lifecycleScope.launch {
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
        notificationRecycler.layoutManager = LinearLayoutManager(context)

        notificationAdapter = NotificationAdapter(notificationList)
        notificationRecycler.adapter = notificationAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                .collection("USER_NOTIFICATIONS")
                .orderBy("date", Query.Direction.DESCENDING)
        } else {
            firebaseFirestore.collection("USERS").document(user!!.uid)
                .collection("USER_NOTIFICATIONS")
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
                    val orderId: String = item.getString("order_id")!!
                    val seen: Boolean = item.getBoolean("seen")!!

                    newNotiList.add(
                        NotificationModel(
                            notificationId,
                            date,
                            description,
                            image,
                            orderId,
                            seen
                        )
                    )


                }
                notificationList.addAll(newNotiList)

                binding.emptyContainer.visibility = View.GONE
                binding.notificationRecycler.visibility = View.VISIBLE
                notificationAdapter.list = notificationList

                if (lastResult == null) {
                    notificationAdapter.notifyItemRangeInserted(0, newNotiList.size)
                } else {
                    notificationAdapter.notifyItemRangeInserted(notificationList.size - 1, newNotiList.size)
                }

                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("date")!!

                isReachLast = allDocumentSnapshot.size < 10 // limit is 10


            } else {
                isReachLast = true
                if (notificationList.isEmpty()) {
                    binding.emptyContainer.visibility = View.VISIBLE
                    binding.notificationRecycler.visibility = View.GONE
                }
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