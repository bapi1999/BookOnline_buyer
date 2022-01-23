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
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.sbdevs.bookonline.adapters.uiadapter.HorizontalAdapter


import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.uiadapter.PromotedAdapter
import com.sbdevs.bookonline.adapters.uiadapter.SliderAdapter
import com.sbdevs.bookonline.adapters.uiadapter.TopCategoryAdapter
import com.sbdevs.bookonline.models.uidataclass.SliderModel
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Runnable
import java.util.logging.Handler
import kotlin.math.abs


private const val SLIDER: Int = 0
private const val TOP_CATEGORY:Int = 1
private const val PRODUCT_HORIZONTAL:Int = 2
private const val STRIP_LAYOUT:Int = 3
private const val PROMOTED_LAYOUT:Int = 4
private const val BIG_ADS_LINK:Int = 5
private const val PRODUCT_GRID:Int = 6

class HomeAdapter(var homeModelList: List<HomeModel>  ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return when (homeModelList[position].view_type) {
            0L -> SLIDER
            1L -> TOP_CATEGORY
            2L -> PRODUCT_HORIZONTAL
            3L -> STRIP_LAYOUT
            4L -> PROMOTED_LAYOUT
            5L-> BIG_ADS_LINK
            6L-> PRODUCT_GRID

            else -> -1
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            SLIDER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.le_slider_layout, parent, false)
                return SliderViewHolder(view)
            }
            TOP_CATEGORY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.le_top_category_layout, parent, false)
                return CategoryViewHolder(view)
            }
            PRODUCT_HORIZONTAL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.le_product_horizontal_layout, parent, false)
                return HorizontalViewHolder(view)
            }
            STRIP_LAYOUT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.le_strip_layout, parent, false)
                return StripViewHolder(view)
            }
            PROMOTED_LAYOUT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.le_promoted_layout, parent, false)
                return PromotedViewHolder(view)
            }
            BIG_ADS_LINK -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.le_big_ads_layout, parent, false)
                return BigAdsLinkViewHolder(view)
            }
            PRODUCT_GRID -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.le_product_grid_layout, parent, false)
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
            PRODUCT_GRID->(holder as BigAdsLinkViewHolder).bind(homeModelList[position])
        }
    }

    override fun getItemCount(): Int {
        return homeModelList.size
    }

    class SliderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val sliderView:ViewPager2 = itemView.findViewById(R.id.imageSliderNew)
        private var sliderModelList = ArrayList<SliderModel>()
        private val dotsIndicator = itemView.findViewById<DotsIndicator>(R.id.dots_indicator)
        lateinit var sliderHandel:android.os.Handler

        lateinit var adapter: SliderAdapter
        init {


        }
        fun bind(homeModel: HomeModel){
            val uiId:String = homeModel.UI_VIEW_ID.trim()
            sliderModelList.clear()
            val pos:String = adapterPosition.toString()
            getFirebaeData(uiId)

            adapter = SliderAdapter(sliderModelList,sliderView)

            sliderView.adapter = adapter
            dotsIndicator.setViewPager2(sliderView)


            val toast = Toast.makeText(itemView.context,sliderModelList.size.toString(),Toast.LENGTH_SHORT)

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

        private fun getFirebaeData(uiId:String )  = CoroutineScope(Dispatchers.IO).launch {

            val firebaseFirestore = Firebase.firestore.collection("UI_SLIDER").document(uiId)
            firebaseFirestore.get().addOnSuccessListener {
                val noOfSl = it.getLong("no_of_slider")
                for (i in 1..noOfSl!!){
                    val url = it.getString("${i}_slider")?.trim()
                    val sliderAction = it.getString("${i}_slider_bg")
                    sliderModelList.add(SliderModel(url!!, sliderAction!!))
                }
                adapter.picList = sliderModelList
                adapter.notifyDataSetChanged()

            }.addOnFailureListener {
                Log.e("SliderViewModel","${it.message}")
            }.await()
        }

    }

    class CategoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val categoryRecycler:RecyclerView = itemView.findViewById(R.id.topCategoryRecycler)
        private val firebaseFirestore = Firebase.firestore
        private var categoryList: ArrayList<MutableMap<String,String>> = ArrayList()
        private lateinit var categoryAdapter: TopCategoryAdapter
        fun bind(homeModel: HomeModel){

            categoryRecycler.layoutManager = GridLayoutManager(itemView.context,2)
            val uiId= homeModel.UI_VIEW_ID.trim()
            val pos:String = adapterPosition.toString()

            getfireBsedata(uiId)



        }



        fun getfireBsedata(uiId:String) = CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO){
                firebaseFirestore.collection("UI_TOP_4_CATEGORY").document(uiId)
                    .get().addOnSuccessListener{
                        categoryList = it.get("categories") as ArrayList<MutableMap<String,String>>



                        categoryAdapter = TopCategoryAdapter(categoryList)
                        categoryRecycler.adapter = categoryAdapter
                        categoryAdapter.notifyDataSetChanged()


                    }.addOnFailureListener {
                        Log.e("CategoryViewModel","${it.message}")
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

            val uiId:String = homeModel.UI_VIEW_ID.trim()
            getFirebaeData(uiId)

            val pos:String = adapterPosition.toString()




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
        private val productRecycler:RecyclerView = itemView.findViewById(R.id.product_grid_recycler)
        private val batchHeader:TextView = itemView.findViewById(R.id.batch_header)
        //private val batchBackground:LinearLayout = itemView.findViewById(R.id.batch_background)
        private var productIdList= ArrayList<String>()
        private lateinit var adapter1: HorizontalAdapter
        fun bind(homeModel: HomeModel){

            val uiId:String = homeModel.UI_VIEW_ID.trim()
            getFirebaeData(uiId)

            val pos:String = adapterPosition.toString()




        }
        private fun getFirebaeData(uiId:String ) = CoroutineScope(Dispatchers.IO).launch {

            val firebaseFirestore = Firebase.firestore.collection("UI_PRODUCT_HORIZONTAL").document(uiId)
            firebaseFirestore.get().addOnSuccessListener {
                val header = it.getString("layout_title")
                val bgColor = it.getString("bg_color")?.trim()
                productIdList = it.get("products") as ArrayList<String>
                batchHeader.text = header
                //batchBackground.setBackgroundColor(parseColor(bgColor))

                productRecycler.layoutManager = LinearLayoutManager(itemView.context,LinearLayoutManager.HORIZONTAL ,false)
                adapter1 = HorizontalAdapter(productIdList)
                productRecycler.adapter = adapter1

                adapter1.notifyDataSetChanged()

            }.addOnFailureListener {
                Log.e("HorizontalViewModel","${it.message}")
            }

        }
    }



    class StripViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private var stripImage:ImageView = itemView.findViewById(R.id.strip_image)
        private val firebaseFirestore = Firebase.firestore
        fun bind(homeModel: HomeModel){
            val uiId:String = homeModel.UI_VIEW_ID.trim()
            firebaseFirestore.collection("UI_STRIP_LAYOUT").document(uiId)
                .get().addOnCompleteListener {
                    val url = it.result?.getString("image")?.trim()
                    Glide.with(itemView.context).load(url).placeholder(R.drawable.as_banner_placeholder).into(stripImage)
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
            val uiId:String = homeModel.UI_VIEW_ID.trim()
            getFirebaeData(uiId)

            val pos:String = adapterPosition.toString()
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
        private var adsImage:ImageView = itemView.findViewById(R.id.bigads_image)
        private val adTitleTxt:TextView =itemView.findViewById(R.id.ad_title)
        private val adDescriptionTxt:TextView =itemView.findViewById(R.id.ad_description)
        private val adProviderTxt:TextView =itemView.findViewById(R.id.ad_provider_name)

        private val firebaseFirestore = Firebase.firestore

        fun bind(homeModel: HomeModel){
            val uiId:String = homeModel.UI_VIEW_ID.trim()

            firebaseFirestore.collection("UI_BIG_ADS").document(uiId)
                .get().addOnSuccessListener {
                    CoroutineScope(Dispatchers.IO).launch{
                        val url = it.getString("image")
                        val addTitle = it.getString("ad_title")
                        val adDescription = it.getString("ad_description")
                        val provider = it.getString("ad_provider")
                        val actionType =it.getLong("action_type")


                        withContext(Dispatchers.Main){
                            Glide.with(itemView.context).load(url).placeholder(R.drawable.as_banner_placeholder).into(adsImage)
                            adTitleTxt.text = addTitle
                            adDescriptionTxt.text = adDescription
                            adProviderTxt.text = provider
                        }



                    }
                }.addOnFailureListener {
                    Log.e("HorizontalAdapter","${it.message}")
                }



        }
    }

    class NillViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        private val firebaseFirestore = Firebase.firestore

    }

}