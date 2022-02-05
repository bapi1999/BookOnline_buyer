package com.sbdevs.bookonline.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.RangeSlider
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.SearchFilterAdapter
import com.sbdevs.bookonline.databinding.ActivitySearchFilterBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.models.uidataclass.SearchModel
import java.time.Year
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SearchFilterActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchFilterBinding
    lateinit var dialog: BottomSheetDialog
    private val firebaseFirestore = Firebase.firestore


    private var allSearchList: ArrayList<SearchModel> = ArrayList()


    private lateinit var searchFilterAdapter: SearchFilterAdapter

    private val typeList: MutableList<String> = ArrayList()
    private val conditionList: MutableList<String> = ArrayList()
    private val yearList: MutableList<String> = ArrayList()
    private var tags:MutableList<String> = ArrayList()
    private var priceRelevenceDirection = Query.Direction.ASCENDING

    private var mainFilterMap:MutableMap<Any,String> = HashMap()
    private val subFilterMap:MutableMap<Any,String> = HashMap()

    private var lastResult: DocumentSnapshot ? =null
    private lateinit var times:Timestamp
    private var inStockOrder:Long = 0L
    private var isReachLast:Boolean = false

    private var priceRageIsApplied = false
    var lowerLimit = 0L
    var upperLimit = 1000L
    private var searchCode = 0
    private lateinit var searchRecycler:RecyclerView

    private val visible = View.VISIBLE
    private val gone = View.GONE
    private val loadingDialog = LoadingDialog()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchFilterBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar2)
        loadingDialog.show(supportFragmentManager,"show")
        dialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)
        val view: View = layoutInflater.inflate(R.layout.ar_search_filter_bottom_sheet, null)
        dialog.setContentView(view)
        dialogFunction(dialog)

        searchRecycler = binding.searchRecycler
        searchRecycler.layoutManager = LinearLayoutManager(this)

        val query = intent.getStringExtra("query")
        binding.queryText.text  = query.toString()


        var queryList: List<String> = query?.lowercase()?.split(" ")!!


        tags.addAll(queryList)
        queryEqualCount0()

        searchFilterAdapter = SearchFilterAdapter(allSearchList)
        searchRecycler.adapter = searchFilterAdapter


        binding.queryText.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }


    }

    override fun onStart() {
        super.onStart()


        searchRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)


                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    // end scrolling: do what you want here and after calling the function change the value of boolean

                    if (isReachLast){
                        Log.w("Query item","Last item is reached already")
                        binding.progressBar2.visibility = View.GONE
                        Log.e("last query", "${lastResult.toString()}")

                    }else{
                        binding.progressBar2.visibility = View.VISIBLE

                        Log.e("last query", "${lastResult.toString()}")

                        when(searchCode){
                            0 ->{
                                queryEqualCount0()
                            }
                            1 -> {
                                queryEqualCount1(subFilterMap)
                            }
                            2 -> {
                                queryEqualCount2(subFilterMap)
                            }
                            3->{
                                queryEqualCount3(subFilterMap)
                            }
                            4->{
                                queryEqualCount4(subFilterMap)
                            }
                            5->{
                                queryEqualCount5(subFilterMap)
                            }
                            6->{
                                queryEqualCount6(subFilterMap)
                            }

                        }
                    }

                }

            }


        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.filter_menu, menu)
