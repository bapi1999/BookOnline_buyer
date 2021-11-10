package com.sbdevs.bookonline.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.ProductDetailsActivity

class WishlistAdapter (var list:ArrayList<String>, val listner: MyonItemClickListener):RecyclerView.Adapter<WishlistAdapter.ViewHolder>() {


    interface MyonItemClickListener{
        fun onItemClick(position: Int)
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
        private val productName:TextView = itemView.findViewById(R.id.product_name)
        private val productPrice:TextView = itemView.findViewById(R.id.product_price)
        private val productRealPrice:TextView = itemView.findViewById(R.id.product_real_price)
        private val priceOff:TextView = itemView.findViewById(R.id.percent_off)
        var removeBtn: TextView = itemView.findViewById(R.id.textView38)
        var ratingTotalTxt: TextView = itemView.findViewById(R.id.mini_totalNumberOf_ratings)
        var miniRatingTxt: TextView = itemView.findViewById(R.id.mini_product_rating)
        var bookSateTxt: TextView = itemView.findViewById(R.id.product_state)
        val firebaseFirestore = Firebase.firestore


        fun bind(productId:String){
            itemView.setOnClickListener {
                val productIntent = Intent(itemView.context, ProductDetailsActivity::class.java)
                productIntent.putExtra("productId",productId)
                itemView.context.startActivity(productIntent)
            }
            removeBtn.setOnClickListener {
                listner.onItemClick(adapterPosition)
            }

            firebaseFirestore.collection("PRODUCTS").document(productId)
                .get().addOnCompleteListener {
                    if (it.isSuccessful){
                        val url:String = it.result!!.getString("product_thumbnail")!!
                        val title:String = it.result!!.getString("book_title")!!
                        val ratingTotal = it.result!!.getLong("rating_total")!!.toString()

                        val priceOriginal = it.result!!.get("price_original").toString().trim()
                        val priceSelling = it.result!!.get("price_selling").toString().trim()

                        miniRatingTxt.text = it.result!!.getString("rating_avg")!!
                        ratingTotalTxt.text = "( $ratingTotal ratings )"

                        if (priceOriginal == ""){
                            productPrice.text = priceSelling
                            priceOff.text = "Buy Now"
                            productRealPrice.visibility = View.GONE

                        }else{
                            val percent:Int = (100* (priceOriginal.toInt() - priceSelling.toInt())) / ( priceOriginal.toInt() )

                            productPrice.text = priceSelling
                            productRealPrice.text = priceOriginal
                            priceOff.text = "${percent}% off"

                        }
                        productName.text = title
                        bookSateTxt.text = it.result?.getString("book_state")!!

                        Glide.with(itemView.context).load(url).placeholder(R.drawable.as_square_placeholder).into(productImage);





                    }
                }
        }

    }
}