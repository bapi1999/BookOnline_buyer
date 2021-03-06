package com.sbdevs.bookonline.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.facebook.ads.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.dynamiclinks.ktx.component1
import com.google.firebase.dynamiclinks.ktx.component2
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.user.CartActivity
import com.sbdevs.bookonline.activities.user.ProceedOrderActivity
import com.sbdevs.bookonline.activities.donation.AllDonationActivity
import com.sbdevs.bookonline.activities.java.SearchActivity2
import com.sbdevs.bookonline.activities.user.SellerShopActivity
import com.sbdevs.bookonline.adapters.ProductImgAdapter
import com.sbdevs.bookonline.adapters.ProductReviewAdapter
import com.sbdevs.bookonline.adapters.RecommendedProductAdapter
import com.sbdevs.bookonline.databinding.FragmentProductBinding
import com.sbdevs.bookonline.fragments.register.LoginDialogFragment
import com.sbdevs.bookonline.fragments.user.AllReviewFragment
import com.sbdevs.bookonline.models.user.CartModel
import com.sbdevs.bookonline.models.ProductReviewModel
import com.sbdevs.bookonline.models.SearchModel
import com.sbdevs.bookonline.othercalss.SharedDataClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ProductFragment : Fragment(), ProductImgAdapter.MyOnItemClickListener {
    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!
    private var firebaseFirestore = Firebase.firestore
    private lateinit var addToCartBtn: Button
    lateinit var buyNowBtn: Button
    lateinit var fabBtn: FloatingActionButton
    private val gone = View.GONE
    private val visible = View.VISIBLE
    private var reviewList: MutableList<ProductReviewModel> = ArrayList()
    private lateinit var reviewAdapter: ProductReviewAdapter
    private lateinit var productImgViewPager: ViewPager2
    private var recommendedList: MutableList<String> = ArrayList()

    lateinit var cartBadgeText: TextView

    private var wishList: ArrayList<String> = ArrayList()

    private var sendingList: ArrayList<CartModel> = ArrayList()
    private var totalPrice: Int = 0
    private var discount = 0
    private var totalAmount = 0
    var deliveryCharge = 0L

    private var productImgList: ArrayList<String> = ArrayList()

    private var ALREADY_ADDED_TO_WISHLIST: Boolean = false
    private var ALREADY_ADDED_TO_CART: Boolean = false
    private lateinit var productId: String
    private var wishListIndex = 0
    lateinit var enterQuantityInput: TextInputLayout

    var dbStockQty = 0
    private var loginDialog = LoginDialogFragment()
    private val loadingDialog = LoadingDialog()

    private var allSearchList: ArrayList<SearchModel> = ArrayList()
    private lateinit var recommendedProductAdapter: RecommendedProductAdapter
    private lateinit var recommendedRecycler: RecyclerView
    private var lastResult: DocumentSnapshot? = null
    private lateinit var times: Timestamp
    private var isRecyclerEnd: Boolean = false
    private var isScrollEnd: Boolean = false

    private var avgRating = ""
    private var totalRating = 0
    private var ratingNum: ArrayList<String> = ArrayList()
    private var sellerId = ""
    private var myCoins = 0L

    private var nativeAdLayout: NativeAdLayout? = null
    private var adView: LinearLayout? = null
    private var nativeAd: NativeAd? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)

        // Initialize the Audience Network SDK
        AudienceNetworkAds.initialize(requireContext())
        val adId = resources.getString(R.string.fb_native_ad)
        nativeAd =  NativeAd(requireContext(), adId)

        addToCartBtn = binding.lay3.addToCartBtn

        buyNowBtn = binding.lay3.buyNowBtn
        productImgViewPager = binding.lay1.productImgViewPager


