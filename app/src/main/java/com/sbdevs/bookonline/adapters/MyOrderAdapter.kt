package com.sbdevs.bookonline.adapters

import android.content.Intent
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
import com.sbdevs.bookonline.activities.OrderDetailsActivity

class MyOrderAdapter(var list:ArrayList<MutableMap<String,Any>>):
    RecyclerView.Adapter<MyOrderAdapter.ViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_order_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        private val firebaseFirestore = Firebase.firestore

        private val productImage:ImageView = itemView.findViewById(R.id.product_image)
        private val productNameTxt:TextView = itemView.findViewById(R.id.title_txt)
        private val productPriceTxt:TextView = itemView.findViewById(R.id.price_txt)
        private val productQuantityTxt:TextView = itemView.findViewById(R.id.product_quantity)
        private val productStatusTxt:TextView = itemView.findViewById(R.id.status_txt)


        fun bind(group:MutableMap<String,Any>){
            val docName =  group["orderID"].toString()
            val sellerId = group["sellerId"].toString()

            itemView.setOnClickListener {
                val intent = Intent(itemView.context,OrderDetailsActivity::class.java)
                intent.putExtra("orderID",docName)
                intent.putExtra("sellerID",sellerId)
                itemView.context.startActivity(intent)
            }

            firebaseFirestore.collection("USERS").document(sellerId)
                .collection("SELLER_DATA")
                .document("5_ALL_ORDERS").collection("ORDER")
                .document(docName).get().addOnSuccessListener {
                    var totalAmount = 0
                    val productThumbnail= it.get("productThumbnail").toString()
                    val title=it.get("productTitle").toString()

                    val price = it.get("price").toString()
                    val orderedQty = it.getLong("ordered_Qty")!!
                    val status = it.get("status").toString()

                    totalAmount = price.toInt()*orderedQty.toInt()

                    Glide.with(itemView.context).load(productThumbnail)
                        .placeholder(R.drawable.as_square_placeholder)
                        .into(productImage)
                    productNameTxt.text = title
                    productPriceTxt.text = "Rs. $totalAmount"
                    productQuantityTxt.text ="Qty- $orderedQty"
                    productStatusTxt.text = status


                }.addOnFailureListener {
                    //
                }


        }

    }

}