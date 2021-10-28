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


class SliderAdapter(var picList:ArrayList<SliderModel> ,var viewPager2: ViewPager2): RecyclerView.Adapter<SliderAdapter.ViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.le_slider_item_lay,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pic = picList[position].url
        val sliderAction = picList[position].sliderAction
        holder.bind(pic,sliderAction)

    }

    override fun getItemCount(): Int {
        return picList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView:ImageView = itemView.findViewById(R.id.imageSliderItem)

        fun bind(data: String,sliderAction: String){

            Glide.with(itemView.context).load(data).into(imageView)
//            itemView.setOnClickListener {
//                Toast.makeText(itemView.context,"the action is $sliderAction", Toast.LENGTH_LONG).show()
//            }
        }

    }
}

