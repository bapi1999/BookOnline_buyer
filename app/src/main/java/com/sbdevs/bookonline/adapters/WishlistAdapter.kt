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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.ProductActivity

class WishlistAdapter (var list:ArrayList<String>, val listner: MyonItemClickListener):RecyclerView.Adapter<WishlistAdapter.ViewHolder>() {


    interface MyonItemClickListener{
        fun onItemClick(position: Int,productId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistAdapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.le_wishlist_item_lay, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: WishlistAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])
    }



    override fun getItemCount(): Int {
        return list.size
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage : ImageView = itemView.findViewById(R.id.product_image)
        private val outOfStockIcon : ImageView = itemView.findViewById(R.id.outofstock_icon)
        private val productName:TextView = itemView.findViewById(R.id.product_name)
        private val productPrice:TextView = itemView.findViewById(R.id.product_price)
        private val productRealPrice:TextView = itemView.findViewById(R.id.product_real_price)
        private val priceOff:TextView = itemView.findViewById(R.id.percent_off)
        var removeBtn: LinearLayout = itemView.findViewById(R.id.remove_btn)
        var ratingTotalTxt: TextView = itemView.findViewById(R.id.mini_totalNumberOf_ratings)
        var miniRatingTxt: TextView = itemView.findViewById(R.id.mini_product_rating)
        var bookSateTxt: TextView = itemView.findViewById(R.id.product_state)
        val firebaseFirestore = Firebase.firestore


        fun bind(productId:String){
            itemView.setOnClickListener {
                val productIntent = Intent(itemView.context, ProductActivity::class.java)
                productIntent.putExtra("productId",productId)
                itemView.context.startActivity(productIntent)
            }
            removeBtn.setOnClickListener {
                listner.onItemClick(absoluteAdapterPosition,productId)
            }

            firebaseFirestore.collection("PRODUCTS").document(productId)
                .get().addOnSuccessListener {

                    val url:String = it.getString("product_thumbnail")!!
                    val title:String = it.getString("book_title")!!
                    val ratingTotal = it.getLong("rating_total")!!.toString()
                    val stockQty = it.getLong("in_stock_quantity")!!
                    val priceOriginal = it.getLong("price_original")!!.toLong()
                    val priceSelling = it.getLong("price_selling")!!.toLong()
                    val avgRating = it.getString("rating_avg").toString()
                    val bookType = it.getString("book_type")!!

                    miniRatingTxt.text = avgRating

                    ratingTotalTxt.text = "( $ratingTotal ratings )"

                    if (priceOriginal == 0L){
                        productPrice.text = priceSelling.toString()
                        priceOff.text = "Buy Now"
                        productRealPrice.visibility = View.GONE

                    }else{
                        val percent:Int = (100* (priceOriginal.toInt() - priceSelling.toInt())) / ( priceOriginal.toInt() )

                        productPrice.text = priceSelling.toString()
                        productRealPrice.text = priceOriginal.toString()
                        priceOff.text = "${percent}% off"

                    }
                    productName.text = title
                    bookSateTxt.text = bookType

                    if (stockQty == 0L){
                        outOfStockIcon.visibility = View.VISIBLE
                    }else{
                        outOfStockIcon.visibility = View.GONE
                    }

                    Glide.with(itemView.context).load(url).placeholder(R.drawable.as_square_placeholder).into(productImage);


                }.addOnFailureListener {

                }
        }

    }
}