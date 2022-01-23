package com.sbdevs.bookonline.adapters.uiadapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.ProductActivity
import com.squareup.picasso.Picasso

class PromotedAdapter(var list:ArrayList<String>): RecyclerView.Adapter<PromotedAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_promoted_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productIds = list[position].trim()
        holder.bind(productIds)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {

        private val productImage : ImageView = itemView.findViewById(R.id.product_image)
        private val productName:TextView = itemView.findViewById(R.id.product_name)
        private val productPrice:TextView = itemView.findViewById(R.id.product_price)
        private val priceOff:TextView = itemView.findViewById(R.id.priceOff)

        private val firebaseFirestore = Firebase.firestore
        fun bind(productId:String) {
            itemView.setOnClickListener {
                val productIntent = Intent(itemView.context, ProductActivity::class.java)
                productIntent.putExtra("productId",productId)
                itemView.context.startActivity(productIntent)
            }
            firebaseFirestore.collection("PRODUCTS").document(productId)
                .get().addOnSuccessListener {

                    val url:String = it.get("product_thumbnail").toString().trim()
                    val title:String = it.getString("book_title")!!


                    val priceOriginal = it.getLong("price_original")!!.toLong()
                    val priceSelling = it.getLong("price_selling")!!.toLong()

                    if (priceOriginal == 0L){
                        productPrice.text = priceSelling.toString()
                        priceOff.text = "Buy Now"

                    }else{
                        val percent:Int = (100* (priceOriginal.toInt() - priceSelling.toInt())) / ( priceOriginal.toInt() )

                        productPrice.text = priceSelling.toString()
                        priceOff.text = "get ${percent}% off"

                    }
                    productName.text = title

                    Picasso.get()
                        .load(url)
                        .placeholder(R.drawable.as_square_placeholder)
                        //.resize(300, 300)
                        .fit()
                        .into(productImage)


                }.addOnFailureListener {
                    Log.e("PromotedAdapter","${it.message}")
                }
        }

    }


}