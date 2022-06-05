package com.sbdevs.bookonline.adapters


import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.parseColor
import android.os.Handler
import android.util.Log
import android.util.TimingLogger
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.ProductActivity
import com.sbdevs.bookonline.activities.ViewAllProductActivity
import com.sbdevs.bookonline.activities.donation.AllDonationActivity
import com.sbdevs.bookonline.activities.java.SearchFilterJavaActivity
import com.sbdevs.bookonline.activities.user.SellerShopActivity
import com.sbdevs.bookonline.adapters.uiadapter.*
import com.sbdevs.bookonline.models.HomeModel
import com.sbdevs.bookonline.models.SearchModel
import com.sbdevs.bookonline.models.uidataclass.GridModel
import com.sbdevs.bookonline.models.uidataclass.SliderModel
import com.sbdevs.bookonline.models.uidataclass.TopCategoryModel
import com.sbdevs.bookonline.othercalss.HomeCacheClass
import com.sbdevs.bookonline.othercalss.MiddleDividerItemDecoration
import com.sbdevs.bookonline.othercalss.SharedDataClass
import com.sbdevs.bookonline.seller.activities.SlSplashActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList


private const val SLIDER: Int = 0
private const val TOP_CATEGORY:Int = 1
private const val PRODUCT_HORIZONTAL:Int = 2
private const val STRIP_LAYOUT:Int = 3
private const val PROMOTED_LAYOUT:Int = 4
private const val BIG_ADS_LINK:Int = 5
private const val PRODUCT_GRID:Int = 6
private const val NEW_ARRIVAL_GRID:Int = 7
private const val Tag = "HomeAdapter-"

