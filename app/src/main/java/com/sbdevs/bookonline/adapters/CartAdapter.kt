package com.sbdevs.bookonline.adapters

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


class CartAdapter(var list:ArrayList<MutableMap<String,Any>>,val listener: MyOnItemClickListener) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {


    interface MyOnItemClickListener{
        fun onItemClick(position: Int)
        fun onQuantityChange(position: Int,textView: TextView)
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_cart_item_lay_1, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: CartAdapter.ViewHolder, position: Int) {


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
        private val variantTxt:TextView = itemView.findViewById(R.id.variant)
        private val viewBtn:AppCompatButton = itemView.findViewById(R.id.view_details)
        private val removeBtn:LinearLayout = itemView.findViewById(R.id.remove_btn)
        private val quantityContainer:LinearLayout = itemView.findViewById(R.id.quantity_container)




        fun bind(group:MutableMap<String,Any>){
            val productId:String = group["product"] as String
            val quantity:Long = group["quantity"] as Long

            quantitiesTxt.text = quantity.toString()
            removeBtn.setOnClickListener {
                listener.onItemClick(adapterPosition)

            }

            quantityContainer.setOnClickListener {
                listener.onQuantityChange(adapterPosition,quantitiesTxt)
            }

            viewBtn.setOnClickListener {
                val productIntent = Intent(itemView.context, ProductActivity::class.java)
                productIntent.putExtra("productId",productId)
                itemView.context.startActivity(productIntent)
            }

            firebaseFirestore.collection("PRODUCTS").document(productId)
                .get().addOnSuccessListener {
                    var categoryString = ""
                    val url = it.get("product_thumbnail").toString().trim()
                    val title:String = it.getString("book_title")!!
                    val stock = it.getLong("in_stock_quantity")!!
                    val categoryList: ArrayList<String> = it.get("categories") as ArrayList<String>

                    val priceOriginal = it.getLong("price_original")!!.toLong()
                    val priceSelling = it.getLong("price_selling")!!.toLong()

                    productName.text = title

                    Glide.with(itemView.context).load(url).placeholder(R.drawable.as_square_placeholder).into(productImage);

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

                    for (catrgorys in categoryList) {
                        categoryString += "$catrgorys,  "
                    }
                    variantTxt.text = categoryString

                }.addOnFailureListener {
                    Log.e("CartAdapter","${it.message}")
                }
        }
    }

}