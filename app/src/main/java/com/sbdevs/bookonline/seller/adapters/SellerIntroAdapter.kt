package com.sbdevs.bookonline.seller.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.chrisbanes.photoview.PhotoView
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.ProductZoomImageAdapter
import com.sbdevs.bookonline.seller.models.IntroModel

class SellerIntroAdapter (var list: ArrayList<IntroModel>) :
    RecyclerView.Adapter<SellerIntroAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SellerIntroAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sl_item_intro,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SellerIntroAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val introImageView: ImageView = itemView.findViewById(R.id.intro_image)
        private val introTextView: TextView = itemView.findViewById(R.id.intro_text)

        fun bind(model:IntroModel) {

            Glide.with(itemView.context).load(model.image)
                .apply(RequestOptions().placeholder(R.drawable.as_square_placeholder))
                .into(introImageView)

            introTextView.text = model.title


        }
    }


}