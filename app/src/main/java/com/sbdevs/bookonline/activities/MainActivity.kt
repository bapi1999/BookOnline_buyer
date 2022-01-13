package com.sbdevs.bookonline.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityMainBinding
import com.sbdevs.bookonline.fragments.LoginDialogFragment
import com.sbdevs.bookonline.othercalss.FireStoreData
import com.sbdevs.bookonline.othercalss.SharedDataClass


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser


    lateinit var cartBadgeText: TextView
    lateinit var notificationBadgeText: TextView
    private val loginDialog = LoginDialogFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

//        val list1 =  fireStoreData.getFirebaseCartList(this)

        val actionBar = binding.toolbar


        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.myCartFragment,
                R.id.myOrderFragment,
                R.id.myWishlistFragment,
                R.id.notificationFragment,
                R.id.myAccountFragment
            ), binding.drawerLayout
        )

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        setSupportActionBar(actionBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setupActionBarWithNavController(navController, appBarConfiguration)
//        (this as AppCompatActivity?)!!.supportActionBar!!.show()
        binding.navView.setupWithNavController(navController)
        val header = binding.navView.getHeaderView(0)
        val userName: TextView = header.findViewById(R.id.nav_header_txt)
        getUsername(userName)

        notificationBadgeText = binding.layNotify.notificationBadgeCounter
        cartBadgeText = binding.layCart.cartBadgeCounter

    }

    override fun onStart() {
        super.onStart()

        getNotificationForOptionMenu()
        val dataClass = SharedDataClass()
        dataClass.cartNumber
        dataClass.getCartListForOptionMenu(cartBadgeText)

        binding.searchBtn.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        binding.layCart.cartBadgeContainerLay.setOnClickListener {
            if (user != null){
                navController.navigateUp() // to clear previous navigation history
                navController.navigate(R.id.myCartFragment)
            }else{
                loginDialog.show(supportFragmentManager, "custom login dialog")

            }
        }

        binding.layNotify.notificationBadgeContainerLay.setOnClickListener {
            if (user != null){
                updateNotificationForOptionMenu()
                navController.navigateUp() // to clear previous navigation history
                navController.navigate(R.id.notificationFragment)
            }else{
                loginDialog.show(supportFragmentManager, "custom login dialog")
            }
        }


    }




    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun getCartListForOptionMenu(textView: TextView) {
        if(user != null){
            firebaseFirestore.collection("USERS").document(user.uid)
                .collection("USER_DATA")
                .document("MY_CART").addSnapshotListener { value, error ->
                    error?.let {
                        Log.e(TAG,"error in loading cart",it.cause)
                        return@addSnapshotListener
                    }
                    value?.let {
                        val x = value.get("cart_list")
                        if (x != null) {
                            val fbCartList = x as ArrayList<MutableMap<String, Any>>
                            if (fbCartList.size <= 0) {
                                textView.visibility = View.GONE
                            } else {
                                textView.visibility = View.VISIBLE
                                textView.text = fbCartList.size.toString()
                            }
                        } else {
                            textView.visibility = View.GONE
                        }
                    }

                }

        }else{
           Log.w("CartList","User not logged in")
        }

    }

    private fun getNotificationForOptionMenu() {
        if (user != null){
            val ref = firebaseFirestore.collection("USERS")
                .document(user.uid)
                .collection("USER_DATA")
                .document("MY_NOTIFICATION")

            ref.addSnapshotListener { value, error ->
                error?.let {
                    Log.e(TAG,"can not load notification",it.cause)
                    return@addSnapshotListener
                }
                value?.let {
                    val newNotification = it.getLong("new_notification")
                    if (newNotification == 0L) {
                        notificationBadgeText.visibility = View.GONE
                    } else {
                        notificationBadgeText.text = newNotification.toString()
                    }

                }
            }
        }else{
            Log.w("Notification","User not logged in")
        }

    }

    private fun updateNotificationForOptionMenu() {
        if (user!= null){
            val ref = firebaseFirestore.collection("USERS")
                .document(user.uid)
                .collection("USER_DATA")
                .document("MY_NOTIFICATION")

            val notiMAp: MutableMap<String, Any> = HashMap()
            notiMAp["new_notification"] = 0L
            ref.update(notiMAp)
        }


    }

    private fun getUsername(textView: TextView) {
        if (user != null){
            firebaseFirestore.collection("USERS").document(user.uid)
                .addSnapshotListener { value, error ->
                    error?.let {
                        Log.e(TAG,"error in loading username",it.cause)
                        return@addSnapshotListener
                    }
                    value?.let {
                        val email = it.getString("email").toString()
                        val name = it.getString("name").toString()
                        if (name == "") {
                            textView.text = email
                        } else {
                            textView.text = name
                        }
                    }
                }
        }else{
            textView.text = R.string.you_aren_t_logged_in.toString()
        }

    }


}

