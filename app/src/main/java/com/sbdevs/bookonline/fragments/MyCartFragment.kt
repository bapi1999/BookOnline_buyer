package com.sbdevs.bookonline.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.ProceedOrderActivity
import com.sbdevs.bookonline.adapters.CartAdapter
import com.sbdevs.bookonline.databinding.FragmentMyCartBinding
import com.sbdevs.bookonline.models.CartModel
import kotlinx.coroutines.*


class MyCartFragment : Fragment(),CartAdapter.MyonItemClickListener {
    private var _binding: FragmentMyCartBinding?=null
    private val binding get() = _binding!!
    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    var cartList:ArrayList<MutableMap<String,Any>> = ArrayList()

    var sendingList:ArrayList<CartModel> = ArrayList()

    var priceToPay: Int = 0
    var discount = 0
    var totalPrice = 0
    lateinit var swipeRefresh: SwipeRefreshLayout
    lateinit var recyclerView:RecyclerView
    lateinit var adapter:CartAdapter //= CartAdapter(cartList,sendingList,this)

    private var allItemStocked = true

    private val loadingDialog = LoadingDialog()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
//        (activity as AppCompatActivity?)!!.supportActionBar!!.show() todo: to show ActionBar
//        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        _binding = FragmentMyCartBinding.inflate(inflater, container, false)

        loadingDialog.show(childFragmentManager,"Show")

        recyclerView = binding.cartRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.isNestedScrollingEnabled = false



        if (user != null){

            this.lifecycleScope.launch(Dispatchers.IO){
                withContext(Dispatchers.IO){

//                getFirebaseData()
                    getFirebaseData3()
                    delay(1000)
                }

                withContext(Dispatchers.Main){
                    delay(500)
                    calculateThePrice(sendingList)


                }
//            withContext(Dispatchers.Main){
////                delay(500)
//               setValueToTextView()
//
//
//            }
                withContext(Dispatchers.Main){
                    loadingDialog.dismiss()
                }

            }
        }else{
            binding.emptyContainer.visibility = View.VISIBLE
            binding.btnContainer.visibility =View.GONE
            binding.scrollviewCart.visibility = View.GONE
            loadingDialog.dismiss()
        }




        swipeRefresh = binding.swipeRefresh

        adapter = CartAdapter(cartList,this)
        recyclerView.adapter = adapter


        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing =true
            refreshFragment()
//            refreshList()
            setValueToTextView()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()


        binding.proceedBtn.setOnClickListener {
            if (allItemStocked){
                //            val action = MyCartFragmentDirections.actionMyCartFragmentToOrderSummaryFragment()
//            findNavController().navigate(action)
                val intent  = Intent(context,ProceedOrderActivity::class.java)
                intent.putExtra("From_To",1)
                //todo: 1=> MyCart / 2=> BuyNow

                intent.putParcelableArrayListExtra("productList",sendingList);
                intent.putExtra("total_price",totalPrice)
                intent.putExtra("total_discount",discount)
                intent.putExtra("total_amount",priceToPay)

                if(totalPrice ==0 && priceToPay==0){
                    Toast.makeText(context,"Problem in fetching data",Toast.LENGTH_LONG).show()
                }else{
                    startActivity(intent)
                }
            }else{
                val snack = Snackbar.make(it, R.string.snackBarText_outOfStockItem, Snackbar.LENGTH_SHORT)
                snack.show()
            }



        }

    }

    private fun getFirebaseData3(){
         firebaseFirestore.collection("USERS").document(user!!.uid)
             .collection("USER_DATA").document("MY_CART")
            .get().addOnCompleteListener {
                if (it.isSuccessful){
                    val x = it.result?.get("cart_list")
                    if (x ==null){
                        binding.emptyContainer.visibility = View.VISIBLE
                        binding.btnContainer.visibility =View.GONE
                        binding.scrollviewCart.visibility = View.GONE


                    }else{
                        binding.proceedBtn.isEnabled = true
                        binding.proceedBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.purple_500)

                        cartList = x as ArrayList<MutableMap<String, Any>>
                        if (cartList.size == 0){
                            binding.emptyContainer.visibility = View.VISIBLE
                            binding.btnContainer.visibility =View.GONE
                            binding.scrollviewCart.visibility = View.GONE

                        }else{
                            adapter.list = cartList
                            adapter.notifyDataSetChanged()
                            binding.lay2.totalItem.text = "( ${cartList.size} item)"
                            getProduct(cartList)
                        }

                    }
                }
             }

    }
    fun getProduct(list :ArrayList<MutableMap<String, Any>>){
//        var  group:MutableMap<String, Any>
        for (group:MutableMap<String, Any> in list){
            val productId:String = group["product"] as String
            val qty:Long = group["quantity"] as Long

            firebaseFirestore.collection("PRODUCTS").document(productId)
                .get().addOnCompleteListener {
                    if (it.isSuccessful){
                        val url = it.result!!.get("product_thumbnail").toString().trim()
                        val sellerId = it.result!!.getString("PRODUCT_SELLER_ID").toString()
                        val title = it.result!!.getString("book_title")!!
                        val stockQuantity = it.result!!.getLong("in_stock_quantity")!!

                        val priceOriginal = it.result!!.getLong("price_original")!!.toLong()
                        val priceSelling = it.result!!.getLong("price_selling")!!.toLong()

                        sendingList.add(CartModel(productId,sellerId,url,title,priceOriginal,priceSelling,stockQuantity,qty))
//                        adapter.notifyDataSetChanged()

                        if (stockQuantity == 0L){
//                            binding.proceedBtn.isEnabled = false
                            binding.proceedBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.gray_400)
                            allItemStocked = false

                        }

                        swipeRefresh.isRefreshing = false

                    }
                }

        }

    }

    override fun onItemClick(position: Int) {
        cartList.removeAt(position)
        adapter.notifyItemRemoved(position)
        val cartmap:MutableMap<String,Any> = HashMap()
        cartmap["cart_list"] = cartList
        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_CART").update(cartmap).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(context,R.string.snackBarText_itemRemoved,Toast.LENGTH_SHORT).show()

                    refreshFragment()
                }else{
                    Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show()
                }
            }



    }

    override fun onQuantityChange(position: Int, textView: TextView) {
        val qtyDialog :Dialog = Dialog(requireContext())
        qtyDialog.setContentView(R.layout.ar_qualtity_dialog)
        qtyDialog.setCancelable(false)
        val cartModelAtIndex = sendingList[position]
        qtyDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(requireActivity().applicationContext, R.drawable.s_shape_bg_2)
        )

        qtyDialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        qtyDialog.show()

        val enterQuantity:TextInputLayout = qtyDialog.findViewById(R.id.enter_quantity_input)

        val cancelBtn:TextView = qtyDialog.findViewById(R.id.cancel_button)
        cancelBtn.setOnClickListener {
            qtyDialog.dismiss()
        }

