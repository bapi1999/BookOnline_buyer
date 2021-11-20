package com.sbdevs.bookonline.activities

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.SearchFilterAdapter
import com.sbdevs.bookonline.databinding.ActivitySearchFilterBinding
import com.sbdevs.bookonline.models.CartModel
import com.sbdevs.bookonline.models.uidataclass.SearchModel
import java.time.Year
import java.util.*
import kotlin.collections.ArrayList

class SearchFilterActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchFilterBinding
    lateinit var dialog: BottomSheetDialog
    private val firebaseFirestore = Firebase.firestore
    private val docRef = firebaseFirestore.collection("PRODUCTS")

    private var searchList: ArrayList<SearchModel> = ArrayList()
    private var allSearchList: ArrayList<SearchModel> = ArrayList()
    var newlist: ArrayList<SearchModel> = ArrayList()

    private lateinit var searchFilterAdapter: SearchFilterAdapter

    private val typeConditionList: MutableList<String> = ArrayList()

    private var tags: ArrayList<String> = ArrayList()

    private var codeList: ArrayList<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchFilterBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar2)


        dialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)
        val view: View = layoutInflater.inflate(R.layout.ar_filter_bottom_sheet, null)
        dialog.setContentView(view)
        dialogFunction(dialog)

        val recyclerView = binding.searchRecycler
        recyclerView.layoutManager = LinearLayoutManager(this)

        val query = intent.getStringExtra("query")
