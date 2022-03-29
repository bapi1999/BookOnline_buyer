package com.sbdevs.bookonline.adapters.uiadapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.ProductActivity
import com.sbdevs.bookonline.models.SearchModel
import com.squareup.picasso.Picasso

class ProductGrid2Adapter (var list:ArrayList<SearchModel>):
    RecyclerView.Adapter<ProductGrid2Adapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductGrid2Adapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_product_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductGrid2Adapter.ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder (itemView:View):RecyclerView.ViewHolder(itemView) {
        private val productImage : ImageView = itemView.findViewById(R.id.product_image)
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)
        private val productRealPrice: TextView = itemView.findViewById(R.id.product_real_price)

//        private val avgRatingText: TextView = itemView.findViewById(R.id.mini_product_rating)
//        private val totalRatingsText: TextView = itemView.findViewById(R.id.mini_totalNumberOf_ratings)
//        private val outOfStockIcon: ImageView = itemView.findViewById(R.id.outofstock_icon)
//        private val bookTypeText: TextView = itemView.findViewById(R.id.book_type)
//        private val bookConditionText: TextView = itemView.findViewById(R.id.product_condition)

        fun bind(model: SearchModel){
            itemView.setOnClickListener {
                val productIntent = Intent(itemView.context, ProductActivity::class.java)
                productIntent.putExtra("productId",model.productId)
                itemView.context.startActivity(productIntent)
            }
            productName.text = model.book_title
            val url = model.productImage_List[0]
            val stockQty:Long = model.in_stock_quantity

            val priceOriginal:Long = model.price_original
            val priceSelling:Long =model.price_selling
//            avgRatingText.text = model.rating_avg
//            totalRatingsText.text = "( ${model.rating_total} ratings )"
//            bookConditionText.text = model.book_condition

            Picasso.get()
                .load(url)
                .placeholder(R.drawable.as_square_placeholder)
                .resize(200, 200)
                .centerCrop()
                .into(productImage)

            if (priceOriginal == 0L){
                productPrice.text = priceSelling.toString()
                productRealPrice.visibility = View.GONE


            }else{

                val price = priceSelling.toInt()
                val realPriceInt = priceOriginal.toInt()

                val percent:Int = (100* (realPriceInt - price)) / ( realPriceInt )

                productPrice.text = priceSelling.toString()
                productRealPrice.text = priceOriginal.toString()

            }

        }
    }

}