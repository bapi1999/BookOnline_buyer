package com.sbdevs.bookonline.othercalss

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.SearchQueryAdapter
import java.util.*
import kotlin.collections.ArrayList



class FireStoreData {

    public val firebaseFirestore = Firebase.firestore
    val firebaseAuth = Firebase.auth

    private var searchList:ArrayList<String> = ArrayList()
    private lateinit var searchAdapter: SearchQueryAdapter //= SearchQueryAdapter(searchList,0,1)


//    var list:ArrayList<MutableMap<String, Any>> = ArrayList()

    fun getFirebaseCartList(context:Context,list:ArrayList<MutableMap<String, Any>>){

        firebaseFirestore.collection("USERS").document(firebaseAuth.currentUser!!.uid)
            .collection("USER_DATA").document("MY_CART")
            .get().addOnCompleteListener {
                if (it.isSuccessful){
                    val x = it.result?.get("cart_list")
                    if (x ==null){
                        Toast.makeText(context,"Failed", Toast.LENGTH_SHORT).show()
                    }else{
//                        list = x
                    }
                }
            }

    }

    @Suppress("UNCHECKED_CAST")
    fun getProductnameList(recyclerView:RecyclerView){
        firebaseFirestore.collection("PRODUCT_FILTER").document("FILTER_1").get().addOnSuccessListener {

            searchList  = it.get("LIST1") as ArrayList<String>
//            searchAdapter.list =searchList
            searchAdapter.notifyDataSetChanged()
            //searchAdapter = SearchQueryAdapter(searchList)
            recyclerView.adapter = searchAdapter
        }
    }




    fun durationFromNow(startDate: Date): String {
        var different: Long = System.currentTimeMillis() - startDate.time
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
        if (elapsedMinutes > 0 || elapsedSeconds > 0) output += "$elapsedSeconds seconds"
        return output
    }



    fun msToTimeAgo(context: Context,startDate:Date): String {
        val seconds = (System.currentTimeMillis() - startDate.time) / 1000f

        return when (true) {
            seconds < 60 -> context.resources.getQuantityString(R.plurals.seconds_ago, seconds.toInt(), seconds.toInt())
            seconds < 3600 -> {
                val minutes = seconds / 60f
                context.resources.getQuantityString(R.plurals.minutes_ago, minutes.toInt(), minutes.toInt())
            }
            seconds < 86400 -> {
                val hours = seconds / 3600f
                context.resources.getQuantityString(R.plurals.hours_ago, hours.toInt(), hours.toInt())
            }
            seconds < 604800 -> {
                val days = seconds / 86400f
                context.resources.getQuantityString(R.plurals.days_ago, days.toInt(), days.toInt())
            }
            seconds < 2_628_000 -> {
                val weeks = seconds / 604800f
                context.resources.getQuantityString(R.plurals.weeks_ago, weeks.toInt(), weeks.toInt())
            }
            seconds < 31_536_000 -> {
                val months = seconds / 2_628_000f
                context.resources.getQuantityString(R.plurals.months_ago, months.toInt(), months.toInt())
            }
            else -> {
                val years = seconds / 31_536_000f
                context.resources.getQuantityString(R.plurals.years_ago, years.toInt(), years.toInt())
            }
        }
    }

    object Dbconst{
        val firebaseFirestore1 = Firebase.firestore
        val firebaseAuth1 = Firebase.auth
    }


}