//        return super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.search_filter) {
//            codeList.clear()
            dialog.show()

            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun dialogFunction(dialog: BottomSheetDialog) {
        val applyBtn: AppCompatButton = dialog.findViewById(R.id.apply_btn)!!
        val typeChipGroup: ChipGroup = dialog.findViewById(R.id.type_chipGroup)!!
        val conditionChipGroup: ChipGroup = dialog.findViewById(R.id.condition_chipGroup)!!
        val relevanceChipGroup: ChipGroup = dialog.findViewById(R.id.relevance_chipGroup)!!
        val yearChipGroup:ChipGroup = dialog.findViewById(R.id.print_chipGroup)!!
        val thisYearChip: Chip = dialog.findViewById(R.id.print_chip2)!!
//        val previousYearChip:Chip = dialog.findViewById(R.id.print_chip3)!!
        val priceChipGroup: ChipGroup = dialog.findViewById(R.id.price_chipGroup)!!
        val priceRaneSlider:RangeSlider = dialog.findViewById(R.id.price_range_slider)!!
        val priceRangeTextCOntainer:LinearLayout = dialog.findViewById(R.id.price_range_text_container)!!
        val lowerInput: TextView = dialog.findViewById(R.id.lower_input)!!
        val upperInput: TextView = dialog.findViewById(R.id.upper_input)!!


        val year = Year.now().value.toLong()
        //val previousYear = year -1
        thisYearChip.text = "$year"
        thisYearChip.tag = "$year"

        chipListenerForType(typeChipGroup)
        chipListenerForCondition(conditionChipGroup)

        relevanceChipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.relevance_chip1 -> {
                    priceRelevenceDirection = Query.Direction.ASCENDING
                    //low to high
                }
                R.id.relevance_chip2 -> {
                    priceRelevenceDirection = Query.Direction.DESCENDING
                    //high to low
                }
            }

        }

        priceChipGroup.setOnCheckedChangeListener { group, checkedId ->

            when (checkedId) {
                R.id.price_chip1 -> {
                    priceRaneSlider.visibility = View.GONE
                    priceRangeTextCOntainer.visibility = View.GONE
                    priceRageIsApplied = false
                }
                R.id.price_chip2 -> {
                    priceRaneSlider.visibility = View.VISIBLE
                    priceRangeTextCOntainer.visibility = View.VISIBLE
                    priceRageIsApplied = true
                }
                else -> {
                    priceRageIsApplied = false
                    priceRaneSlider.visibility = View.GONE
                    priceRangeTextCOntainer.visibility = View.GONE
                }
            }

        }

        yearChipGroup.setOnCheckedChangeListener { group, checkedId ->

            when (checkedId) {
                R.id.print_chip1 -> {
                    mainFilterMap.remove(year,"book_printed_ON")
                }
                R.id.print_chip2 -> {
//                    yearList.add(year.toString())
                    mainFilterMap[year] = "book_printed_ON"
                }
                else -> {
                    mainFilterMap.remove(year,"book_printed_ON")
                }
            }

        }

//        priceRaneSlider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
//            override fun onStartTrackingTouch(slider: RangeSlider) {
//                // Responds to when slider's touch event is being started
//            }
//
//            override fun onStopTrackingTouch(slider: RangeSlider) {
//                // Responds to when slider's touch event is being stopped
//            }
//        })

        priceRaneSlider.addOnChangeListener { rangeSlider, value, fromUser ->
            lowerLimit = rangeSlider.values[0].toLong()
            upperLimit = rangeSlider.values[1].toLong()
            if (lowerLimit == upperLimit){
                lowerInput.backgroundTintList = AppCompatResources.getColorStateList(this,R.color.red_700)
                upperInput.backgroundTintList = AppCompatResources.getColorStateList(this,R.color.red_700)
                applyBtn.isEnabled = false
                applyBtn.backgroundTintList =AppCompatResources.getColorStateList(this,R.color.grey_500)
            }else{
                lowerInput.backgroundTintList = AppCompatResources.getColorStateList(this,R.color.grey_700)
                upperInput.backgroundTintList = AppCompatResources.getColorStateList(this,R.color.grey_700)
                applyBtn.isEnabled = true
                applyBtn.backgroundTintList =AppCompatResources.getColorStateList(this,R.color.blueLink)

            }

            lowerInput.text = lowerLimit.toString()
            upperInput.text = upperLimit.toString()

        }


        applyBtn.setOnClickListener {
            subFilterMap.clear()
//            searchList.clear()
            searchFilterAdapter.notifyItemRangeRemoved(0,allSearchList.size)
            allSearchList.clear()
            lastResult = null
//            newlist.clear()



            subFilterMap.putAll(mainFilterMap)
//
//
            if (typeList.size == 3){
                subFilterMap.remove("new","book_type")
                subFilterMap.remove("used","book_type")
                subFilterMap.remove("refurbished","book_type")
            }

            if (conditionList.size == 4){
                subFilterMap.remove("good","book_condition")
                subFilterMap.remove("almost_new","book_condition")
                subFilterMap.remove("half_bad","book_condition")
                subFilterMap.remove("bad","book_condition")
            }


//            var st = "map size ${mainFilterMap.size} \n"
//            st += "_______________________\n"
//            val keynum = subFilterMap.keys
//            val valunum = subFilterMap.values
//            for (i in 0 until subFilterMap.size){
//                st +="${valunum.elementAt(i)} = ${keynum.elementAt(i)} \n"
//            }
//            st += "_______________________\n"
//            for (key in mainFilterMap.keys){
//                st += "${mainFilterMap[key]} =  $key \n"
//            }

            when(subFilterMap.size){
                0->{
                    queryEqualCount0()
                    searchCode = 0
                }
                1->{
                    queryEqualCount1(subFilterMap)
                    searchCode = 1
                }
                2->{
                    queryEqualCount2(subFilterMap)
                    searchCode = 2
                }
                3->{
                    queryEqualCount3(subFilterMap)
                    searchCode = 3
                }
                4->{
                    queryEqualCount4(subFilterMap)
                    searchCode = 4
                }
                5->{
                    queryEqualCount5(subFilterMap)
                    searchCode = 5
                }
                6->{
                    queryEqualCount6(subFilterMap)
                    searchCode = 6
                }

            }

            dialog.dismiss()

        }

    }


    private fun chipListenerForType(chipGroup: ChipGroup) {
        for (index in 0 until chipGroup.childCount) {
            val chip: Chip = chipGroup.getChildAt(index) as Chip

            // Set the chip checked change listener
            chip.setOnCheckedChangeListener { view, isChecked ->
                if (isChecked) {
                    typeList.add(view.tag.toString())
                    mainFilterMap[view.tag.toString()] = "book_type"

                } else {
                    typeList.remove(view.tag.toString())
                    mainFilterMap.remove(view.tag.toString(),"book_type")

                }

            }
        }
    }


    private fun chipListenerForCondition(chipGroup: ChipGroup) {
        for (index in 0 until chipGroup.childCount) {
            val chip: Chip = chipGroup.getChildAt(index) as Chip

            // Set the chip checked change listener
            chip.setOnCheckedChangeListener { view, isChecked ->
                if (isChecked) {
                    conditionList.add(view.tag.toString())
                    mainFilterMap[view.tag.toString()] = "book_condition"
                } else {
                    conditionList.remove(view.tag.toString())
                    mainFilterMap.remove(view.tag.toString(),"book_condition")
                }

            }
        }
    }




