package com.sbdevs.bookonline.adapters.user

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.ProductActivity
import com.sbdevs.bookonline.models.user.CartModel


class CartAdapter(var list:ArrayList<CartModel>, val listener: MyOnItemClickListener) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {


    interface MyOnItemClickListener{
        fun onItemClick(position: Int)
        fun onQuantityChange(position: Int,textView: TextView)
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_cart_item_lay_1, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        holder.bind(list[position])


    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView:View) :RecyclerView.ViewHolder(itemView){
        val firebaseFirestore = Firebase.firestore
        val firebaseAuth = FirebaseAuth.getInstance()
        private val productImage : ImageView = itemView.findViewById(R.id.product_image)
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)
        private val productRealPrice:TextView = itemView.findViewById(R.id.product_real_price)
        private val percentOff: TextView = itemView.findViewById(R.id.percent_off)
        private val quantitiesTxt:TextView = itemView.findViewById(R.id.quantity)
        private val outofstockIcon:ImageView = itemView.findViewById(R.id.outofstock_icon)
        private val viewBtn:AppCompatButton = itemView.findViewById(R.id.view_details)
        private val removeBtn:LinearLayout = itemView.findViewById(R.id.remove_btn)
        private val quantityContainer:LinearLayout = itemView.findViewById(R.id.quantity_container)




        fun bind(item: CartModel){
            val productId:String = item.productId
            val quantity:Long = item.orderQuantity

            quantitiesTxt.text = quantity.toString()


            removeBtn.setOnClickListener {
                listener.onItemClick(absoluteAdapterPosition)

            }

            quantityContainer.setOnClickListener {
                listener.onQuantityChange(absoluteAdapterPosition,quantitiesTxt)
            }

            viewBtn.setOnClickListener {
                val productIntent = Intent(itemView.context, ProductActivity::class.java)
                productIntent.putExtra("productId",productId)
                itemView.context.startActivity(productIntent)
            }

            val priceOriginal = item.priceOriginal
            val priceSelling = item.priceSelling
            val stock = item.stockQty


            productName.text = item.title

            Glide.with(itemView.context).load(item.url).placeholder(R.drawable.as_square_placeholder).into(productImage);

            if (priceOriginal == 0L){
                val price = priceSelling.toInt()*quantity.toInt()
                productPrice.text = price.toString()
                productRealPrice.visibility = View.GONE
                percentOff.visibility = View.GONE

            }else{

                val price = priceSelling.toInt()*quantity.toInt()
                val realPrice = priceOriginal.toInt()*quantity.toInt()
                val percent:Int = (100* (realPrice - price)) / ( realPrice )

                productPrice.text = price.toString()
                productRealPrice.text = realPrice.toString()
                percentOff.text = "${percent}% off"

            }
            if (stock == 0L){
                outofstockIcon.visibility = View.VISIBLE
            }else{
                outofstockIcon.visibility = View.GONE
            }

        }

    }

}