//        binding.queryText.text  = query.toString()


        var queryList: List<String> = query?.lowercase()?.split(" ")!!


        tags.addAll(queryList)

        for (tag: String in tags) {
            getSearchData(tag)
        }

        searchFilterAdapter = SearchFilterAdapter(searchList)
        recyclerView.adapter = searchFilterAdapter


        binding.cancelQueryBtn.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }


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


    private fun getSearchData(tag: String) {
        firebaseFirestore.collection("PRODUCTS").whereArrayContains("tags", tag)
            .limit(10).get().addOnSuccessListener { task ->

                //////////////////////////////////////////////////////////
                //// TODO = optimize query or it will cost you too much
                //// TODO = optimize query or it will cost you too much
                // ///////////////////////////////////////////////////////
                searchList.clear()

                for (documentSnapshot: DocumentSnapshot in task.documents) {
                    val productId = documentSnapshot.id
                    val productName = documentSnapshot.getString("book_title").toString()

                    val url: String =
                        documentSnapshot.getString("product_thumbnail").toString().trim()
                    val stockQty: Long = documentSnapshot.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = documentSnapshot.getString("rating_avg")!!
                    val totalRatings: Long = documentSnapshot.getLong("rating_total")!!

                    val priceOriginal = documentSnapshot.getLong("price_original")!!.toLong()
                    val priceSelling = documentSnapshot.getLong("price_selling")!!.toLong()

                    val printedYear = documentSnapshot.getString("book_printed_ON")
                    val bookCondition = documentSnapshot.getString("book_condition")

                    searchList.add(
                        SearchModel(
                            productId,
                            productName,
                            url,
                            priceOriginal,
                            priceSelling,
                            stockQty,
                            avgRating,
                            totalRatings
                        )
                    )
                }


//                szearchList = it.result!!.toObjects(SearchModel::class.java) as ArrayList<SearchModel>
                allSearchList.addAll(searchList)
//                allSearchList.union(searchList) // todo use extra query and it requires String object only
                newlist = allSearchList.distinctBy { it.productId } as ArrayList<SearchModel>
                searchFilterAdapter.list = newlist
                searchFilterAdapter.notifyDataSetChanged()
                binding.queryText.text = "union ${allSearchList.size}, din ${newlist.size} "
            }.addOnFailureListener {
                Toast.makeText(applicationContext,it.message.toString(),Toast.LENGTH_LONG).show()
            }


    }


    private fun dialogFunction(dialog: BottomSheetDialog) {
        val applyBtn: TextView = dialog.findViewById(R.id.apply_btn)!!
        val cancelBtn: TextView = dialog.findViewById(R.id.cancel_button)!!
        val typeChipGroup: ChipGroup = dialog.findViewById(R.id.type_chipGroup)!!
        val conditionChipGroup: ChipGroup = dialog.findViewById(R.id.condition_chipGroup)!!
        val relevanceChipGroup: ChipGroup = dialog.findViewById(R.id.relevance_chipGroup)!!
        val typeYearChip: Chip = dialog.findViewById(R.id.type_chip4)!!
        val lowerInput: EditText = dialog.findViewById(R.id.lower_input)!!
        val upperInput: EditText = dialog.findViewById(R.id.upper_input)!!
        val errorText: TextView = dialog.findViewById(R.id.error_message_text)!!
        var cancelable = true
        var order: Int = -1
        var methodSwitcher = 0


        val year = Year.now().value.toString()

        typeYearChip.text = "$year edition"
        typeYearChip.tag = "TAG_${year}"




        chipListenerForTypeAndCondition(typeChipGroup)
        chipListenerForTypeAndCondition(conditionChipGroup)


        relevanceChipGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == -1) {
                order = -1

            } else {
                when (group.checkedChipId) {
                    R.id.relevance_chip1 -> {
                        order = 0
                        methodSwitcher = 0

                    }
                    R.id.relevance_chip2 -> {
                        order = 1
                        methodSwitcher = 0
                    }//"high to low"
                }

            }
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }


        applyBtn.setOnClickListener {

            val lowerString = lowerInput.text.toString().trim()
            val upperString = upperInput.text.toString().trim()

            tags.addAll(typeConditionList)
//
            searchList.clear()
            allSearchList.clear()
            newlist.clear()
//            getSearchData2(tags)

            if (typeConditionList.size == 0) {
                //
            } else {
                //
            }


            if (lowerString.isNotEmpty() and upperString.isNotEmpty()) {
                if (lowerString.toInt() >= upperString.toInt()) {
                    errorText.visibility = View.VISIBLE
                    errorText.text = "Lower limit can't be greater than or equal to upper limit"
                    cancelable = false
                } else {
                    errorText.visibility = View.GONE
                    cancelable = true
                    methodSwitcher = 1
                    sortingByPriceRangeMethod(tags, order, lowerString, upperString)
                }
            } else if (lowerString.isEmpty() and upperString.isNotEmpty()) {
                errorText.text = "Fill both fields"
                cancelable = false
            } else if (lowerString.isNotEmpty() and upperString.isEmpty()) {
                errorText.text = "Fill both fields"
                cancelable = false
            } else {
                methodSwitcher = 0
                cancelable = true
            }


            when (methodSwitcher) {
                0 -> {
                    sortingSearchDataMethod(tags,order)
                    dialog.dismiss()
                }
                1 -> {
                    if (cancelable) {
                        dialog.dismiss()
                        sortingByPriceRangeMethod(tags, order, lowerString, upperString)
                    }
                }

            }


        }

    }


    private fun chipListenerForTypeAndCondition(chipGroup: ChipGroup) {
        for (index in 0 until chipGroup.childCount) {
            val chip: Chip = chipGroup.getChildAt(index) as Chip

            // Set the chip checked change listener
            chip.setOnCheckedChangeListener { view, isChecked ->
                if (isChecked) {
                    typeConditionList.add(view.tag.toString())

                } else {
                    typeConditionList.remove(view.text.toString())
                }

            }
        }
    }


    fun sortingSearchDataMethod(list: ArrayList<String>, order: Int) {
        for (tag in list) {

            firebaseFirestore.collection("PRODUCTS").whereArrayContains("tags", tag)
                .limit(10).get().addOnSuccessListener { task ->
                    searchList.clear()

                    for (documentSnapshot: DocumentSnapshot in task.documents) {
                        val productId = documentSnapshot.id
                        val productName = documentSnapshot.getString("book_title").toString()

                        val url: String =
                            documentSnapshot.getString("product_thumbnail").toString().trim()
                        val stockQty: Long =
                            documentSnapshot.getLong("in_stock_quantity")!!.toLong()
                        val avgRating = documentSnapshot.getString("rating_avg")!!
                        val totalRatings: Long = documentSnapshot.getLong("rating_total")!!

                        val priceOriginal = documentSnapshot.getLong("price_original")!!.toLong()
                        val priceSelling = documentSnapshot.getLong("price_selling")!!.toLong()

                        val printedYear = documentSnapshot.getString("book_printed_ON")
                        val bookCondition = documentSnapshot.getString("book_condition")

                        searchList.add(
                            SearchModel(
                                productId,
                                productName,
                                url,
                                priceOriginal,
                                priceSelling,
                                stockQty,
                                avgRating,
                                totalRatings
                            )
                        )
                    }


//                szearchList = it.result!!.toObjects(SearchModel::class.java) as ArrayList<SearchModel>
                    allSearchList.addAll(searchList)
//                allSearchList.union(searchList) // todo use extra query and it requires String object only
                    newlist = allSearchList.distinctBy { it.productId } as ArrayList<SearchModel>
                    if (order == 0) {
                        newlist.sortBy {
                            it.priceSelling
                        }
                    } else {
                        newlist.sortByDescending {
                            it.priceSelling
                        }
                    }
                    searchFilterAdapter.list = newlist
                    searchFilterAdapter.notifyDataSetChanged()
                    binding.queryText.text = "union ${allSearchList.size}, din ${newlist.size} "
                }.addOnFailureListener {
                    Toast.makeText(applicationContext,it.message.toString(),Toast.LENGTH_LONG).show()
                }


        }


    }

    fun sortingByPriceRangeMethod(
        list: ArrayList<String>,
        order: Int,
        lowerInput: String,
        upperInput: String
    ) {

        var filterTask:Query

        when (order) {
            0 -> {
                filterTask = docRef.whereArrayContains("tags", "all")
                    .whereGreaterThanOrEqualTo("price_selling", lowerInput.toLong())
                    .whereLessThanOrEqualTo("price_selling", upperInput.toLong())
                    .orderBy("price_selling",Query.Direction.ASCENDING)
            }
            1 -> {
                filterTask = docRef.whereArrayContains("tags", "all")
                    .whereGreaterThanOrEqualTo("price_selling", lowerInput.toLong())
                    .whereLessThanOrEqualTo("price_selling", upperInput.toLong())
                    .orderBy("price_selling",Query.Direction.DESCENDING)

            }
            else -> {
                filterTask = docRef.whereArrayContains("tags", "all")
                    .whereGreaterThanOrEqualTo("price_selling", lowerInput.toLong())
                    .whereLessThanOrEqualTo("price_selling", upperInput.toLong())
            }
        }
        // TODO- filter is partially done and not full proof. ${filterTask} is also not tested or implemented yet






        for (tag in list) {

            val rangeTask = docRef.whereArrayContains("tags", "all")
                .whereGreaterThanOrEqualTo("price_selling", lowerInput.toLong())
                .whereLessThanOrEqualTo("price_selling", upperInput.toLong()).get()
                .addOnSuccessListener { it ->
                    Toast.makeText(baseContext, "success all task", Toast.LENGTH_SHORT).show()

                        searchList.clear()
                        for (documentSnapshot: DocumentSnapshot in it.documents) {

                            val productId = documentSnapshot.id
                            val productName = documentSnapshot.getString("book_title").toString()

                            val url: String =
                                documentSnapshot.getString("product_thumbnail").toString().trim()
                            val stockQty: Long =
                                documentSnapshot.getLong("in_stock_quantity")!!.toLong()
                            val avgRating = documentSnapshot.getString("rating_avg")!!
                            val totalRatings: Long = documentSnapshot.getLong("rating_total")!!

                            val priceOriginal =
                                documentSnapshot.getLong("price_original")!!.toLong()
                            val priceSelling = documentSnapshot.getLong("price_selling")!!.toLong()

                            val printedYear = documentSnapshot.getString("book_printed_ON")
                            val bookCondition = documentSnapshot.getString("book_condition")

                            searchList.add(
                                SearchModel(
                                    productId,
                                    productName,
                                    url,
                                    priceOriginal,
                                    priceSelling,
                                    stockQty,
                                    avgRating,
                                    totalRatings
                                )
                            )
                        }
                        allSearchList.addAll(searchList)
                        newlist = allSearchList.distinctBy { it.productId } as ArrayList<SearchModel>


                        var searchList2 = ArrayList(allSearchList)

                        val newty:ArrayList<SearchModel> = ArrayList()

//                        for (item in allSearchList){
//                            var samecounter = 0
//                            for (j in searchList2 ){
//                                if (item == j){
//                                    samecounter += 1
//                                    if (samecounter >= 5){
//                                        newty.add(j)
//                                    }
//                                }
//                            }
//                            searchList2.remove(item)
//
//                        }



                        if (order == 0) {
                            newlist.sortBy {
                                it.priceSelling
                            }
                        } else if (order == 1) {
                            newlist.sortByDescending {
                                it.priceSelling
                            }
                        } else {
                            Log.d(TAG, "nothing")
                        }

                        searchFilterAdapter.list = newlist
                        searchFilterAdapter.notifyDataSetChanged()
                        binding.queryText.text = "union ${allSearchList.size}, din ${newlist.size} "



                }.addOnFailureListener {
                    Toast.makeText(baseContext, it.message.toString(), Toast.LENGTH_LONG).show()
                }


        }
    }

}