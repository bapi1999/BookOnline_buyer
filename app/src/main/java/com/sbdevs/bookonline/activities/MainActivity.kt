package com.sbdevs.bookonline.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
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
import kotlinx.coroutines.tasks.await
import androidx.core.view.GravityCompat





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

        actionBar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)

        setupActionBarWithNavController(navController, appBarConfiguration)
//        (this as AppCompatActivity?)!!.supportActionBar!!.show()
        binding.navView.setupWithNavController(navController)
        val header = binding.navView.getHeaderView(0)
        val userName: TextView = header.findViewById(R.id.nav_header_txt)

        lifecycleScope.launch(Dispatchers.IO) {


            getTimeStamp()
        }
        getUsername(userName)

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

                    val cartIntent = Intent(this,CartActivity::class.java)
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
                R.id.termCondition ->{
                    val webIntent = Intent(this,WebViewActivity::class.java)
                    startActivity(webIntent)
                    closeDrawer()
                }
                R.id.privacy_policy ->{
                    closeDrawer()
                    //
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
        //Toast.makeText(this,"On start",Toast.LENGTH_SHORT).show()

        if(SharedDataClass.newLogin){
            user = Firebase.auth.currentUser
            SharedDataClass.newLogin = false
        }



        binding.searchBtn.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        binding.layCart.cartBadgeContainerLay.setOnClickListener {
            if (user != null){

//                navController.navigateUp() // to clear previous navigation history
//                navController.navigate(R.id.myCartFragment)

                val cartIntent = Intent(this,CartActivity::class.java)
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
        //Toast.makeText(this,"OnResume",Toast.LENGTH_SHORT).show()
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

    override fun onPause() {
        super.onPause()
        //Toast.makeText(this,"OnPause",Toast.LENGTH_SHORT).show()


    }

    override fun onDestroy() {
        super.onDestroy()
        //Toast.makeText(this,"OnDestroy",Toast.LENGTH_SHORT).show()

    }




    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    private suspend fun getTimeStamp(){

        if (user != null){
            firebaseFirestore.collection("USERS")
                .document(user!!.uid)
                .collection("USER_DATA")
                .document("MY_NOTIFICATION")
                .get().addOnSuccessListener {

                    //val timeStamp1 = it.getTimestamp("new_notification")!!
                    val timeStamp1:Timestamp = (it.get("new_notification") as Timestamp?)!!

                    getNotificationForOptionMenu(timeStamp1,notificationBadgeText)
                }.addOnFailureListener {
                    Log.e("get Notification time","${it.message}")
                }.await()
        }


    }

    private fun getNotificationForOptionMenu(timeStamp1:Timestamp,textView: TextView) {

        if (user != null) {
            val ref = firebaseFirestore.collection("USERS")
                .document(user!!.uid)
                .collection("USER_DATA")
                .document("MY_NOTIFICATION")
                .collection("NOTIFICATION")
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
                .collection("USER_DATA")
                .document("MY_NOTIFICATION")

            val notiMAp: MutableMap<String, Any> = HashMap()
            notiMAp["new_notification"] = FieldValue.serverTimestamp()
            ref.update(notiMAp)
        }


    }

    private fun getUsername(textView: TextView) {
        if (user != null){
            firebaseFirestore.collection("USERS").document(user!!.uid)
                .get().addOnSuccessListener {
                    val email = it.getString("email").toString()
                    val name = it.getString("name").toString()
                    if (name == "") {
                        textView.text = email
                    } else {
                        textView.text = name
                    }
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

