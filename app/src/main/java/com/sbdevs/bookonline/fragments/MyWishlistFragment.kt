package com.sbdevs.bookonline.fragments

import android.app.Dialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.WishlistAdapter
import com.sbdevs.bookonline.databinding.FragmentMyWishlistBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MyWishlistFragment : Fragment(), WishlistAdapter.MyonItemClickListener {

    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth
    private val user = firebaseAuth.currentUser

    private var _binding: FragmentMyWishlistBinding? = null
    private val binding get() = _binding!!
    lateinit var loadingDialog: Dialog
    var list = ArrayList<String>()
    lateinit var wishlistAdapter: WishlistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMyWishlistBinding.inflate(inflater, container, false)


        loadingDialog = Dialog(requireContext())
        loadingDialog.setContentView(R.layout.le_loading_progress_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                requireContext().applicationContext,
                R.drawable.s_shape_bg_2
            )
        )
        loadingDialog.window!!.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        loadingDialog.show()


        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (user!=null){
                    getWishListData()
                    delay(1000)
                }else{
                    binding.emptyContainer.visibility = View.VISIBLE
                    binding.wishlistRecycler.visibility = View.GONE
                }

            }
            withContext(Dispatchers.Main) {
                loadingDialog.dismiss()
            }
        }


        val recyclerView = binding.wishlistRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)
        wishlistAdapter = WishlistAdapter(list, this)
        recyclerView.adapter = wishlistAdapter




        return binding.root
    }

    private fun getWishListData() {

        firebaseAuth.currentUser?.let {
            firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
                .document("MY_WISHLIST").get().addOnSuccessListener {

                    val x = it.get("wish_list")
                    if (x == null){
                        binding.emptyContainer.visibility = View.VISIBLE
                        binding.wishlistRecycler.visibility = View.GONE
                    }else{
                        list = x as ArrayList<String>
                        if (list.size == 0){

                            binding.emptyContainer.visibility = View.VISIBLE
                            binding.wishlistRecycler.visibility = View.GONE
                        }else{
                            binding.emptyContainer.visibility = View.GONE
                            binding.wishlistRecycler.visibility = View.VISIBLE
                            wishlistAdapter.list = list
                            wishlistAdapter.notifyDataSetChanged()
                        }

                    }


                }.addOnFailureListener {
                    Log.e("WishList","Failed to load wist list",it.cause)
                }
        }

    }

    override fun onItemClick(position: Int) {
        list.removeAt(position)
        wishlistAdapter.notifyItemRemoved(position)
        val cartmap: MutableMap<String, Any> = HashMap()
        cartmap["wish_list"] = list
        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_WISHLIST").update(cartmap).addOnSuccessListener {
                Toast.makeText(context, "successfully removed", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {
                Toast.makeText(context, it.message.toString(), Toast.LENGTH_SHORT).show()
            }

    }


}