package com.sbdevs.bookonline.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.WishlistAdapter
import com.sbdevs.bookonline.databinding.FragmentMyWishlistBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MyWishlistFragment : Fragment(),WishlistAdapter.MyonItemClickListener {

    private val firebaseFirestore = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser
    private var _binding: FragmentMyWishlistBinding?=null
    private val binding get() = _binding!!
    lateinit var loadingDialog :Dialog
    var list = ArrayList<String>()
    lateinit var wishlistAdapter: WishlistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMyWishlistBinding.inflate(inflater, container, false)


        loadingDialog = Dialog(activity!!)
        loadingDialog.setContentView(R.layout.le_loading_progress_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(activity!!.applicationContext, R.drawable.s_shape_bg_2)
        )
        loadingDialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        loadingDialog.show()
        lifecycleScope.launch(Dispatchers.IO){
            withContext(Dispatchers.IO){
                getWishListData()
                delay(1000)
            }
            withContext(Dispatchers.Main){
                loadingDialog.dismiss()
            }
        }



        val recyclerView = binding.wishlistRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)
        wishlistAdapter = WishlistAdapter(list,this)
        recyclerView.adapter = wishlistAdapter




        return binding.root
    }
    fun getWishListData(){
        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_WISHLIST").get().addOnCompleteListener {
                if (it.isSuccessful ){
                    list = it.result?.get("wish_list") as ArrayList<String>
                    wishlistAdapter.list = list
                    wishlistAdapter.notifyDataSetChanged()
                }
            }
    }

    override fun onItemClick(position: Int) {
        list.removeAt(position)
        wishlistAdapter.notifyItemRemoved(position)
        val cartmap:MutableMap<String,Any> = HashMap()
        cartmap["wish_list"] = list
        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_WISHLIST").update(cartmap).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(context,"successful",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show()
                }
            }

    }


}