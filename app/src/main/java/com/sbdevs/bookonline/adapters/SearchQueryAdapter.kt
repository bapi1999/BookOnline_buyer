package com.sbdevs.bookonline.adapters

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.SearchActivity
import com.sbdevs.bookonline.activities.SearchFilterActivity
import java.util.*
import kotlin.collections.ArrayList

class SearchQueryAdapter( val listner:MyonItemClickListener,val listType:Int)
    : RecyclerView.Adapter<SearchQueryAdapter.ViewHolder>(), Filterable {

    //(var list:ArrayList<String>, val listner:MyonItemClickListener,val listType:Int)

    var mainList = ArrayList<String>()
    var searchList = ArrayList<String>()

    fun setData(list: ArrayList<String>){
        this.mainList = list
        searchList= ArrayList<String>(list)
    }




    interface MyonItemClickListener{
        fun onItemClick1(position: Int)
        fun onItemClick2(position: Int)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.le_search_query_item_lay, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mainList[position])

    }

    override fun getItemCount(): Int {

//        return if (list.size>20){
//            20
//        }else{
//            list.size
//        }

        return mainList.size
    }

    inner class ViewHolder(itemViewe: View):RecyclerView.ViewHolder(itemViewe) {
        private val queryText:TextView = itemViewe.findViewById(R.id.query_text)
        private val deleteBtn:ImageView = itemViewe.findViewById(R.id.imageView4)
        fun bind(query:String){
            queryText.text = query
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, SearchFilterActivity::class.java)
                intent.putExtra("query",query)
                itemView.context.startActivity(intent)
//                SearchActivity().finish()
            }
            if (listType == 1){
                deleteBtn.visibility = View.GONE
            }

            deleteBtn.setOnClickListener {
                listner.onItemClick1(adapterPosition)

            }

        }

    }

    override fun getFilter(): Filter {
        return object :Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = ArrayList<String>()

                if (constraint == null  || constraint.isEmpty()) {
                    filteredList.addAll(searchList)
                } else {
                    val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()
//                    for (item in searchList){
//                        if (item.lowercase(Locale.getDefault()).trim().startsWith(filterPattern)) {
//                            filteredList.add(item)
//                        }
//                    }
                    searchList.forEach {
                        if (it.lowercase().trim().startsWith(filterPattern)) {
                            filteredList.add(it)
                        }
                    }
                }

                val result = FilterResults()
                result.values = filteredList

                return result
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                mainList.clear()
                mainList.addAll(results!!.values as ArrayList<String>)
                notifyDataSetChanged()
            }

        }
    }

}