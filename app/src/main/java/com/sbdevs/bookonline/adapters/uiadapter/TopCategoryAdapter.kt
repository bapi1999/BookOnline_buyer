package com.sbdevs.bookonline.adapters.uiadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.squareup.picasso.Picasso

class TopCategoryAdapter(val list: ArrayList<MutableMap<String, String>>) :
    RecyclerView.Adapter<TopCategoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): TopCategoryAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.le_top_category_item, parent, false)
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
        private val firebaseFirestore = Firebase.firestore
        val imageView: ImageView = itemView.findViewById(R.id.categoryImage)
        val name: TextView = itemView.findViewById(R.id.categoryName)

        fun bind(item: MutableMap<String, String>) {


            val url = item["image"].toString()
            val categoryName = item["name"].toString()

//            Glide.with(itemView.context).load(url)
//                .placeholder(R.drawable.as_square_placeholder)
//                .into(imageView)

            name.text = categoryName

            Picasso.get()
                .load(url)
                .placeholder(R.drawable.as_square_placeholder)
                .resize(300, 300)
                .centerCrop()
                .into(imageView)


//            itemView.setOnClickListener {
//                Toast.makeText(itemView.context, categoryName, Toast.LENGTH_LONG).show()
//            }
        }
    }

}