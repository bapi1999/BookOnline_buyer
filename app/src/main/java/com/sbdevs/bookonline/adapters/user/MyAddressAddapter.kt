package com.sbdevs.bookonline.adapters.user

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.user.EditAddressActivity
import com.sbdevs.bookonline.models.user.AddressModel
import java.io.Serializable

class MyAddressAddapter (var list:ArrayList<MutableMap<String,Any>>,var selectNo:Long,val listner: MyonItemClickListener) :
    RecyclerView.Adapter<MyAddressAddapter.ViewHolder>() {


    interface MyonItemClickListener{
        fun onItemClick(position: Int)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mini_address_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position],selectNo,list)
    }



    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val nameTxt:TextView = itemView.findViewById(R.id.buyer_name)
        val addressTxt:TextView = itemView.findViewById(R.id.buyer_address)
        val townAndPincodeTxt:TextView = itemView.findViewById(R.id.buyer_TownAndPin)
        private val stateTxt:TextView = itemView.findViewById(R.id.buyer_state)
        private val addressTypeTxt:TextView = itemView.findViewById(R.id.buyer_address_type)
        private val phoneTxt:TextView = itemView.findViewById(R.id.buyer_phone)
        private val checked:ImageView = itemView.findViewById(R.id.radioBtn)
        private val editBtn:Button = itemView.findViewById(R.id.edit_address_btn)



        fun bind(group:MutableMap<String,Any>,selectNo1:Long,adList:ArrayList<MutableMap<String,Any>>){

            var newAdrsList:ArrayList<AddressModel> = ArrayList()
            newAdrsList = adList as ArrayList<AddressModel>

            checked.setOnClickListener {
                listner.onItemClick(absoluteAdapterPosition)
            }

            editBtn.setOnClickListener {

                val intent = Intent(itemView.context, EditAddressActivity::class.java)

                intent.putExtra("editMap",group as Serializable)
                intent.putExtra("position",absoluteAdapterPosition)
                intent.putParcelableArrayListExtra("AddressList",newAdrsList)// address as parcelable


                itemView.context.startActivity(intent)

            }


            val position = adapterPosition.toLong()
            if (position == selectNo1){
                Glide.with(itemView.context).load(R.drawable.ic_check_box_24).into(checked)
            }else{
                Glide.with(itemView.context).load(R.drawable.ic_baseline_check_box_outline_blank_24).into(checked)
            }


            val buyerName:String = group["name"].toString()
            val buyerAddress1:String = group["address1"].toString()
            val buyerAddress2:String = group["address2"].toString()
            val buyerAddressType:String = group["address_type"].toString()


            val buyerTown:String = group["city_vill"].toString()
            val buyerPinCode:String = group["pincode"].toString()

            val buyerState:String = group["state"].toString()
            val buyerPhone:String = group["phone"].toString()

            addressTypeTxt.text = buyerAddressType
            nameTxt.text = buyerName
            addressTxt.text = "${buyerAddress1}, $buyerAddress2"
            townAndPincodeTxt.text = """$buyerTown, $buyerPinCode"""
            stateTxt.text = buyerState
            phoneTxt.text = buyerPhone

        }

    }
}