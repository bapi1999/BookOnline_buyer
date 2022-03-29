package com.sbdevs.bookonline.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.SearchActivity
import com.sbdevs.bookonline.activities.SearchFilterActivity
import com.sbdevs.bookonline.models.QueryModel1

class FireBaseAdapter1(var options: FirebaseRecyclerOptions<QueryModel1>): FirebaseRecyclerAdapter<QueryModel1, FireBaseAdapter1.ViewHolder>(options) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_search_query, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model1: QueryModel1) {
        holder.bind(model1)
    }

    inner class ViewHolder(itemViewe: View): RecyclerView.ViewHolder(itemViewe) {
        private val queryText: TextView = itemViewe.findViewById(R.id.query_text)

        fun bind(query1:QueryModel1){
            val squery = query1.name
            queryText.text = squery.toString()

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, SearchFilterActivity::class.java)
                intent.putExtra("query",squery)
                itemView.context.startActivity(intent)
                SearchActivity().finish()
            }


        }

    }
}