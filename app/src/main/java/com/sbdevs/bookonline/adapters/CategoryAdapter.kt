package com.sbdevs.bookonline.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sbdevs.bookonline.R

class CategoryAdapter(var list:ArrayList<String>):
    RecyclerView.Adapter<CategoryAdapter.ViweHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryAdapter.ViweHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_home_category_item_lay,parent,false)
        return ViweHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryAdapter.ViweHolder, position: Int) {
        val categoryId = list[position]
        holder.bind(categoryId)
    }



    override fun getItemCount(): Int {
        return list.size
    }


    class ViweHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var categoryTxt:TextView = itemView.findViewById(R.id.miniCategory)

        fun bind(category:String){
            categoryTxt.text = category
        }
    }
}