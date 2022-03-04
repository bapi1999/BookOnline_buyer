package com.sbdevs.bookonline.adapters.donate

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.models.MyDonationModel
import java.util.*
import kotlin.collections.ArrayList

class MyDonationAdapter(var list: MutableList<MyDonationModel>, ) : RecyclerView.Adapter<MyDonationAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_my_donation_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        private val totalQtyText: TextView = itemView.findViewById(R.id.total_item_count)
        private val totalPointText: TextView = itemView.findViewById(R.id.total_point)
        private val timeText: TextView = itemView.findViewById(R.id.donation_time)
        private val itemsText: TextView = itemView.findViewById(R.id.donated_items)

        fun bind(model:MyDonationModel){
            val totalPoint = model.total_point
            val totalQty = model.total_qty
            val timeRequest: Date = model.Time_donate_request
            val timeReceived: Date = model.Time_donate_received
            val isReceived = model.is_received
            var st  = ""
            val itemList = model.item_List


            for ((i, item) in itemList.withIndex()){
                val name = item["Type"]
                val qty = item["qty"]
                st+="$name ($qty)"

                st += if (i >= itemList.size-1){
                    ""
                }else{
                    ", "
                }
            }

            if (isReceived){
                timeText.text = getDateTime(timeReceived)
            }else{
                timeText.text = getDateTime(timeRequest)
            }

            totalPointText.text = totalPoint.toString()
            totalQtyText.text = totalQty.toString()
            itemsText.text = st





        }
        @SuppressLint("SimpleDateFormat")
        private fun getDateTime(date: Date): String? {
            return try {
                val sdf = SimpleDateFormat("dd MMMM yyyy hh:mm a")
                //val netDate = Date(s.toLong() * 1000)
                sdf.format(date)
            } catch (e: Exception) {
                e.toString()
            }
        }
    }
}