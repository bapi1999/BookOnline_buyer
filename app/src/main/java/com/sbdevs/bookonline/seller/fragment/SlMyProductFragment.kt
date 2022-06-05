package com.sbdevs.bookonline.seller.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.fragment.app.commit
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.FragmentSlMyProductBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.seller.activities.SlAddProductActivity
import com.sbdevs.bookonline.seller.activities.SlEarningActivity
import com.sbdevs.bookonline.seller.adapters.MyProductAdapter
import com.sbdevs.bookonline.seller.models.MyProductModel

class SlMyProductFragment : Fragment() {
    private var _binding: FragmentSlMyProductBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private lateinit var recyclerView:RecyclerView
    private lateinit var productAdapter: MyProductAdapter
    private var productlist:ArrayList<MyProductModel> = ArrayList()

    private lateinit var searchContainer :LinearLayout
    private lateinit var searchView: SearchView

    private val loadingDialog = LoadingDialog()
    private var dateModified = Query.Direction.DESCENDING
    private var searchCode:Int = 0

    private val gone = View.GONE
    private val visible = View.VISIBLE

    private var lastResult:DocumentSnapshot ? = null
    private lateinit var times:Timestamp
    private var inStockOrder:Long = 0L
    private var isReachLast:Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlMyProductBinding.inflate(inflater, container, false)

        val bottomBar = binding.bottomBar
        bottomBar.orderIcon.setImageResource(R.drawable.ic_order_icon_3_outline)
        bottomBar.productIcon.setImageResource(R.drawable.ic_shopping_cart_24)
        bottomBar.earningIcon.setImageResource(R.drawable.ic_outline_payments_24)
        bottomBar.profileIcon.setImageResource(R.drawable.ic_account_circle_outline_24)


        searchContainer = binding.searchContainer
        searchView = binding.searchView

        recyclerView = binding.myProductRecycler
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        productAdapter = MyProductAdapter(productlist)
        recyclerView.adapter = productAdapter
        loadingDialog.show(childFragmentManager,"Show")
        getMyProduct(dateModified)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomBar = binding.bottomBar