//TODO #######################################################################################################################
//Filter methods
//TODO#######################################################################################################################


    private fun queryEqualCount0(){

        val searchList: ArrayList<SearchModel> = ArrayList()
        searchList.clear()

        val filterTask:Query
        if (lastResult == null){
            filterTask = if (!priceRageIsApplied){
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            } else{
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereGreaterThan("price_selling",lowerLimit)
                    .whereLessThan("price_selling",upperLimit)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            }
        }
        else{
            filterTask = if (!priceRageIsApplied){
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
                    .startAfter(lastResult)
            } else{
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereGreaterThan("price_selling",lowerLimit)
                    .whereLessThan("price_selling",upperLimit)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
                    .startAfter(lastResult)
            }
        }

        filterTask.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){



                for (documentSnapshot in allDocumentSnapshot) {
                    val productId = documentSnapshot.id
                    val productName = documentSnapshot.getString("book_title").toString()

                    val url: String = documentSnapshot.getString("product_thumbnail").toString().trim()
                    val stockQty: Long = documentSnapshot.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = documentSnapshot.getString("rating_avg")!!
                    val totalRatings: Long = documentSnapshot.getLong("rating_total")!!

                    val priceOriginal = documentSnapshot.getLong("price_original")!!.toLong()
                    val priceSelling = documentSnapshot.getLong("price_selling")!!.toLong()

                    val printedYear = documentSnapshot.getLong("book_printed_ON")!!
                    val bookCondition = documentSnapshot.getString("book_condition").toString()
                    val bookType = documentSnapshot.getString("book_type")!!

                    searchList.add(SearchModel(productId, productName, url, priceOriginal, priceSelling,
                        stockQty, avgRating, totalRatings, bookCondition, bookType, printedYear))
                }

//Todo- old method =================================================================
//                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
//                lastResult = lastR
//                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!
//Todo- old methods =================================================================


                isReachLast = allDocumentSnapshot.size < 10 // limit is 10


            }else{
                isReachLast = true
            }

            allSearchList.addAll(searchList)

            if (allSearchList.isEmpty()){
                searchRecycler.visibility = gone
                binding.progressBar2.visibility = gone
                binding.noResultFoundText.visibility = visible
            }else{
                searchRecycler.visibility = visible
                binding.progressBar2.visibility = visible
                binding.noResultFoundText.visibility = gone

                searchFilterAdapter.list = allSearchList

                if (lastResult == null ){
                    searchFilterAdapter.notifyItemRangeInserted(0,searchList.size)
                }else{
                    searchFilterAdapter.notifyItemRangeInserted(allSearchList.size-1,searchList.size)
                }
//                binding.queryText.text = "union ${allSearchList.size}"



//Todo- new approach =================================================================
                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!
//Todo- new approach =================================================================




                binding.progressBar2.visibility = View.GONE
            }


            loadingDialog.dismiss()
        }.addOnFailureListener {
            Log.e("get search query 0", "${it.message}")
            loadingDialog.dismiss()
        }

    }


    private fun queryEqualCount1(queryMap: MutableMap<Any,String>){
        Toast.makeText(this,"1 query",Toast.LENGTH_LONG).show()
        val keys = queryMap.keys
        val values = queryMap.values
        val searchList: ArrayList<SearchModel> = ArrayList()
        searchList.clear()

        val filterTask:Query

        if (lastResult == null){
            filterTask = if (!priceRageIsApplied){
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            } else{
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereGreaterThan("price_selling",lowerLimit)
                    .whereLessThan("price_selling",upperLimit)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            }
        }
        else{
            filterTask = if (!priceRageIsApplied){
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .orderBy("price_selling",Query.Direction.ASCENDING)
                    .startAfter(lastResult)
            } else{
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereGreaterThan("price_selling",lowerLimit)
                    .whereLessThan("price_selling",upperLimit)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
                    .startAfter(lastResult)
            }
        }

        filterTask.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){



                for (documentSnapshot in allDocumentSnapshot) {
                    val productId = documentSnapshot.id
                    val productName = documentSnapshot.getString("book_title").toString()

                    val url: String =
                        documentSnapshot.getString("product_thumbnail").toString().trim()
                    val stockQty: Long = documentSnapshot.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = documentSnapshot.getString("rating_avg")!!
                    val totalRatings: Long = documentSnapshot.getLong("rating_total")!!

                    val priceOriginal = documentSnapshot.getLong("price_original")!!.toLong()
                    val priceSelling = documentSnapshot.getLong("price_selling")!!.toLong()

                    val printedYear = documentSnapshot.getLong("book_printed_ON")!!
                    val bookCondition = documentSnapshot.getString("book_condition").toString()
                    val bookType = documentSnapshot.getString("book_type")!!

                    searchList.add(SearchModel(productId, productName, url, priceOriginal, priceSelling,
                        stockQty, avgRating, totalRatings, bookCondition, bookType, printedYear))
                }

                isReachLast = allDocumentSnapshot.size <= 10 // limit is 10


            }else{
                isReachLast = true
            }

            allSearchList.addAll(searchList)

            if (allSearchList.isEmpty()){
                searchRecycler.visibility = gone
                binding.progressBar2.visibility = gone
                binding.noResultFoundText.visibility = visible
            }else{
                searchRecycler.visibility = visible
                binding.progressBar2.visibility = visible
                binding.noResultFoundText.visibility = gone

                searchFilterAdapter.list = allSearchList

                if (lastResult == null ){
                    searchFilterAdapter.notifyItemRangeInserted(0,searchList.size)
                }else{
                    searchFilterAdapter.notifyItemRangeInserted(allSearchList.size-1,searchList.size)
                }


                //Todo- new approach =================================================================
                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!
//Todo- new approach =================================================================

                binding.progressBar2.visibility = View.GONE
            }

