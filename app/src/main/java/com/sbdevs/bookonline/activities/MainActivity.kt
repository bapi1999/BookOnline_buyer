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
import com.sbdevs.bookonline.othercalss.FireStoreData


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth


    lateinit var cartBadgeText:TextView
    lateinit var notificationBadgeText:TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

//        val list1 =  fireStoreData.getFirebaseCartList(this)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,R.id.myCartFragment,R.id.myOrderFragment,R.id.myWishlistFragment,
                R.id.notificationFragment,R.id.myAccountFragment
            ), binding.drawerLayout
        )

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
//        (this as AppCompatActivity?)!!.supportActionBar!!.show()
        binding.navView.setupWithNavController(navController)
        val header = binding.navView.getHeaderView(0)
        val userName:TextView = header.findViewById(R.id.nav_header_txt)
        getUsername(userName)
//        userName.text = "edfwefwefwe"




    }

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_option_menu,menu)

        val cartMenu = menu?.findItem(R.id.main_cart)
        val notificationMenu = menu?.findItem(R.id.main_notification)

        val cartActionView = cartMenu!!.actionView
        cartBadgeText = cartActionView!!.findViewById(R.id.cart_badge_counter)
        getCartListForOptionMenu(cartBadgeText)
        cartActionView.setOnClickListener {
            onOptionsItemSelected(cartMenu)
        }


        val notifyActionView = notificationMenu!!.actionView
        notificationBadgeText = notifyActionView!!.findViewById(R.id.notification_badge_counter)
        getNotificationForOptionMenu(notificationBadgeText)
        notifyActionView.setOnClickListener {
            onOptionsItemSelected(notificationMenu)
        }



        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)

        val itemID = item.itemId

//        if (itemID == R.id.main_cart){
//            navController.navigateUp() // to clear previous navigation history
//            navController.navigate(R.id.myCartFragment)
//            return true
//        }
//        if (item.itemId == R.id.main_search){
////            val intent = Intent(this,RegisterActivity::class.java)
////            startActivity(intent)
//            val intent = Intent(this,SearchActivity::class.java)
//            startActivity(intent)
//            return true
//        }

        return when(itemID){
            R.id.main_cart -> {
                navController.navigateUp() // to clear previous navigation history
                navController.navigate(R.id.myCartFragment)
                true
            }
            R.id.main_search -> {
                val intent = Intent(this,SearchActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.main_notification -> {
                updateNotificationForOptionMenu()
                navController.navigateUp() // to clear previous navigation history
                navController.navigate(R.id.notificationFragment)
                true
            }
            else-> {
                Log.d("","")
                false
            }

        }


        //return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun getCartListForOptionMenu(textView: TextView) {

        firebaseFirestore.collection("USERS").document(firebaseAuth.currentUser!!.uid).collection("USER_DATA")
            .document("MY_CART").addSnapshotListener { value, error ->
                error?.let {
                    Toast.makeText(this,"Can't load cart. Contact Help-Center for further support ",Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                value?.let { val x = value.get("cart_list")
                    if (x != null){
                        val fbCartList = x as ArrayList<MutableMap<String,Any>>
                        if (fbCartList.size ==0){
                            textView.visibility = View.GONE
                        }else{
                            textView.visibility = View.VISIBLE
                            textView.text = fbCartList.size.toString()
                        }
                    }else{
                        textView.visibility = View.GONE
                    }
                }

            }

    }

    fun getNotificationForOptionMenu(textView: TextView){
        val ref = firebaseFirestore.collection("USERS")
            .document(firebaseAuth.currentUser!!.uid)
            .collection("USER_DATA")
            .document("MY_NOTIFICATION")


        ref.addSnapshotListener { value, error ->
            error?.let {
                Toast.makeText(this,"Fail to load notification",Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            value?.let {
                val newNotification = it.getLong("new_notification")
                if (newNotification ==0L){
                    textView.visibility =View.GONE
                }else{
                    textView.text = newNotification.toString()
                }

            }
        }
    }

    fun updateNotificationForOptionMenu(){
        val ref = firebaseFirestore.collection("USERS")
            .document(firebaseAuth.currentUser!!.uid)
            .collection("USER_DATA")
            .document("MY_NOTIFICATION")

        val notiMAp:MutableMap<String,Any> = HashMap()
        notiMAp["new_notification"] = 0L
        ref.update(notiMAp)

    }

    private fun getUsername(textView: TextView){
        firebaseFirestore.collection("USERS").document(firebaseAuth.currentUser!!.uid)
            .addSnapshotListener { value,error ->
                error?.let {
                    Toast.makeText(this,"Failed to load name",Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                value?.let {
                    val email = it.getString("email").toString()
                    val name = it.getString("name").toString()
                    if (name == ""){
                        textView.text = email
                    }else{
                        textView.text = name
                    }
                }
            }
    }




    override fun onBackPressed() {
        super.onBackPressed()
//        finish()
    }


}

