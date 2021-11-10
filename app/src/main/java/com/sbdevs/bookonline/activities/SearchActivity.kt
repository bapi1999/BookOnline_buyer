package com.sbdevs.bookonline.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.adapters.SearchQueryAdapter
import com.sbdevs.bookonline.databinding.ActivitySearchBinding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class SearchActivity : AppCompatActivity(),SearchQueryAdapter.MyonItemClickListener {
    private lateinit var binding:ActivitySearchBinding
    val firebaseFirestore = Firebase.firestore
    private var searchList:ArrayList<String> = ArrayList()
//    private var allSearchList:ArrayList<String> = ArrayList()
//    var newlist:ArrayList<String> = ArrayList()
    private lateinit var searchQueryAdapter: SearchQueryAdapter




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar:Toolbar = binding.toolbar2
        setSupportActionBar(toolbar)

        lifecycleScope.launch(Dispatchers.IO){
            getProductnameList()
            delay(500)
        }









        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
//                searchList.clear()

//                val tags: List<String> = query?.lowercase()?.split(" ")!!
//                for (tag:String in tags){
//                    getSearchData(tag)
//                }
                val intent = Intent(this@SearchActivity,SearchFilterActivity::class.java)
                intent.putExtra("query",query)
                startActivity(intent)
                finish()

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQueryAdapter.filter.filter(newText!!.lowercase())

                return false
            }
        })

        val recyclerView = binding.searchRecycler
        recyclerView.layoutManager = LinearLayoutManager(this)
//        FireStoreData().getProductnameList(recyclerView)

        searchQueryAdapter = SearchQueryAdapter(this,1)
        recyclerView.adapter = searchQueryAdapter



    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item)
    }

    fun getProductnameList(){
        firebaseFirestore.collection("PRODUCT_FILTER").document("FILTER_1").get().addOnSuccessListener {
            searchList  = it.get("LIST1") as ArrayList<String>
            searchQueryAdapter.setData(searchList)
            searchQueryAdapter.notifyDataSetChanged()

        }
    }

    override fun onItemClick1(position: Int) {
        Toast.makeText(this,position.toString(),Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick2(position: Int) {
        //TODO("Not yet implemented")
    }

}