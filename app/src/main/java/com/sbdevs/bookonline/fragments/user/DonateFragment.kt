package com.sbdevs.bookonline.fragments.user

import android.annotation.SuppressLint
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
import com.sbdevs.bookonline.adapters.donate.DonateItemAdapter
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

    var itemNameList1: MutableList<String> = ArrayList()
    var quantityList: MutableList<Int> = ArrayList()
    var pointList: MutableList<Int> = ArrayList()


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

        itemNameList1.add("Books")
        itemNameList1.add("School Bags")
        itemNameList1.add("Pencil Box")
        itemNameList1.add("Clip Board")
        itemNameList1.add("Colors set")
        itemNameList1.add("Paint Brush")
        itemNameList1.add("Toy")


        recyclerView.adapter = itemAdapter



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
                itemMap["Type"] = itemNameList1[i]
                itemMap["qty"] = quantityList[i]
                itemMap["point"] = pointList[i]

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
        firebaseFirestore.collection("DONATIONS").add(donateMap)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Successful", Toast.LENGTH_SHORT).show()

                loadingDialog.dismiss()
                dbItemList.clear()
                activity?.finish()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
                Log.e("Exception", "${it.message}")
                loadingDialog.dismiss()
            }.await()


    }



}