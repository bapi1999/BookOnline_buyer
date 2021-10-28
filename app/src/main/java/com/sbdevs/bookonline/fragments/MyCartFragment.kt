package com.sbdevs.bookonline.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
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
    private val user = FirebaseAuth.getInstance().currentUser


    var list:ArrayList<MutableMap<String,Any>> = ArrayList()

    var sendingList:ArrayList<CartModel> = ArrayList()

    var priceString: Int = 0
    var discount = 0
    var result= 0
    lateinit var recyclerView:RecyclerView



    var adapter:CartAdapter = CartAdapter(list,this)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
//        (activity as AppCompatActivity?)!!.supportActionBar!!.show() todo: to show ActionBar
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        _binding = FragmentMyCartBinding.inflate(inflater, container, false)

        val loadingDialog :Dialog = Dialog(activity!!)
        loadingDialog.setContentView(R.layout.le_loading_progress_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(activity!!.applicationContext, R.drawable.s_shape_bg_2)
        )
        loadingDialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        loadingDialog.show()

        recyclerView = binding.cartRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.isNestedScrollingEnabled = false

        lifecycleScope.launch(Dispatchers.IO){
            withContext(Dispatchers.IO){

//                getFirebaseData()
                getFirebaseData3()
                delay(1000)
            }


            withContext(Dispatchers.Main){
//                delay(500)
                binding.lay2.totalPrice.text = result.toString()
                binding.lay2.totalDiscount.text = discount.toString()

                binding.lay2.amountToPay.text = priceString.toString()
                binding.totalAmount.text = priceString.toString()

                val lList = sendingList


            }
            withContext(Dispatchers.Main){
                loadingDialog.dismiss()
            }

        }



        binding.proceedBtn.setOnClickListener {
//            val action = MyCartFragmentDirections.actionMyCartFragmentToOrderSummaryFragment()
//            findNavController().navigate(action)
            val intent  = Intent(context,ProceedOrderActivity::class.java)
            intent.putExtra("From_To",1)
            //todo: 1=> MyCart / 2=> BuyNow
            intent.putParcelableArrayListExtra("productList",sendingList);
            intent.putExtra("total_price",result)
            intent.putExtra("total_discount",discount)
            intent.putExtra("total_amount",priceString)
            startActivity(intent)

        }


        adapter = CartAdapter(list,this)
        recyclerView.adapter = adapter

        return binding.root
    }


//    private fun getFirebaseData2() = CoroutineScope(Dispatchers.IO).launch {
//         firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
//            .document("MY_CART").collection("CART")
//            .orderBy("priority", Query.Direction.ASCENDING).limit(12)
//            .get().addOnSuccessListener {
//                for (query:DocumentSnapshot in it){
//                    if (query.exists()){
//                        val view_type = query.getLong("view_type")!!
//                        val view_ID:String = query.getString("view_ID")!!
//                        val quantity:Long= query.getLong("quantity")!!
////                        val priority:Long = query.getLong("priority")!!
//                        getcal(view_ID,quantity)
//
//                        list.add(CartModel(view_ID,quantity,priority, view_type))
//                    }
//                }
//                 adapter.cartModelList =list
//                 adapter.notifyDataSetChanged()
//
//            }.await()
//
//
//
//    }


    fun getFirebaseData3(){
         firebaseFirestore.collection("USERS").document(user!!.uid)
             .collection("USER_DATA").document("MY_CART")
            .get().addOnCompleteListener {
                if (it.isSuccessful){
                    val x = it.result?.get("cart_list")
                    if (x ==null){
                        binding.emptyContainer.visibility = View.VISIBLE
                        binding.linearLayout10.visibility =View.GONE
                        binding.scrollviewCart.visibility = View.GONE
                        binding.proceedBtn.isEnabled = false
                        binding.proceedBtn.backgroundTintList = ContextCompat.getColorStateList(context!!, R.color.gray_800)
                    }else{
                        binding.proceedBtn.isEnabled = true
                        binding.proceedBtn.backgroundTintList = ContextCompat.getColorStateList(context!!, R.color.purple_500)
                        list = x as ArrayList<MutableMap<String, Any>>
                        adapter.list = list
                        adapter.notifyDataSetChanged()
                        getProduct(list)
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
                        val title = it.result!!.getString("book_title")!!
                        val inStock = it.result!!.getBoolean("in_stock")!!
                        val stockQuantity = it.result!!.getLong("in_stock_quantity")!!
                        val price = it.result!!.getString("price_Rs")!!.trim()
                        val offerPrice = it.result!!.getString("price_offer")!!
                        sendingList.add(CartModel(productId,url,title,price,inStock,stockQuantity,offerPrice,qty))

                        if (offerPrice == ""){

                            priceString += price.toInt()*qty.toInt()

                        }else{
                            discount += (price.toInt() - offerPrice.toInt())*qty.toInt()
                            priceString += offerPrice.toInt()*qty.toInt()

                        }

                        result = priceString+discount

                    }
                }

        }

    }

    override fun onItemClick(position: Int) {
        list.removeAt(position)
        adapter.notifyItemRemoved(position)
        val cartmap:MutableMap<String,Any> = HashMap()
        cartmap["cart_list"] = list
        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_CART").update(cartmap).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(context,"successful",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show()
                }
            }



    }


}

