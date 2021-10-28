package com.sbdevs.bookonline.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    private val firebaseAuth = FirebaseAuth.getInstance()
    val fireStoreData = FireStoreData()
//    lateinit var toggle:ActionBarDrawerToggle

    lateinit var badgeTxt:TextView
    var counter  = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getCartList()
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
        binding.navView.setupWithNavController(navController)
        val header = binding.navView.getHeaderView(0)
        val userName:TextView = header.findViewById(R.id.nav_header_txt)
        userName.text = "edfwefwefwe"


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_option_menu,menu)

        val cartMenu = menu?.findItem(R.id.main_cart)
//        cartMenu!!.setActionView(R.layout.le_notification_badge)
        var actionView = cartMenu!!.actionView
        val badgeCounter:TextView = actionView!!.findViewById(R.id.badge_counter)

        if (counter == 0) {
            badgeCounter.visibility = View.GONE
        } else {
            badgeCounter.text = counter.toString()

        }
        actionView.setOnClickListener {
            onOptionsItemSelected(cartMenu)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)

        if (item.itemId == R.id.main_cart){
            Toast.makeText(this,"Goto cart",Toast.LENGTH_SHORT).show()
            return true
        }
        if (item.itemId == R.id.main_search){
            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
//            val intent = Intent(this,SearchActivity::class.java)
//            startActivity(intent)


            return true
        }


        return super.onOptionsItemSelected(item)


    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    private fun getCartList() {

        firebaseFirestore.collection("USERS").document(firebaseAuth.currentUser!!.uid).collection("USER_DATA")
            .document("MY_CART").get().addOnCompleteListener {
                if (it.isSuccessful){
                    val x = it.result?.get("cart_list")

                    if (x != null){
                        val fbCartList = x as ArrayList<MutableMap<String,Any>>
                        counter = fbCartList.size
                        invalidateOptionsMenu()

                    }else{
                       counter =0
                    }
                }else{
                    Toast.makeText(this,"Failed cart",Toast.LENGTH_SHORT).show()
                }
            }

    }


    override fun onBackPressed() {
        super.onBackPressed()
//        finish()
    }


}

