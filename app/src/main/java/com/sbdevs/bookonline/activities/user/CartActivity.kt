package com.sbdevs.bookonline.activities.user

import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.user.CartAdapter
import com.sbdevs.bookonline.databinding.ActivityCartBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.models.user.CartMapModel
import com.sbdevs.bookonline.models.user.CartModel
import com.sbdevs.bookonline.othercalss.SharedDataClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CartActivity : AppCompatActivity(), CartAdapter.MyOnItemClickListener {

    private lateinit var binding: ActivityCartBinding

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    var cartList:ArrayList<MutableMap<String,Any>> = ArrayList()
    var sendingList:ArrayList<CartModel> = ArrayList()

    var netSellingPrice: Int = 0
    var discount = 0
    var sellingPrice = 0
    var deliveryCharge = 0L
    var totalPriceToPay = 0
    lateinit var swipeRefresh: SwipeRefreshLayout
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: CartAdapter //= CartAdapter(cartList,sendingList,this)

    private var allItemStocked = true

    private val loadingDialog = LoadingDialog()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        loadingDialog.show(supportFragmentManager,"Show")

        recyclerView = binding.cartRecycler
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false

        val actionBar = binding.toolbar
        setSupportActionBar(actionBar)
//        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        if (user != null){

            this.lifecycleScope.launch(Dispatchers.IO){
                getFirebaseData3()

            }
        }else{
            binding.emptyContainer.visibility = View.VISIBLE
            binding.btnContainer.visibility =View.GONE
            binding.scrollviewCart.visibility = View.GONE
            loadingDialog.dismiss()
        }




        swipeRefresh = binding.swipeRefresh

        adapter = CartAdapter(sendingList,this)
        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false


        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing =true
            //refreshFragment()
            //refreshList()
            setValueToTextView()
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        binding.proceedBtn.setOnClickListener {
            if (allItemStocked){

                val intent  = Intent(this, ProceedOrderActivity::class.java)
                intent.putExtra("From_To",1)
                //todo: 1=> MyCart / 2=> BuyNow

                intent.putParcelableArrayListExtra("productList",sendingList);
                intent.putExtra("total_amount",totalPriceToPay)

                if(sellingPrice ==0 && netSellingPrice==0){
                    Toast.makeText(this,"Problem in fetching data",Toast.LENGTH_LONG).show()
                }else{
                    startActivity(intent)
                }
            }else{
                val snack = Snackbar.make(it, R.string.snackBarText_outOfStockItem, Snackbar.LENGTH_SHORT)
                snack.show()
            }



        }
    }


    private suspend fun getFirebaseData3(){
        firebaseFirestore.collection("USERS").document(user!!.uid)
            .collection("USER_DATA").document("MY_CART")
            .get().addOnSuccessListener {
                val x = it.get("cart_list")
                if (x ==null){
                    binding.emptyContainer.visibility = View.VISIBLE
                    binding.btnContainer.visibility = View.GONE
                    binding.scrollviewCart.visibility = View.GONE
                    loadingDialog.dismiss()

                }else{

                    cartList = x as ArrayList<MutableMap<String, Any>>

                    if (cartList.isEmpty()){

                        binding.emptyContainer.visibility = View.VISIBLE
                        binding.btnContainer.visibility = View.GONE
                        binding.scrollviewCart.visibility = View.GONE
                        loadingDialog.dismiss()

                    }else{

                        binding.proceedBtn.isEnabled = true
                        binding.proceedBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.purple_500)


                        binding.lay2.totalItem.text = "( ${cartList.size} item)"

                        lifecycleScope.launch {
                            getProduct(cartList)
                        }


                    }

                }

            }.addOnFailureListener {
                Log.e("Fetch cartList","${it.message}")
            }.await()

    }



    private suspend fun getProduct(list :ArrayList<MutableMap<String, Any>>){
        var count = 0
        for (group in list){
            val productId:String = group["product"] as String
            val qty:Long = group["quantity"] as Long

            firebaseFirestore.collection("PRODUCTS").document(productId)
                .get().addOnSuccessListener {

                    val productImgList = it.get("productImage_List") as ArrayList<String>
                    val sellerId = it.getString("PRODUCT_SELLER_ID").toString()
                    val title = it.getString("book_title")!!
                    val stockQuantity = it.getLong("in_stock_quantity")!!
                    val sellerProfit = it.getString("SELLER_PROFIT")!!

                    val priceOriginal = it.getLong("price_original")!!.toLong()
                    val priceSelling = it.getLong("price_selling")!!.toLong()
                    val returnPolicy = it.getString("Replacement_policy")!!

                    var deliveryCharge1 = 40L

                    when  {
                        priceSelling in 1..99 -> {
                            deliveryCharge1 = 80
                        }

                        priceSelling in 100..199 -> {
                            deliveryCharge1 = 60
                        }

                        priceSelling in 200..499 -> {
                            deliveryCharge1 = 40
                        }

                        priceSelling > 500 -> {
                            deliveryCharge1 = 0
                        }


                    }

                    sendingList.add(CartModel(productId,sellerId,productImgList[0],title,priceOriginal,priceSelling,deliveryCharge1,stockQuantity,qty,returnPolicy,sellerProfit))


                    if (stockQuantity == 0L){
                        binding.proceedBtn.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.grey_400)
                        allItemStocked = false

                    }

                    if (count == list.size - 1){
                        loadingDialog.dismiss()
                        adapter.list = sendingList
                        adapter.notifyDataSetChanged()
                        calculateThePrice(sendingList)
                    }

                    swipeRefresh.isRefreshing = false
                }.addOnFailureListener {
                    Log.e("Individual product Data fetch","${it.message}")
                    loadingDialog.dismiss()
                }.await()


            count +=1
        }

    }

    override fun onItemRemoveClick(position: Int) {
        removeItemFromCart(position)

    }

    override fun onQuantityChange(position: Int, textView: TextView, quantity: Long, type: String) {

        val cartModelAtIndex = sendingList[position]
        val stockQty = cartModelAtIndex.stockQty
        if (type=="-ve"){
            val newQty = quantity-1
            if(newQty<1){
                removeItemFromCart(position)
            }else{
                updateProductQuantityInsideDB(cartModelAtIndex.productId,newQty,position,textView)
            }

        }else{
            //+ve

            val newQty = quantity+1
            if(newQty>stockQty){
                Toast.makeText(this,"Order quantity exceeds Stock quantity", Toast.LENGTH_LONG).show()
            }else{
                updateProductQuantityInsideDB(cartModelAtIndex.productId,newQty,position,textView)
            }
        }

    }


    private fun removeItemFromCart(position: Int){
        cartList.removeAt(position)
        sendingList.removeAt(position)
        SharedDataClass.dbCartList.removeAt(position)
        adapter.notifyItemRemoved(position)

        val cartmap:MutableMap<String,Any> = HashMap()
        cartmap["cart_list"] = cartList

        firebaseFirestore.collection("USERS").document(user!!.uid)
            .collection("USER_DATA")
            .document("MY_CART").update(cartmap)
            .addOnSuccessListener {

                Log.i("product remover from cart","successful")

                if (sendingList.size == 0){
                    binding.emptyContainer.visibility = View.VISIBLE
                    binding.btnContainer.visibility = View.GONE
                    binding.scrollviewCart.visibility = View.GONE
                }else{
                    calculateThePrice(sendingList)
                }


            }.addOnFailureListener {
                Log.e("product remover from cart","Failed: ${it.message}")
            }

    }

    private fun setValueToTextView(){
        binding.lay2.totalSellingPrice.text = sellingPrice.toString()
        binding.lay2.totalDiscount.text = "-$discount"


        binding.lay2.totalItem.text = "( ${cartList.size} item)"

        totalPriceToPay = (deliveryCharge+netSellingPrice).toInt()

        binding.lay2.deliveryCharge.text = deliveryCharge.toString()
        binding.lay2.amountToPay.text = totalPriceToPay.toString()
        binding.totalAmount.text = totalPriceToPay.toString()

    }

    private fun calculateThePrice(list: ArrayList<CartModel> ){

        netSellingPrice = 0
        discount = 0
        sellingPrice = 0

        deliveryCharge = 0
        totalPriceToPay = 0

        var counter = 0
        for ( group  in list){

            val priceOriginal:Long = group.priceOriginal
            val priceSelling:Long = group.priceSelling
            val quantity:Long = group.orderQuantity

            deliveryCharge += group.deliveryCharge

            discount += if (priceOriginal == 0L){
                0
            }else{
                (priceOriginal.toInt() - priceSelling.toInt())*quantity.toInt()
            }

            netSellingPrice += priceSelling.toInt()*quantity.toInt()


            sellingPrice = netSellingPrice+discount

            if (counter == list.size - 1){
                setValueToTextView()
            }
            counter += 1


        }
    }

    private fun updateProductQuantityInsideDB(productId:String, qty:Long, position: Int,textView: TextView){
        val listMap:MutableMap<String,Any> = HashMap()
        listMap["product"] = productId
        listMap["quantity"] = qty
        cartList[position] = listMap

        val cartmap:MutableMap<String,Any> = HashMap()
        cartmap["cart_list"] = cartList

        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_CART").update(cartmap)
            .addOnSuccessListener {

                textView.text = qty.toString()
                sendingList[position].orderQuantity = qty
                adapter.notifyItemChanged(position)
                calculateThePrice(sendingList)

            }
    }


}