class HomeAdapter(var homeModelList: MutableList<HomeModel>  ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return when (homeModelList[position].view_type) {
            0L -> SLIDER
            1L -> TOP_CATEGORY
            2L -> PRODUCT_HORIZONTAL
            3L -> STRIP_LAYOUT
            4L -> PROMOTED_LAYOUT
            5L-> BIG_ADS_LINK
            6L-> PRODUCT_GRID
            7L-> NEW_ARRIVAL_GRID

            else -> -5
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            SLIDER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slider_lay, parent, false)
                return SliderViewHolder(view)
            }
            TOP_CATEGORY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_top_category_lay, parent, false)
                return CategoryViewHolder(view)
            }
            PRODUCT_HORIZONTAL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_horizon_lay, parent, false)
                return HorizontalViewHolder(view)
            }
            STRIP_LAYOUT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.le_strip_layout, parent, false)
                return StripViewHolder(view)
            }
            PROMOTED_LAYOUT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_promoted_lay, parent, false)
                return PromotedViewHolder(view)
            }
            BIG_ADS_LINK -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_big_ads_layout, parent, false)
                return BigAdsLinkViewHolder(view)
            }
            PRODUCT_GRID -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_grid_lay, parent, false)
                return GridViewHolder(view)
            }
            NEW_ARRIVAL_GRID -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_grid_lay, parent, false)
                return NewArrivalViewHolder(view)
            }
            else->{
                val view = LayoutInflater.from(parent.context).inflate(R.layout.le_loading_progress_dialog, parent, false)
                return NillViewHolder(view)
            }
        }


    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)){
            SLIDER ->(holder as SliderViewHolder).bind(homeModelList[position])
            TOP_CATEGORY ->(holder as CategoryViewHolder).bind(homeModelList[position])
            PRODUCT_HORIZONTAL->(holder as HorizontalViewHolder).bind(homeModelList[position])

            STRIP_LAYOUT ->(holder as StripViewHolder).bind(homeModelList[position])
            PROMOTED_LAYOUT->(holder as PromotedViewHolder).bind(homeModelList[position])
            BIG_ADS_LINK->(holder as BigAdsLinkViewHolder).bind(homeModelList[position])
            PRODUCT_GRID->(holder as GridViewHolder).bind(homeModelList[position])
            NEW_ARRIVAL_GRID->(holder as NewArrivalViewHolder).bind(homeModelList[position])
        }
    }

    override fun getItemCount(): Int {
        return homeModelList.size
    }



    class SliderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val firebaseDatabase = SharedDataClass.database

        val imageSlider: ImageSlider = itemView.findViewById<ImageSlider>(R.id.image_slider)
        private var sliderModelList = ArrayList<SliderModel>()
        val imageList = ArrayList<SlideModel>()

        fun bind(homeModel: HomeModel){

            val uiId:String = homeModel.ui_VIEW_ID.trim()
            sliderModelList.clear()

            if(HomeCacheClass.isSliderImageAvailable){
                if (imageList.size == 0){

                    for (item in HomeCacheClass.sliderModelList){
                        imageList.add(SlideModel(item.image , ScaleTypes.CENTER_CROP))
                    }
                    imageSlider.setImageList(imageList)
                }else{
                    imageSlider.setImageList(imageList)
                }

                Log.e("slider list size","${imageList.size}")
                setClickInSlider(HomeCacheClass.sliderModelList)

            }else{
                getSliderUi(uiId)
            }


        }


        private fun getSliderUi(uiId:String )  {
            val resultList = ArrayList<SliderModel>()
            Log.e("SLIDER take of", " now")
            firebaseDatabase.child("Sliders").child(uiId).get()
                .addOnSuccessListener {

                    for (snapShot in it.children){
                        val element = snapShot.getValue(SliderModel::class.java)
                        if (element != null) {
                            resultList.add(element)
                            imageList.add(SlideModel(element.image , ScaleTypes.CENTER_CROP))
                        }
                    }

                    HomeCacheClass.sliderModelList.addAll(resultList)
                    HomeCacheClass.isSliderImageAvailable = true


                    imageSlider.setImageList(imageList)
                    setClickInSlider(resultList)

                }
                .addOnFailureListener {
                    Log.e("$Tag Error in get Slider","${it.message}")
                }
        }

        private fun setClickInSlider(itemList: ArrayList<SliderModel>){
            imageSlider.setItemClickListener(object : ItemClickListener {
                override fun onItemSelected(position: Int) {
                    val actionType = itemList[position].action_type
                    val actionString = itemList[position].action_string
                    Log.e("Action","$actionType")

                    when(actionType){
                        101L->{
                            val newIntent = Intent(itemView.context,ProductActivity::class.java)
                            newIntent.putExtra("productId",actionString)
                            itemView.context.startActivity(newIntent)
                        }
                        102L->{
                            val newIntent = Intent(itemView.context,SellerShopActivity::class.java)
                            newIntent.putExtra("sellerId",actionString)
                            itemView.context.startActivity(newIntent)
                        }
                        109L->{
                            val newIntent = Intent(itemView.context,SearchFilterJavaActivity::class.java)
                            newIntent.putExtra("query",actionString)
                            itemView.context.startActivity(newIntent)
                        }
                        111L->{
                            val newIntent = Intent(itemView.context,AllDonationActivity::class.java)
                            itemView.context.startActivity(newIntent)
                        }
                        122L->{
                            val newIntent = Intent(itemView.context,SlSplashActivity::class.java)
                            itemView.context.startActivity(newIntent)
                        }

                    }
                }
            })
        }




    }


    class CategoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val firebaseDatabase = SharedDataClass.database
        private val categoryRecycler:RecyclerView = itemView.findViewById(R.id.topCategoryRecycler)
        private var categoryList: ArrayList<TopCategoryModel> = ArrayList()
        private var categoryAdapter: TopCategoryAdapter = TopCategoryAdapter(categoryList)
        fun bind(homeModel: HomeModel){

            categoryRecycler.layoutManager = LinearLayoutManager(itemView.context,LinearLayoutManager.HORIZONTAL,false)

            categoryAdapter = TopCategoryAdapter(categoryList)

            val uiId= homeModel.ui_VIEW_ID
            if (HomeCacheClass.isCategoryAvailable){
                if (categoryList.size == 0){
                    categoryList.addAll(HomeCacheClass.categoryList)
                    Log.e("Category size ", "${categoryList.size}")
                }else{
                    categoryList = HomeCacheClass.categoryList
                }
                categoryAdapter.list = categoryList

            }else{
                getTopCategoryUi(uiId)
            }

            categoryRecycler.adapter = categoryAdapter


        }



        private fun getTopCategoryUi(uiId:String) {
            val resultList = ArrayList<TopCategoryModel>()
            Log.e("CATEGORY take of", " now")
            firebaseDatabase.child("UI_TOP_4_CATEGORY").child(uiId).get()
                .addOnSuccessListener {

                    for (snapShot in it.children){
                        val element = snapShot.getValue(TopCategoryModel::class.java)
                        if (element != null) {
                            resultList.add(element)
                        }
                    }

                    HomeCacheClass.isCategoryAvailable = true
                    HomeCacheClass.categoryList.addAll(resultList)

                    categoryList.addAll(resultList)
                    categoryAdapter.list = categoryList
                    categoryAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Log.e("$Tag Error in Category ui","${it.message}")
                }

        }
    }


    class HorizontalViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val productRecycler:RecyclerView = itemView.findViewById(R.id.product_horizontal_recycler)
        private val batchHeader:TextView = itemView.findViewById(R.id.batch_header)
        private val batchBackground:LinearLayout = itemView.findViewById(R.id.batch_background)
        private var productIdList= ArrayList<String>()
        private lateinit var adapter1: HorizontalAdapter
        fun bind(homeModel: HomeModel){

            val uiId:String = homeModel.ui_VIEW_ID.trim()
//            getFirebaeData(uiId)

            val pos:String = absoluteAdapterPosition.toString()




        }
        private fun getFirebaeData(uiId:String ) = CoroutineScope(Dispatchers.IO).launch {

            val firebaseFirestore = Firebase.firestore.collection("UI_PRODUCT_HORIZONTAL").document(uiId)
            firebaseFirestore.get().addOnSuccessListener {
                val header = it.getString("layout_title")
                val bgColor = it.getString("bg_color")?.trim()
                productIdList = it.get("products") as ArrayList<String>
                batchHeader.text = header
                batchBackground.setBackgroundColor(parseColor(bgColor))

                productRecycler.layoutManager = LinearLayoutManager(itemView.context,LinearLayoutManager.HORIZONTAL ,false)
                adapter1 = HorizontalAdapter(productIdList)
                productRecycler.adapter = adapter1

                adapter1.notifyDataSetChanged()

            }.addOnFailureListener {
                Log.e("HorizontalViewModel","${it.message}")
            }

        }
    }



    class GridViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val firebaseDatabase = SharedDataClass.database
        val firebaseFirestore = Firebase.firestore
        private val productRecycler:RecyclerView = itemView.findViewById(R.id.product_grid_recycler)
        private val batchHeader:TextView = itemView.findViewById(R.id.batch_header)
        private var productList= ArrayList<GridModel>()
        private lateinit var adapter1: ProductGridAdapter
        private val viewAllBtn:ImageView = itemView.findViewById(R.id.view_all_button)
        private val loadMoreBtn:Button = itemView.findViewById(R.id.load_more)

        fun bind(homeModel: HomeModel){

            val uiId:String = homeModel.ui_VIEW_ID.trim()
            getGridQuery(uiId)

            loadMoreBtn.visibility = View.GONE

            productRecycler.addItemDecoration(MiddleDividerItemDecoration(itemView.context, MiddleDividerItemDecoration.ALL))
            productRecycler.layoutManager = GridLayoutManager(itemView.context,2)
            adapter1 = ProductGridAdapter(productList)
            productRecycler.adapter = adapter1
//            batchHeader.text = uiId

            viewAllBtn.setOnClickListener {
                val viewAllIntent = Intent(itemView.context,ViewAllProductActivity::class.java)
                itemView.context.startActivity(viewAllIntent)
            }


        }

        private fun getGridQuery(uiId:String) = CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO){


                firebaseDatabase.child("UI_PRODUCT_GRID").child(uiId).get()
                    .addOnSuccessListener {

                        val queryOrderBy = it.child("query_orderBy").value.toString()
                        val queryDirection = it.child("query_direction").value.toString()
                        val title = it.child("batch_title").value.toString()
                        var direction = Query.Direction.DESCENDING

                        direction = if(queryDirection == "D"){
                            Query.Direction.DESCENDING
                        }else{
                            Query.Direction.ASCENDING
                        }

                        batchHeader.text = title
                        getFirebaeData(queryOrderBy,direction)
                    }
                    .addOnFailureListener {
                        Log.e("Error in get Grid ui","${it.message}")
                    }.await()
            }

        }


        private fun getFirebaeData(orderByString:String,direction:Query.Direction ) = CoroutineScope(Dispatchers.IO).launch {
            Log.e("GRID $orderByString take of", " now")
            firebaseFirestore.collection("PRODUCTS")
                .orderBy(orderByString,direction).limit(4L)
                .get().addOnSuccessListener {
                    val allDocumentSnapshot = it.documents
                    for (documentSnapshot in allDocumentSnapshot) {
                        val productId = documentSnapshot.id
                        val productName = documentSnapshot.getString("book_title").toString()
                        val productImgList:ArrayList<String> = documentSnapshot.get("productImage_List") as ArrayList<String>
                        val priceOriginal = documentSnapshot.getLong("price_original")!!.toLong()
                        val priceSelling = documentSnapshot.getLong("price_selling")!!.toLong()
                        val productAddedOn = documentSnapshot.getTimestamp("PRODUCT_ADDED_ON")!!

                        productList.add(
                            GridModel(productId, productName, productImgList, priceOriginal, priceSelling,productAddedOn.toDate())
                        )
                    }


                    adapter1.list = productList
                adapter1.notifyDataSetChanged()

            }.addOnFailureListener {
                Log.e("HorizontalViewModel","${it.message}")
            }.await()

        }
    }




    class NewArrivalViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val firebaseFirestore = Firebase.firestore
        private val productRecycler:RecyclerView = itemView.findViewById(R.id.product_grid_recycler)
        private val loadMoreBtn:Button = itemView.findViewById(R.id.load_more)
        private val batchHeader:TextView = itemView.findViewById(R.id.batch_header)
