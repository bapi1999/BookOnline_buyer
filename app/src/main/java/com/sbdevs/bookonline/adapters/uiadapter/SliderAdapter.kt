package com.sbdevs.bookonline.adapters.uiadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.models.uidataclass.SliderModel


class SliderAdapter(var picList:ArrayList<SliderModel> ): RecyclerView.Adapter<SliderAdapter.ViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slider,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(picList[position])

    }

    override fun getItemCount(): Int {
        return picList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView:ImageView = itemView.findViewById(R.id.imageSliderItem)

        fun bind(model:SliderModel){
            val image = model.image
            val ff = model.action_type

            Glide.with(itemView.context).load(image)
                .placeholder(R.drawable.as_rectangle_placeholder)
                .into(imageView)
            itemView.setOnClickListener {
                Toast.makeText(itemView.context,"the action is $ff", Toast.LENGTH_LONG).show()
            }
        }

    }
}

