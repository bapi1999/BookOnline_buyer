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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_order_summery_item_lay, parent, false)
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

        private val stockNumberTxt:TextView = itemView.findViewById(R.id.stock)
        private val outofstockTxt:TextView = itemView.findViewById(R.id.outofstockText)
        private val variantTxt:TextView = itemView.findViewById(R.id.variant)
        init {
            variantTxt.visibility = View.GONE
        }

        fun bind(group:CartModel){

            val productId:String = group.productId
            val quantity:Long = group.orderQuantity
            val url:String = group.url
            val title:String = group.title
            val priceOriginal:String = group.priceOriginal
            val priceSelling:String = group.priceSelling
            val stockQuantity = group.stockQty

            quantitiesTxt.text = quantity.toString()
            productName.text = title

            if (stockQuantity != 0L){
                stockNumberTxt.text = stockQuantity.toString()
                outofstockTxt.visibility = View.GONE
            }else{
                stockNumberTxt.text = stockQuantity.toString()
                outofstockTxt.visibility = View.VISIBLE
            }

            Glide.with(itemView.context).load(url).placeholder(R.drawable.as_square_placeholder).into(productImage);


            if (priceOriginal == ""){
                val price = priceSelling.toInt()*quantity.toInt()
                productPrice.text = price.toString()
                productRealPrice.visibility = View.GONE
                percentOff.text = "Buy Now"

            }else{

                val price = priceSelling.toInt()*quantity.toInt()
                val realPrice = priceOriginal.toInt()*quantity.toInt()

                val percent:Int = (100* (realPrice - price)) / ( realPrice )

                productPrice.text = price.toString()
                productRealPrice.text = realPrice.toString()
                percentOff.text = "${percent}% off"

            }



        }
    }

}