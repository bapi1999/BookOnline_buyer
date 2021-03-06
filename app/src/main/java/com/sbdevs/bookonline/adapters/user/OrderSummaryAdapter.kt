package com.sbdevs.bookonline.adapters.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.models.user.CartModel

class OrderSummaryAdapter(var list:ArrayList<CartModel>): RecyclerView.Adapter<OrderSummaryAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_summery, parent, false)
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
        //private val productRealPrice: TextView = itemView.findViewById(R.id.product_real_price)
        private val quantitiesTxt: TextView = itemView.findViewById(R.id.quantity)

        private val outofstockIcon:ImageView = itemView.findViewById(R.id.outofstock_icon)
        private val variantTxt:TextView = itemView.findViewById(R.id.variant)
        init {
            variantTxt.visibility = View.GONE
        }

        fun bind(group: CartModel){

            val productId:String = group.productId
            val quantity:Long = group.orderQuantity
            val url:String = group.url
            val title:String = group.title
            val priceOriginal:Long = group.priceOriginal
            val priceSelling:Long = group.priceSelling
            val stockQuantity = group.stockQty

            quantitiesTxt.text = quantity.toString()
            productName.text = title

            if (stockQuantity != 0L){
                outofstockIcon.visibility = View.GONE
            }else{
                outofstockIcon.visibility = View.VISIBLE
            }

            Glide.with(itemView.context).load(url).placeholder(R.drawable.as_square_placeholder).into(productImage);


            if (priceOriginal == 0L){
                val price = priceSelling.toInt()*quantity.toInt()
                productPrice.text = price.toString()


            }else{

                val price = priceSelling.toInt()*quantity.toInt()
                val realPrice = priceOriginal.toInt()*quantity.toInt()

                productPrice.text = price.toString()



            }



        }
    }

}