//        Toast.makeText(context,"$position --- ${cartModelAtIndex.productId}",Toast.LENGTH_LONG).show()
        val continueBtn:TextView = qtyDialog.findViewById(R.id.continue_to_cart)

        continueBtn.setOnClickListener {

            val qty = enterQuantity.editText!!.text.toString().trim()

            if (qty.isNotEmpty() && qty.toInt() != 0 ){
                if(qty.toInt()>cartModelAtIndex.stockQty){
                    Toast.makeText(context,"Your entered Quantity exceeds Stock Quantity",Toast.LENGTH_LONG).show()

                }else{
                    textView.text = qty

//                    sendingList[position] = CartModel(cartModelAtIndex.productId,cartModelAtIndex.sellerId,cartModelAtIndex.url,
//                        cartModelAtIndex.title,cartModelAtIndex.price,cartModelAtIndex.inStock,cartModelAtIndex.stockQty,
//                        cartModelAtIndex.offerPrice,qty.toLong())
//
//
//                    adapter.notifyItemChanged(position)
////                    adapter.notifyDataSetChanged()
//                    calculateThePrice(sendingList)

                    updateProductQuantityInsideDB(cartModelAtIndex.productId,qty.toLong(),position)
                    qtyDialog.dismiss()
                    refreshFragment()


                }

                //Todo- only change the orderQty and all other field remain same
            }
        }

    }

    private fun setValueToTextView(){
        binding.lay2.totalPrice.text = totalPrice.toString()
        binding.lay2.totalDiscount.text = discount.toString()

        binding.lay2.amountToPay.text = priceToPay.toString()
        binding.totalAmount.text = priceToPay.toString()
    }

    fun refreshList(){
        getFirebaseData3()
    }
    private fun refreshFragment(){
        val navController: NavController = requireActivity().findNavController(R.id.nav_host_fragment)
        navController.run {
            popBackStack()
            navigate(R.id.myCartFragment)
        }
    }

    private fun calculateThePrice(list: ArrayList<CartModel> ){

        priceToPay = 0
        discount = 0
        totalPrice = 0

        for ( group  in list){

            val priceOriginal:Long = group.priceOriginal
            val priceSelling:Long = group.priceSelling
            val quantity:Long = group.orderQuantity

            if (priceOriginal == 0L){

                priceToPay += priceSelling.toInt()*quantity.toInt()

            }else{
                discount += (priceOriginal.toInt() - priceSelling.toInt())*quantity.toInt()
                priceToPay += priceSelling.toInt()*quantity.toInt()

            }
            totalPrice = priceToPay+discount
            setValueToTextView()

        }
    }

    private fun updateProductQuantityInsideDB(productId:String, qty:Long, position: Int){
        val listMap:MutableMap<String,Any> = HashMap()
        listMap["product"] = productId
        listMap["quantity"] = qty
        cartList[position] = listMap

        val cartmap:MutableMap<String,Any> = HashMap()
        cartmap["cart_list"] = cartList

        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_CART").update(cartmap)
            .addOnCompleteListener {
                if (it.isSuccessful){
//                    Toast.makeText(context,"Successful",Toast.LENGTH_SHORT).show()
                }else{
//                    Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show()
                }
            }
    }




}

