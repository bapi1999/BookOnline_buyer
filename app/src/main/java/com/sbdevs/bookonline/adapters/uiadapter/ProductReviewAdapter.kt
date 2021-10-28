package com.sbdevs.bookonline.adapters.uiadapter

import com.sbdevs.bookonline.R

import androidx.annotation.NonNull

import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import com.google.firebase.Timestamp
import com.sbdevs.bookonline.models.ProductReviewModel
import java.util.*


class ProductReviewAdapter(var list: List<ProductReviewModel>) :
    RecyclerView.Adapter<ProductReviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.le_product_review_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val buyerId: String = list[position].buyer_ID
        val buyerName: String = list[position].buyer_name
        val rating: Int = list[position].rating
        val review: String = list[position].review
        val reviewDate = list[position].review_Date!!
        holder.setData(buyerName, reviewDate, review, rating)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var buyerNameTxt: TextView = itemView.findViewById(R.id.buyer_name)
        var reviewDateTxt: TextView = itemView.findViewById(R.id.review_date)
        var reviewTxt: TextView = itemView.findViewById(R.id.buyer_review)
        var ratingTxt: TextView = itemView.findViewById(R.id.mini_rating)


        fun setData(buyerName: String, reviewDate: Date, review: String, rating: Int) {
            buyerNameTxt.text = buyerName
            reviewDateTxt.text = reviewDate.toString()
            reviewTxt.text = review
            ratingTxt.text = rating.toString() + ""
        }

    }


}