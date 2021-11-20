package com.sbdevs.bookonline.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.ProductActivity
import com.sbdevs.bookonline.models.uidataclass.SearchModel
import com.squareup.picasso.Picasso

class SearchFilterAdapter(var list:ArrayList<SearchModel>):
    RecyclerView.Adapter<SearchFilterAdapter.ViewHolder>()  {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.le_search_filter_item_lay, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        private val productImage : ImageView = itemView.findViewById(R.id.product_image)
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)
        private val productRealPrice: TextView = itemView.findViewById(R.id.product_real_price)
        private val percentOff: TextView = itemView.findViewById(R.id.percent_off)

        private val avgRatingText: TextView = itemView.findViewById(R.id.mini_product_rating)
        private val totalRatingsText: TextView = itemView.findViewById(R.id.mini_totalNumberOf_ratings)
        private val outOfStockText: TextView = itemView.findViewById(R.id.outofstockText)

        fun bind(model:SearchModel){
            itemView.setOnClickListener {
                val productIntent = Intent(itemView.context, ProductActivity::class.java)
                productIntent.putExtra("productId",model.productId)
                itemView.context.startActivity(productIntent)
            }
            productName.text = model.productName
            val url = model.url
            val stockQty:Long = model.stockQty

            val priceOriginal:Long = model.priceOriginal
            val priceSelling:Long =model.priceSelling

            Picasso.get()
                .load(url)
                .placeholder(R.drawable.as_square_placeholder)
                //.resize(300, 300)
                //.centerCrop()
                .into(productImage)

            if (priceOriginal == 0L){
                productPrice.text = priceSelling.toString()
                productRealPrice.visibility = View.GONE
                percentOff.text = "Buy Now"

            }else{

                val price = priceSelling.toInt()
                val realPriceInt = priceOriginal.toInt()

                val percent:Int = (100* (realPriceInt - price)) / ( realPriceInt )

                productPrice.text = priceSelling.toString()
                productRealPrice.text = priceOriginal.toString()
                percentOff.text = "${percent}% off"

            }

            if (stockQty != 0L){
                outOfStockText.visibility = View.GONE
            }else{
                outOfStockText.visibility = View.VISIBLE
            }




        }

    }

}