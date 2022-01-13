package com.sbdevs.bookonline.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.ProceedOrderActivity
import com.sbdevs.bookonline.activities.SearchActivity
import com.sbdevs.bookonline.adapters.ProductImgAdapter
import com.sbdevs.bookonline.adapters.ProductReviewAdapter
import com.sbdevs.bookonline.databinding.FragmentProductBinding
import com.sbdevs.bookonline.models.CartModel
import com.sbdevs.bookonline.models.ProductReviewModel
import com.sbdevs.bookonline.othercalss.SharedDataClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ProductFragment : Fragment(),ProductImgAdapter.MyOnItemClickListener {
    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!


    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val user = firebaseAuth.currentUser

    lateinit var addToCartBtn: LinearLayout
    lateinit var buyNowBtn: Button

    lateinit var fabBtn: FloatingActionButton
    private val gone = View.GONE
    private val visible = View.VISIBLE
    private var reviewList: List<ProductReviewModel> = ArrayList()
    private lateinit var reviewAdapter: ProductReviewAdapter

    private lateinit var productImgViewPager: ViewPager2


    private var cartList: ArrayList<MutableMap<String, Any>> = ArrayList()
    lateinit var cartBadgeText: TextView
    private var fbCartList: ArrayList<MutableMap<String, Any>> = ArrayList()

    private var wishList: ArrayList<String> = ArrayList()
    private var fbWishList: ArrayList<String> = ArrayList()

    private var sendingList: ArrayList<CartModel> = ArrayList()
    private var totalPrice: Int = 0
    private var discount = 0
    private var totalAmount = 0

    private var productImgList: ArrayList<String> = ArrayList()
    private var ALREADY_ADDED_TO_WISHLIST: Boolean = false
    private var ALREADY_ADDED_TO_CART: Boolean = false
    private lateinit var productId: String
    private var wishListIndex = 0
    lateinit var enterQuantityInput: TextInputLayout

    var dbStockQty = 0
    private var loginDialog = LoginDialogFragment()
    private val loadingDialog = LoadingDialog()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)





        addToCartBtn = binding.lay3.addToCartBtn

        buyNowBtn = binding.lay3.buyNowBtn
        productImgViewPager = binding.lay1.productImgViewPager


        loadingDialog.show(childFragmentManager,"Show");

        val intent = requireActivity().intent
        productId = intent.getStringExtra("productId").toString().trim()

        fabBtn = binding.lay1.floatingActionButton





        lifecycleScope.launch(Dispatchers.IO) {


            getProductData(productId)
            if (user!=null){
                getWishList()
                getCartList()
            }
            getReview(productId)

        }

        enterQuantityInput = binding.lay3.enterQuantityInput


        val layoutManager = LinearLayoutManager(context)
        val reviewRecyclerView = binding.layRating.reviewRecycler
        reviewRecyclerView.layoutManager = layoutManager

        reviewAdapter = ProductReviewAdapter(reviewList)
        reviewRecyclerView.adapter = reviewAdapter

        cartBadgeText = binding.layCart.cartBadgeCounter

        val scrollView = binding.scrollView
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val view = scrollView.getChildAt(scrollView.childCount - 1)
            val diff =
                view.bottom + scrollView.paddingBottom - (scrollView.height + scrollView.scrollY)

            // if diff is zero, then the bottom has been reached

            // if diff is zero, then the bottom has been reached
            if (diff == 0) {
                // do stuff
                Toast.makeText(requireContext(),"Bottom reached",Toast.LENGTH_LONG).show()
            }
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dataClass = SharedDataClass()
        cartBadgeText.text = dataClass.cartNumber.toString()
        //dataClass.getCartListForOptionMenu(cartBadgeText)

        binding.searchBtn.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }

        binding.layCart.cartBadgeContainerLay.setOnClickListener {
            if (user != null){
                val action = ProductFragmentDirections.actionProductFragmentToMyCartFragment2()
                findNavController().navigate(action)
            }else{
                loginDialog.show(childFragmentManager, "custom login dialog")

            }
        }

    }

    override fun onStart() {
        super.onStart()





        fabBtn.setOnClickListener {

            if (user == null){
                loginDialog.show(childFragmentManager, "custom login dialog")
            }else{
                if (!ALREADY_ADDED_TO_WISHLIST) {

                    wishList.add(productId)
                    val wishmap: MutableMap<String, Any> = HashMap()
                    wishmap["wish_list"] = wishList
                    firebaseFirestore.collection("USERS").document(user.uid).collection("USER_DATA")
                        .document("MY_WISHLIST").update(wishmap)

                    ALREADY_ADDED_TO_WISHLIST = true
                    fabBtn.supportImageTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.red_a700)
                    fabBtn.rippleColor =
                        ContextCompat.getColor(requireContext(), R.color.grey_400)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Removed from position $wishListIndex ",
                        Toast.LENGTH_SHORT
                    ).show()

                    wishList.removeAt(wishListIndex)
                    val cartmap: MutableMap<String, Any> = HashMap()
                    cartmap["wish_list"] = wishList
                    firebaseFirestore.collection("USERS").document(user.uid).collection("USER_DATA")
                        .document("MY_WISHLIST").update(cartmap)

                    ALREADY_ADDED_TO_WISHLIST = false
                    fabBtn.supportImageTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.grey_400)
                    fabBtn.rippleColor =
                        ContextCompat.getColor(requireContext(), R.color.red_a700)
                }

            }


        }


        addToCartBtn.setOnClickListener { it1 ->
            if (user == null) {
                loginDialog.show(childFragmentManager, "custom login dialog")
            } else {
                if (ALREADY_ADDED_TO_CART) {
                    Snackbar.make(it1, "Already added to cart", Snackbar.LENGTH_SHORT).show()
                } else {
                    val listMap: MutableMap<String, Any> = HashMap()
                    listMap["product"] = productId
                    listMap["quantity"] = 1
                    cartList.add(listMap)
                    val cartmap: MutableMap<String, Any> = HashMap()
                    cartmap["cart_list"] = cartList
                    val snack = Snackbar.make(it1, "Successfully added to cart", Snackbar.LENGTH_SHORT)
                    firebaseFirestore.collection("USERS").document(user.uid).collection("USER_DATA")
                        .document("MY_CART").update(cartmap).addOnSuccessListener {
                            snack.show()

                        }.addOnFailureListener{
                            Log.e("AddToCart","${it.message}",it.cause)
                        }
                }
            }


        }

        buyNowBtn.setOnClickListener {

            val quantityString = enterQuantityInput.editText!!.text.toString().trim()
            if (user != null){

                if (quantityString.isEmpty()){
                    val newSendingList: ArrayList<CartModel> = ArrayList()
                    newSendingList.add(
                        CartModel(
                            productId,
                            sendingList[0].sellerId,
                            sendingList[0].url,
                            sendingList[0].title,
                            sendingList[0].priceOriginal,
                            sendingList[0].priceSelling,
                            sendingList[0].stockQty,
                            1
                        )
                    )


                    val intentProceedOrderActivity =
                        Intent(requireContext(), ProceedOrderActivity::class.java);
                    intentProceedOrderActivity.putExtra("From_To", 2);
                    //todo: 1=> MyCart / 2=> BuyNow


                    intentProceedOrderActivity.putParcelableArrayListExtra(
                        "productList", newSendingList)
                    intentProceedOrderActivity.putExtra("total_price", (totalPrice *1))
                    intentProceedOrderActivity.putExtra("total_discount", (discount *1))
                    intentProceedOrderActivity.putExtra("total_amount", (totalAmount *1))
                    startActivity(intentProceedOrderActivity)
                }else{

                    if (!checkIsQuantityEntered(dbStockQty)) {
                        return@setOnClickListener

                    }
                    else {
                        val qty =quantityString.toLong()
                        val newSendingList: ArrayList<CartModel> = ArrayList()
                        newSendingList.add(
                            CartModel(
                                productId,
                                sendingList[0].sellerId,
                                sendingList[0].url,
                                sendingList[0].title,
                                sendingList[0].priceOriginal,
                                sendingList[0].priceSelling,
                                sendingList[0].stockQty,
                                qty
                            )
                        )


                        val intentProceedOrderActivity =
                            Intent(requireContext(), ProceedOrderActivity::class.java);
                        intentProceedOrderActivity.putExtra("From_To", 2);
                        //todo: 1=> MyCart / 2=> BuyNow


                        intentProceedOrderActivity.putParcelableArrayListExtra(
                            "productList", newSendingList)
                        intentProceedOrderActivity.putExtra("total_price", (totalPrice * qty.toInt()))
                        intentProceedOrderActivity.putExtra("total_discount", (discount * qty.toInt()))
                        intentProceedOrderActivity.putExtra("total_amount", (totalAmount * qty.toInt()))
                        startActivity(intentProceedOrderActivity)

                    }
                }


            }else{
                loginDialog.show(childFragmentManager, "custom login dialog")
            }



        }


        binding.layRating.rateNowBtn.setOnClickListener {
            if (user !=null){
                val action = ProductFragmentDirections.actionProductFragmentToRateNowFragment(productId)
                findNavController().navigate(action)
            }else{
                loginDialog.show(childFragmentManager, "custom login dialog")
            }

        }

        binding.layRating.viewAllButton.setOnClickListener {
            val action =
                ProductFragmentDirections.actionProductFragmentToAllRatingFragment(productId)
            findNavController().navigate(action)
        }

    }


    private suspend fun getProductData(productId: String) {
        val lay2 = binding.lay2
        val lay4 = binding.lay4
        val lay5 = binding.lay5
        val lay6 = binding.lay6
        val layR = binding.layRating
        firebaseFirestore.collection("PRODUCTS").document(productId).get()
            .addOnSuccessListener {
                if (it.exists()) {

                    var categoryString = ""
                    var tagsString = ""

                    val productName = it.getString("book_title")!!

                    val priceOriginal = it.getLong("price_original")!!.toLong()
                    val priceSelling = it.getLong("price_selling")!!.toLong()

                    val avgRating = it.getString("rating_avg")!!
                    val sellerId = it.getString("PRODUCT_SELLER_ID")!!
                    val totalRating: Int = it.getLong("rating_total")!!.toInt()
                    val stock = it.getLong("in_stock_quantity")!!
                    val description = it.getString("book_details")!!
                    val categoryList: ArrayList<String> = it.get("categories") as ArrayList<String>
                    val tagList: ArrayList<String> = it.get("tags") as ArrayList<String>
                    val url = it.get("product_thumbnail").toString().trim()

                    val bookWriter = it.getString("book_writer")
                    val bookPublisherName = it.getString("book_publisher")
                    val bookLanguage = it.getString("book_language")
                    val bookType = it.getString("book_type")!!
                    val bookPrintDate = it.getLong("book_printed_ON")
                    val bookCondition = it.getString("book_condition")
                    val bookPageCount = it.getString("book_pageCount")
                    val isbnNumber = it.getString("book_ISBN")
                    val bookDimension = it.getString("book_dimension")

                    dbStockQty = stock.toInt()

                    sendingList.add(
                        CartModel(
                            productId,
                            sellerId,
                            url,
                            productName,
                            priceOriginal,
                            priceSelling,
                            stock,
                            1
                        )
                    )


                    for (catrgorys in categoryList) {
                        categoryString += "$catrgorys,  "
                    }

                    for (tag in tagList) {
                        tagsString += "#$tag  "
                    }

                    productImgList = it.get("productImage_List") as ArrayList<String>

                    val adapter = ProductImgAdapter(productImgList,this@ProductFragment)

                    productImgViewPager.adapter = adapter
                    binding.lay1.dotsIndicator.setViewPager2(productImgViewPager)

                    lay2.productName.text = productName


                    if (priceOriginal == 0L) {
                        lay2.productPrice.text = priceSelling.toString()
                        lay2.strikeThroughPrice.visibility = gone
                        lay2.percentOff.visibility = gone

                        totalAmount = priceSelling.toInt()
                        totalPrice = priceSelling.toInt()
                        discount = 0

                    } else {
                        val percent =
                            100 * (priceOriginal.toInt() - priceSelling.toInt()) / (priceOriginal.toInt())

                        lay2.productPrice.text = priceSelling.toString()
                        lay2.strikeThroughPrice.text = priceOriginal.toString()
                        lay2.percentOff.text = "${percent}% off"


                        totalAmount = priceSelling.toInt()
                        totalPrice = priceOriginal.toInt()
                        discount = totalPrice - totalAmount

                    }


                    lay2.productState.text = bookType

                    lay2.miniProductRating.text = avgRating

                    lay2.miniTotalNumberOfRatings.text = "(${totalRating} ratings)"

                    when {
                        stock > 5 -> {
                            lay2.stockState.visibility = gone
                            lay2.stockQuantity.visibility = gone
                        }
                        stock in 1..5 -> {
                            lay2.stockState.visibility = visible
                            lay2.stockQuantity.visibility = visible
                            lay2.stockState.text = "low"
                            lay2.stockQuantity.text = "only $stock available in stock"
                        }
                        stock == 0L ->{
                            lay2.stockState.text = "out of stock"
                            lay2.stockQuantity.visibility = gone
                            addToCartBtn.isEnabled = false
                            addToCartBtn.backgroundTintList =
                                ContextCompat.getColorStateList(requireContext(), R.color.grey_400)
                            buyNowBtn.isEnabled = false
                            buyNowBtn.backgroundTintList =
                                ContextCompat.getColorStateList(requireContext(), R.color.grey_400)
                        }
                    }

// todo layout 2

                    lay4.productDetailsText.text = description

                    //todo layout 3
                    lay5.writerName.text = bookWriter
                    lay5.publisherName.text = bookPublisherName
                    lay5.bookLanguage.text = bookLanguage

                    if (bookPrintDate == 0L) {
                        lay5.printDate.text = "Not available"
                    } else {
                        lay5.printDate.text = bookPrintDate.toString()
                    }
                    lay5.bookType.text = bookType
                    lay5.bookCondition.text =bookCondition
                    lay5.pageCount.text = bookPageCount
                    lay5.isbnNumber.text = isbnNumber
                    lay5.bookDimension.text = bookDimension

                    //todo layout 4
                    lay6.categoryText.text = categoryString
                    lay6.tagsText.text = tagsString

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

                    loadingDialog.dismiss()

                }
            }.addOnFailureListener {
                Log.e("Product","${it.message}",it.cause)
            }.await()
    }

    private fun getReview(productID: String) {
        firebaseFirestore.collection("PRODUCTS").document(productID)
            .collection("PRODUCT_REVIEW")
            .orderBy("review_Date", Query.Direction.DESCENDING).limit(5)
            .get().addOnSuccessListener {
                reviewList = it.toObjects(ProductReviewModel::class.java)
                reviewAdapter.list = reviewList
                reviewAdapter.notifyDataSetChanged()


            }.addOnFailureListener{
                Log.e("Review","${it.message}",it.cause)
            }

    }

    private fun getCartList() {

        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_CART").get().addOnSuccessListener {

                val x = it.get("cart_list")

                if (x != null) {
                    fbCartList = x as ArrayList<MutableMap<String, Any>>
                    cartList.addAll(fbCartList)

                    for (item in fbCartList) {
                        val productIdDB: String = item["product"] as String
                        if (productIdDB.contentEquals(productId)) {
                            ALREADY_ADDED_TO_CART = true
                        }
                    }

                } else {
                    Log.w("CartList", "Cart list not found")
                }

            }.addOnFailureListener {
                Log.e("CartList", "${it.message}",it.cause)
            }

    }


    private fun getWishList(){


        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_WISHLIST").get().addOnSuccessListener {

                val x = it.get("wish_list")

                if (x != null) {
                    fbWishList = x as ArrayList<String>

                    wishList.addAll(fbWishList)

                    for ((index, ids: String) in fbWishList.withIndex()) {

                        if (ids.contains(productId)) {
                            wishListIndex = index
                            ALREADY_ADDED_TO_WISHLIST = true
                            fabBtn.supportImageTintList =
                                AppCompatResources.getColorStateList(requireContext(), R.color.red_a700)
                            fabBtn.rippleColor =
                                ContextCompat.getColor(requireContext(), R.color.grey_400)

                        }
                    }

                } else {
                    Log.w("WishList", "No wish list found")
                }


            }.addOnFailureListener{
                Log.e("WishList", "${it.message}",it.cause)
            }

    }


    private fun checkIsQuantityEntered(stockQty: Int): Boolean {
        val quantityString = enterQuantityInput.editText!!.text.toString().trim()
        return if (quantityString.toInt() != 0) {

            if (quantityString.toInt() > stockQty) {

                enterQuantityInput.isErrorEnabled = true
                enterQuantityInput.error = "Your entered Quantity exceeds Stock Quantity"
                false
            } else {

                enterQuantityInput.isErrorEnabled = false
                enterQuantityInput.error = null

                true

            }
        } else {
            enterQuantityInput.isErrorEnabled = false
            enterQuantityInput.error = "Quantity mustn't be 0"

            false


        }
    }

    override fun onItemClick(position: Int, url: String) {
        val action = ProductFragmentDirections.actionProductFragmentToProductImageFragment(url)
        findNavController().navigate(action)
    }

    private fun getRecommendedProduct(){
        //todo= generates from converting product name to array and query the array

    }

    private fun getSimilarProduct(){
        //todo= generates from user search result

    }


}