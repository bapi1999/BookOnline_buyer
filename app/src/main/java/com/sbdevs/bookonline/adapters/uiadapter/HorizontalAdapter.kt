package com.sbdevs.bookonline.adapters.uiadapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey


import com.sbdevs.bookonline.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.sbdevs.bookonline.activities.ProductDetailsActivity

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
        val productImage : ImageView = itemView.findViewById(R.id.product_image)
        val product_name : TextView = itemView.findViewById(R.id.product_name)
        val product_price : TextView = itemView.findViewById(R.id.product_price)
        val product_real_price : TextView = itemView.findViewById(R.id.product_real_price)
        private val firebaseFirestore = Firebase.firestore
//        val storageReference = Firebase.storage//.reference
        val storage = FirebaseStorage.getInstance()
        fun bind(productId:String) {
            itemView.setOnClickListener {
                val productIntent = Intent(itemView.context,ProductDetailsActivity::class.java)
                productIntent.putExtra("productId",productId)
                itemView.context.startActivity(productIntent)
            }
            firebaseFirestore.collection("PRODUCTS").document(productId)
                .get().addOnCompleteListener {
                    if (it.isSuccessful){
                        val url = it.result!!.get("product_thumbnail").toString().trim()
                        val title:String = it.result!!.getString("book_title")!!
                        val price = it.result!!.getString("price_Rs")
                        val offsetPrice = it.result!!.getString("price_offer")!!
                        if (offsetPrice == ""){
                            product_price.text = price
                            product_real_price.visibility = View.GONE

                        }else{
                            product_real_price.text = price
                            product_price.text = offsetPrice
                        }
                        product_name.text = title
                        //val imgRef = storageReference.child("image/")
//                        val gsReference = storage.getReferenceFromUrl("gs://ecommerceapp2-891bc.appspot.com/image/IMG_20211013_174128688.jpg")

                        val requestOptions = RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                        // resize does not respect aspect ratio

                        Glide.with(itemView.context)
                            .load(url)
                            .placeholder(R.drawable.as_square_placeholder)
                            .apply(requestOptions)
                            .into(productImage);


                    }
                }
        }

    }

}