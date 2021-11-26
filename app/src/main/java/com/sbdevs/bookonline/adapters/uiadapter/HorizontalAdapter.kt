package com.sbdevs.bookonline.adapters.uiadapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions


import com.sbdevs.bookonline.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.sbdevs.bookonline.activities.ProductActivity

class HorizontalAdapter(var list:ArrayList<String>): RecyclerView.Adapter<HorizontalAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int
    ): HorizontalAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_product_horizontal_item_lay,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HorizontalAdapter.ViewHolder, position: Int) {
        val productIds = list[position].trim()
        holder.bind(productIds)
    }

    override fun getItemCount(): Int {
        return list.size
    }
    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        private val productImage : ImageView = itemView.findViewById(R.id.product_image)
        private val productName : TextView = itemView.findViewById(R.id.product_name)
        private val productPrice : TextView = itemView.findViewById(R.id.product_price)
        private val productRealPrice : TextView = itemView.findViewById(R.id.product_real_price)
        private val offsetPriceText : TextView = itemView.findViewById(R.id.offset_price)
        private val percentOffContainer : LinearLayout = itemView.findViewById(R.id.percent_off_container)
        private val buyNowContainer : LinearLayout = itemView.findViewById(R.id.buy_now_container)
        val gone = View.GONE
        val visible = View.VISIBLE

        private val firebaseFirestore = Firebase.firestore
        val storage = FirebaseStorage.getInstance()
        fun bind(productId:String) {
            itemView.setOnClickListener {
                val productIntent = Intent(itemView.context,ProductActivity::class.java)
                productIntent.putExtra("productId",productId)
                itemView.context.startActivity(productIntent)
            }
            firebaseFirestore.collection("PRODUCTS").document(productId)
                .get().addOnSuccessListener{
                    val url = it.get("product_thumbnail").toString().trim()
                    val title:String = it.getString("book_title")!!
                    val priceOriginal = it.getLong("price_original")!!.toLong()
                    val priceSelling = it.getLong("price_selling")!!.toLong()


                    if (priceOriginal == 0L){
                        productPrice.text = priceSelling.toString()
                        productRealPrice.visibility = gone
                        percentOffContainer.visibility = gone
                        buyNowContainer.visibility = visible

                    }else{
                        val priceOff = priceOriginal.toInt() - priceSelling.toInt()
                        offsetPriceText.text = priceOff.toString()
                        productRealPrice.text = priceOriginal.toString()
                        productPrice.text = priceSelling.toString()
                        buyNowContainer.visibility =gone
                        percentOffContainer.visibility = visible
                        productRealPrice.visibility = visible
                    }
                    productName.text = title

                    val requestOptions = RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                    // resize does not respect aspect ratio

                    Glide.with(itemView.context)
                        .load(url)
                        .placeholder(R.drawable.as_square_placeholder)
                        .apply(requestOptions)
                        .into(productImage);


                }.addOnFailureListener {
                    Log.e("HorizontalAdapter","${it.message}")
                }
        }

    }

}