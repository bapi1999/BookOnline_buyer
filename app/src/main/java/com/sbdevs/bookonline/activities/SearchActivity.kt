package com.sbdevs.bookonline.activities

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.databinding.ActivitySearchBinding
import java.util.*
import com.google.firebase.database.Query
import com.google.firebase.database.ktx.database
import com.sbdevs.bookonline.adapters.FireBaseAdapter1
import com.sbdevs.bookonline.models.QueryModel1


class SearchActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySearchBinding

    lateinit var adapter1:FireBaseAdapter1
    private lateinit var searchRecycler:RecyclerView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar:Toolbar = binding.toolbar2
        setSupportActionBar(toolbar)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                val intent = Intent(this@SearchActivity,SearchFilterActivity::class.java)
                intent.putExtra("query",query)
                startActivity(intent)
                finish()

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    searchText(newText)
                }else{
                    productNames()
                }
                return false
            }
        })



        val query: Query = Firebase.database
            .reference
            .child("for_test")
            .limitToLast(20)


        val options: FirebaseRecyclerOptions<QueryModel1> = FirebaseRecyclerOptions.Builder<QueryModel1>()
            .setQuery(query, QueryModel1::class.java)
            .build()


        adapter1 = FireBaseAdapter1(options)


        searchRecycler = binding.searchRecycler
        searchRecycler.setHasFixedSize(true)
        searchRecycler.layoutManager = LinearLayoutManager(this)


        searchRecycler.adapter = adapter1


    }

    private fun productNames(){
        val query: Query = Firebase.database
            .reference
            .child("for_test")
            .limitToLast(20)


        val options: FirebaseRecyclerOptions<QueryModel1> = FirebaseRecyclerOptions.Builder<QueryModel1>()
            .setQuery(query, QueryModel1::class.java)
            .build()


        adapter1 = FireBaseAdapter1(options)
        adapter1.startListening()
        adapter1.notifyDataSetChanged()
        searchRecycler.adapter = adapter1
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

    override fun onStart() {
        super.onStart()
        adapter1.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter1.stopListening()
    }

    private fun searchText(seractString:String){
        val query: Query = Firebase.database
            .reference
            .child("for_products")
            .orderByChild("name")
            .startAt(seractString)
            .limitToLast(20)


        val options: FirebaseRecyclerOptions<QueryModel1> = FirebaseRecyclerOptions.Builder<QueryModel1>()
            .setQuery(query, QueryModel1::class.java)
            .build()

        adapter1 = FireBaseAdapter1(options)
        adapter1.startListening()
        adapter1.notifyDataSetChanged()
        searchRecycler.adapter = adapter1

    }


}