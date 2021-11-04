package com.sbdevs.bookonline.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.ProductDetailsActivity
import com.sbdevs.bookonline.models.CartModel
import kotlinx.coroutines.tasks.await


class CartAdapter(var list:ArrayList<MutableMap<String,Any>>,val listner: MyonItemClickListener) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {


    interface MyonItemClickListener{
        fun onItemClick(position: Int,view: View)
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
        private val stockNumberTxt:TextView = itemView.findViewById(R.id.stock)
        private val outofstockTxt:TextView = itemView.findViewById(R.id.outofstockText)
        private val variantTxt:TextView = itemView.findViewById(R.id.variant)
        private val viewBtn:TextView = itemView.findViewById(R.id.textView37)
        private val removeBtn:TextView = itemView.findViewById(R.id.textView38)
        private val quantityContainer:LinearLayout = itemView.findViewById(R.id.quantity_container)




        fun bind(group:MutableMap<String,Any>){
            val productId:String = group["product"] as String
            val quantity:Long = group["quantity"] as Long

            quantitiesTxt.text = quantity.toString()
            removeBtn.setOnClickListener {
                listner.onItemClick(adapterPosition,it)

            }

            quantityContainer.setOnClickListener {
                listner.onQuantityChange(adapterPosition,quantitiesTxt)
            }

            viewBtn.setOnClickListener {
                val productIntent = Intent(itemView.context, ProductDetailsActivity::class.java)
                productIntent.putExtra("productId",productId)
                itemView.context.startActivity(productIntent)
            }

            firebaseFirestore.collection("PRODUCTS").document(productId)
                .get().addOnCompleteListener {
                    if (it.isSuccessful){
                        var categoryString = ""
                        val url = it.result!!.get("product_thumbnail").toString().trim()
                        val title:String = it.result!!.getString("book_title")!!
                        val realPriceDB = it.result!!.getString("price_Rs")!!.trim()
                        val offerPriceDB = it.result!!.getString("price_offer")!!
                        val stock = it.result!!.getLong("in_stock_quantity")!!
                        val categoryList: ArrayList<String> = it.result!!.get("categories") as ArrayList<String>

                        productName.text = title

                        Glide.with(itemView.context).load(url).placeholder(R.drawable.as_square_placeholder).into(productImage);

                        if (offerPriceDB == ""){
                            val price = realPriceDB.toInt()*quantity.toInt()
                            productPrice.text = price.toString()
                            productRealPrice.visibility = View.GONE
                            percentOff.text = "Buy Now"

                        }else{

                            val price = offerPriceDB.toInt()*quantity.toInt()
                            val realPrice = realPriceDB.toInt()*quantity.toInt()

                            val percent:Int = (100* (realPrice - price)) / ( realPrice )

                            productPrice.text = price.toString()
                            productRealPrice.text = realPrice.toString()
                            percentOff.text = "${percent}% off"

                        }
                        if (stock != 0L){
                            stockNumberTxt.text = stock.toString()
                            outofstockTxt.visibility = View.GONE
                        }else{
                            stockNumberTxt.text = stock.toString()
                            outofstockTxt.visibility = View.VISIBLE
                        }

                        for (catrgorys in categoryList) {
                            categoryString += "$catrgorys,  "
                        }
                        variantTxt.text = categoryString


                    }else{
                        Log.d("Firebase error","nullable reference on firebase ")
                    }



                }
        }
    }

}