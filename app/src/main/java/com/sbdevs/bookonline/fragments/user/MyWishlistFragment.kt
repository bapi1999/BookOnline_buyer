package com.sbdevs.bookonline.fragments.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.adapters.user.WishlistAdapter
import com.sbdevs.bookonline.databinding.FragmentMyWishlistBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.othercalss.SharedDataClass
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
    private val loadingDialog = LoadingDialog()
    var list = ArrayList<String>()
    lateinit var wishlistAdapter: WishlistAdapter
    private lateinit var bannerAdView: AdView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMyWishlistBinding.inflate(inflater, container, false)

        loadingDialog.show(childFragmentManager,"Show")

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


        MobileAds.initialize(requireContext()) {}
        bannerAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        bannerAdView.loadAd(adRequest)

        bannerAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                Log.e("Banner","Ad loaded successfully")
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                Log.e("Banner load Failed","${adError.message}")
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }
        }


        return binding.root
    }

    override fun onPause() {
        super.onPause()
        bannerAdView.pause()
    }

    override fun onResume() {
        super.onResume()
        bannerAdView.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        bannerAdView.destroy()
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

    override fun onItemClick(position: Int, productId: String) {
        list.remove(productId)
        wishlistAdapter.notifyItemRemoved(position)
        SharedDataClass.dbWishList.remove(productId)
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