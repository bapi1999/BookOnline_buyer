package com.sbdevs.bookonline.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.ProceedOrderActivity
import com.sbdevs.bookonline.adapters.ProductImgAdapter
import com.sbdevs.bookonline.adapters.ProductReviewAdapter
import com.sbdevs.bookonline.databinding.FragmentProductBinding
import com.sbdevs.bookonline.models.CartModel
import com.sbdevs.bookonline.models.ProductReviewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class ProductFragment : Fragment() {
    private var _binding :FragmentProductBinding? = null
    private val binding get() = _binding!!



    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val user = firebaseAuth.currentUser
//
//    lateinit var addToCartBtn: AppCompatButton
//    lateinit var buyNowBtn: AppCompatButton

    lateinit var addToCartBtn: Button
    lateinit var buyNowBtn: Button

    lateinit var fabBtn: FloatingActionButton
    private val gone = View.GONE
    private val visible = View.VISIBLE
    private var reviewList: List<ProductReviewModel> = ArrayList()
    private lateinit var reviewAdapter: ProductReviewAdapter

    private lateinit var productImgViewPager: ViewPager
    private lateinit var productImgIndicator: TabLayout

    private var cartList :ArrayList<MutableMap<String,Any>> = ArrayList()
    private var fbCartList:ArrayList<MutableMap<String,Any>> = ArrayList()

    private var wishList :ArrayList<String> = ArrayList()
    private var fbWishList :ArrayList<String> = ArrayList()

    private var sendingList:ArrayList<CartModel> = ArrayList()
    private var totalPrice: Int = 0
    private var discount = 0
    private var totalAmount= 0

    private var productImgList: ArrayList<String> = ArrayList()
    private var ALREADY_ADDED_TO_WISHLIST :Boolean = false
    private var ALREADY_ADDED_TO_CART :Boolean = false
    private lateinit var productId: String
    private var wishListIndex = 0
    lateinit var enterQuantityInput: TextInputLayout

    var dbStockQty = 0



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding  = FragmentProductBinding.inflate(inflater, container, false)



        addToCartBtn = binding.lay21.addToCartBtn

        buyNowBtn = binding.lay21.buyNowBtn
        productImgViewPager = binding.lay1.productImgViewPager
        productImgIndicator = binding.lay1.productImgIndicator
        productImgIndicator.setupWithViewPager(productImgViewPager, true)


        val loadingDialog = Dialog(context!!)
        loadingDialog.setContentView(R.layout.le_loading_progress_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                context!!,
                R.drawable.s_shape_bg_2
            )
        )
        loadingDialog.window!!.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        loadingDialog.show();

        val intent = requireActivity().intent
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

        enterQuantityInput = binding.lay21.enterQuantityInput


        val layoutManager = LinearLayoutManager(context)
        val reviewRecyclerView = binding.layRating.reviewRecycler
        reviewRecyclerView.layoutManager = layoutManager

        reviewAdapter = ProductReviewAdapter(reviewList)
        reviewRecyclerView.adapter = reviewAdapter








        return binding.root
    }

    override fun onStart() {
        super.onStart()

        fabBtn.setOnClickListener {
            if (!ALREADY_ADDED_TO_WISHLIST){

                wishList.add(productId)
                val wishmap:MutableMap<String,Any> = HashMap()
                wishmap["wish_list"] = wishList
                firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
                    .document("MY_WISHLIST").update(wishmap)

                ALREADY_ADDED_TO_WISHLIST = true
                fabBtn.supportImageTintList =
                    ContextCompat.getColorStateList(context!!, R.color.red)
                fabBtn.rippleColor =
                    ContextCompat.getColor(context!!, R.color.gray_400)
            }else{
                Toast.makeText(context!!,"Removed from position $wishListIndex ",Toast.LENGTH_SHORT).show()

                wishList.removeAt(wishListIndex)
                val cartmap:MutableMap<String,Any> = HashMap()
                cartmap["wish_list"] = wishList
                firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
                    .document("MY_WISHLIST").update(cartmap)

                ALREADY_ADDED_TO_WISHLIST = false
                fabBtn.supportImageTintList =
                    ContextCompat.getColorStateList(context!!, R.color.gray_400)
                fabBtn.rippleColor =
                    ContextCompat.getColor(context!!, R.color.red)
            }

        }

        addToCartBtn.setOnClickListener {

            if (ALREADY_ADDED_TO_CART){
                Snackbar.make(it, "Already added to cart", Snackbar.LENGTH_SHORT).show()

            }else{
                val listMap:MutableMap<String,Any> = HashMap()
                listMap["product"] = productId
                listMap["quantity"] = 1
                cartList.add(listMap)
                val cartmap:MutableMap<String,Any> = HashMap()
                cartmap["cart_list"] = cartList
                val snack = Snackbar.make(it, "Successfully added to cart", Snackbar.LENGTH_SHORT)
                firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
                    .document("MY_CART").update(cartmap).addOnCompleteListener {itdb->
                        if (itdb.isSuccessful){

                            snack.show()
                        }else{
                            Toast.makeText(context!!,"Failed",Toast.LENGTH_SHORT).show()
                        }
                    }
            }



        }

        buyNowBtn.setOnClickListener {

            if(!checkIsQuantityEntered(dbStockQty)){
                return@setOnClickListener
            }else{
                val qty = enterQuantityInput.editText?.text.toString().toLong()
                val newSendingList:ArrayList<CartModel> = ArrayList()
                newSendingList.add(CartModel(productId,sendingList[0].sellerId, sendingList[0].url,sendingList[0].title
                    ,sendingList[0].priceOriginal, sendingList[0].priceSelling,sendingList[0].stockQty,qty))


                val intentProceedOrderActivity = Intent(context!!, ProceedOrderActivity::class.java);
                intentProceedOrderActivity.putExtra("From_To",2);
                //todo: 1=> MyCart / 2=> BuyNow


                intentProceedOrderActivity.putParcelableArrayListExtra("productList",newSendingList);
                intentProceedOrderActivity.putExtra("total_price",(totalPrice*qty.toInt()))
                intentProceedOrderActivity.putExtra("total_discount",(discount*qty.toInt()))
                intentProceedOrderActivity.putExtra("total_amount",(totalAmount*qty.toInt()))
                startActivity(intentProceedOrderActivity)

            }

        }


        binding.layRating.rateNowBtn.setOnClickListener {
            val action = ProductFragmentDirections.actionProductFragmentToRateNowFragment(productId)
            findNavController().navigate(action)
        }

        binding.layRating.viewAllButton.setOnClickListener {
            val action = ProductFragmentDirections.actionProductFragmentToAllRatingFragment(productId)
            findNavController().navigate(action)
        }

    }




    private fun getFirebaseData(productId: String) = CoroutineScope(Dispatchers.IO).launch {
        val lay1 = binding.lay1
        val lay11 = binding.lay11
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

                    val priceOriginal = it.getLong("price_original")!!.toLong()
                    val priceSelling = it.getLong("price_selling")!!.toLong()

                    val avgRating = it.getString("rating_avg")!!
                    val sellerId = it.getString("PRODUCT_SELLER_ID")!!
                    val totalRating: Int = it.getLong("rating_total")!!.toInt()
                    val inStock = it.getBoolean("in_stock")!!
                    val stock = it.getLong("in_stock_quantity")!!
                    val description = it.getString("book_details")!!
                    val categoryList: ArrayList<String> = it.get("categories") as ArrayList<String>
                    val tagList: ArrayList<String> = it.get("tags") as ArrayList<String>
                    val url = it.get("product_thumbnail").toString().trim()

                    dbStockQty = stock.toInt()

//                    totalAmount = priceSelling.toInt()
//                    totalPrice = priceOriginal.toInt()
//                    discount = totalPrice - totalAmount
                    sendingList.add(CartModel(productId,sellerId,url,productName,priceOriginal,priceSelling,stock,1))


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

                    lay11.productName.text = productName

                    if (priceOriginal == 0L) {
                        lay11.productPrice.text = priceSelling.toString()
                        lay11.strikeThroughPrice.visibility = gone
                        lay11.percentOff.visibility = gone

                        totalAmount = priceSelling.toInt()
                        totalPrice = priceSelling.toInt()
                        discount = 0

                    } else {
                        val percent =
                            100 * (priceOriginal.toInt() - priceSelling.toInt()) / (priceOriginal.toInt())

                        lay11.productPrice.text = priceSelling.toString()
                        lay11.strikeThroughPrice.text = priceOriginal.toString()
                        lay11.percentOff.text = "${percent}% off"


                        totalAmount = priceSelling.toInt()
                        totalPrice = priceOriginal.toInt()
                        discount = totalPrice - totalAmount

                    }


                    lay11.productState.text = it.getString("book_state")!!
                    lay11.miniProductRating.text = avgRating
                    lay11.miniTotalNumberOfRatings.text = "(${totalRating} ratings)"

                    if (stock > 5) {
                        lay11.stockState.visibility = gone
                        lay11.stockQuantity.visibility = gone
                    } else if (stock in 1..5) {
                        lay11.stockState.text = "low"
                        lay11.stockQuantity.text = "only $stock available in stock"
                    }
                    else {
                        lay11.stockState.text = "out of stock"
                        lay11.stockQuantity.visibility = gone
                        binding.addToCartBtn.isEnabled = false
                        binding.addToCartBtn.backgroundTintList = ContextCompat.getColorStateList(context!!, R.color.gray_400)
                        binding.buyNowBtn.isEnabled = false
                        binding.buyNowBtn.backgroundTintList = ContextCompat.getColorStateList(context!!, R.color.gray_400)
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
            .orderBy("review_Date", Query.Direction.DESCENDING).limit(7)
            .get().addOnCompleteListener {

                if (it.isSuccessful) {
                    reviewList = it.result!!.toObjects(ProductReviewModel::class.java)
                    reviewAdapter.list = reviewList
                    reviewAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context!!, it.exception?.message, Toast.LENGTH_LONG).show()
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

                        for (item in fbCartList){
                            val productIdDB:String = item["product"] as String
                            if (productIdDB.contentEquals(productId)){
                                ALREADY_ADDED_TO_CART = true
                            }
                        }

                    }else{
                        Log.d("CartList","NoCart list found")
                    }
                }else{
                    Toast.makeText(context!!,"Failed cart", Toast.LENGTH_SHORT).show()
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
                                    ContextCompat.getColorStateList(context!!, R.color.red)
                                fabBtn.rippleColor =
                                    ContextCompat.getColor(context!!, R.color.gray_400)

                            }
                            index++

                        }

                    }else{
                        Log.d("WishList","No wish list found")
                    }
                }else{
                    Toast.makeText(context!!,"Failed", Toast.LENGTH_SHORT).show()
                }
            }.await()

    }


    private fun checkIsQuantityEntered(stockQty:Int):Boolean{
        val quantityString = enterQuantityInput.editText!!.text.toString().trim()
        return if (quantityString.isNotEmpty() && quantityString.toInt() != 0) {
            if(quantityString.toInt() > stockQty ){

                enterQuantityInput.isErrorEnabled = true
                enterQuantityInput.error = "Your entered Quantity exceeds Stock Quantity"
                false
            }else{

                enterQuantityInput.isErrorEnabled = false
                enterQuantityInput.error = null

                true

            }
        } else {
            enterQuantityInput.isErrorEnabled = true
            enterQuantityInput.error = "Field can't be empty"

            false



        }
    }




}