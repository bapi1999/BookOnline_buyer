package com.sbdevs.bookonline.adapters

import androidx.annotation.NonNull

import android.view.ViewGroup

import com.sbdevs.bookonline.R
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView

import com.bumptech.glide.request.RequestOptions

import com.bumptech.glide.Glide

import androidx.viewpager.widget.PagerAdapter
import com.squareup.picasso.Picasso


class ProductImgAdapter(var productImgList: ArrayList<String>) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val productImage = ImageView(container.context)
        val url = productImgList[position].trim()
        Glide.with(container.context).load(url)
            .apply(RequestOptions().placeholder(R.drawable.as_square_placeholder))
            .into(productImage)
//        Picasso.get()
//            .load(url)
//            .placeholder(R.drawable.as_square_placeholder)
////            .resize(300, 300)
////            .centerCrop()
//            .into(productImage)
        //productImage.setImageResource(productImgList.get(position));
        container.addView(productImage, 0)
        return productImage
    }

    override fun getCount(): Int {
        return productImgList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        //super.destroyItem(container, position, object);
        container.removeView(`object` as ImageView)
    }
}