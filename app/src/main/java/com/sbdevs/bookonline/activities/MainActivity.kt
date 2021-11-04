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
        FireStoreData().getUsername(userName)
//        userName.text = "edfwefwefwe"




    }

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_option_menu,menu)

        val cartMenu = menu?.findItem(R.id.main_cart)
//        cartMenu!!.setActionView(R.layout.le_notification_badge)
        var actionView = cartMenu!!.actionView
        badgeTxt = actionView!!.findViewById(R.id.badge_counter)

//        if (counter == 0) {
//            badgeCounter.visibility = View.GONE
//        } else {
//            badgeCounter.text = counter.toString()
//
//        }
//        invalidateOptionsMenu()
        getCartListForOptionMenu(badgeTxt)

        actionView.setOnClickListener {
            onOptionsItemSelected(cartMenu)
        }

        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)

        if (item.itemId == R.id.main_cart){
            navController.navigateUp() // to clear previous navigation history
            navController.navigate(R.id.myCartFragment)
            return true
        }
        if (item.itemId == R.id.main_search){
//            val intent = Intent(this,RegisterActivity::class.java)
//            startActivity(intent)
            val intent = Intent(this,SearchActivity::class.java)
            startActivity(intent)


            return true
        }


        return super.onOptionsItemSelected(item)
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



    override fun onBackPressed() {
        super.onBackPressed()
//        finish()
    }


}

