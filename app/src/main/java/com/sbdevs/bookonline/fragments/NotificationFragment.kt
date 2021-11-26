package com.sbdevs.bookonline.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.OrderDetailsActivity
import com.sbdevs.bookonline.adapters.NotificationAdapter
import com.sbdevs.bookonline.databinding.FragmentNotificationBinding
import com.sbdevs.bookonline.models.HomeModel
import com.sbdevs.bookonline.models.NotificationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationFragment : Fragment() {
    private var _binding:FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    lateinit var notificationAdapter: NotificationAdapter
    private var notificationList:List<NotificationModel> = ArrayList()

    var notificationDocIdList:ArrayList<String> = ArrayList()

    lateinit var loadingDialog : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)

        loadingDialog = Dialog(requireActivity())
        loadingDialog.setContentView(R.layout.le_loading_progress_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(requireActivity().applicationContext,R.drawable.s_shape_bg_2)
        )
        loadingDialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        loadingDialog.show()

        if (user != null){
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    getNotificationFormDB()
                }
                withContext(Dispatchers.Main){
                    delay(1000)
                    loadingDialog.dismiss()
                }


            }
        }else{
            binding.emptyContainer.visibility = View.VISIBLE
            binding.notificationRecycler.visibility = View.GONE
            loadingDialog.dismiss()
        }




        val recyclerView = binding.notificationRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)

        notificationAdapter = NotificationAdapter(notificationList,notificationDocIdList)
        recyclerView.adapter = notificationAdapter

        return binding.root
    }
    private fun getNotificationFormDB(){
        firebaseFirestore.collection("USERS").document(user!!.uid)
            .collection("USER_DATA")
            .document("MY_NOTIFICATION")
            .collection("NOTIFICATION")
            .orderBy("date",Query.Direction.DESCENDING)
            .get().addOnSuccessListener {
                val allDocumentSnapshot = it.documents
                for (item in allDocumentSnapshot){
                    notificationDocIdList.add(item.id)

                }
                notificationAdapter.docNameList = notificationDocIdList
                notificationList = it.toObjects(NotificationModel::class.java)
                if (notificationList.isEmpty()){
                    binding.emptyContainer.visibility = View.VISIBLE
                    binding.notificationRecycler.visibility = View.GONE
                }else{
                    binding.emptyContainer.visibility = View.GONE
                    binding.notificationRecycler.visibility = View.VISIBLE

                    notificationAdapter.list = notificationList
                    notificationAdapter.notifyDataSetChanged()
                }


            }.addOnFailureListener{
                Log.e("NotificationFragment","${it.message}")
            }

    }




}