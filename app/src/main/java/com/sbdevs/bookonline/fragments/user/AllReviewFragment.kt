package com.sbdevs.bookonline.fragments.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.adapters.ProductReviewAdapter
import com.sbdevs.bookonline.databinding.FragmentAllReviewBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.models.ProductReviewModel


class AllReviewFragment : Fragment() {
    private var _binding: FragmentAllReviewBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore


    private var reviewList: MutableList<ProductReviewModel> = ArrayList()
    private lateinit var reviewAdapter: ProductReviewAdapter
    private lateinit var reviewRecyclerView: RecyclerView

    private var lastResult: DocumentSnapshot? = null
    private lateinit var times: Timestamp
    private var isReachLast: Boolean = false
    private val loadingDialog = LoadingDialog()
    private var counter = 0
    private lateinit var productID:String

    private lateinit var avgRating:String
    private var totalRating = 0
    private var ratingNum :ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllReviewBinding.inflate(inflater, container, false)

        productID = arguments?.getString("productId").toString()

        avgRating = arguments!!.getString("avgRating").toString()
        totalRating = arguments!!.getInt("totalRating").toInt()
        ratingNum = arguments!!.getStringArrayList("ratingCount") as ArrayList<String>


        if (productID != null) {
            loadingDialog.show(childFragmentManager, "show")
            getReview(productID)
            setRatings(totalRating,avgRating)
        }


        reviewRecyclerView = binding.reviewRecycler
        reviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        reviewAdapter = ProductReviewAdapter(reviewList)
        reviewRecyclerView.adapter = reviewAdapter

//        val lay1 = binding.lay1
//
//        lay1.reviewRecycler.visibility = View.GONE
//        lay1.viewAllButton.visibility = View.GONE
//        lay1.rateNowBtn.visibility = View.GONE
//        lay1.textView85.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reviewRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)


                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_RIGHT) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    // end scrolling: do what you want here and after calling the function change the value of boolean

                    if (isReachLast){
                        Log.w("Query item","Last item is reached already")

                    }else{

                        Log.e("last query", "${lastResult.toString()}")
                        binding.progressBar2.visibility = View.VISIBLE
                        getReview(productID)
                    }

                }

            }


        })
    }

    private fun getReview(productID: String) {

        val query = if (lastResult == null) {

            firebaseFirestore.collection("PRODUCTS").document(productID)
                .collection("PRODUCT_REVIEW")
                .orderBy("review_Date", Query.Direction.DESCENDING)

        } else {

            firebaseFirestore.collection("PRODUCTS").document(productID)
                .collection("PRODUCT_REVIEW")
                .orderBy("review_Date", Query.Direction.DESCENDING)
                .startAfter(times)
        }

        query.limit(14L).get().addOnSuccessListener {
            //reviewRecyclerView.visibility = View.VISIBLE
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()) {
                isReachLast = allDocumentSnapshot.size < 14 // limit is 14
            } else {
                isReachLast = true
            }


            val newList = it.toObjects(ProductReviewModel::class.java)

            reviewList.addAll(newList)

            if (reviewList.isEmpty()) {
                reviewRecyclerView.visibility = View.GONE
                binding.emptyContainer.visibility = View.VISIBLE
            } else {
                reviewRecyclerView.visibility = View.VISIBLE
                binding.emptyContainer.visibility = View.GONE

                reviewAdapter.list = reviewList

                binding.textView26.text = reviewList.size.toString()
                //reviewAdapter.notifyDataSetChanged()

                if (counter == 0 ){
                    reviewAdapter.notifyItemRangeInserted(0,newList.size)
                }else{
                    reviewAdapter.notifyItemRangeInserted(reviewList.size-1,newList.size)
                }

                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("review_Date")!!

                loadingDialog.dismiss()
                binding.progressBar2.visibility = View.GONE

            }

            counter = 1
        }.addOnFailureListener {
            reviewRecyclerView.visibility = View.GONE
            binding.emptyContainer.visibility = View.VISIBLE
//                    Toast.makeText(context!!, it.message, Toast.LENGTH_LONG).show()
            Log.e("Loading review", "${it.message}")
        }


    }

    private fun getReview2(productID: String) {
        firebaseFirestore.collection("PRODUCTS").document(productID)
            .collection("PRODUCT_REVIEW")
            .orderBy("review_Date", Query.Direction.DESCENDING).limit(5)
            .get().addOnSuccessListener {
                reviewList = it.toObjects(ProductReviewModel::class.java)
                reviewAdapter.list = reviewList
                reviewAdapter.notifyDataSetChanged()

                loadingDialog.dismiss()

            }.addOnFailureListener{
                loadingDialog.dismiss()
                Log.e("Review","${it.message}",it.cause)
            }

    }

    private fun setRatings(total:Int,avg:String){
        val layR =binding.lay1
        layR.averageRatingText.text = avgRating
        layR.totalRating.text = totalRating.toString()

        for (x in 0..4) {
            var ratingtxt: TextView = layR.ratingsNumberContainer.getChildAt(x) as TextView
            val perccing: String = ratingNum[x]//it.get("rating_Star_" + (5 - x)).toString()

            ratingtxt.text = perccing

            val progressBar: ProgressBar = layR.ratingBarContainter.getChildAt(x) as ProgressBar
            val maxProgress: Int = totalRating
            progressBar.max = maxProgress

            val progress = Integer.valueOf(perccing.trim())
            progressBar.progress = progress
        }
    }


}