//            newlist = allSearchList.distinctBy { it.productId } as ArrayList<SearchModel>
            loadingDialog.dismiss()
        }.addOnFailureListener {
            Log.e("get search query 1", "${it.message}")
            loadingDialog.dismiss()
        }
    }


    private fun queryEqualCount2(queryMap: MutableMap<Any,String>){
        Toast.makeText(this,"1 query",Toast.LENGTH_LONG).show()
        val keys = queryMap.keys
        val values = queryMap.values
        val searchList: ArrayList<SearchModel> = ArrayList()
        searchList.clear()

        val filterTask:Query

        if (lastResult == null){
            filterTask = if (!priceRageIsApplied){
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            } else{
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereGreaterThan("price_selling",lowerLimit)
                    .whereLessThan("price_selling",upperLimit)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            }
        }
        else{
            filterTask = if (!priceRageIsApplied){
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .orderBy("price_selling",Query.Direction.ASCENDING)
                    .startAfter(lastResult)
            } else{
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereGreaterThan("price_selling",lowerLimit)
                    .whereLessThan("price_selling",upperLimit)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
                    .startAfter(lastResult)
            }
        }

        filterTask.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){

                 // limit is 10

                for (documentSnapshot in allDocumentSnapshot) {
                    val productId = documentSnapshot.id
                    val productName = documentSnapshot.getString("book_title").toString()

                    val url: String =
                        documentSnapshot.getString("product_thumbnail").toString().trim()
                    val stockQty: Long = documentSnapshot.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = documentSnapshot.getString("rating_avg")!!
                    val totalRatings: Long = documentSnapshot.getLong("rating_total")!!

                    val priceOriginal = documentSnapshot.getLong("price_original")!!.toLong()
                    val priceSelling = documentSnapshot.getLong("price_selling")!!.toLong()

                    val printedYear = documentSnapshot.getLong("book_printed_ON")!!
                    val bookCondition = documentSnapshot.getString("book_condition").toString()
                    val bookType = documentSnapshot.getString("book_type")!!

                    searchList.add(SearchModel(productId, productName, url, priceOriginal, priceSelling,
                        stockQty, avgRating, totalRatings, bookCondition, bookType, printedYear))
                }

                isReachLast = allDocumentSnapshot.size <= 10


            }else{
                isReachLast = true
            }

            allSearchList.addAll(searchList)

            if (allSearchList.isEmpty()){
                searchRecycler.visibility = gone
                binding.progressBar2.visibility = gone
                binding.noResultFoundText.visibility = visible
            }else{
                searchRecycler.visibility = visible
                binding.progressBar2.visibility = visible
                binding.noResultFoundText.visibility = gone

                searchFilterAdapter.list = allSearchList

                if (lastResult == null ){
                    searchFilterAdapter.notifyItemRangeInserted(0,searchList.size)
                }else{
                    searchFilterAdapter.notifyItemRangeInserted(allSearchList.size-1,searchList.size)
                }
                loadingDialog.dismiss()

                //Todo- new approach =================================================================
                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!
//Todo- new approach =================================================================

                binding.progressBar2.visibility = View.GONE
            }

            loadingDialog.dismiss()
        }.addOnFailureListener {
            Log.e("get search query 2", "${it.message}")
            loadingDialog.dismiss()
        }
    }


    private fun queryEqualCount3(queryMap: MutableMap<Any,String>){
        Toast.makeText(this,"1 query",Toast.LENGTH_LONG).show()
        val keys = queryMap.keys
        val values = queryMap.values
        val searchList: ArrayList<SearchModel> = ArrayList()
        searchList.clear()

        val filterTask:Query

        if (lastResult == null){
            filterTask = if (!priceRageIsApplied){
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            } else{
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .whereGreaterThan("price_selling",lowerLimit)
                    .whereLessThan("price_selling",upperLimit)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            }
        }
        else{
            filterTask = if (!priceRageIsApplied){
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .orderBy("price_selling",Query.Direction.ASCENDING)
                    .startAfter(lastResult)
            } else{
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .whereGreaterThan("price_selling",lowerLimit)
                    .whereLessThan("price_selling",upperLimit)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
                    .startAfter(lastResult)
            }
        }

        filterTask.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){



                for (documentSnapshot in allDocumentSnapshot) {
                    val productId = documentSnapshot.id
                    val productName = documentSnapshot.getString("book_title").toString()

                    val url: String =
                        documentSnapshot.getString("product_thumbnail").toString().trim()
                    val stockQty: Long = documentSnapshot.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = documentSnapshot.getString("rating_avg")!!
                    val totalRatings: Long = documentSnapshot.getLong("rating_total")!!

                    val priceOriginal = documentSnapshot.getLong("price_original")!!.toLong()
                    val priceSelling = documentSnapshot.getLong("price_selling")!!.toLong()

                    val printedYear = documentSnapshot.getLong("book_printed_ON")!!
                    val bookCondition = documentSnapshot.getString("book_condition").toString()
                    val bookType = documentSnapshot.getString("book_type")!!

                    searchList.add(SearchModel(productId, productName, url, priceOriginal, priceSelling,
                        stockQty, avgRating, totalRatings, bookCondition, bookType, printedYear))
                }
                isReachLast = allDocumentSnapshot.size <= 10 // limit is 10

            }else{
                isReachLast = true
            }

            allSearchList.addAll(searchList)

            if (allSearchList.isEmpty()){
                searchRecycler.visibility = gone
                binding.progressBar2.visibility = gone
                binding.noResultFoundText.visibility = visible
            }else{
                searchRecycler.visibility = visible
                binding.progressBar2.visibility = visible
                binding.noResultFoundText.visibility = gone

                searchFilterAdapter.list = allSearchList

                if (lastResult == null ){
                    searchFilterAdapter.notifyItemRangeInserted(0,searchList.size)
                }else{
                    searchFilterAdapter.notifyItemRangeInserted(allSearchList.size-1,searchList.size)
                }
                loadingDialog.dismiss()

                //Todo- new approach =================================================================
                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!
//Todo- new approach =================================================================

                binding.progressBar2.visibility = View.GONE
            }

