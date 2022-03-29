package com.sbdevs.bookonline.adapters.donate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.models.MyDonationModel

class AllDonationsAdapter (var list: MutableList<MyDonationModel>, ) : RecyclerView.Adapter<AllDonationsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_all_donation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        private val donorName: TextView = itemView.findViewById(R.id.donor_name)
        private val badgeBadge: ImageView = itemView.findViewById(R.id.donor_badge)
        private val donorLevel:TextView = itemView.findViewById(R.id.donor_level)
        private var firebaseFirestore = Firebase.firestore

        fun bind(model: MyDonationModel){
            val donorId = model.Donor_Id

            getBadge(donorId)


        }

        private fun getBadge(donorId:String){
            firebaseFirestore.collection("USERS").document(donorId)
                .get().addOnSuccessListener {
                    val name = it.getString("name").toString()
                    val totalQty:Long = it.getLong("total_donation_qty")!!.toLong()
                    val totalPoint = it.getLong("total_donation_point")!!.toLong()
                    donorName.text = name
                    when {
                        totalQty <10 -> {
                            //donor badge image is created
                            donorLevel.text = "Level 0"


                        }
                        totalQty in 10..49 -> {
                            //donor badge image is created
                            donorLevel.text = "Level 1"

                        }
                        totalQty in 50..199 -> {
                            //donor badge image is created
                            donorLevel.text = "Level 2"
                        }
                        totalQty in 200..499 -> {
                            donorLevel.text = "Level 3"

                        }
                        totalQty in 500..1499 -> {
                            donorLevel.text = "Level 4"

                        }
                        totalQty in 1500..4999 -> {
                            donorLevel.text = "Level 5"
                        }
                        totalQty in 5000..9999 -> {
                            donorLevel.text = "Level 6"

                        }
                        totalQty >10000 -> {
                            donorLevel.text = "Level 7"

                        }

                    }
                }
        }
    }
}