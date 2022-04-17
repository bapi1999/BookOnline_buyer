package com.sbdevs.bookonline.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.parseColor
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sbdevs.bookonline.models.HomeModel
import android.view.LayoutInflater
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.firestore.Query


import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.uiadapter.*
import com.sbdevs.bookonline.models.SearchModel
import com.sbdevs.bookonline.models.uidataclass.SliderModel
import com.sbdevs.bookonline.models.uidataclass.TopCategoryModel
import com.sbdevs.bookonline.othercalss.SharedDataClass
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


private const val SLIDER: Int = 0
private const val TOP_CATEGORY:Int = 1
private const val PRODUCT_HORIZONTAL:Int = 2
private const val STRIP_LAYOUT:Int = 3
private const val PROMOTED_LAYOUT:Int = 4
private const val BIG_ADS_LINK:Int = 5
private const val PRODUCT_GRID:Int = 6

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
        }
    }

    override fun getItemCount(): Int {
        return homeModelList.size
    }



    class SliderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val firebaseDatabase = SharedDataClass.database

        private val sliderView:ViewPager2 = itemView.findViewById(R.id.imageSliderNew)
        private var sliderModelList = ArrayList<SliderModel>()
        private val dotsIndicator = itemView.findViewById<DotsIndicator>(R.id.dots_indicator)
        lateinit var adapter: SliderAdapter

        fun bind(homeModel: HomeModel){

            val uiId:String = homeModel.ui_VIEW_ID.trim()
            sliderModelList.clear()
            val pos:String = absoluteAdapterPosition.toString()
            getSliderUi(uiId)

            adapter = SliderAdapter(sliderModelList)

            sliderView.adapter = adapter
            dotsIndicator.setViewPager2(sliderView)

            GlobalScope.launch(Dispatchers.Main) {

                while (true){
                    for (i in 0..sliderModelList.size ){
                        delay(3000)
                        if (i==0){
                            sliderView.setCurrentItem(i,false)
                        }else{
                            sliderView.setCurrentItem(i,true)
                        }
                    }
                }
            }


        }

        private fun getSliderUi(uiId:String )  {
            Log.e("SLIDER take of", " now")
            firebaseDatabase.child("Sliders").child(uiId).get()
                .addOnSuccessListener {

                    for (snapShot in it.children){
                        val element = snapShot.getValue(SliderModel::class.java)
                        if (element != null) {
                            sliderModelList.add(element)
                        }
                    }

                    adapter.picList = sliderModelList
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Log.e("Error in get home ui","${it.message}")
                }
        }

    }


    class CategoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val firebaseDatabase = SharedDataClass.database
        private val categoryRecycler:RecyclerView = itemView.findViewById(R.id.topCategoryRecycler)
        private var categoryList: ArrayList<TopCategoryModel> = ArrayList()
        private lateinit var categoryAdapter: TopCategoryAdapter
        fun bind(homeModel: HomeModel){

            categoryRecycler.layoutManager = GridLayoutManager(itemView.context,2)
            categoryAdapter = TopCategoryAdapter(categoryList)
            val uiId= homeModel.ui_VIEW_ID
            getTopCategoryUi(uiId)



        }



        private fun getTopCategoryUi(uiId:String) = CoroutineScope(Dispatchers.IO).launch {
            Log.e("CATEGORY take of", " now")
            withContext(Dispatchers.IO){

                firebaseDatabase.child("UI_TOP_4_CATEGORY").child(uiId).get()
                    .addOnSuccessListener {


                        for (snapShot in it.children){
                            val element = snapShot.getValue(TopCategoryModel::class.java)
                            if (element != null) {
                                categoryList.add(element)
                            }
                        }

                        categoryRecycler.adapter = categoryAdapter
                        categoryAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Log.e("Error in get Top Category ui","${it.message}")
                    }
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
        private var productList= ArrayList<SearchModel>()
        private lateinit var adapter1: ProductGridAdapter
        fun bind(homeModel: HomeModel){

            val uiId:String = homeModel.ui_VIEW_ID.trim()
            getGridQuery(uiId)

            productRecycler.layoutManager = GridLayoutManager(itemView.context,2)
            adapter1 = ProductGridAdapter(productList)
            productRecycler.adapter = adapter1
//            batchHeader.text = uiId


        }

        private fun getGridQuery(uiId:String) = CoroutineScope(Dispatchers.IO).launch {
            Log.e("GRID take of", " now")
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


            firebaseFirestore.collection("PRODUCTS")
                .orderBy(orderByString,direction).limit(4L)
                .get().addOnSuccessListener {
                    val allDocumentSnapshot = it.documents
                    for (documentSnapshot in allDocumentSnapshot) {
                        val productId = documentSnapshot.id
                        val productName = documentSnapshot.getString("book_title").toString()
                        val productImgList:ArrayList<String> = documentSnapshot.get("productImage_List") as ArrayList<String>
                        val stockQty: Long = documentSnapshot.getLong("in_stock_quantity")!!.toLong()
                        val avgRating = documentSnapshot.getString("rating_avg")!!
                        val totalRatings: Long = documentSnapshot.getLong("rating_total")!!
                        val priceOriginal = documentSnapshot.getLong("price_original")!!.toLong()
                        val priceSelling = documentSnapshot.getLong("price_selling")!!.toLong()
                        val printedYear = documentSnapshot.getLong("book_printed_ON")!!
                        val bookCondition = documentSnapshot.getString("book_condition").toString()
                        val bookType = documentSnapshot.getString("book_type")!!

                        productList.add(
                            SearchModel(productId, productName, productImgList, priceOriginal, priceSelling,
                                stockQty, avgRating, totalRatings, bookCondition, bookType, printedYear)
                        )
                    }


                    adapter1.list = productList
                adapter1.notifyDataSetChanged()

            }.addOnFailureListener {
                Log.e("HorizontalViewModel","${it.message}")
            }.await()

        }
    }



    class StripViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val firebaseDatabase = SharedDataClass.database
        private var stripImage:ImageView = itemView.findViewById(R.id.strip_image)
        private val firebaseFirestore = Firebase.firestore
        fun bind(homeModel: HomeModel){
            val uiId:String = homeModel.ui_VIEW_ID.trim()

            Log.e("STRIP take of", " now")
            firebaseDatabase.child("UI_STRIP_NORMAL").child(uiId).get()
                .addOnSuccessListener {
                    val background:String = it.child("Bg_color").value.toString()
                    val image:String = it.child("image").value.toString()
                    Glide.with(itemView.context).load(image)
                        .placeholder(R.drawable.as_banner_placeholder)
                        .into(stripImage)

                }.addOnFailureListener {
                    Log.e("Get Strip Normal error :","${it.message}")
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