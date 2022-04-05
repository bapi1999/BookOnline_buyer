package com.sbdevs.bookonline.fragments.user

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.donation.ThanksForDonateActivity
import com.sbdevs.bookonline.activities.user.MyAddressActivity
import com.sbdevs.bookonline.adapters.donate.DonateItemAdapter
import com.sbdevs.bookonline.adapters.user.OrderSummaryAdapter
import com.sbdevs.bookonline.databinding.FragmentDonateBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.models.DonateItemModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DonateFragment : Fragment(), DonateItemAdapter.MyOnItemClickListener {

    private var _binding: FragmentDonateBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private var itemList: ArrayList<DonateItemModel> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private var itemAdapter: DonateItemAdapter = DonateItemAdapter(itemList, this)

    private lateinit var totalPointText: TextView
    private lateinit var totalItemCount: TextView
    private var totalPoint: Int = 0
    private var totalQty: Int = 0

    var fromIndex: Int = 0
    var toIndex: Int = 0

    var quantityList: MutableList<Int> = ArrayList()
    var pointList: MutableList<Int> = ArrayList()

    private val gone = View.GONE
    private val visible = View.VISIBLE

    private var thereIsAddressError: Boolean = false
    lateinit var addressList:ArrayList<MutableMap<String, Any>>
    var addressMap:MutableMap<String,Any> = HashMap()

    var dbItemList: ArrayList<MutableMap<String, Any>> = ArrayList()

    private val loadingDialog = LoadingDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDonateBinding.inflate(inflater, container, false)

        totalPointText = binding.totalPoint
        totalItemCount = binding.totalItemCount

        recyclerView = binding.itemRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        itemList.add(DonateItemModel("Books", 5))
        itemList.add(DonateItemModel("School Bags", 8))
        itemList.add(DonateItemModel("Pencil Box", 6))
        itemList.add(DonateItemModel("Clip Board", 8))
        itemList.add(DonateItemModel("Colors set", 1))
        itemList.add(DonateItemModel("Paint Brush", 5))
        itemList.add(DonateItemModel("Toy", 2))


        recyclerView.adapter = itemAdapter

        getAddress()

        val lay2 =  binding.miniAddress
        lay2.buyerAddressType.visibility = gone
        lay2.textView56.text = "Address"

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.contributeButton.setOnClickListener {
            loadingDialog.show(childFragmentManager, "Show")
            lifecycleScope.launch(Dispatchers.IO) {
                normFunc()

                sendDonateRequest()

            }
        }


        binding.changeoraddAddressBtn.setOnClickListener {
            val intent = Intent(requireContext(), MyAddressActivity::class.java)
            intent.putExtra("from",2)
            //1 = from MyAccountFragment 2 = OrderDetailsFRagment
            startActivity(intent)
        }

    }


    override fun onQuantityChange(
        qty: Int,
        point: Int,
        qtyList: MutableList<Int>,
        pntList: MutableList<Int>
    ) {
        totalPoint = point
        totalQty = qty
        totalPointText.text = totalPoint.toString()
        totalItemCount.text = totalQty.toString()

        quantityList = qtyList
        pointList = pntList


        fromIndex = qtyList.size - itemList.size
        toIndex = qtyList.size


    }


    private suspend fun normFunc() {

        for (i in 0 until quantityList.size) {

            if (quantityList[i] == 0) {

                Log.e("list", "empty $i")
            } else {
                val itemMap: MutableMap<String, Any> = HashMap()
                itemMap["Type"] = itemList[i].itemName
                itemMap["qty"] = quantityList[i]
                itemMap["points_per_item"] = itemList[i].xPoint

                dbItemList.add(itemMap)
                Log.e("list", "not empty $i")

            }
        }
    }


    private suspend fun sendDonateRequest() {

        val donateMap: MutableMap<String, Any> = HashMap()
        donateMap["Time_donate_request"] = Timestamp(Date())
//        donateMap["Time_donate_received"]=
        donateMap["total_qty"] = totalQty.toLong()
        donateMap["total_point"] = totalPoint.toLong()
        donateMap["Donor_Id"] = user!!.uid
        donateMap["item_List"] = dbItemList
        donateMap["is_received"] = false
        donateMap["address"] = addressMap
        donateMap["Donation_ID"] = donationId()
        firebaseFirestore.collection("DONATIONS").add(donateMap)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Successful", Toast.LENGTH_SHORT).show()

                loadingDialog.dismiss()
                dbItemList.clear()
                val newIntent = Intent(requireContext(), ThanksForDonateActivity::class.java)
                newIntent.putExtra("totalPoint",totalPoint)
                startActivity(newIntent)
                activity?.finish()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
                Log.e("Exception", "${it.message}")
                loadingDialog.dismiss()
            }.await()


    }



    private fun getAddress(){

        val lay2 =  binding.miniAddress

        firebaseFirestore.collection("USERS").document(user!!.uid).collection("USER_DATA")
            .document("MY_ADDRESSES").addSnapshotListener { value, error ->
                error?.let {
                    Log.e("Get address","${it.message}")

                    binding.addressLay.visibility = gone
                    binding.addressError.visibility = visible
                    thereIsAddressError = true
                    return@addSnapshotListener
                }

                value?.let {
                    val position: Long = it.getLong("select_No")!!
                    val x = it.get("address_list")


                    if (x != null){

                        addressList = x as ArrayList<MutableMap<String, Any>>

                        if (addressList.size != 0){

                            val group:MutableMap<String,Any> = addressList[position.toInt()]
                            addressMap = addressList[position.toInt()]

                            val buyerName:String = group["name"].toString()
                            val buyerAddress1:String = group["address1"].toString()
                            val buyerAddress2:String = group["address2"].toString()
                            val buyerAddressType:String = group["address_type"].toString()
                            val buyerTown:String = group["city_vill"].toString()
                            val buyerPinCode:String = group["pincode"].toString()
                            val buyerState:String = group["state"].toString()
                            val buyerPhone:String = group["phone"].toString()

                            val addressBuilder  = StringBuilder()
                            addressBuilder.append(buyerAddress1).append(", ").append(buyerAddress2)

                            val townPinBuilder  = StringBuilder()
                            townPinBuilder.append(buyerTown).append(", ").append(buyerPinCode)

                            lay2.buyerName.text = buyerName
                            lay2.buyerAddress.text = addressBuilder.toString()
                            lay2.buyerAddressType.text = buyerAddressType
                            lay2.buyerTownAndPin.text =townPinBuilder.toString()
                            lay2.buyerState.text = buyerState
                            lay2.buyerPhone.text = buyerPhone

                            binding.addressLay.visibility = visible
                            binding.addressError.visibility =gone
                            thereIsAddressError = false

                        }else{
                            binding.addressLay.visibility = gone
                            binding.addressError.visibility = visible
                            thereIsAddressError = true
                        }

                    }else{
                        binding.addressLay.visibility = gone
                        binding.addressError.visibility = visible
                        thereIsAddressError = true

                    }


                }
            }

    }

    private fun donationId():String{
        val rnds = (10000000..100000000).random()
        return rnds.toString()
    }



}