//        private var productList= ArrayList<GridModel>()
        private var adapter1: ProductGrid2Adapter =ProductGrid2Adapter(HomeCacheClass.newArrivalProductList)
        private val viewAllBtn:ImageView = itemView.findViewById(R.id.view_all_button)
        fun bind(homeModel: HomeModel){
            productRecycler.addItemDecoration(MiddleDividerItemDecoration(itemView.context, MiddleDividerItemDecoration.ALL))
            productRecycler.layoutManager = GridLayoutManager(itemView.context,2)

            if (HomeCacheClass.isNewArrivalExist){
                adapter1.list = HomeCacheClass.newArrivalProductList
            }else {
                getFirebaeData()
            }

            batchHeader.text = "New Arrival"

            productRecycler.adapter = adapter1

            loadMoreBtn.visibility = View.VISIBLE

            viewAllBtn.setOnClickListener {
                val viewAllIntent = Intent(itemView.context,ViewAllProductActivity::class.java)
                itemView.context.startActivity(viewAllIntent)
            }

            loadMoreBtn.setOnClickListener {
                if (HomeCacheClass.isNewArrivalReachLast){
                   Log.e("New arrival","already reach last")
                }else{
                    getFirebaeData()
                }
            }


        }


        private fun getFirebaeData( ) = CoroutineScope(Dispatchers.IO).launch {
            Log.e("NEW ARRIVAL take of", " now")

            var resultList:ArrayList<GridModel> = ArrayList()

            val query:Query = if(HomeCacheClass.lastResult == null){
                firebaseFirestore.collection("PRODUCTS")
                    .orderBy("PRODUCT_ADDED_ON",Query.Direction.DESCENDING)
            }else{
                firebaseFirestore.collection("PRODUCTS")
                    .orderBy("PRODUCT_ADDED_ON",Query.Direction.DESCENDING)
                    .startAfter()
            }
                query.limit(16).get().addOnSuccessListener {
                    val allDocumentSnapshot = it.documents

                    for (documentSnapshot in allDocumentSnapshot) {

                        val productId = documentSnapshot.id
                        val productName = documentSnapshot.getString("book_title").toString()
                        val productImgList:ArrayList<String> = documentSnapshot.get("productImage_List") as ArrayList<String>
                        val priceOriginal = documentSnapshot.getLong("price_original")!!.toLong()
                        val priceSelling = documentSnapshot.getLong("price_selling")!!.toLong()
                        val productAddedOn = documentSnapshot.getTimestamp("PRODUCT_ADDED_ON")!!
                        resultList.add(GridModel(productId, productName, productImgList, priceOriginal, priceSelling,productAddedOn.toDate()))

                    }
                    HomeCacheClass.isNewArrivalReachLast = allDocumentSnapshot.size < 16 // limit is 10

                    HomeCacheClass.newArrivalProductList.addAll(resultList)
                    HomeCacheClass.isNewArrivalExist = true
                    adapter1.list = HomeCacheClass.newArrivalProductList

                    if (resultList == null ){
                        adapter1.notifyItemRangeInserted(0,resultList.size)
                    }else{
                        adapter1.notifyItemRangeInserted(HomeCacheClass.newArrivalProductList.size-1,resultList.size)
                    }


                    if (allDocumentSnapshot.isNotEmpty()){
                        val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                        HomeCacheClass.lastResult = lastR
                        HomeCacheClass.times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!
                    }


            }.addOnFailureListener {
                Log.e("HorizontalViewModel","${it.message}")
            }.await()

        }
    }



    class StripViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val firebaseDatabase = SharedDataClass.database
        private var stripImage:ImageView = itemView.findViewById(R.id.strip_image)

        fun bind(homeModel: HomeModel){
            val uiId:String = homeModel.ui_VIEW_ID.trim()

            var actionString =""
            var name =""
            var actionType =0L


            Log.e("STRIP take of", " now")
            firebaseDatabase.child("UI_STRIP_NORMAL").child(uiId).get()
                .addOnSuccessListener {
                    actionType = it.child("action_type").value as Long
                    val image:String = it.child("image").value.toString()
                     name = it.child("name").value.toString()
                    actionString = it.child("action_string").value.toString()
                    Glide.with(itemView.context).load(image)
                        .placeholder(R.drawable.as_banner_placeholder)
                        .into(stripImage)

                }.addOnFailureListener {
                    Log.e("Get Strip Normal error :","${it.message}")
                }

            itemView.setOnClickListener {
                if (actionType == 109L){
                    val splitList= Arrays.asList<String>(
                        *actionString.lowercase(Locale.getDefault()).split(",").toTypedArray()
                    )
                    val queryList:ArrayList<String> = ArrayList()
                    queryList.addAll(splitList)
                    val newIntent = Intent(itemView.context, SearchFilterJavaActivity::class.java)
                    newIntent.putStringArrayListExtra("queryList",queryList)
                    newIntent.putExtra("from","ActionString")
                    newIntent.putExtra("queryTitle",name)
                    itemView.context.startActivity(newIntent)
                }

            }





        }
    }


    class PromotedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val productRecycler:RecyclerView = itemView.findViewById(R.id.promoted_recyclerView)
        private var productIdList= ArrayList<String>()
        private lateinit var adapter1: PromotedAdapter
        private val firebaseFirestore = Firebase.firestore
        private var viewAllBtn:Button = itemView.findViewById(R.id.viewAllBtn)
        fun bind(homeModel: HomeModel){
            val uiId:String = homeModel.ui_VIEW_ID.trim()
//            getFirebaeData(uiId)

            viewAllBtn.setOnClickListener {
                Toast.makeText(itemView.context,"View all",Toast.LENGTH_SHORT).show()
            }
        }

        private fun getFirebaeData(uiId:String ) = CoroutineScope(Dispatchers.IO).launch {


            firebaseFirestore.collection("UI_PROMOTED_PRODUCT").document(uiId)
                .get().addOnSuccessListener {
                    val description = it.getString("description")
                    val btnColor = it.getString("button_color")?.trim()
                    productIdList = it.get("products") as ArrayList<String>

                    viewAllBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor(btnColor))

                    productRecycler.layoutManager = LinearLayoutManager(itemView.context,LinearLayoutManager.HORIZONTAL ,false)
                    adapter1 = PromotedAdapter(productIdList)
                    productRecycler.adapter = adapter1

                    adapter1.notifyDataSetChanged()

            }.addOnFailureListener {
                    Log.e("PromotedViewModel","${it.message}")
                }

        }
    }


    class BigAdsLinkViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val firebaseDatabase = SharedDataClass.database
        private var adsImage:ImageView = itemView.findViewById(R.id.bigads_image)
        private val adTitleTxt:TextView =itemView.findViewById(R.id.ad_title)
        private val adDescriptionTxt:TextView =itemView.findViewById(R.id.ad_description)
        private val adProviderTxt:TextView =itemView.findViewById(R.id.ad_provider_name)


        fun bind(homeModel: HomeModel){
            val uiId:String = homeModel.ui_VIEW_ID.trim()

            Log.e("BIG ADS take of", " now")

            firebaseDatabase.child("UI_BIG_ADS").child(uiId).get()
                .addOnSuccessListener {

                    val url = it.child("image").value.toString()
                    val addTitle = it.child("ad_title").value.toString()
                    val adDescription = it.child("ad_description").value.toString()
                    val provider = it.child("ad_provider").value.toString()

                    Glide.with(itemView.context).load(url).placeholder(R.drawable.as_banner_placeholder).into(adsImage)
                    adTitleTxt.text = addTitle
                    adDescriptionTxt.text = adDescription
                    adProviderTxt.text = provider

                }.addOnFailureListener {
                    Log.e("Get Strip Normal error :","${it.message}")
                }

        }
    }

    class NillViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val container:ConstraintLayout = itemView.findViewById(R.id.container)
        init {
            container.visibility = View.GONE
        }
    }

}