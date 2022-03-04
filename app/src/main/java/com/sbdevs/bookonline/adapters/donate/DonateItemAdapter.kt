package com.sbdevs.bookonline.adapters.donate

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.models.DonateItemModel

class DonateItemAdapter(
    var list: ArrayList<DonateItemModel>,
    val listener: MyOnItemClickListener
) : RecyclerView.Adapter<DonateItemAdapter.ViewHolder>() {

    interface MyOnItemClickListener{
        fun onQuantityChange(qty: Int, point:Int,qtyList: MutableList<Int>,pntList: MutableList<Int>)
    }

    var quantityList:MutableList<Int> = ArrayList()// MutableList(7){0}
    var pointList:MutableList<Int> = ArrayList()//MutableList(7){0}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_donate_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        private val itemNameText: TextView = itemView.findViewById(R.id.item_name)
        private val itemQuantity:EditText = itemView.findViewById(R.id.editTextQuantity)
        private val xPointText: TextView = itemView.findViewById(R.id.textView96)
        private val totalPointText:TextView = itemView.findViewById(R.id.textView97)
        private val minusBtn:ImageButton = itemView.findViewById(R.id.down_button)
        private val plusBtn:ImageView = itemView.findViewById(R.id.up_button)


        fun bind(item: DonateItemModel){
            val itemName:String = item.itemName
            val xPoint:Int = item.xPoint

            pointList = MutableList(list.size){0}
            quantityList = MutableList(list.size){0}


            itemNameText.text = "$itemName "
            xPointText.text = "(${xPoint} point / item) size:${pointList.size}"

            plusBtn.setOnClickListener {

                val st = itemQuantity.text.toString().trim()
                if (st.isNullOrEmpty()){
                    itemQuantity.setText("1")
                }else{
                    val qty = st.toInt()+1
                    itemQuantity.setText(qty.toString())
                }
            }

            minusBtn.setOnClickListener {

                val st = itemQuantity.text.toString().trim()
                if (st.isNullOrEmpty()){
                    itemQuantity.setText("0")
                }else{
                    val qty = st.toInt()-1
                    if (qty>0){
                        itemQuantity.setText(qty.toString())
                    }else{
                        itemQuantity.setText("0")
                    }


                }
            }

            itemQuantity.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
//                    TODO("Not yet implemented")
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    TODO("Not yet implemented")
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.isNullOrEmpty()){
                        Log.d("string","empty")
                        totalPointText.text = "0"
                        quantityList[bindingAdapterPosition] = 0
                        pointList[bindingAdapterPosition] = 0
                        setShit(quantityList,pointList)
                    }else{
                        val qty = s.toString().toInt()
                        val point = qty*xPoint
                        totalPointText.text = point.toString()

                        quantityList[bindingAdapterPosition] = qty
                        pointList[bindingAdapterPosition] = point
                        setShit(quantityList,pointList)
                    }
                }
            })





        }

        fun setShit(qtyList: MutableList<Int>,ptList: MutableList<Int>){
            var qty = 0
            var point = 0
            for (qt in qtyList){
                qty+=qt
            }

            for (pt in ptList){
                point+=pt
            }
            listener.onQuantityChange(qty,point,qtyList,ptList)

        }



    }

}