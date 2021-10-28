package com.sbdevs.bookonline.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.ProductDetailsActivity
import com.sbdevs.bookonline.models.uidataclass.SearchModel

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
        private val priceOff: TextView = itemView.findViewById(R.id.percent_off)
        fun bind(model:SearchModel){
            itemView.setOnClickListener {
                val productIntent = Intent(itemView.context, ProductDetailsActivity::class.java)
                productIntent.putExtra("productId",model.productId)
                itemView.context.startActivity(productIntent)
            }
            productName.text = model.productName



        }

    }

}