package com.sbdevs.bookonline.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.AddAddressActivity
import com.sbdevs.bookonline.adapters.MyAddressAddapter
import com.sbdevs.bookonline.databinding.FragmentMyAddressBinding
import com.sbdevs.bookonline.databinding.FragmentMyCartBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


class MyAddressFragment : Fragment(), MyAddressAddapter.MyonItemClickListener {

    private var _binding: FragmentMyAddressBinding?=null
    private val binding get() = _binding!!
    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    var list:ArrayList<MutableMap<String,Any>> = ArrayList()
    lateinit var addressAddapter: MyAddressAddapter
    var selectNo:Long = 0
    var seter = 0

    lateinit var loadingDialog : Dialog
    lateinit var swipeRefresh: SwipeRefreshLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMyAddressBinding.inflate(inflater, container, false)
        loadingDialog = Dialog(activity!!)
        loadingDialog.setContentView(R.layout.le_loading_progress_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(activity!!.applicationContext,R.drawable.s_shape_bg_2)
        )
        loadingDialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        loadingDialog.show()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
            withContext(Dispatchers.Main){
                getAddressList()
                delay(1000)
            }
            withContext(Dispatchers.Main){
                loadingDialog.dismiss()
//                binding.textView44.text = list.size.toString()
                val long = selectNo
            }
        }
        swipeRefresh = binding.swipeRefresh

        val recyclerView = binding.addressRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.isNestedScrollingEnabled = false
        addressAddapter = MyAddressAddapter(list,selectNo,this)
        recyclerView.adapter = addressAddapter

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing =true
        }


        binding.addNewAddress.setOnClickListener {
            val action = MyAddressFragmentDirections.actionMyAddressFragmentToAddAddressFragment()
            findNavController().navigate(action)
        }



        return binding.root
    }



    fun getAddressList() {
        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_ADDRESSES").addSnapshotListener { value ,error ->
                error?.let {
                    Toast.makeText(context,"Problem in fetching address",Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                value?.let {
                    val position: Long = value.getLong("select_No")!!
                    selectNo =  value.getLong("select_No")!!
                    list = value.get("address_list") as ArrayList<MutableMap<String, Any>>



                    addressAddapter.list = list
                    addressAddapter.selectNo =selectNo
                    addressAddapter.notifyDataSetChanged()
                }




            }
    }

    override fun onItemClick(position: Int) {
//        addressAddapter.notifyItemChanged(selectNo.toInt())
//        Toast.makeText(context,"$position checked", Toast.LENGTH_SHORT).show()

        addressAddapter.selectNo = position.toLong()
        addressAddapter.notifyDataSetChanged()
        val addressMap:MutableMap<String,Any> = HashMap<String,Any>()
        addressMap["select_No"] = position
        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_ADDRESSES").update(addressMap).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(context,"updated", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(context,"Failed to update", Toast.LENGTH_SHORT).show()
                }
            }


    }


}