package com.sbdevs.bookonline.adapters.uiadapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.java.SearchFilterJavaActivity
import com.sbdevs.bookonline.models.uidataclass.TopCategoryModel
import com.squareup.picasso.Picasso

class TopCategoryAdapter(var list: ArrayList<TopCategoryModel>) :
    RecyclerView.Adapter<TopCategoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): TopCategoryAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_category, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: TopCategoryAdapter.ViewHolder, position: Int) {
        val categoryId = list[position]
        holder.bind(categoryId)

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.categoryImage)
        val name: TextView = itemView.findViewById(R.id.categoryName)

        fun bind(item: TopCategoryModel) {


            val url = item.image
            val categoryName =item.name
            val actionString =item.action_string

            name.text = categoryName

            Picasso.get()
                .load(url)
                .placeholder(R.drawable.as_square_placeholder)
                .resize(300, 300)
                .centerCrop()
                .into(imageView)


            itemView.setOnClickListener {
                Log.e("Clicked","$categoryName")
                val newIntent = Intent(itemView.context, SearchFilterJavaActivity::class.java)
                newIntent.putExtra("query",actionString);
                itemView.context.startActivity(newIntent)
            }
        }
    }

}