//            newlist = allSearchList.distinctBy { it.productId } as ArrayList<SearchModel>

        }.addOnFailureListener {
            Log.e("get search query 2", "${it.message}")
            loadingDialog.dismiss()
        }
    }

    private fun queryEqualCount4(queryMap: MutableMap<Any,String>){
        Toast.makeText(this,"1 query",Toast.LENGTH_LONG).show()
        val keys = queryMap.keys
        val values = queryMap.values
        val searchList: ArrayList<SearchModel> = ArrayList()
        searchList.clear()

        val filterTask:Query

        if (lastResult == null){
            filterTask = if (!priceRageIsApplied){
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .whereEqualTo(values.elementAt(3),keys.elementAt(3))
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            } else{
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .whereEqualTo(values.elementAt(3),keys.elementAt(3))
                    .whereGreaterThan("price_selling",lowerLimit)
                    .whereLessThan("price_selling",upperLimit)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            }
        }
        else{
            filterTask = if (!priceRageIsApplied){
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .whereEqualTo(values.elementAt(3),keys.elementAt(3))
                    .orderBy("price_selling",Query.Direction.ASCENDING)
                    .startAfter(lastResult)
            } else{
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .whereEqualTo(values.elementAt(3),keys.elementAt(3))
                    .whereGreaterThan("price_selling",lowerLimit)
                    .whereLessThan("price_selling",upperLimit)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
                    .startAfter(lastResult)
            }
        }

        filterTask.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){



                for (documentSnapshot in allDocumentSnapshot) {
                    val productId = documentSnapshot.id
                    val productName = documentSnapshot.getString("book_title").toString()

                    val url: String =
                        documentSnapshot.getString("product_thumbnail").toString().trim()
                    val stockQty: Long = documentSnapshot.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = documentSnapshot.getString("rating_avg")!!
                    val totalRatings: Long = documentSnapshot.getLong("rating_total")!!

                    val priceOriginal = documentSnapshot.getLong("price_original")!!.toLong()
                    val priceSelling = documentSnapshot.getLong("price_selling")!!.toLong()

                    val printedYear = documentSnapshot.getLong("book_printed_ON")!!
                    val bookCondition = documentSnapshot.getString("book_condition").toString()
                    val bookType = documentSnapshot.getString("book_type")!!

                    searchList.add(SearchModel(productId, productName, url, priceOriginal, priceSelling,
                        stockQty, avgRating, totalRatings, bookCondition, bookType, printedYear))
                }
                isReachLast = allDocumentSnapshot.size <= 10 // limit is 10

            }else{
                isReachLast = true
            }

            allSearchList.addAll(searchList)

            if (allSearchList.isEmpty()){
                searchRecycler.visibility = gone
                binding.progressBar2.visibility = gone
                binding.noResultFoundText.visibility = visible
            }else{
                searchRecycler.visibility = visible
                binding.progressBar2.visibility = visible
                binding.noResultFoundText.visibility = gone

                searchFilterAdapter.list = allSearchList

                if (lastResult == null ){
                    searchFilterAdapter.notifyItemRangeInserted(0,searchList.size)
                }else{
                    searchFilterAdapter.notifyItemRangeInserted(allSearchList.size-1,searchList.size)
                }
                loadingDialog.dismiss()

                //Todo- new approach =================================================================
                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!
//Todo- new approach =================================================================

                binding.progressBar2.visibility = View.GONE
            }

