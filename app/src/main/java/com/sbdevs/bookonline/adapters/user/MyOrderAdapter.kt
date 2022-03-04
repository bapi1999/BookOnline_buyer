package com.sbdevs.bookonline.adapters.user

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.user.OrderDetailsActivity
import com.sbdevs.bookonline.models.user.MyOrderModel
import com.sbdevs.bookonline.othercalss.FireStoreData
import java.util.*


class MyOrderAdapter(var list: ArrayList<MyOrderModel>) :
    RecyclerView.Adapter<MyOrderAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.le_order_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val firebaseFirestore = Firebase.firestore

        private val productImage: ImageView = itemView.findViewById(R.id.product_image)
        private val productNameTxt: TextView = itemView.findViewById(R.id.title_txt)
        private val productPriceTxt: TextView = itemView.findViewById(R.id.price_txt)
        private val productQuantityTxt: TextView = itemView.findViewById(R.id.product_quantity)
        private val productStatusTxt: TextView = itemView.findViewById(R.id.status_txt)
        private val orderTimeText: TextView = itemView.findViewById(R.id.textView67)

        fun bind(item: MyOrderModel) {
            val docName = item.orderId
            val productThumbnail = item.productThumbnail
            val productTitle:String = item.productTitle
            val orderTime: Date = item.orderTime
            val price :Long = item.price
            val orderedQty = item.orderedQty
            val status =item.status

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, OrderDetailsActivity::class.java)
                intent.putExtra("orderID", docName)
                itemView.context.startActivity(intent)
            }
//            val myOptions = RequestOptions()
//                .fitCenter() // or centerCrop
//                .override(100, 100)
            Glide.with(itemView.context)
                .load(productThumbnail)
                .apply(RequestOptions().override(100,100))
                .placeholder(R.drawable.as_square_placeholder)
                .into(productImage)
            productNameTxt.text = productTitle
            productPriceTxt.text = price.toString()
            productQuantityTxt.text = "$orderedQty"
            productStatusTxt.text = status
            orderTimeText.text = FireStoreData().msToTimeAgo(itemView.context, orderTime)

            when (status) {
                "new" -> {
                    productStatusTxt.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.amber_600)
                }
                "accepted" -> {
                    productStatusTxt.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.successGreen)
                }
                "packed" -> {
                    productStatusTxt.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.successGreen)
                    productStatusTxt.text ="accepted"
                }
                "shipped" -> {
                    productStatusTxt.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,
                        R.color.blueLink
                    )
                }
                "delivered" -> {
                    productStatusTxt.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,
                        R.color.indigo_900
                    )
                }
                "returned" -> {
                    productStatusTxt.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.amber_900)
                }
                else -> {
                    productStatusTxt.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.red_700)
                }
            }


        }

    }

}