        bottomBar.orderContainer.setOnClickListener {
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.main_frame_layout, SlOrderFragment())
                addToBackStack("sl_order")
            }
        }


        bottomBar.addProductContainer.setOnClickListener {
            val newIntent = Intent(requireContext(), SlAddProductActivity::class.java)
            startActivity(newIntent)
            bottomBar.addProductContainer.isClickable = false
        }


        bottomBar.earningContainer.setOnClickListener {
            val newIntent = Intent(requireContext(), SlEarningActivity::class.java)
            startActivity(newIntent)
            bottomBar.earningContainer.isClickable = false
        }


        bottomBar.profileContainer.setOnClickListener {
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.main_frame_layout, SlProfileFragment())
                addToBackStack("sl_profile")
            }
        }





        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)


                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    // end scrolling: do what you want here and after calling the function change the value of boolean

                    if (isReachLast){
                        Log.w("Query item","Last item is reached already")
                        binding.progressBar2.visibility = View.GONE

                    }else{
                        binding.progressBar2.visibility = View.VISIBLE

                        Log.e("last query", "${lastResult.toString()}")
                        when(searchCode){
                            0 ->{

                                getMyProduct(dateModified)
                            }
                            1 -> {
                                //out of stock = 1
                                getOutOfStockProduct(dateModified)
                            }
                            2 -> {
                                //low in stock = 2
                                getLowStockProduct(dateModified)
                            }
                            3 -> {
                                //hidden = 3
                                getHiddenProduct(dateModified)
                            }
                            else -> {
                                //all = 0
                                getMyProduct(dateModified)
                            }
                        }
                    }

                }

            }

        })



        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()){

                    changeProductType()
                    getProductBySKU(query)
                }

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })






        binding.productTypeRadioGroup.setOnCheckedChangeListener { group, checkedId ->

            when (checkedId) {
                R.id.radioButton1 -> {
                    //all = 0
                    searchCode = 0
                    changeProductType()
                    getMyProduct(dateModified)
                }
                R.id.radioButton2 -> {
                    //out of stock = 1
                    searchCode = 1
                    changeProductType()
                    getOutOfStockProduct(dateModified)
                }
                R.id.radioButton3 -> {
                    //low in stock = 2
                    searchCode = 2
                    changeProductType()
                    getLowStockProduct(dateModified)
                }
                R.id.radioButton4 -> {
                    //hidden = 3
                    searchCode = 3
                    changeProductType()
                    getHiddenProduct(dateModified)
                }
            }
        }



    }

    override fun onResume() {
        super.onResume()
        binding.bottomBar.earningContainer.isClickable = true
        binding.bottomBar.addProductContainer.isClickable = true

    }



    private fun changeProductType(){

        productlist.clear()
        productAdapter.notifyDataSetChanged()
        lastResult = null
        isReachLast = false
        loadingDialog.show(childFragmentManager,"Show")

    }




    private fun getMyProduct(direction:Query.Direction){
        val resultList:ArrayList<MyProductModel> = ArrayList()

        val query:Query = if (lastResult == null){
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereEqualTo("hide_this_product",false)
                .orderBy("PRODUCT_UPDATE_ON",direction)
        }else{
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereEqualTo("hide_this_product",false)
                .orderBy("PRODUCT_UPDATE_ON",direction)
                .startAfter(times)

        }

        query.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){

                for (item in allDocumentSnapshot){
                    val documentId = item.id
                    val sku = item.getString("SKU").toString()
                    val productName = item.getString("book_title").toString()
                    val productImgList:ArrayList<String> = item.get("productImage_List") as ArrayList<String>
                    val stockQty: Long = item.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = item.getString("rating_avg")!!
                    val totalRatings: Long = item.getLong("rating_total")!!
                    val priceOriginal = item.getLong("price_original")!!.toLong()
                    val priceSelling = item.getLong("price_selling")!!.toLong()
                    val updateDate = item.getTimestamp("PRODUCT_UPDATE_ON")!!.toDate()

                    resultList.add(MyProductModel(documentId,sku,productName,productImgList,priceSelling,priceOriginal,avgRating,totalRatings,stockQty,updateDate))
                }

                productlist.addAll(resultList)

                if (productlist.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.myProductRecycler.visibility = gone
                }else{
                    binding.emptyContainer.visibility = gone
                    binding.myProductRecycler.visibility = visible

                    productAdapter.list = productlist

                    if (lastResult == null ){
                        productAdapter.notifyItemRangeInserted(0,resultList.size)
                    }else{
                        productAdapter.notifyItemRangeInserted(productlist.size-1,resultList.size)
                    }

                    val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                    lastResult = lastR
                    times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!
                }

                isReachLast = allDocumentSnapshot.size < 10

            }else{
                isReachLast = true
                if (productlist.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.myProductRecycler.visibility = gone
                }
            }

            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone
        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")
            binding.progressBar2.visibility = gone
            loadingDialog.dismiss()

        }

    }

    private fun getOutOfStockProduct(direction:Query.Direction){
        val resultList:ArrayList<MyProductModel> = ArrayList()

        val query:Query = if (lastResult == null){
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereEqualTo("in_stock_quantity",0L)
                .orderBy("PRODUCT_UPDATE_ON",direction)
        }else{
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereEqualTo("in_stock_quantity",0L)
                .orderBy("PRODUCT_UPDATE_ON",direction)
                .startAfter(times)

        }


        query.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents


            if (allDocumentSnapshot.isNotEmpty()){

                for (item in allDocumentSnapshot){
                    val documentId = item.id
                    val sku = item.getString("SKU").toString()
                    val productName = item.getString("book_title").toString()
                    val productImgList:ArrayList<String> = item.get("productImage_List") as ArrayList<String>
                    val stockQty: Long = item.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = item.getString("rating_avg")!!
                    val totalRatings: Long = item.getLong("rating_total")!!
                    val priceOriginal = item.getLong("price_original")!!.toLong()
                    val priceSelling = item.getLong("price_selling")!!.toLong()
                    val updateDate = item.getTimestamp("PRODUCT_UPDATE_ON")!!.toDate()

                    resultList.add(MyProductModel(documentId,sku,productName,productImgList,priceSelling,priceOriginal,avgRating,totalRatings,stockQty,updateDate))
                }

                productlist.addAll(resultList)

                if (productlist.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.myProductRecycler.visibility = gone
                }else{
                    binding.emptyContainer.visibility = gone
                    binding.myProductRecycler.visibility =visible

                    productAdapter.list = productlist

                    if (lastResult == null ){
                        productAdapter.notifyItemRangeInserted(0,resultList.size)
                    }else{
                        productAdapter.notifyItemRangeInserted(productlist.size-1,resultList.size)
                    }

                    val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                    lastResult = lastR
                    times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!
                }
                isReachLast = allDocumentSnapshot.size < 10

            }else{
                isReachLast = true
                if (productlist.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.myProductRecycler.visibility = gone
                }
            }

            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone

        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")
            loadingDialog.dismiss()

        }

    }



    private fun getLowStockProduct(direction:Query.Direction){
        val resultList:ArrayList<MyProductModel> = ArrayList()

        val query:Query = if (lastResult == null){
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereLessThan("in_stock_quantity",5L)
                .orderBy("in_stock_quantity")
                .orderBy("PRODUCT_UPDATE_ON",direction)
        }else{
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereLessThan("in_stock_quantity",5L)
                .orderBy("in_stock_quantity")
                .orderBy("PRODUCT_UPDATE_ON",direction)
                .startAfter(times)

        }


        query.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){
                for (item in allDocumentSnapshot){
                    val documentId = item.id
                    val sku = item.getString("SKU").toString()
                    val productName = item.getString("book_title").toString()
                    val productImgList:ArrayList<String> = item.get("productImage_List") as ArrayList<String>
                    val stockQty: Long = item.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = item.getString("rating_avg")!!
                    val totalRatings: Long = item.getLong("rating_total")!!
                    val priceOriginal = item.getLong("price_original")!!.toLong()
                    val priceSelling = item.getLong("price_selling")!!.toLong()
                    val updateDate = item.getTimestamp("PRODUCT_UPDATE_ON")!!.toDate()

                    resultList.add(MyProductModel(documentId,sku,productName,productImgList,priceSelling,priceOriginal,avgRating,totalRatings,stockQty,updateDate))
                }

                productlist.addAll(resultList)

                if (productlist.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.myProductRecycler.visibility = gone
                }else{
                    binding.emptyContainer.visibility = gone
                    binding.myProductRecycler.visibility =visible

                    productAdapter.list = productlist

                    if (lastResult == null ){
                        productAdapter.notifyItemRangeInserted(0,resultList.size)
                    }else{
                        productAdapter.notifyItemRangeInserted(productlist.size-1,resultList.size)
                    }
                    val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                    lastResult = lastR
                    times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!
                }

                isReachLast = allDocumentSnapshot.size < 10
            }else{
                isReachLast = true
                if (productlist.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.myProductRecycler.visibility = gone
                }

            }

            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone
        }.addOnFailureListener {
            Log.e("getLowStockProduct error","${it.message}")
            loadingDialog.dismiss()

        }

    }

    private fun getHiddenProduct(direction:Query.Direction){
        val resultList:ArrayList<MyProductModel> = ArrayList()

        val query:Query = if (lastResult == null){
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereEqualTo("hide_this_product",true)
                .orderBy("PRODUCT_UPDATE_ON",direction)
        }else{
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereEqualTo("hide_this_product",true)
                .orderBy("PRODUCT_UPDATE_ON",direction)
                .startAfter(times)

        }

        query.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){

                for (item in allDocumentSnapshot){
                    val documentId = item.id
                    val sku = item.getString("SKU").toString()
                    val productName = item.getString("book_title").toString()
                    val productImgList:ArrayList<String> = item.get("productImage_List") as ArrayList<String>
                    val stockQty: Long = item.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = item.getString("rating_avg")!!
                    val totalRatings: Long = item.getLong("rating_total")!!
                    val priceOriginal = item.getLong("price_original")!!.toLong()
                    val priceSelling = item.getLong("price_selling")!!.toLong()
                    val updateDate = item.getTimestamp("PRODUCT_UPDATE_ON")!!.toDate()
                    resultList.add(MyProductModel(documentId,sku,productName,productImgList,priceSelling,priceOriginal,avgRating,totalRatings,stockQty,updateDate))
                }

                productlist.addAll(resultList)

                if (productlist.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.myProductRecycler.visibility = gone
                }else{
                    binding.emptyContainer.visibility =gone
                    binding.myProductRecycler.visibility = visible

                    productAdapter.list = productlist

                    if (lastResult == null ){
                        productAdapter.notifyItemRangeInserted(0,resultList.size)
                    }else{
                        productAdapter.notifyItemRangeInserted(productlist.size-1,resultList.size)
                    }

                    val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                    lastResult = lastR
                    times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!

                }

                isReachLast = allDocumentSnapshot.size < 10


            }else{
                isReachLast = true
                if (productlist.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.myProductRecycler.visibility = gone
                }
            }


            loadingDialog.dismiss()
            binding.progressBar2.visibility =gone
        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")
            loadingDialog.dismiss()

        }

    }


    private fun getProductBySKU(sku:String){
        val resultList:ArrayList<MyProductModel> = ArrayList()

        val query:Query = firebaseFirestore.collection("PRODUCTS")
            .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
            .whereEqualTo("SKU",sku)

        query.get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents


            if (allDocumentSnapshot.isNotEmpty()){

                for (item in allDocumentSnapshot){
                    val documentId = item.id
                    val sku = item.getString("SKU").toString()
                    val productName = item.getString("book_title").toString()
                    val productImgList:ArrayList<String> = item.get("productImage_List") as ArrayList<String>
                    val stockQty: Long = item.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = item.getString("rating_avg")!!
                    val totalRatings: Long = item.getLong("rating_total")!!
                    val priceOriginal = item.getLong("price_original")!!.toLong()
                    val priceSelling = item.getLong("price_selling")!!.toLong()
                    val updateDate = item.getTimestamp("PRODUCT_UPDATE_ON")!!.toDate()

                    resultList.add(MyProductModel(documentId,sku,productName,productImgList,priceSelling,priceOriginal,avgRating,totalRatings,stockQty,updateDate))
                }

                isReachLast = true

                productlist.addAll(resultList)

                if (productlist.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.myProductRecycler.visibility = gone
                }else{
                    binding.emptyContainer.visibility =gone
                    binding.myProductRecycler.visibility = visible

                    if (lastResult == null ){
                        productAdapter.list = productlist
                        productAdapter.notifyItemRangeInserted(0,resultList.size)
                    }else{
                        productAdapter.notifyItemRangeInserted(productlist.size-1,resultList.size)
                    }
                }

            }else{
                isReachLast = true
                if (productlist.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.myProductRecycler.visibility = gone
                }
            }

            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone
        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")
            loadingDialog.dismiss()

        }

    }


}