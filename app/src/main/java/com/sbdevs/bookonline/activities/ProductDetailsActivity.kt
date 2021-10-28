package com.sbdevs.bookonline.activities

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityProductDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.sbdevs.bookonline.adapters.uiadapter.ProductReviewAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.sbdevs.bookonline.models.ProductReviewModel
import androidx.appcompat.content.res.AppCompatResources
import com.google.firebase.firestore.Query
import kotlinx.coroutines.withContext
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import com.sbdevs.bookonline.adapters.ProductImgAdapter
import android.widget.ProgressBar
import android.widget.TextView
import com.sbdevs.bookonline.models.CartModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ProductDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailsBinding
    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val user = firebaseAuth.currentUser
    lateinit var addToCartBtn: AppCompatButton
    lateinit var buyNowBtn: AppCompatButton
    lateinit var fabBtn: FloatingActionButton
    private val gone = View.GONE
    private val visible = View.VISIBLE
    private var reviewList: List<ProductReviewModel> = ArrayList()
    private lateinit var reviewAdapter: ProductReviewAdapter

    private lateinit var productImgViewPager: ViewPager
    private lateinit var productImgIndicator: TabLayout

    var cartList :ArrayList<MutableMap<String,Any>> = ArrayList()
    var fbCartList:ArrayList<MutableMap<String,Any>> = ArrayList()

    var wishList :ArrayList<String> = ArrayList()
    var fbWishList :ArrayList<String> = ArrayList()

    var sendingList:ArrayList<CartModel> = ArrayList()
    var totalPrice: Int = 0
    var discount = 0
    var totalAmount= 0

    var productImgList: ArrayList<String> = ArrayList()
    public var ALREADY_ADDED_TO_WISHLIST :Boolean = false
    lateinit var productId: String
    var wishListIndex = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addToCartBtn = binding.addToCartBtn
        buyNowBtn = binding.buyNowBtn
        productImgViewPager = binding.lay1.productImgViewPager
        productImgIndicator = binding.lay1.productImgIndicator
        productImgIndicator.setupWithViewPager(productImgViewPager, true)


        val loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.le_loading_progress_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                this,
                R.drawable.s_shape_bg_2
            )
        )
        loadingDialog.window!!.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        loadingDialog.show();


        productId= intent.getStringExtra("productId").toString().trim()

        fabBtn = binding.lay1.floatingActionButton



        lifecycleScope.launch(Dispatchers.IO) {


            getFirebaseData(productId)

            withContext(Dispatchers.IO){
                getWishList()
                getCartList()
            }

            withContext(Dispatchers.Main) {
                getReview(productId)
            }
            withContext(Dispatchers.Main) {
                loadingDialog.dismiss()
            }
        }