//        productId ="2022_01_06T22_50_02_682LgDXu4pnoRfxNJgF8bQrhN8Faxt2"
//        productId ="5VtYiOejv8ZRaLjDJwCN"
        productId = arguments?.getString("productId").toString().trim()

        fabBtn = binding.lay1.floatingActionButton





        lifecycleScope.launch(Dispatchers.IO) {

            loadNativeAd()

            getProductData(productId)

            if (Firebase.auth.currentUser != null) {
                getWishList1()
                getCartList()
            }
            getReview(productId)

        }

        enterQuantityInput = binding.lay3.enterQuantityInput


        val layoutManager = LinearLayoutManager(requireContext())
        val reviewRecyclerView = binding.layRating.reviewRecycler
        reviewRecyclerView.layoutManager = layoutManager
        reviewAdapter = ProductReviewAdapter(reviewList)
        reviewRecyclerView.adapter = reviewAdapter
        cartBadgeText = binding.layCart.cartBadgeCounter

        val layoutManager2 =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recommendedRecycler = binding.lay7.recommendProductRecycler
        recommendedRecycler.layoutManager = layoutManager2
        recommendedProductAdapter = RecommendedProductAdapter(allSearchList)
        recommendedRecycler.adapter = recommendedProductAdapter


        val scrollView = binding.scrollView
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val view = scrollView.getChildAt(scrollView.childCount - 1)
            val diff =
                view.bottom + scrollView.paddingBottom - (scrollView.height + scrollView.scrollY)

            // if diff is zero, then the bottom has been reached

            // if diff is zero, then the bottom has been reached


            if (diff == 0) {

                if (isScrollEnd) {
                    Log.w("Query item", "Last item is reached already")
                    //Toast.makeText(requireContext()," already reached Bottom",Toast.LENGTH_LONG).show()

                } else {

//                    Toast.makeText(requireContext(),"Bottom reached",Toast.LENGTH_LONG).show()
                    isScrollEnd = true
                    binding.lay7.container.visibility = visible
                    binding.progressBar2.visibility = visible
                    getRecommendedProduct()
                }
                // do stuff

            }
        }





        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBtn2.setOnClickListener {
            requireActivity().finish()
        }

        binding.searchBtn.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity2::class.java)
            startActivity(intent)
        }

        binding.layCart.cartBadgeContainerLay.setOnClickListener {
            if (Firebase.auth.currentUser != null) {

                val cartIntent = Intent(requireContext(), CartActivity::class.java)
                startActivity(cartIntent)


            } else {
                loginDialog.show(childFragmentManager, "custom login dialog")

            }
        }

        binding.lay3.qtyPlusBtn.setOnClickListener {
            val qty =  binding.lay3.enterQuantityInput.editText?.text.toString().toInt()
            val newQty = qty+1
            binding.lay3.enterQuantityInput.editText?.setText("$newQty")
        }

        binding.lay3.qtyMinesBtn.setOnClickListener {
            val qty =  binding.lay3.enterQuantityInput.editText?.text.toString().toInt()
            val newQty = qty-1
            if (newQty<1){
                binding.lay3.enterQuantityInput.editText?.setText("1")
            }else{
                binding.lay3.enterQuantityInput.editText?.setText("$newQty")
            }
        }


        recommendedRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)


                if (!recyclerView.canScrollHorizontally(RecyclerView.FOCUS_RIGHT) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    // end scrolling: do what you want here and after calling the function change the value of boolean

                    if (isRecyclerEnd) {
                        Log.w("Query item", "Last item is reached already")

//                        Toast.makeText(requireContext()," already reached End",Toast.LENGTH_LONG).show()
                    } else {

                        Log.e("last query", "${lastResult.toString()}")
                        getRecommendedProduct()
                    }

                }

            }


        })


