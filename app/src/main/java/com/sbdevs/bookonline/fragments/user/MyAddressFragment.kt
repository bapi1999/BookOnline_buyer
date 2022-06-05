package com.sbdevs.bookonline.fragments.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.user.MyAddressAddapter
import com.sbdevs.bookonline.databinding.FragmentMyAddressBinding
import com.sbdevs.bookonline.fragments.LoadingDialog

import kotlinx.coroutines.*


class MyAddressFragment : Fragment(), MyAddressAddapter.MyonItemClickListener {

    private var _binding: FragmentMyAddressBinding?=null
    private val binding get() = _binding!!
    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    var list:ArrayList<MutableMap<String,Any>> = ArrayList()
    lateinit var addressAddapter: MyAddressAddapter
    var selectNo:Long = 0
    var addressMap:MutableMap<String,Any> = HashMap()
    private var loadingDialog = LoadingDialog()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMyAddressBinding.inflate(inflater, container, false)

        loadingDialog.show(childFragmentManager,"Show")

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


        val recyclerView = binding.addressRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.isNestedScrollingEnabled = false
        addressAddapter = MyAddressAddapter(list,selectNo,this)
        recyclerView.adapter = addressAddapter


        binding.addNewAddress.setOnClickListener {
//            val action = MyAddressFragmentDirections.actionMyAddressFragmentToAddAddressFragment()
//            findNavController().navigate(action)
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container, AddAddressFragment())
                addToBackStack("addAddress")
            }
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.continueBtn.setOnClickListener {
            requireActivity().finish()
        }
    }


    private fun getAddressList() {
        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_ADDRESSES").addSnapshotListener { value ,error ->
                error?.let {
                    Log.e("Get Address","${it.message}")
                    return@addSnapshotListener
                }
                value?.let {
                    val position: Long = value.getLong("select_No")!!
                    selectNo =  value.getLong("select_No")!!
                    val x = value.get("address_list")

                    if(x!= null){

                        list =  x as ArrayList<MutableMap<String, Any>>
                        if (list.size == 0){
                            binding.linearLayout1.visibility = View.VISIBLE
                            binding.addressRecycler.visibility = View.GONE
                        }else{
                            binding.linearLayout1.visibility = View.GONE
                            binding.addressRecycler.visibility = View.VISIBLE
                        }
                    }else{
                        binding.linearLayout1.visibility = View.VISIBLE
                        binding.addressRecycler.visibility = View.GONE
                    }




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
            .document("MY_ADDRESSES").update(addressMap)
            .addOnSuccessListener {
                Log.i("update selected address","successful")
            }.addOnFailureListener {
                Log.e("update selected address","${it.message}")
            }


    }

}