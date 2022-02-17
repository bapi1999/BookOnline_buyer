package com.sbdevs.bookonline.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.user.OrderDetailsActivity
import com.sbdevs.bookonline.models.NotificationModel
import com.sbdevs.bookonline.othercalss.FireStoreData

class NotificationAdapter(var list:List<NotificationModel>): RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {



    interface MyonItemClickListener{
        fun onItemClick(position: Int)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder (itemView: View):RecyclerView.ViewHolder(itemView) {
        private val firebaseFirestore = Firebase.firestore
        private val user = FirebaseAuth.getInstance().currentUser


        private val notificationDescription:TextView = itemView.findViewById(R.id.notification_description)
        private val notificationImage:ImageView = itemView.findViewById(R.id.notification_image)
        private val notificationTime:TextView = itemView.findViewById(R.id.notification_time)
        private val container:ConstraintLayout = itemView.findViewById(R.id.notification_container)


        fun bind(item:NotificationModel){
            val image = item.image.trim()
            val seen:Boolean = item.seen
            val docName = item.notificationId
            val orderId = item.order_id
            val sellerId = item.sellerId

            itemView.setOnClickListener {

                if (sellerId.isNotEmpty() and orderId.isNotEmpty()){
                    val orderActivityIntent = Intent(itemView.context, OrderDetailsActivity::class.java)
                    orderActivityIntent.putExtra("orderID",orderId)
                    orderActivityIntent.putExtra("sellerID",sellerId)
                    itemView.context.startActivity(orderActivityIntent)
                    //todo - sellerId is also needed
                }


                //todo welcome notification does not have any seller id or OrderID
                updateViewStatusInNotification(docName)
                container.backgroundTintList =  AppCompatResources.getColorStateList(itemView.context!!, R.color.white)

            }

             if (!seen){
                 container.backgroundTintList =  AppCompatResources.getColorStateList(itemView.context!!, R.color.notificationBg)
             }else{
                 container.backgroundTintList =  AppCompatResources.getColorStateList(itemView.context!!, R.color.white)
             }
            notificationDescription.text = item.description

            Glide.with(itemView.context).load(image)
                .placeholder(R.drawable.as_notification_holder)
                .into(notificationImage)
            val dateFormat = FireStoreData()
//            val dayAgo = dateFormat.durationFromNow(item.date)
            val msAgo = dateFormat.msToTimeAgo(itemView.context,item.date)
            notificationTime.text = msAgo
        }


        private fun updateViewStatusInNotification(notificationID:String){

            val updateMap:MutableMap<String,Any> = HashMap()
            updateMap["seen"] = true
            firebaseFirestore.collection("USERS").document(user!!.uid)
                .collection("NOTIFICATIONS")
                .document(notificationID)
                .update(updateMap)

        }
    }

}