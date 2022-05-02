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
import com.sbdevs.bookonline.models.uidataclass.GridModel
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class ProductGridAdapter(var list:ArrayList<GridModel>): RecyclerView.Adapter<ProductGridAdapter.ViewHolder>() {

//todo= ONLY TO 4 PRODUCT MAXIMUM IN FRONT PAGE =============================================================
//--------------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int
    ):ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_grid,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return if(list.size >4){
            4
        }else{
            list.size
        }
    }
    class ViewHolder (itemView:View):RecyclerView.ViewHolder(itemView) {
        private val productImage : ImageView = itemView.findViewById(R.id.product_image)
        private val newProductTag : ImageView = itemView.findViewById(R.id.newProductTag)
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)
        private val productRealPrice: TextView = itemView.findViewById(R.id.product_real_price)
        private val priceOffTextView:TextView = itemView.findViewById(R.id.priceOff)
        private val gone = View.GONE
        private val visible = View.VISIBLE

        fun bind(model: GridModel){
            itemView.setOnClickListener {
                val productIntent = Intent(itemView.context, ProductActivity::class.java)
                productIntent.putExtra("productId",model.productId)
                itemView.context.startActivity(productIntent)
            }
            productName.text = model.book_title
            val url = model.productImage_List[0]

            val priceOriginal:Long = model.price_original
            val priceSelling:Long =model.price_selling


            Picasso.get()
                .load(url)
                .placeholder(R.drawable.as_square_placeholder)
                .resize(200, 200)
                .centerCrop()
                .into(productImage)

            productPrice.text = priceSelling.toString()

            if (priceOriginal == 0L){
                productRealPrice.visibility = gone
                priceOffTextView.visibility = gone


            }else{

                productRealPrice.visibility = visible
                priceOffTextView.visibility = visible

                val price = priceSelling.toInt()
                val realPriceInt = priceOriginal.toInt()

                val percent:Int = (100* (realPriceInt - price)) / ( realPriceInt )

                priceOffTextView.text = "$percent % off"
                productRealPrice.text = priceOriginal.toString()

            }
            val days4lay = Date(Date().time -(1000 * 60 * 60 * 24*4)).time
            val productAddedOn = model.PRODUCT_ADDED_ON.time
            if (productAddedOn>days4lay){
                newProductTag.visibility = visible
            }else{
                newProductTag.visibility = gone
            }



        }
    }
}