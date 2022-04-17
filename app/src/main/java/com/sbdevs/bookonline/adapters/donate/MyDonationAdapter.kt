package com.sbdevs.bookonline.adapters.donate

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.models.MyDonationModel
import java.util.*

class MyDonationAdapter(var list: MutableList<MyDonationModel>, ) : RecyclerView.Adapter<MyDonationAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_donation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        private val receivedText: TextView = itemView.findViewById(R.id.received_text)
        private val totalQtyText: TextView = itemView.findViewById(R.id.total_item_count)
        private val totalPointText: TextView = itemView.findViewById(R.id.total_point)
        private val timeText: TextView = itemView.findViewById(R.id.donation_time)
        private val itemsText: TextView = itemView.findViewById(R.id.donated_items)

        fun bind(model:MyDonationModel){
            var totalPoint = 0L
            var totalQty = 0L
            val timeRequest: Date = model.Time_donate_request
            var isReceived = model.is_received
            var st  = ""
            val itemList = model.item_List


            for ((i, item) in itemList.withIndex()){
                val name = item["Type"]
                val qty:Long = item["qty"] as Long
                val poitPerItem:Long = item["points_per_item"] as Long

                totalQty += qty
                totalPoint+=(qty*poitPerItem)

                st+="$name ($qty)"

                st += if (i >= itemList.size-1){
                    ""
                }else{
                    ", "
                }
            }

            timeText.text = getDateTime(timeRequest)

            if (isReceived){
                receivedText.text = "Received"
                receivedText.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.lightGreen_700)
            }else{
                receivedText.text = "Not received"
                receivedText.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.red_500)
            }



            totalPointText.text = totalPoint.toString()
            totalQtyText.text = "$totalQty items"
            itemsText.text = st





        }
        @SuppressLint("SimpleDateFormat")
        private fun getDateTime(date: Date): String? {
            return try {
                val sdf = SimpleDateFormat("dd/MM/yyyy")
                //val netDate = Date(s.toLong() * 1000)
                sdf.format(date)
            } catch (e: Exception) {
                e.toString()
            }
        }
    }
}