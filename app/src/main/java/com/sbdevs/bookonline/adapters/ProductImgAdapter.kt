package com.sbdevs.bookonline.adapters

import androidx.annotation.NonNull

import android.view.ViewGroup

import com.sbdevs.bookonline.R
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.request.RequestOptions

import com.bumptech.glide.Glide

import androidx.viewpager.widget.PagerAdapter
import com.squareup.picasso.Picasso


class ProductImgAdapter(var productImgList: ArrayList<String>,val listener: MyOnItemClickListener) :
    RecyclerView.Adapter<ProductImgAdapter.ViewHolder>() {

    interface MyOnItemClickListener{
        fun onItemClick(position: Int,url:String)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductImgAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_product_image_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductImgAdapter.ViewHolder, position: Int) {
        holder.bind(productImgList[position])
    }

    override fun getItemCount(): Int {
        return productImgList.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImg:ImageView = itemView.findViewById(R.id.product_image)

        fun bind(url:String) {
            itemView.setOnClickListener {
                listener.onItemClick(absoluteAdapterPosition,url)
            }
            Glide.with(itemView.context).load(url)
                .apply(RequestOptions().placeholder(R.drawable.as_square_placeholder))
                .into(productImg)
//            Picasso.get()
//                .load(url)
//                .placeholder(R.drawable.as_square_placeholder)
//                .resize(300, 300)
//                .centerCrop()
//                .into(productImage)
        }

    }

}