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
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.le_search_filter_item, parent, false)
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
        private val outOfStockIcon: ImageView = itemView.findViewById(R.id.outofstock_icon)
        private val bookTypeText:TextView = itemView.findViewById(R.id.book_type)
        private val bookConditionText:TextView = itemView.findViewById(R.id.product_condition)

        fun bind(model:SearchModel){
            itemView.setOnClickListener {
                val productIntent = Intent(itemView.context, ProductActivity::class.java)
                productIntent.putExtra("productId",model.productId)
                itemView.context.startActivity(productIntent)
            }
            productName.text = model.book_title
            val url = model.product_thumbnail
            val stockQty:Long = model.in_stock_quantity

            val priceOriginal:Long = model.price_original
            val priceSelling:Long =model.price_selling
            avgRatingText.text = model.rating_avg
            totalRatingsText.text = "( ${model.rating_total} ratings )"
            bookConditionText.text = model.book_condition

            Picasso.get()
                .load(url)
                .placeholder(R.drawable.as_square_placeholder)
                //.resize(300, 300)
                //.centerCrop()
                .into(productImage)

            if (priceOriginal == 0L){
                productPrice.text = priceSelling.toString()
                productRealPrice.visibility = View.GONE
                percentOff.visibility = View.GONE

            }else{

                val price = priceSelling.toInt()
                val realPriceInt = priceOriginal.toInt()

                val percent:Int = (100* (realPriceInt - price)) / ( realPriceInt )

                productPrice.text = priceSelling.toString()
                productRealPrice.text = priceOriginal.toString()
                percentOff.text = "${percent}% off"

            }

            if (stockQty == 0L){
                outOfStockIcon.visibility = View.VISIBLE
            }else{
                outOfStockIcon.visibility = View.GONE
            }

            if (model.book_printed_ON == 0L) {
                bookTypeText.text = "${model.book_type}"
            } else {
                bookTypeText.text = "${model.book_type} (${model.book_printed_ON})"
            }




        }

    }

}