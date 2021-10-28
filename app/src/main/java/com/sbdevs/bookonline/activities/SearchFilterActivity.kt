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
import com.google.firebase.firestore.DocumentSnapshot
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
    private val firebaseAuth = FirebaseAuth.getInstance()
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
        val view: View = layoutInflater.inflate(R.layout.le_bottom_sheet_1,null)
        dialog.setContentView(view)
        dialogFunction(dialog)

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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

    fun getSearchData(tag:String){
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
                    val product = documentSnapshot.id
                    val productName = documentSnapshot.getString("book_title").toString()
                    searchList.add(SearchModel(product,productName))
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



    fun dialogFunction(dialog: BottomSheetDialog){
        val button1: Button = dialog.findViewById(R.id.button3)!!
        val typeChipGroup: ChipGroup = dialog.findViewById(R.id.chipGroup)!!
        button1.setOnClickListener {
            Toast.makeText(this,"Clicked",Toast.LENGTH_SHORT).show()
        }
        for (index in 0 until typeChipGroup!!.childCount) {
            val chip: Chip = typeChipGroup!!.getChildAt(index) as Chip

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

}