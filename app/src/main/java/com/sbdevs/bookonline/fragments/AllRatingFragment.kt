package com.sbdevs.bookonline.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.ProductReviewAdapter
import com.sbdevs.bookonline.databinding.FragmentAllRatingBinding
import com.sbdevs.bookonline.models.ProductReviewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AllRatingFragment : Fragment() {
    private var _binding:FragmentAllRatingBinding? =null
    private val binding get() = _binding!!

    private val args:AllRatingFragmentArgs by navArgs()

    private val firebaseFirestore = Firebase.firestore


    private var reviewList: List<ProductReviewModel> = ArrayList()
    private lateinit var reviewAdapter: ProductReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllRatingBinding.inflate(inflater, container, false)

        val productID = args.productId

        getReview(productID)


        val reviewRecyclerView = binding.reviewRecycler
        reviewRecyclerView.layoutManager =  LinearLayoutManager(context)

        reviewAdapter = ProductReviewAdapter(reviewList)
        reviewRecyclerView.adapter = reviewAdapter

        return binding.root
    }

    private fun getReview(productID: String) = CoroutineScope(Dispatchers.IO).launch {
        firebaseFirestore.collection("PRODUCTS").document(productID)
            .collection("PRODUCT_REVIEW")
            .orderBy("review_Date", Query.Direction.DESCENDING).limit(7)
            .get().addOnSuccessListener {
                reviewList = it.toObjects(ProductReviewModel::class.java)
                if (reviewList.isEmpty()){
                    binding.reviewRecycler.visibility = View.GONE
                    binding.emptyContainer.visibility = View.VISIBLE
                }else{

                    reviewAdapter.list = reviewList
                    reviewAdapter.notifyDataSetChanged()
                }





            }.addOnFailureListener{
                binding.reviewRecycler.visibility = View.GONE
                binding.emptyContainer.visibility = View.VISIBLE
                Toast.makeText(context!!, it.message, Toast.LENGTH_LONG).show()
            }.await()

    }


}