package com.sbdevs.bookonline.adapters.uiadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.fragments.HomeFragment
import kotlinx.coroutines.GlobalScope

class TopCategoryAdapter (val list:ArrayList<String>):
    RecyclerView.Adapter<TopCategoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int
    ): TopCategoryAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_top_category_item_lay,parent,false)
        return ViewHolder(view)
    }



    override fun onBindViewHolder(holder: TopCategoryAdapter.ViewHolder, position: Int) {
        val categoryId = list[position].trim()
        holder.bind(categoryId)

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        private val firebaseFirestore = Firebase.firestore
        val imageView:ImageView = itemView.findViewById(R.id.categoryImage)
        fun bind(categoryId:String){

            firebaseFirestore.collection("CATEGORIES").document(categoryId)
                .get().addOnCompleteListener {
                    if (it.isSuccessful){
                        val url = it.result!!.getString("image")
                        val categoryName = it.result!!.getString("categoryName")
                        Glide.with(itemView.context).load(url).placeholder(R.drawable.as_square_placeholder).into(imageView)
                        itemView.setOnClickListener {
                            Toast.makeText(itemView.context,categoryName,Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }
    }

}