//        binding.lay51.gotoSellerShop.setOnClickListener {
//            val sellerIntent = Intent(requireContext(), SellerShopActivity::class.java)
//            sellerIntent.putExtra("sellerId", sellerId)
//            startActivity(sellerIntent)
//        }

        binding.donateBtn.setOnClickListener {
            val donationIntent = Intent(requireContext(), AllDonationActivity::class.java)
            startActivity(donationIntent)
        }


    }

    override fun onStart() {

        super.onStart()
        val cartNum = SharedDataClass.dbCartList
        if (Firebase.auth.currentUser != null) {
            if (cartNum.size == 0) {
                cartBadgeText.visibility = gone
            } else {
                cartBadgeText.text = cartNum.size.toString()
            }

        } else {
            cartBadgeText.visibility = gone
        }


        val args = Bundle()
        args.putString("productId", productId)


        fabBtn.setOnClickListener {
            val  currentUser= Firebase.auth.currentUser
            if (currentUser == null) {
                loginDialog.show(childFragmentManager, "custom login dialog")
            } else {
                if (!ALREADY_ADDED_TO_WISHLIST) {

                    wishList.add(productId)
                    SharedDataClass.dbWishList.add(productId)
                    val wishmap: MutableMap<String, Any> = HashMap()
                    wishmap["wish_list"] = wishList
                    firebaseFirestore.collection("USERS").document(currentUser.uid)
                        .collection("USER_DATA")
                        .document("MY_WISHLIST").update(wishmap)

                    ALREADY_ADDED_TO_WISHLIST = true
                    fabBtn.supportImageTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.red_a700)
                    fabBtn.rippleColor =
                        ContextCompat.getColor(requireContext(), R.color.grey_400)
                } else {
//                    Toast.makeText(requireContext(), "Removed from position $wishListIndex ", Toast.LENGTH_SHORT).show()

                    wishList.remove(productId)
                    SharedDataClass.dbWishList.remove(productId)
                    val cartmap: MutableMap<String, Any> = HashMap()
                    cartmap["wish_list"] = wishList
                    firebaseFirestore.collection("USERS").document(currentUser.uid)
                        .collection("USER_DATA")
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
            addToCartBtn.isEnabled = false
            val currentUser= Firebase.auth.currentUser
            if (currentUser == null) {
                loginDialog.show(childFragmentManager, "custom login dialog")
            } else {
                if (ALREADY_ADDED_TO_CART) {
                    Snackbar.make(it1, "Already added to cart", Snackbar.LENGTH_SHORT).show()
                    addToCartBtn.isEnabled = true

                } else {
                    val dbcart = SharedDataClass.dbCartList

                    if (dbcart.size == 12) {
                        Snackbar.make(it1, "Only 12 product can be added to cart", Snackbar.LENGTH_SHORT).show()
                        addToCartBtn.isEnabled = true

                    } else {

                        val listMap: MutableMap<String, Any> = HashMap()
                        listMap["product"] = productId
                        listMap["quantity"] = 1
                        dbcart.add(listMap)

                        val cartmap: MutableMap<String, Any> = HashMap()
                        cartmap["cart_list"] = dbcart

                        cartBadgeText.visibility = visible
                        cartBadgeText.text = dbcart.size.toString()

                        val snack = Snackbar.make(it1, "Successfully added to cart", Snackbar.LENGTH_SHORT)

                        firebaseFirestore.collection("USERS").document(currentUser.uid)
                            .collection("USER_DATA").document("MY_CART")
                            .update(cartmap).addOnSuccessListener {
                                snack.show()
                                ALREADY_ADDED_TO_CART = true
                                addToCartBtn.isEnabled = true

                            }.addOnFailureListener {
                                Log.e("AddToCart", "${it.message}", it.cause)
                            }
                    }


                }
            }


        }

        buyNowBtn.setOnClickListener {
            val currentUser= Firebase.auth.currentUser
            val quantityString = enterQuantityInput.editText!!.text.toString().trim()
            val qty: Long
            if (currentUser != null) {

                if (quantityString.isEmpty()) {
                    qty = 1L
                    sendingList[0].orderQuantity = qty
                } else {
                    if (!checkIsQuantityEntered(dbStockQty)) {
                        return@setOnClickListener
                    } else {
                        qty = quantityString.toLong()
                        sendingList[0].orderQuantity = qty
                    }

                }

                val intentProceedOrderActivity =
                    Intent(requireContext(), ProceedOrderActivity::class.java)
                intentProceedOrderActivity.putExtra("From_To", 2)
                //todo: 1=> MyCart / 2=> BuyNow
                intentProceedOrderActivity.putParcelableArrayListExtra("productList", sendingList)
                intentProceedOrderActivity.putExtra("total_price", (totalPrice * qty.toInt()))
                intentProceedOrderActivity.putExtra("total_discount", (discount * qty.toInt()))
                intentProceedOrderActivity.putExtra(
                    "total_amount",
                    ((totalAmount * qty) + (sendingList[0].deliveryCharge)).toInt()
                )
                startActivity(intentProceedOrderActivity)

            } else {
                loginDialog.show(childFragmentManager, "custom login dialog")
            }


        }




        binding.layRating.viewAllButton.setOnClickListener {

            val allRatingArgs = Bundle()
            allRatingArgs.putString("productId", productId)
            allRatingArgs.putInt("totalRating", totalRating)
            allRatingArgs.putString("avgRating", avgRating)
            allRatingArgs.putStringArrayList("ratingCount", ratingNum)

            val allRatingFragment = AllReviewFragment()
            allRatingFragment.arguments = allRatingArgs

            parentFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container, allRatingFragment)
                addToBackStack("all_retting")
            }

        }
        binding.lay1.fabShareButton.setOnClickListener {
            creatDynamickLink()
        }

    }

    override fun onResume() {
        super.onResume()


    }


    private fun loadNativeAd() {


        val nativeAdListener =object : NativeAdListener {
            override fun onError(p0: Ad?, p1: AdError?) {
                Log.e("Ads load error","$p1 \n message: ${p1!!.errorMessage}")
            }

            override fun onAdLoaded(p0: Ad?) {
                // Race condition, load() called again before last ad was displayed
                if (nativeAd == null || nativeAd != p0) {
                    return
                }
                // Inflate Native Ad into Container
                inflateAd(nativeAd!!)
            }

            override fun onAdClicked(p0: Ad?) {
                Log.d("Ads","Clicked")
            }

            override fun onLoggingImpression(p0: Ad?) {
                Log.d("Ads","Log Impression")
            }

            override fun onMediaDownloaded(p0: Ad?) {
                Log.d("Ads","MediaDownload")
            }


        }

        // Request an ad
        nativeAd!!.loadAd(
            nativeAd!!.buildLoadAdConfig()
                .withAdListener(nativeAdListener)
                .build())
    }

    private fun inflateAd(nativeAd: NativeAd) {
        nativeAd.unregisterView()

        nativeAdLayout = binding.nativeAdContainer
        val inflater = LayoutInflater.from(requireContext())
        adView = inflater.inflate(R.layout.ad_native_f_1, nativeAdLayout, false) as LinearLayout

        nativeAdLayout!!.addView(adView)

        // Add the AdOptionsView
        val adChoicesContainer:LinearLayout = adView!!.findViewById(R.id.ad_choices_container)
        val adOptionsView = AdOptionsView(requireContext(), nativeAd, nativeAdLayout)
        adChoicesContainer.removeAllViews()
        adChoicesContainer.addView(adOptionsView, 0)

        // Create native UI using the ad metadata.

        // Create native UI using the ad metadata.
        val nativeAdIcon: MediaView = adView!!.findViewById(R.id.native_ad_icon)
        val nativeAdTitle = adView!!.findViewById<TextView>(R.id.native_ad_title)
        val nativeAdMedia: MediaView = adView!!.findViewById(R.id.native_ad_media)
        val nativeAdSocialContext = adView!!.findViewById<TextView>(R.id.native_ad_social_context)
        val nativeAdBody = adView!!.findViewById<TextView>(R.id.native_ad_body)
//        val sponsoredLabel = adView!!.findViewById<TextView>(R.id.native_ad_sponsored_label)
        val nativeAdCallToAction = adView!!.findViewById<Button>(R.id.native_ad_call_to_action)

        // Set the Text.
        nativeAdTitle.text = nativeAd.advertiserName
        nativeAdBody.text = nativeAd.adBodyText
        nativeAdSocialContext.text = nativeAd.adSocialContext
        nativeAdCallToAction.visibility = if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction.text = nativeAd.adCallToAction
//        sponsoredLabel.text = nativeAd.sponsoredTranslation

        // Create a list of clickable views
        val clickableViews: MutableList<View> = ArrayList()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(adView, nativeAdMedia, nativeAdIcon,clickableViews)
    }



    private suspend fun getProductData(productId: String) {
        val lay2 = binding.lay2
        val lay5 = binding.lay5
        val lay6 = binding.lay6
        val layR = binding.layRating

        loadingDialog.show(childFragmentManager, "show")
        firebaseFirestore.collection("PRODUCTS").document(productId).get()
            .addOnSuccessListener {
                if (it.exists()) {

                    binding.scrollView.visibility = visible
                    binding.emptyProduct.visibility = gone


                    var categoryString = ""
                    var tagsString = ""

                    val productName = it.getString("book_title")!!

                    val priceOriginal = it.getLong("price_original")!!.toLong()
                    val priceSelling = it.getLong("price_selling")!!.toLong()

                    val sellerProfit = it.getString("SELLER_PROFIT")!!
                    avgRating = it.getString("rating_avg")!!
                    sellerId = it.getString("PRODUCT_SELLER_ID")!!
                    totalRating = it.getLong("rating_total")!!.toInt()
                    val stock = it.getLong("in_stock_quantity")!!
                    val description = it.getString("book_details")!!
                    val categoryList: ArrayList<String> = it.get("categories") as ArrayList<String>
                    val tagList: ArrayList<String> = it.get("tags") as ArrayList<String>

                    val bookWriter = it.getString("book_writer")
                    val bookPublisherName = it.getString("book_publisher")
                    val bookLanguage = it.getString("book_language")
                    val bookType = it.getString("book_type")!!
                    val bookPrintDate = it.getLong("book_printed_ON")
                    val bookCondition = it.getString("book_condition")
                    val bookPageCount = it.getString("book_pageCount")
                    val isbnNumber = it.getString("book_ISBN")
                    val bookDimension = it.getString("book_dimension")
                    val productReturn = it.getString("Replacement_policy")!!

                    productImgList = it.get("productImage_List") as ArrayList<String>
                    dbStockQty = stock.toInt()

                    if (productReturn == "No Replacement Policy") {
                        val imgview = binding.lay21.replacementPolicyImg
                        imgview.imageTintList =
                            AppCompatResources.getColorStateList(requireContext(), R.color.red_700)
                        Glide.with(requireContext()).load(R.drawable.ic_outline_cancel_24)
                            .into(imgview)
                    }
                    binding.lay21.replacementPolicyText.text = productReturn

                    val deliveryCharge1 = if (priceSelling >= 500) {
                        0L
                    } else {
                        40L
                    }

//                    getSellerName(sellerId)

                    sendingList.add(
                        CartModel(
                            productId,
                            sellerId,
                            productImgList[0],
                            productName,
                            priceOriginal,
                            priceSelling,
                            deliveryCharge1,
                            stock,
                            1,
                            productReturn,
                            sellerProfit
                        )
                    )

                    getMyCoin(priceSelling)



                    for (category in categoryList) {
                        categoryString += "$category,  "
                    }

                    for (tag in tagList) {
                        tagsString += "#$tag  "
                    }


                    val adapter = ProductImgAdapter(productImgList, this@ProductFragment)

                    productImgViewPager.adapter = adapter
                    binding.lay1.dotsIndicator.setViewPager2(productImgViewPager)

                    lay2.productName.text = productName

                    val queryList: List<String> = productName.lowercase().split(" ")

                    recommendedList.addAll(queryList)
                    recommendedList.remove("")

                    totalAmount = priceSelling.toInt()
                    totalPrice = priceSelling.toInt()

                    if (priceOriginal == 0L) {
                        lay2.productPrice.text = priceSelling.toString()
                        lay2.strikeThroughPrice.visibility = gone
                        lay2.percentOff.visibility = gone

                        discount = 0

                    } else {
                        val percent =
                            100 * (priceOriginal.toInt() - priceSelling.toInt()) / (priceOriginal.toInt())

                        lay2.productPrice.text = priceSelling.toString()
                        lay2.strikeThroughPrice.text = priceOriginal.toString()
                        lay2.percentOff.text = "${percent}% off"

                        discount = totalPrice - totalAmount
                    }


                    lay2.productState.text = bookType

                    lay2.miniProductRating.text = avgRating

                    lay2.miniTotalNumberOfRatings.text = "(${totalRating} ratings)"

                    when {
                        stock > 5 -> {
                            lay2.stockState.visibility = gone
                            lay2.stockQuantity.visibility = gone
                            lay2.stockState.text = "available"
                            lay2.stockState.setTextColor(AppCompatResources.getColorStateList(requireContext(),R.color.grey_600))
                        }
                        stock in 1..5 -> {
                            lay2.stockState.visibility = visible
                            lay2.stockQuantity.visibility = visible
                            lay2.stockState.text = "low"
                            lay2.stockQuantity.text = "only $stock available in stock"
                        }
                        stock == 0L -> {
                            lay2.stockState.text = "out of stock"
                            lay2.stockState.setTextColor(AppCompatResources.getColorStateList(requireContext(),R.color.red_500))
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
                    if (description.isEmpty()){
                        lay5.productDescriptionText.text = "No Description"
                    }else{
                        lay5.productDescriptionText.text = description
                    }


                    //todo layout 3
                    lay5.writerName.text = bookWriter
                    lay5.publisherName.text = bookPublisherName
                    lay5.bookLanguage.text = bookLanguage

                    if (bookPrintDate == 0L) {
                        lay5.printDate.text = "Not available"
                    } else {
                        lay5.printDate.text = bookPrintDate.toString()
                    }
                    when(bookType){
                        "new_printed"->{
                            lay5.bookType.text = "New Book"
                        }
                        "old_printed"->{
                            lay5.bookType.text = "New Book"
                        }
                        "used"->{
                            lay5.bookType.text = "Used Book"
                        }
                        "refurbished"->{
                            lay5.bookType.text = "Refurbished"
                        }
                    }

                    when(bookCondition){
                        "new_condition"->{
                            lay5.bookCondition.text = "Good"
                        }
                        "almost_new"->{
                            lay5.bookCondition.text = "Almost New"
                        }
                        "slightly_damaged"->{
                            lay5.bookCondition.text = "Slightly Damaged"
                        }
                        "fully_damaged"->{
                            lay5.bookCondition.text = "Fully Damaged"
                        }
                    }



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

                        val perccing: String = it.get("rating_Star_" + (5 - x)).toString()
                        ratingNum.add(perccing)
                        ratingtxt.text = perccing
                        val progressBar: ProgressBar =
                            layR.ratingBarContainter.getChildAt(x) as ProgressBar
                        val maxProgress: Int = totalRating
                        progressBar.max = maxProgress

                        val progress = Integer.valueOf(perccing)
                        progressBar.progress = progress
                    }

                    loadingDialog.dismiss()

                } else {
                    binding.scrollView.visibility = gone
                    binding.emptyProduct.visibility = visible
                    loadingDialog.dismiss()
                }
            }.addOnFailureListener {
                Log.e("Product", "${it.message}", it.cause)
                binding.scrollView.visibility = gone
                binding.emptyProduct.visibility = visible
                loadingDialog.dismiss()
            }.await()
    }



//todo this seller shop is being disabled to reduce database cost================================
//    private fun getSellerName(seller: String) {
//        firebaseFirestore.collection("USERS")
//            .document(seller)
//            .collection("SELLER_DATA")
//            .document("BUSINESS_DETAILS")
//            .get()
//            .addOnSuccessListener {
//                val sellName: String = it.getString("Business_name")!!
//                binding.lay51.sellerName.text = sellName
//            }.addOnFailureListener {
//                Log.e("Get seller name", "${it.message}", it.cause)
//            }
//    }



    private fun getReview(productID: String) {
        firebaseFirestore.collection("PRODUCTS").document(productID)
            .collection("PRODUCT_REVIEW")
            .orderBy("review_Date", Query.Direction.DESCENDING).limit(5)
            .get().addOnSuccessListener {
                reviewList = it.toObjects(ProductReviewModel::class.java)
                reviewAdapter.list = reviewList
                reviewAdapter.notifyDataSetChanged()


            }.addOnFailureListener {
                Log.e("Review", "${it.message}", it.cause)
            }

    }

    private fun getCartList() {

        for (item in SharedDataClass.dbCartList){
            val productIdDB: String = item["product"] as String

            if (productIdDB.contentEquals(productId)) {
                ALREADY_ADDED_TO_CART = true
            }
        }

    }


    private fun getWishList1() {

        wishList.addAll(SharedDataClass.dbWishList)

        for ((index: Int, ids: String) in SharedDataClass.dbWishList.withIndex()) {

            if (ids.contains(productId)) {
                wishListIndex = index
                ALREADY_ADDED_TO_WISHLIST = true
                fabBtn.supportImageTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.red_a700)
                fabBtn.rippleColor =
                    ContextCompat.getColor(requireContext(), R.color.grey_400)

            }
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
        val productImageFragment = ProductImageFragment()
        val args = Bundle()
        args.putStringArrayList("image_list", productImgList)

        productImageFragment.arguments = args



            parentFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container, productImageFragment)

                addToBackStack("imageView")
            }

    }

    private fun getRecommendedProduct() {
        //todo= generates from converting product name to array and query the array

        val searchList: ArrayList<SearchModel> = ArrayList()
        searchList.clear()

        val filterTask: Query = if (lastResult == null) {
            firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", recommendedList)
                .orderBy("price_selling", Query.Direction.ASCENDING)

        } else {
            firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", recommendedList)
                .orderBy("price_selling", Query.Direction.ASCENDING)
                .startAfter(lastResult)

        }

        filterTask.limit(5L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()) {

                isRecyclerEnd = allDocumentSnapshot.size < 5 // limit is 5

                for (documentSnapshot in allDocumentSnapshot) {
                    val productId = documentSnapshot.id
                    val productName = documentSnapshot.getString("book_title").toString()

                    val productImgList: MutableList<String> =
                        (documentSnapshot.get("productImage_List") as MutableList<String>?)!!

                    val stockQty: Long = documentSnapshot.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = documentSnapshot.getString("rating_avg")!!
                    val totalRatings: Long = documentSnapshot.getLong("rating_total")!!

                    val priceOriginal = documentSnapshot.getLong("price_original")!!.toLong()
                    val priceSelling = documentSnapshot.getLong("price_selling")!!.toLong()

                    val printedYear = documentSnapshot.getLong("book_printed_ON")!!
                    val bookCondition = documentSnapshot.getString("book_condition").toString()
                    val bookType = documentSnapshot.getString("book_type")!!

                    searchList.add(
                        SearchModel(
                            productId, productName, productImgList, priceOriginal, priceSelling,
                            stockQty, avgRating, totalRatings, bookCondition, bookType, printedYear
                        )
                    )
                }

                allSearchList.addAll(searchList)

                if (allSearchList.isEmpty()) {
                    binding.lay7.container.visibility = gone

                } else {
                    binding.lay7.container.visibility = visible

                    recommendedProductAdapter.list = allSearchList

                    if (lastResult == null) {

                        recommendedProductAdapter.notifyItemRangeInserted(0, searchList.size)
                    } else {
                        recommendedProductAdapter.notifyItemRangeInserted(
                            allSearchList.size - 1,
                            searchList.size
                        )
                    }


                }

                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!

            } else {
                isRecyclerEnd = true
                if (allSearchList.isEmpty()) {
                    binding.lay7.container.visibility = gone

                }
            }


            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone

        }.addOnFailureListener {
            Log.e("get search query 0", "${it.message}")
            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone
        }

    }

    private fun creatDynamickLink() {

        val sharelink2 = "https://www.example.com/?"+"productid=${productId}"

        Firebase.dynamicLinks.shortLinkAsync {

            link = Uri.parse(sharelink2)
            domainUriPrefix = "https://bookonline.page.link/?"
            androidParameters("com.sbdevs.bookonline") {
                minimumVersion = 1
            }
            iosParameters("com.example.ios") {
                appStoreId = "123456789"
                minimumVersion = "1.0.1"
            }
            socialMetaTagParameters {
                title = "Example of a Dynamic Link"
                description = "This link works whether the app is installed or not!"
                imageUrl = Uri.parse("https://cdn-icons-png.flaticon.com/512/2232/2232688.png")
            }

        }.addOnSuccessListener { (shortLink, flowchartLink) ->

            Log.e("Long Link:  ", "$shortLink")
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, shortLink.toString())
            intent.type = "text/plain"
            startActivity(intent)
        }.addOnFailureListener {

            Log.e("create dynamic link error", "${it.message}")
        }


    }

    private fun getMyCoin(priceSell:Long){
        val currentUser= Firebase.auth.currentUser
        if (currentUser != null){
            firebaseFirestore.collection("USERS")
                .document(currentUser.uid).get()
                .addOnSuccessListener {
                    myCoins = it.getLong("my_donation_coins")!!.toLong()
                    if (myCoins <= 0L){

                        binding.noDcContainer.visibility = visible
                        binding.haveDcContainer.visibility = gone

                    }else{
                        binding.noDcContainer.visibility = gone
                        binding.haveDcContainer.visibility = visible
                        var discount = 0
                        var discountPrice = 0
                        when {
                            priceSell>myCoins -> {
                                discountPrice = (priceSell - myCoins).toInt()
                                discount = ((myCoins *100)/priceSell).toInt()
                            }
                            priceSell<myCoins -> {
                                discountPrice = 0
                                discount = 100
                            }
                        }
                        binding.coinDisCountPrice.text = discountPrice.toString()
                        binding.realPrice.text = priceSell.toString()
                        binding.coinDisCount1.text = "$discount%"
                        binding.coinDisCount2.text = "$discount%"
                    }
                }
                .addOnFailureListener {
                    Log.e("Get MyCoin","${it.message}")
                    binding.payWithCoinBtn.visibility = gone
                }
        }
        else{
            binding.noDcContainer.visibility = visible
            binding.haveDcContainer.visibility = gone
        }



    }




}