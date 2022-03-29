package com.sbdevs.bookonline.activities.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.java.SearchActivity2
import com.sbdevs.bookonline.adapters.uiadapter.ProductGrid2Adapter
import com.sbdevs.bookonline.databinding.ActivitySellerShopBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.models.SearchModel

class SellerShopActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySellerShopBinding

    private val firebaseFirestore = Firebase.firestore

    private lateinit var horizontalRecycler:RecyclerView

    private lateinit var gridRecycler:RecyclerView
    private val gridList:ArrayList<SearchModel> = ArrayList()
    private val gridAdapter:ProductGrid2Adapter= ProductGrid2Adapter(gridList)

    private val visible = View.VISIBLE
    private val gone = View.GONE
    private val loadingDialog = LoadingDialog()

    private lateinit var shopNameText:TextView
    private lateinit var shopAddrerss:TextView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerShopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val lay1 = binding.lay1
        val lay2 = binding.lay2
        val lay3 = binding.lay3
        shopNameText = binding.shopName
        shopAddrerss = binding.lay1.sellerAddress
        lay2.batchBackground.backgroundTintList =AppCompatResources.getColorStateList(this,R.color.white)
        lay2.batchHeader.setTextColor(AppCompatResources.getColorStateList(this,R.color.grey_900))
        lay3.batchBackground.backgroundTintList =AppCompatResources.getColorStateList(this,R.color.white)
        lay3.batchHeader.setTextColor(AppCompatResources.getColorStateList(this,R.color.grey_900))
        horizontalRecycler = lay2.productHorizontalRecycler

        val actionBar = binding.toolbar
        setSupportActionBar(actionBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        gridRecycler = lay3.productGridRecycler
        gridRecycler.layoutManager = GridLayoutManager(this,2)
        gridRecycler.adapter = gridAdapter
        loadingDialog.show(supportFragmentManager,"show")
        val seller = intent.getStringExtra("sellerId").toString()
        getBestSellingProduct(seller)
        getBusinessDetails(seller)


    }

    override fun onStart() {
        super.onStart()

        binding.searchBtn.setOnClickListener {
            val searchIntent = Intent(this,
                SearchActivity2::class.java)
            startActivity(searchIntent)
        }

        binding.lay3.viewAllButton.setOnClickListener {
            val searchIntent = Intent(this,
                SearchActivity2::class.java)
            startActivity(searchIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            android.R.id.home -> {
                finish()
            }
            else -> {
                Log.i("", "")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getBestSellingProduct(sellerId:String){
        firebaseFirestore.collection("PRODUCTS")
            .whereEqualTo("PRODUCT_SELLER_ID",sellerId)
            .orderBy("number_of_item_sold", Query.Direction.DESCENDING)
            .limit(16L).get().addOnSuccessListener {

            val allDocumentSnapshot = it.documents
            Log.e("QuerySnapshot size","${allDocumentSnapshot.size}")

            if (allDocumentSnapshot.isNotEmpty()){

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

                    gridList.add(
                        SearchModel(productId, productName, productImgList, priceOriginal, priceSelling,
                        stockQty, avgRating, totalRatings, bookCondition, bookType, printedYear)
                    )
                }




            }else{

            }



            if (gridList.isEmpty()){
                gridRecycler.visibility = gone
            }
            else{
                gridRecycler.visibility = visible

                gridAdapter.list = gridList
                gridAdapter.notifyDataSetChanged()

            }


            loadingDialog.dismiss()
        }.addOnFailureListener {
            Log.e("get search query 0", "${it.message}")
            loadingDialog.dismiss()
        }
    }

    private fun getBusinessDetails(sellerId:String){
        firebaseFirestore.collection("USERS")
            .document(sellerId)
            .collection("SELLER_DATA")
            .document("BUSINESS_DETAILS").get()
            .addOnSuccessListener {
                val shopName:String = it.getString("Business_name").toString()
                val addressMap:Map<String,Any> = (it.get("address") as Map<String, Any>?)!!

                val city = addressMap["Town_Vill"].toString()
                val state = addressMap["State"].toString()
                val st:String = "$city, $state"

                shopNameText.text = shopName
                shopAddrerss.text = st

            }
            .addOnFailureListener {

            }
    }




}