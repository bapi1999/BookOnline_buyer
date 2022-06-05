package com.sbdevs.bookonline.seller.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.FragmentSlMyEarningBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import com.sbdevs.bookonline.seller.adapters.EarningAdapter
import com.sbdevs.bookonline.seller.models.EarningModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SlMyEarningFragment : Fragment() {
    private var _binding: FragmentSlMyEarningBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private val loadingDialog = LoadingDialog()
    private var orderList: MutableList<EarningModel> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private val earningAdapter = EarningAdapter(orderList)
    private lateinit var accountBalanceText: TextView
    private lateinit var upcomingPaymentText: TextView

    private lateinit var withdrawalBtn:Button
    private val gone = View.GONE
    private val visible = View.VISIBLE
    var st = ""
    private var accountBalance = 0L
    var newBalance = 0L


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSlMyEarningBinding.inflate(inflater, container, false)
        recyclerView = binding.lay2.earningRecycler
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadingDialog.show(childFragmentManager, "show")

        getAccountBalance()

        getDeliveredProduct()


        lifecycleScope.launch(Dispatchers.IO) {
            calculateAccountBalance()
        }


        accountBalanceText = binding.lay1.accountBalanceText
        upcomingPaymentText = binding.lay1.upcomingPaymentText
        withdrawalBtn = binding.lay1.withdrawalBtn

        withdrawalBtn.setOnClickListener {
            if (accountBalance>=200L){
                parentFragmentManager.commit {
                    setReorderingAllowed(true)
                    add(R.id.fragment_container, WithdrawalFragment())
                    addToBackStack("withdrawal")
                }


            }else{
                binding.lay1.errorMessageText.visibility = visible
                binding.lay1.errorMessageText.text = "Minimum balance to withdraw is rs.200"
            }

        }



        recyclerView.adapter = earningAdapter

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }



    private fun getDeliveredProduct() {

        firebaseFirestore.collection("ORDERS")
            .whereEqualTo("ID_Of_SELLER", user!!.uid)
            .whereEqualTo("status", "delivered")
            .whereEqualTo("already_credited", false)
//            .whereGreaterThan("Time_delivered",last_time_sale_check)
            .orderBy("Time_delivered", Query.Direction.ASCENDING)
            .limit(10L).get().addOnSuccessListener {
                val allDocumentSnapshot = it.documents

                orderList = it.toObjects(EarningModel::class.java)


                if (orderList.isEmpty()) {
                    Toast.makeText(requireContext(), "List is empty", Toast.LENGTH_SHORT).show()
                    upcomingPaymentText.text = "no payment found"
                } else {
                    earningAdapter.list = orderList
                    earningAdapter.notifyDataSetChanged()

                    upcomingPaymentText.text =
                        "Rs.${orderList[0].PRICE_SELLING_TOTAL}/-  will be added in next ${
                            durationFromNow(orderList[0])
                        }"
                }

                loadingDialog.dismiss()
            }.addOnFailureListener {
                Log.e("Load orders", "${it.message}")
                loadingDialog.dismiss()
            }
    }

    //todo- check account balance in real time
    private fun getAccountBalance() {
        firebaseFirestore.collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_DATA")
            .document("MY_EARNING")
            .addSnapshotListener { value, error ->
                error?.let {
                    Log.e("Get Account balanceError", "${it.message}")
                    return@addSnapshotListener
                }
                value?.let {
                    accountBalance = it.getLong("current_amount")!!.toLong()
                    accountBalanceText.text = accountBalance.toString()
                }
            }
    }


    //todo- calculate all UnAdded balance
    private suspend fun calculateAccountBalance(){
        firebaseFirestore
            .collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_DATA")
            .document("MY_EARNING")
            .collection("EARNINGS")
            .whereEqualTo("is_added",false)
            .get().addOnSuccessListener {

                val allDocument = it.documents

                if (allDocument.isNotEmpty()){
                    lifecycleScope.launch(Dispatchers.IO) {
                        withContext(Dispatchers.IO){
                            for (doc in allDocument){
                                val docId = doc.id
                                val isAdded:Boolean = doc["is_added"] as Boolean
                                val amount1:Long = doc["AMOUNT"].toString().toLong()

                                newBalance +=amount1

                                //todo-make is_added = true for document whom balance is already added
                                updateEarningCollection(docId)

                            }
                        }
                        withContext(Dispatchers.IO){

                            //todo-update account balance after calculate all document
                            updateAccountBalance(newBalance)

                        }
                    }

                }else{
                    Log.i("calculateAccountBalance","empty :list")
                }

            }.addOnFailureListener {
                Log.e("ERROR calculateAccountBalance:" ,"${it.message}")
            }.await()
    }

    private suspend fun updateEarningCollection(docId:String){
        val newmap:MutableMap<String,Any> = HashMap()
        newmap["is_added"] = true

        firebaseFirestore
            .collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_DATA")
            .document("MY_EARNING")
            .collection("EARNINGS")
            .document(docId).update(newmap)
            .addOnSuccessListener {  }.await()
    }

    private fun updateAccountBalance(balance:Long){
        val newmap:MutableMap<String,Any> = HashMap()
        newmap["current_amount"] = (balance + accountBalance)

        firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")
            .document("MY_EARNING")
            .update(newmap)

    }



    private fun durationFromNow(model:EarningModel): String {

        val timeDelivered = model.Time_delivered!!
        val timePeriod = model.Time_period

        val afterTimePeriod = Date(timeDelivered.time + (1000 * 60 * 60 * 24*timePeriod))
        val cal = Calendar.getInstance()
        cal.time = afterTimePeriod
        cal[Calendar.HOUR_OF_DAY] = 23
        cal[Calendar.MINUTE] = 59
        cal[Calendar.SECOND] = 50
        cal[Calendar.MILLISECOND] = 0
        val dd: Date = cal.time

        val difDate = Date(dd.time - Date().time)

        var different: Long = difDate.time

        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24


        val elapsedDays = different / daysInMilli
        different %= daysInMilli
        val elapsedHours = different / hoursInMilli
        different %= hoursInMilli
        val elapsedMinutes = different / minutesInMilli
        different %= minutesInMilli
        val elapsedSeconds = different / secondsInMilli
        var output = ""
        if (elapsedDays > 0) output += elapsedDays.toString() + "days "
        if (elapsedDays > 0 || elapsedHours > 0) output += "$elapsedHours hours "
        if (elapsedHours > 0 || elapsedMinutes > 0) output += "$elapsedMinutes minutes "
//        if (elapsedMinutes > 0 || elapsedSeconds > 0) output += "$elapsedSeconds seconds"
        return output
    }



}