//            newlist = allSearchList.distinctBy { it.productId } as ArrayList<SearchModel>

        }.addOnFailureListener {
            Log.e("get search query 2", "${it.message}")
            loadingDialog.dismiss()
        }
    }


    private fun queryEqualCount5(queryMap: MutableMap<Any,String>){
        Toast.makeText(this,"1 query",Toast.LENGTH_LONG).show()
        val keys = queryMap.keys
        val values = queryMap.values
        val searchList: ArrayList<SearchModel> = ArrayList()
        searchList.clear()

        val filterTask:Query

        if (lastResult == null){
            filterTask = if (!priceRageIsApplied){
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .whereEqualTo(values.elementAt(3),keys.elementAt(3))
                    .whereEqualTo(values.elementAt(4),keys.elementAt(4))
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            } else{
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .whereEqualTo(values.elementAt(3),keys.elementAt(3))
                    .whereEqualTo(values.elementAt(4),keys.elementAt(4))
                    .whereGreaterThan("price_selling",lowerLimit)
                    .whereLessThan("price_selling",upperLimit)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            }
        }
        else{
            filterTask = if (!priceRageIsApplied){
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .whereEqualTo(values.elementAt(3),keys.elementAt(3))
                    .whereEqualTo(values.elementAt(4),keys.elementAt(4))
                    .orderBy("price_selling",Query.Direction.ASCENDING)
                    .startAfter(lastResult)
            } else{
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .whereEqualTo(values.elementAt(3),keys.elementAt(3))
                    .whereEqualTo(values.elementAt(4),keys.elementAt(4))
                    .whereGreaterThan("price_selling",lowerLimit)
                    .whereLessThan("price_selling",upperLimit)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
                    .startAfter(lastResult)
            }
        }

        filterTask.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){



                for (documentSnapshot in allDocumentSnapshot) {
                    val productId = documentSnapshot.id
                    val productName = documentSnapshot.getString("book_title").toString()

                    val url: String =
                        documentSnapshot.getString("product_thumbnail").toString().trim()
                    val stockQty: Long = documentSnapshot.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = documentSnapshot.getString("rating_avg")!!
                    val totalRatings: Long = documentSnapshot.getLong("rating_total")!!

                    val priceOriginal = documentSnapshot.getLong("price_original")!!.toLong()
                    val priceSelling = documentSnapshot.getLong("price_selling")!!.toLong()

                    val printedYear = documentSnapshot.getLong("book_printed_ON")!!
                    val bookCondition = documentSnapshot.getString("book_condition").toString()
                    val bookType = documentSnapshot.getString("book_type")!!

                    searchList.add(SearchModel(productId, productName, url, priceOriginal, priceSelling,
                        stockQty, avgRating, totalRatings, bookCondition, bookType, printedYear))
                }
                isReachLast = allDocumentSnapshot.size <= 10 // limit is 10


            }else{
                isReachLast = true
            }

            allSearchList.addAll(searchList)

            if (allSearchList.isEmpty()){
                searchRecycler.visibility = gone
                binding.progressBar2.visibility = gone
                binding.noResultFoundText.visibility = visible
            }else{
                searchRecycler.visibility = visible
                binding.progressBar2.visibility = visible
                binding.noResultFoundText.visibility = gone

                searchFilterAdapter.list = allSearchList

                if (lastResult == null ){
                    searchFilterAdapter.notifyItemRangeInserted(0,searchList.size)
                }else{
                    searchFilterAdapter.notifyItemRangeInserted(allSearchList.size-1,searchList.size)
                }
                loadingDialog.dismiss()
                //Todo- new approach =================================================================
                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!
//Todo- new approach =================================================================

                binding.progressBar2.visibility = View.GONE
            }

