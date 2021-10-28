package com.sbdevs.bookonline.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.ProductDetailsActivity
import com.sbdevs.bookonline.models.CartModel

class OrderSummaryAdapter(var list:ArrayList<CartModel>): RecyclerView.Adapter<OrderSummaryAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderSummaryAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_cart_item_lay_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        holder.bind(list[position])


    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView:View) :RecyclerView.ViewHolder(itemView){
        val firebaseFirestore = Firebase.firestore
        val firebaseAuth = FirebaseAuth.getInstance()
        private val productImage : ImageView = itemView.findViewById(R.id.product_image)
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)
        private val productRealPrice: TextView = itemView.findViewById(R.id.product_real_price)
        private val percentOff: TextView = itemView.findViewById(R.id.percent_off)
        private val quantitiesTxt: TextView = itemView.findViewById(R.id.quantity)
        private val btnContainer: LinearLayout= itemView.findViewById(R.id.btn_container)
        init {
            btnContainer.visibility = View.GONE
        }

        fun bind(group:CartModel){

            val productId:String = group.productId
            val quantity:Long = group.quantity
            val url:String = group.url
            val title:String = group.title
            val price:String = group.price
            val offerPrice:String = group.offerPrice
            val in_stock:Boolean = group.inStock
            val stockQuantity = group.stock


//            val productId:String = group["product"] as String
//            val quantity:Long = group["quantity"] as Long

            quantitiesTxt.text = quantity.toString()
            productName.text = title

            Glide.with(itemView.context).load(url).placeholder(R.drawable.as_square_placeholder).into(productImage);

            if (offerPrice == ""){
                productPrice.text = price
                productRealPrice.visibility = View.GONE
                percentOff.text = "Buy Now"

            }else{
                val percent:Int = (100* (price.toInt() - offerPrice.toInt())) / ( price.toInt() )

                productPrice.text = offerPrice
                productRealPrice.text = price
                percentOff.text = "${percent}% off"

            }

//            val productId:String = group["product"] as String
//            val quantity:Long = group["quantity"] as Long
//            firebaseFirestore.collection("PRODUCTS").document(productId)
//                .get().addOnCompleteListener {
//
//                    val url = it.result!!.get("product_thumbnail").toString().trim()
//                    val title:String = it.result!!.getString("book_title")!!
//                    val price = it.result!!.getString("price_Rs")!!.trim()
//                    val offerPrice = it.result!!.getString("price_offer")!!
//
//                }



        }
    }

}