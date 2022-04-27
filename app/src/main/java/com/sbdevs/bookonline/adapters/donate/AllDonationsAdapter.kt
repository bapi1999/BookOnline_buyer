package com.sbdevs.bookonline.adapters.donate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
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
        private val donationPoint:TextView = itemView.findViewById(R.id.donation_point)
        private var firebaseFirestore = Firebase.firestore

        fun bind(model: MyDonationModel){
            val donorId = model.Donor_Id
            val points = model.total_point
            donationPoint.text = "$points Points"

            getBadge(donorId)


        }

        private fun getBadge(donorId:String){
            firebaseFirestore.collection("USERS").document(donorId)
                .get().addOnSuccessListener {
                    val name = it.getString("name").toString()
//                    val totalQty:Long = it.getLong("total_donation_qty")!!.toLong()
                    val totalPoint = it.getLong("total_donation_point")!!.toLong()
                    donorName.text = name

                    when {
                        totalPoint <100 -> {
                            badgeBadge.setImageResource(R.drawable.ic_slide1)
                            badgeBadge.imageTintList = AppCompatResources.getColorStateList(itemView.context,R.color.grey_400)

                        }
                        totalPoint in 100..499 -> {
                            //donor badge image is created
                            badgeBadge.setImageResource(R.drawable.ic_slide1)
                            badgeBadge.imageTintList = null
                        }
                        totalPoint in 500..1999 -> {
                            badgeBadge.setImageResource(R.drawable.ic_slide2)
                            badgeBadge.imageTintList = null
                        }
                        totalPoint in 2000..4999 -> {
                            badgeBadge.setImageResource(R.drawable.ic_slide3)
                            badgeBadge.imageTintList = null

                        }
                        totalPoint in 5000..14999 -> {
                            badgeBadge.setImageResource(R.drawable.ic_slide4)
                            badgeBadge.imageTintList = null
                        }
                        totalPoint in 15000..49999 -> {
                            badgeBadge.setImageResource(R.drawable.ic_slide5)
                            badgeBadge.imageTintList = null
                        }
                        totalPoint in 50000..99999 -> {
                            badgeBadge.setImageResource(R.drawable.ic_slide6)
                            badgeBadge.imageTintList = null
                        }
                        totalPoint >100000 -> {
                            badgeBadge.setImageResource(R.drawable.ic_slide6)
                            badgeBadge.imageTintList = null
                        }

                    }
                }
        }
    }
}