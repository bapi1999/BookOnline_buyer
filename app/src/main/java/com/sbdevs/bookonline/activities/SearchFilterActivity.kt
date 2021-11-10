package com.sbdevs.bookonline.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.core.OrderBy
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.SearchFilterAdapter
import com.sbdevs.bookonline.databinding.ActivitySearchFilterBinding
import com.sbdevs.bookonline.models.uidataclass.SearchModel

class SearchFilterActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySearchFilterBinding
    lateinit var dialog:BottomSheetDialog
    val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth
    private var searchList:ArrayList<SearchModel> = ArrayList()
    private var allSearchList:ArrayList<SearchModel> = ArrayList()
    var newlist:ArrayList<SearchModel> = ArrayList()
    private lateinit var searchFilterAdapter: SearchFilterAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchFilterBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar2)


        dialog = BottomSheetDialog(this,R.style.CustomBottomSheetDialog)
        val view: View = layoutInflater.inflate(R.layout.ar_filter_bottom_sheet,null)
        dialog.setContentView(view)
//        dialogFunction(dialog)

        val recyclerView = binding.searchRecycler
        recyclerView.layoutManager = LinearLayoutManager(this)

        val query = intent.getStringExtra("query")
//        binding.queryText.text  = query.toString()


        val tags:List<String> = query?.lowercase()?.split(" ")!!
                for (tag:String in tags){
                    getSearchData(tag)
                }



        searchFilterAdapter = SearchFilterAdapter(searchList)
        recyclerView.adapter = searchFilterAdapter




    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.filter_menu,menu)
//        return super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.search_filter){
            dialog.show()

            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getSearchData(tag:String){
        firebaseFirestore.collection("PRODUCTS").whereArrayContains("tags",tag)
            .limit(10).get().addOnCompleteListener { task ->
                ////////////////////////////////////////////////////////////////////////////////
                //// TODO = optimize query or it will cost you too much
                //// TODO = optimize query or it will cost you too much
                //// TODO = optimize query or it will cost you too much
                //// TODO = optimize query or it will cost you too much
                //// TODO = optimize query or it will cost you too much
                //// TODO = optimize query or it will cost you too much
                //// TODO = optimize query or it will cost you too much
                //// TODO = optimize query or it will cost you too much
                //// TODO = optimize query or it will cost you too much
                //// TODO = optimize query or it will cost you too much
                // ////////////////////////////////////////////////////////
                for (documentSnapshot: DocumentSnapshot in task.result!!.documents){
                    val productId = documentSnapshot.id
                    val productName = documentSnapshot.getString("book_title").toString()

                    val url:String =documentSnapshot.getString("product_thumbnail").toString().trim()
                    val stockQty:Long = documentSnapshot.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = documentSnapshot.getString("rating_avg")!!
                    val totalRatings: Long = documentSnapshot.getLong("rating_total")!!

                    val priceOriginal = documentSnapshot.get("price_original").toString().trim()
                    val priceSelling = documentSnapshot.get("price_selling").toString().trim()

                    val printedYear = documentSnapshot.getString("book_printed_ON")
                    val bookCondition = documentSnapshot.getString("book_condition")

                    searchList.add(SearchModel(productId,productName,url, priceOriginal,priceSelling, stockQty ,avgRating,totalRatings))
                }


//                szearchList = it.result!!.toObjects(SearchModel::class.java) as ArrayList<SearchModel>
                allSearchList.addAll(searchList)
//                allSearchList.union(searchList) // todo use extra query and it requires String object only
                newlist= allSearchList.distinctBy { it.productId } as ArrayList<SearchModel>
                searchFilterAdapter.list =newlist
                searchFilterAdapter.notifyDataSetChanged()
                binding.queryText.text = "union ${allSearchList.size}, din ${newlist.size} "
            }



    }



    private fun dialogFunction(dialog: BottomSheetDialog){
        val applyBtn: Button = dialog.findViewById(R.id.apply_btn)!!
        val typeChipGroup: ChipGroup = dialog.findViewById(R.id.type_chipGroup)!!
        val conditionChipGroup: ChipGroup = dialog.findViewById(R.id.condition_chipGroup)!!
        val priceChipGroup: ChipGroup = dialog.findViewById(R.id.price_chipGroup)!!
        val relevanceChipGroup: ChipGroup = dialog.findViewById(R.id.relevance_chipGroup)!!


        applyBtn.setOnClickListener {
            Toast.makeText(this,"Clicked",Toast.LENGTH_SHORT).show()
        }

        chipListnerForAll( typeChipGroup)
        chipListnerForAll(conditionChipGroup)
        chipListnerForAll(priceChipGroup)

    }

    private fun chipListnerForAll(chipGroup: ChipGroup){
        for (index in 0 until chipGroup.childCount) {
            val chip: Chip = chipGroup.getChildAt(index) as Chip

            // Set the chip checked change listener
            chip.setOnCheckedChangeListener{view, isChecked ->
                if (isChecked){
//                    list.add(view.text.toString())
                    Toast.makeText(this, view.text.toString(), Toast.LENGTH_SHORT).show()
                }else{
//                    list.remove(view.text.toString())
                }

//                if (list.isNotEmpty()){
//                    // Show the selection
//                    Log.d(LOGTAG,"Selected $list")
//                }
            }
        }
    }


    private fun getQueryData(tag:String){

        val priceLowToHighTask = firebaseFirestore.collection("PRODUCTS").whereArrayContains("tags",tag)
            .orderBy("price",Query.Direction.DESCENDING)
            .limit(10).get()

        val priceHighToLowTask = firebaseFirestore.collection("PRODUCTS").whereArrayContains("tags",tag)
            .orderBy("price",Query.Direction.DESCENDING)
            .limit(10).get()




    }

}