//        getFirebaseData(productId)


        val layoutManager = LinearLayoutManager(this)
        val reviewRecyclerView = binding.layRating.reviewRecycler
        reviewRecyclerView.layoutManager = layoutManager

        reviewAdapter = ProductReviewAdapter(reviewList)
        reviewRecyclerView.adapter = reviewAdapter
    }

    override fun onStart() {
        super.onStart()

        fabBtn.setOnClickListener {
            if (ALREADY_ADDED_TO_WISHLIST != true){

                wishList.add(productId)
                val wishmap:MutableMap<String,Any> = HashMap()
                wishmap["wish_list"] = wishList
                firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
                    .document("MY_WISHLIST").update(wishmap).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this,"successful",Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
                        }
                    }
                ALREADY_ADDED_TO_WISHLIST = true
                fabBtn.supportImageTintList =
                    ContextCompat.getColorStateList(this, R.color.red)
                fabBtn.rippleColor =
                    ContextCompat.getColor(this, R.color.gray_400)
            }else{
                Toast.makeText(this,"Removed from position $wishListIndex ",Toast.LENGTH_SHORT).show()

                wishList.removeAt(wishListIndex)
                val cartmap:MutableMap<String,Any> = HashMap()
                cartmap["wish_list"] = wishList
                firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
                    .document("MY_WISHLIST").update(cartmap).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this,"successful",Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
                        }
                    }

                ALREADY_ADDED_TO_WISHLIST = false
                fabBtn.supportImageTintList =
                    ContextCompat.getColorStateList(this, R.color.gray_400)
                fabBtn.rippleColor =
                    ContextCompat.getColor(this, R.color.red)
            }

        }



        addToCartBtn.setOnClickListener {
            val listMap:MutableMap<String,Any> = HashMap()
            listMap["product"] = productId
            listMap["quantity"] = 1
            cartList.add(listMap)
            val cartmap:MutableMap<String,Any> = HashMap()
            cartmap["cart_list"] = cartList
            firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
                .document("MY_CART").update(cartmap).addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(this,"successful",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
                    }
                }

        }
        buyNowBtn.setOnClickListener {
            val intent  = Intent(this,ProceedOrderActivity::class.java)
            intent.putExtra("From_To",2)
            //todo: 1=> MyCart / 2=> BuyNow
            intent.putParcelableArrayListExtra("productList",sendingList);
            intent.putExtra("total_price",totalPrice)
            intent.putExtra("total_discount",discount)
            intent.putExtra("total_amount",totalAmount)
            startActivity(intent)
        }

    }


    private fun getFirebaseData(productId: String) = CoroutineScope(Dispatchers.IO).launch {
        val lay1 = binding.lay1
        val lay2 = binding.lay2
        val lay3 = binding.lay3
        val lay4 = binding.lay4
        val layR = binding.layRating
        firebaseFirestore.collection("PRODUCTS").document(productId).get()
            .addOnSuccessListener {
                if (it.exists()) {
//                    lifecycleScope.launch(Dispatchers.IO) {
//
//                    }
                    var categoryString = ""
                    var tagsString = ""

                    val productName = it.getString("book_title")!!
                    val priceReal = it.getString("price_Rs")!!.trim()
                    val priceOffer = it.getString("price_offer")!!.trim()
                    val avgRating = it.getString("rating_avg")!!
                    val totalRating: Int = it.getLong("rating_total")!!.toInt()
                    val inStock = it.getBoolean("in_stock")!!
                    val stock = it.getLong("in_stock_quantity")!!
                    val description = it.getString("book_details")!!
                    val categoryList: ArrayList<String> = it.get("categories") as ArrayList<String>
                    val tagList: ArrayList<String> = it.get("tags") as ArrayList<String>
                    val url = it.get("product_thumbnail").toString().trim()

                    totalAmount = priceOffer.toInt()
                    totalPrice = priceReal.toInt()
                    discount = totalPrice - totalAmount
                    sendingList.add(CartModel(productId,url,productName,priceReal,inStock,stock,priceOffer,1))


                    for (catrgorys in categoryList) {
                        categoryString += "$catrgorys,  "
                    }

                    for (tag in tagList) {
                        tagsString += "#$tag  "
                    }

                    for (x in 0 until it.getLong("no_of_img")!!) {
                        productImgList.add(it.get("product_img_$x").toString().trim())
                    }
                    val adapter = ProductImgAdapter(productImgList)
                    productImgViewPager.adapter = adapter

                    lay1.productName.text = productName

                    if (priceOffer == "") {
                        lay1.productPrice.text = priceReal
                        lay1.strikeThroughPrice.visibility = gone
                        lay1.percentOff.visibility = gone
                    } else {
                        val percent =
                            100 * (priceReal.toInt() - priceOffer.toInt()) / (priceReal.toInt())

                        lay1.productPrice.text = priceOffer
                        lay1.strikeThroughPrice.text = priceReal
                        lay1.percentOff.text = "${percent}% off"

                    }
                    lay1.productState.text = it.getString("book_state")!!
                    lay1.miniProductRating.text = avgRating
                    lay1.miniTotalNumberOfRatings.text = "(${totalRating} ratings)"

                    if (stock > 3) {
                        lay1.stockState.visibility = gone
                        lay1.stockQuantity.visibility = gone
                    } else if (stock in 1..2) {
                        lay1.stockState.text = "low"
                        lay1.stockQuantity.text = "only $stock available in stock"
                    }
                    else {
                        lay1.stockState.text = "out of stock"
                        lay1.stockQuantity.visibility = gone
                    }

// todo layout 2

                    lay2.productDetailsText.text = description

                    //todo layout 3
                    lay3.writerName.text = it.getString("book_writer")
                    lay3.publisherName.text = it.getString("book_publisher")
                    lay3.bookLanguage.text = it.getString("book_language")
                    lay3.printDate.text = it.getString("book_printed_ON")
                    lay3.bookCondition.text = it.getString("book_condition")
                    lay3.pageCount.text = it.getString("book_pageCount")
                    lay3.isbnNumber.text = it.getString("book_ISBN")
//                    lay3.bookDimension.text = it.getString("")

                    //todo layout 4
                    lay4.categoryText.text = categoryString
                    lay4.tagsText.text = tagsString

                    //todo rating
                    layR.averageRatingText.text = avgRating
                    layR.totalRating.text = totalRating.toString()

                    for (x in 0..4) {
                        var ratingtxt: TextView =
                            layR.ratingsNumberContainer.getChildAt(x) as TextView
                        ratingtxt.text = (it.get("rating_Star_" + (5 - x)).toString())
                        val progressBar: ProgressBar =
                            layR.ratingBarContainter.getChildAt(x) as ProgressBar
                        val maxProgress: Int = it.getLong("rating_total")!!.toInt()
                        progressBar.max = maxProgress
                        val perccing: String = it.get("rating_Star_" + (5 - x)).toString()
                        val progress = Integer.valueOf(perccing)
                        progressBar.progress = progress
                    }


                }
            }.await()
    }

    private fun getReview(productID: String) = CoroutineScope(Dispatchers.IO).launch {
        firebaseFirestore.collection("PRODUCTS").document(productID)
            .collection("PRODUCT_REVIEW")
            .orderBy("priority", Query.Direction.DESCENDING).limit(7)
            .get().addOnCompleteListener {

                if (it.isSuccessful) {
                    reviewList = it.result!!.toObjects(ProductReviewModel::class.java)
                    reviewAdapter.list = reviewList
                    reviewAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@ProductDetailsActivity, it.exception?.message, Toast.LENGTH_LONG).show()
                }

            }.await()

    }

    private fun getCartList() {

        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_CART").get().addOnCompleteListener {
                if (it.isSuccessful){
                    val x = it.result?.get("cart_list")

                    if (x != null){
                        fbCartList = x as ArrayList<MutableMap<String,Any>>
                        cartList.addAll(fbCartList)

                    }else{
                        Toast.makeText(this,"baal cart",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this,"Failed cart",Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun getWishList() = lifecycleScope.launch(Dispatchers.IO) {


        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_WISHLIST").get().addOnCompleteListener {
                if (it.isSuccessful){
                    val x = it.result?.get("wish_list")

                    if (x != null){
                        fbWishList = x as ArrayList<String>

                        wishList.addAll(fbWishList)
                        var index = 0
                        for (ids: String in fbWishList) {

                            if (ids.contains(productId) ) {
                                wishListIndex = index
                                ALREADY_ADDED_TO_WISHLIST = true
//                                Toast.makeText(this@ProductDetailsActivity,"ALREADY_ADDED_TO_WISHLIST ",Toast.LENGTH_SHORT).show()
                                fabBtn.supportImageTintList =
                                    ContextCompat.getColorStateList(this@ProductDetailsActivity, R.color.red)
                                fabBtn.rippleColor =
                                    ContextCompat.getColor(this@ProductDetailsActivity, R.color.gray_400)

                            }
                            index++

                        }

                    }else{
                        Toast.makeText(this@ProductDetailsActivity,"baal",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this@ProductDetailsActivity,"Failed",Toast.LENGTH_SHORT).show()
                }
            }.await()

    }


}