//            newlist = allSearchList.distinctBy { it.productId } as ArrayList<SearchModel>

        }.addOnFailureListener {
            Log.e("get search query 2", "${it.message}")
            loadingDialog.dismiss()
        }
    }

    private fun queryEqualCount6(queryMap: MutableMap<Any,String>){
        Toast.makeText(this,"1 query",Toast.LENGTH_LONG).show()
        val keys = queryMap.keys
        val values = queryMap.values
        val searchList: ArrayList<SearchModel> = ArrayList()
        searchList.clear()

        val filterTask:Query

        if (lastResult == null){
            filterTask = if (!priceRageIsApplied){
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .whereEqualTo(values.elementAt(3),keys.elementAt(3))
                    .whereEqualTo(values.elementAt(4),keys.elementAt(4))
                    .whereEqualTo(values.elementAt(5),keys.elementAt(5))
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            } else{
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .whereEqualTo(values.elementAt(3),keys.elementAt(3))
                    .whereEqualTo(values.elementAt(4),keys.elementAt(4))
                    .whereEqualTo(values.elementAt(5),keys.elementAt(5))
                    .whereGreaterThan("price_selling",lowerLimit)
                    .whereLessThan("price_selling",upperLimit)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            }
        }
        else{
            filterTask = if (!priceRageIsApplied){
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .whereEqualTo(values.elementAt(3),keys.elementAt(3))
                    .whereEqualTo(values.elementAt(4),keys.elementAt(4))
                    .whereEqualTo(values.elementAt(5),keys.elementAt(5))
                    .orderBy("price_selling",Query.Direction.ASCENDING)
                    .startAfter(lastResult)
            } else{
                firebaseFirestore.collection("PRODUCTS").whereArrayContainsAny("tags", tags)
                    .whereEqualTo(values.elementAt(0),keys.elementAt(0))
                    .whereEqualTo(values.elementAt(1),keys.elementAt(1))
                    .whereEqualTo(values.elementAt(2),keys.elementAt(2))
                    .whereEqualTo(values.elementAt(3),keys.elementAt(3))
                    .whereEqualTo(values.elementAt(4),keys.elementAt(4))
                    .whereEqualTo(values.elementAt(5),keys.elementAt(5))
                    .whereGreaterThan("price_selling",lowerLimit)
                    .whereLessThan("price_selling",upperLimit)
                    .orderBy("price_selling",Query.Direction.ASCENDING)
                    .startAfter(lastResult)
            }
        }

        filterTask.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){



                for (documentSnapshot in allDocumentSnapshot) {
                    val productId = documentSnapshot.id
                    val productName = documentSnapshot.getString("book_title").toString()

                    val url: String =
                        documentSnapshot.getString("product_thumbnail").toString().trim()
                    val stockQty: Long = documentSnapshot.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = documentSnapshot.getString("rating_avg")!!
                    val totalRatings: Long = documentSnapshot.getLong("rating_total")!!

                    val priceOriginal = documentSnapshot.getLong("price_original")!!.toLong()
                    val priceSelling = documentSnapshot.getLong("price_selling")!!.toLong()

                    val printedYear = documentSnapshot.getLong("book_printed_ON")!!
                    val bookCondition = documentSnapshot.getString("book_condition").toString()
                    val bookType = documentSnapshot.getString("book_type")!!

                    searchList.add(SearchModel(productId, productName, url, priceOriginal, priceSelling,
                        stockQty, avgRating, totalRatings, bookCondition, bookType, printedYear))
                }
                isReachLast = allDocumentSnapshot.size <= 10 // limit is 10


            }else{
                isReachLast = true
            }

            allSearchList.addAll(searchList)

            if (allSearchList.isEmpty()){
                searchRecycler.visibility = gone
                binding.progressBar2.visibility = gone
                binding.noResultFoundText.visibility = visible
            }else{
                searchRecycler.visibility = visible
                binding.progressBar2.visibility = visible
                binding.noResultFoundText.visibility = gone

                searchFilterAdapter.list = allSearchList

                if (lastResult == null ){
                    searchFilterAdapter.notifyItemRangeInserted(0,searchList.size)
                }else{
                    searchFilterAdapter.notifyItemRangeInserted(allSearchList.size-1,searchList.size)
                }
                loadingDialog.dismiss()
                //Todo- new approach =================================================================
                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!
//Todo- new approach =================================================================

                binding.progressBar2.visibility = View.GONE
            }

//            newlist = allSearchList.distinctBy { it.productId } as ArrayList<SearchModel>

        }.addOnFailureListener {
            Log.e("get search query 2", "${it.message}")
            loadingDialog.dismiss()
        }
    }

}