package com.sbdevs.bookonline.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.FragmentRateNowBinding
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap


class RateNowFragment : Fragment() {
    private var _binding:FragmentRateNowBinding? = null
    private val binding get() = _binding!!
    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth
    private val args:RateNowFragmentArgs by navArgs()

    private var ALL_READY_REVIEWED = false

    lateinit var ratingBar:RatingBar
    lateinit var productId:String
    lateinit var reviewInput:TextInputLayout

    var rating5:Long = 0
    var rating4:Long  = 0
    var rating3:Long  = 0
    var rating2 :Long = 0
    var rating1:Long  = 0
    var totalRatingsNumber= 0L

    private lateinit var buyerName:String

    lateinit var  loadingDialog : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRateNowBinding.inflate(inflater, container, false)
        productId = args.productId

        loadingDialog = Dialog(context!!)
        loadingDialog.setContentView(R.layout.le_loading_progress_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(context!!.applicationContext, R.drawable.s_shape_bg_2)
        )
        loadingDialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        loadingDialog.show()

        viewLifecycleOwner.lifecycleScope.launch{
            withContext(Dispatchers.IO) {


                getProductReting()

                getUsername()

                getMyBoughtProducts(productId)

            }
            withContext(Dispatchers.Main){
                delay(1000)
                loadingDialog.dismiss()
            }
        }


        ratingBar = binding.rateNowContainer
        reviewInput = binding.reviewInput

        ratingBar.onRatingBarChangeListener =
        RatingBar.OnRatingBarChangeListener { ratingBar, fl, b ->
            //
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()


        binding.submitBtn.setOnClickListener {
            loadingDialog.show()
            if (ALL_READY_REVIEWED){
                Toast.makeText(context,"You already rate this product",Toast.LENGTH_LONG).show()
            }else{
                if (ratingBar.rating != 0F){
                    lifecycleScope.launch{
                        withContext(Dispatchers.IO){
                            updateProductReting()
                            delay(200)
                        }
                        withContext(Dispatchers.IO){
                            creatingRatingDocToProduct()
                            uploadMyRatingToFireBase()
                        }
                        withContext(Dispatchers.Main){
                            loadingDialog.dismiss()
                        }




                    }
                }
            }


        }
    }



    private fun getMyBoughtProducts(productId:String){
        firebaseFirestore.collection("USERS").document(firebaseAuth.currentUser!!.uid)
            .collection("USER_DATA").document("THINGS_I_BOUGHT").get()
            .addOnSuccessListener {
                val x = it.get("my_bought_items")
                var productList:ArrayList<String> = ArrayList()
                if (x!=null){
                    productList = x as ArrayList<String>
                    for (i in productList){
                        if(productId == i){
                            ALL_READY_REVIEWED = true
                        }
                    }
                }else{
                    Toast.makeText(context,"blank list",Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun uploadMyRatingToFireBase(){
        val reviewMap:MutableMap<String,Any> = HashMap()
        reviewMap["product_id"] = productId
        reviewMap["rating_id"] = firebaseAuth.currentUser!!.uid
        reviewMap["time"] = FieldValue.serverTimestamp()
        firebaseFirestore.collection("USERS").document(firebaseAuth.currentUser!!.uid)
            .collection("USER_DATA").document("THINGS_I_BOUGHT")
            .collection("MY_REVIEWS").add(reviewMap)
    }


    private fun creatingRatingDocToProduct(){
        val ratingMap:MutableMap<String,Any> = HashMap()
        ratingMap["buyer_name"] = buyerName
        ratingMap["rating"] = ratingBar.rating.toLong()
        ratingMap["review_Date"] = FieldValue.serverTimestamp()
        ratingMap["buyer_ID"] = firebaseAuth.currentUser!!.uid
        ratingMap["review"]= reviewInput.editText?.text.toString()

        firebaseFirestore.collection("PRODUCTS")
            .document(productId).collection("PRODUCT_REVIEW")
            .document(firebaseAuth.currentUser!!.uid.toString())
            .set(ratingMap)
    }

    private fun updateProductReting(){
        val productMap:MutableMap<String,Any> = HashMap()

        when(val thisRating = ratingBar.rating.toInt()){
            5->{
                productMap["rating_Star_$thisRating"]= rating5+1
            }
            4->{
                productMap["rating_Star_$thisRating"]= rating4+1
            }
            3->{
                productMap["rating_Star_$thisRating"]= rating3+1
            }
            2->{
                productMap["rating_Star_$thisRating"]= rating2+1
            }
            1->{
                productMap["rating_Star_$thisRating"]= rating1+1
            }

        }
        productMap["rating_total"] = totalRatingsNumber+1
        productMap["rating_avg"] = calculateRating().toString()
        firebaseFirestore.collection("PRODUCTS").document(productId).update(productMap)
    }


    private fun calculateRating():Float{

        var total = 0L

        when(val thisRating = ratingBar.rating.toInt()){
            5->{
                total = (rating5+1)*5+rating4*4+rating3*3+rating2*2+rating1*1
            }
            4->{
                total = rating5*5+(rating4+1)*4+rating3*3+rating2*2+rating1*1
            }
            3->{
                total = rating5*5+rating4*4+(rating3+1)*3+rating2*2+rating1*1
            }
            2->{
                total = rating5*5+rating4*4+rating3*3+(rating2+1)*2+rating1*1
            }
            1->{
                total = rating5*5+rating4*4+rating3*3+rating2*2+(rating1+1)*1
            }
        }

        val avgRating:Float = (total.toFloat())/(totalRatingsNumber.toFloat()+1)

        return avgRating

    }


    private fun getProductReting(){
        firebaseFirestore.collection("PRODUCTS")
            .document(productId).get().addOnSuccessListener {

                rating5 = it.getLong("rating_Star_5")!!
                rating4 = it.getLong("rating_Star_4")!!
                rating3 = it.getLong("rating_Star_3")!!
                rating2 = it.getLong("rating_Star_2")!!
                rating1 = it.getLong("rating_Star_1")!!
                totalRatingsNumber= it.getLong("rating_total")!!

            }
    }

    private fun getUsername(){
        firebaseFirestore.collection("USERS").document(firebaseAuth.currentUser!!.uid)
            .addSnapshotListener { value,error ->
                error?.let {
                    Toast.makeText(context,"Failed to load name",Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                value?.let {
                    val email = it.getString("email").toString()
                    val name = it.getString("name").toString()
                    buyerName = if (name == ""){
                        email
                    }else{
                        name
                    }
                }
            }
    }




}