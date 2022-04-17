package com.sbdevs.bookonline.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityMainBinding
import com.sbdevs.bookonline.fragments.LoginDialogFragment
import com.sbdevs.bookonline.models.NotificationModel
import com.sbdevs.bookonline.othercalss.SharedDataClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.view.GravityCompat
import com.sbdevs.bookonline.activities.donation.AllDonationActivity
import com.sbdevs.bookonline.activities.donation.MyDonationActivity
import com.sbdevs.bookonline.activities.java.SearchActivity2
import com.sbdevs.bookonline.activities.user.CartActivity
import com.sbdevs.bookonline.activities.user.SellerShopActivity
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var firebaseFirestore = Firebase.firestore
    private var user = Firebase.auth.currentUser

    private var notificationList:List<NotificationModel> = ArrayList()
    lateinit var cartBadgeText: TextView
    lateinit var notificationBadgeText: TextView
    private val loginDialog = LoginDialogFragment()
    private lateinit var timeStamp:Timestamp
    private val gone = View.GONE
    private val visible = View.VISIBLE
    private var isSeller = false

    private lateinit var userImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userMail: TextView
    private lateinit var profileText: TextView
    private lateinit var donationCoinText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val actionBar = binding.toolbar


        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
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

        userImage = header.findViewById(R.id.user_image)
        userName = header.findViewById(R.id.nav_header_txt)
        userMail = header.findViewById(R.id.user_mail)
        profileText = header.findViewById(R.id.profile_text)

        donationCoinText = header.findViewById(R.id.donationCoinText)


        lifecycleScope.launch(Dispatchers.IO) {

            getUsername(userName)

        }


        notificationBadgeText = binding.layNotify.notificationBadgeCounter
        cartBadgeText = binding.layCart.cartBadgeCounter
        val v = SharedDataClass

        v.getCartListForOptionMenu()
        v.getWishList()

        //Toast.makeText(this,"OnCreate",Toast.LENGTH_SHORT).show()



        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){


                R.id.homeFragment ->{
                    //navController.navigateUp() // to clear previous navigation history
                    navController.navigate(R.id.homeFragment)
                    binding.drawerLayout.closeDrawers()
                    //closeDrawer()
                }
                R.id.myWishlistFragment ->{
                    navController.navigate(R.id.myWishlistFragment)
                    binding.drawerLayout.closeDrawers()
                }
                R.id.myCartFragment ->{

                    val cartIntent = Intent(this, CartActivity::class.java)
                    startActivity(cartIntent)
                    closeDrawer()
                }
                R.id.myAccountFragment ->{
                    navController.navigate(R.id.myAccountFragment)
                    binding.drawerLayout.closeDrawers()
                }
                R.id.myOrderFragment ->{
                    navController.navigate(R.id.myOrderFragment)
                    closeDrawer()
                }
                R.id.notificationFragment ->{
                    navController.navigate(R.id.notificationFragment)
                    closeDrawer()
                }

                R.id.donate_menu ->{
                    val donationIntent = Intent(this, AllDonationActivity::class.java)
                    startActivity(donationIntent)
                    closeDrawer()
                }

                R.id.my_donations ->{
                    val myDonationIntent = Intent(this, MyDonationActivity::class.java)
                    startActivity(myDonationIntent)
                    closeDrawer()
                }

                R.id.termCondition ->{
                    closeDrawer()
                }
                R.id.privacy_policy ->{

                    closeDrawer()
                }
                R.id.return_policy ->{
                    closeDrawer()
                    //
                }
                R.id.help ->{
                    closeDrawer()
                    //
                }



            }
            true
        }



    }

    override fun onStart() {
        super.onStart()

        if(SharedDataClass.newLogin){
            user = Firebase.auth.currentUser
            SharedDataClass.newLogin = false
        }

        binding.searchBtn.setOnClickListener {
            val intent = Intent(this, SearchActivity2::class.java)
            startActivity(intent)
        }

        binding.layCart.cartBadgeContainerLay.setOnClickListener {
            if (user != null){

                val cartIntent = Intent(this, CartActivity::class.java)
                startActivity(cartIntent)

            }else{
                loginDialog.show(supportFragmentManager, "custom login dialog")

            }
        }

        binding.layNotify.notificationBadgeContainerLay.setOnClickListener {
            if (user != null){
                updateNotificationForOptionMenu()
                navController.navigateUp() // to clear previous navigation history
                navController.navigate(R.id.notificationFragment)
                notificationBadgeText.visibility = View.GONE
            }else{
                loginDialog.show(supportFragmentManager, "custom login dialog")
            }
        }

    }


    override fun onResume() {
        super.onResume()
        if (user == null){
            cartBadgeText.visibility = gone
        }else{
            if (SharedDataClass.dbCartList.size == 0){
                cartBadgeText.visibility = gone
            }else{
                cartBadgeText.visibility = visible
                cartBadgeText.text = SharedDataClass.dbCartList.size.toString()
            }
        }

        SharedDataClass.currentACtivity = 1
        SharedDataClass.product_id = ""

    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    private fun getNotificationForOptionMenu(timeStamp1:Timestamp,textView: TextView) {

        if (user != null) {
            val ref = firebaseFirestore.collection("USERS")
                .document(user!!.uid)
                .collection("USER_NOTIFICATIONS")
                .whereGreaterThan("date",timeStamp1)

            ref.addSnapshotListener { value, error ->
                error?.let {
                    Log.e("Notification","can not load notification",it.cause)
                    textView.visibility = View.GONE
                }

                value?.let {

                    notificationList = it.toObjects(NotificationModel::class.java)

                    //binding.layNotify.notificationBadgeCounter.text= notificationList.size.toString()
                    if (notificationList.isEmpty()){
                        textView.visibility = View.GONE
                    }else{
                        textView.visibility = View.VISIBLE
                        textView.text = notificationList.size.toString()
                    }
                }


            }
        }else{
            notificationBadgeText.visibility = View.GONE
        }


    }

    private fun updateNotificationForOptionMenu() {
        if (user!= null){
            val ref = firebaseFirestore.collection("USERS")
                .document(user!!.uid)

            val notiMAp: MutableMap<String, Any> = HashMap()
            notiMAp["new_notification_user"] = FieldValue.serverTimestamp()
            ref.update(notiMAp)
        }


    }

    private fun getUsername(textView: TextView) {
        if (user != null){
            firebaseFirestore.collection("USERS").document(user!!.uid)
                .get().addOnSuccessListener {
                    val email = it.getString("email").toString()
                    val name = it.getString("name").toString()
                    val profile = it.getString("profile").toString()
                    val timeStamp1:Timestamp = (it.get("new_notification_user") as Timestamp?)!!
                    val donationCoin:Long = it.getLong("my_donation_coins")!!.toLong()


                    userName.text = name
                    userMail.text = email

                    donationCoinText.text = "$donationCoin dc"

                    if (profile.isNullOrEmpty()){
                        profileText.visibility = visible
                        val firstLetter = name.substring( 0 , 1 ).uppercase(Locale.getDefault())
                        profileText.text = firstLetter
                    }else{
                        profileText.visibility = View.INVISIBLE
                        Picasso.get()
                            .load(profile)
                            .placeholder(R.drawable.as_square_placeholder)
                            .resize(100, 100)
                            .centerCrop()
                            .into(userImage)
                    }





                    getNotificationForOptionMenu(timeStamp1,notificationBadgeText)


                }
        }else{
            textView.text = getString(R.string.you_aren_t_logged_in)
        }

    }



    private fun closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }


}

