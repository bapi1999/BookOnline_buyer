package com.sbdevs.bookonline.adapters.user

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
        fun onItemRemoveClick(position: Int)
        fun onQuantityChange(position: Int,textView: TextView,quantity:Long,type:String)
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart_product, parent, false)
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
        private val quantitiesTxt:TextView = itemView.findViewById(R.id.quantity)
        private val deliveryChargeText:TextView = itemView.findViewById(R.id.delivery_charge)
        private val outofstockIcon:ImageView = itemView.findViewById(R.id.outofstock_icon)
        private val viewBtn:Button = itemView.findViewById(R.id.view_details)
        private val removeBtn:Button = itemView.findViewById(R.id.remove_btn)
        private val qtyPlusBtn:ImageView = itemView.findViewById(R.id.qtyPlusBtn)
        private val qtyMinusBtn:ImageView = itemView.findViewById(R.id.qtyMinesBtn)
        private val quantityContainer:LinearLayout = itemView.findViewById(R.id.quantity_container)
        private val warningsAndStockContainer:LinearLayout = itemView.findViewById(R.id.warningAndStockContainer)
        private val stockQuantityText:TextView = itemView.findViewById(R.id.stock_quantity)
        private val gone = View.GONE
        private val visible = View.VISIBLE


        fun bind(item: CartModel){
            val productId:String = item.productId
            val quantity:Long = item.orderQuantity

            val deliveryCharge = item.deliveryCharge

            quantitiesTxt.text = quantity.toString()


            removeBtn.setOnClickListener {
                listener.onItemRemoveClick(absoluteAdapterPosition)

            }

            qtyPlusBtn.setOnClickListener {
                listener.onQuantityChange(absoluteAdapterPosition,quantitiesTxt,quantity,"+ve",)
            }

            qtyMinusBtn.setOnClickListener {
                listener.onQuantityChange(absoluteAdapterPosition,quantitiesTxt,quantity,"-ve")
            }

            deliveryChargeText.text = deliveryCharge.toString()


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
                productRealPrice.visibility = gone


            }else{

                val price = priceSelling.toInt()*quantity.toInt()
                val realPrice = priceOriginal.toInt()*quantity.toInt()

                productPrice.text = price.toString()
                productRealPrice.text = realPrice.toString()


            }
            if (stock == 0L){
                outofstockIcon.visibility = visible
            }else{
                outofstockIcon.visibility = gone
            }

            stockQuantityText.text = "Only $stock left in stock"

            if (stock<quantity){
                warningsAndStockContainer.visibility = visible
            }else{
                warningsAndStockContainer.visibility = gone
            }

        }

    }

}