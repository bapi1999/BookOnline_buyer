package com.sbdevs.bookonline.adapters

import com.sbdevs.bookonline.R

import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import com.sbdevs.bookonline.models.ProductReviewModel
import com.sbdevs.bookonline.othercalss.TimeDateAgo


class ProductReviewAdapter(var list: MutableList<ProductReviewModel>) :
    RecyclerView.Adapter<ProductReviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.le_product_review_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.setData(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var buyerNameTxt: TextView = itemView.findViewById(R.id.buyer_name)
        var reviewDateTxt: TextView = itemView.findViewById(R.id.review_date)
        var reviewTxt: TextView = itemView.findViewById(R.id.buyer_review)
        var ratingTxt: TextView = itemView.findViewById(R.id.mini_rating)


        fun setData(reviewModel: ProductReviewModel) {

            val buyerId: String = reviewModel.buyer_ID
            val buyerName: String = reviewModel.buyer_name
            val rating: Int = reviewModel.rating
            val review: String = reviewModel.review
            val reviewDate = reviewModel.review_Date!!
            val daysAgo = TimeDateAgo().msToTimeAgo(itemView.context,reviewDate)

            buyerNameTxt.text = buyerName
            reviewDateTxt.text = daysAgo
            reviewTxt.text = review

            ratingTxt.text = rating.toString()
        }

    }


}