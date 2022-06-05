package com.sbdevs.bookonline.activities.user

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.MainActivity
import com.sbdevs.bookonline.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val myPreference = "ShowLoginPref"
    private val showLoginScreen = "ShowLoginScreen"

    var fromIntent = 0

    private lateinit var sharedPreferences:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(myPreference, MODE_PRIVATE)
//        val name = sharedPreferences.getString("signature", "")

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        fromIntent = intent.getIntExtra("from",0)

        if (fromIntent == 1){
            binding.skipBtn.visibility = View.VISIBLE
        }else{
            binding.skipBtn.visibility = View.GONE
        }

        binding.skipBtn.setOnClickListener {
            val newIntent = Intent(this,MainActivity::class.java)
            startActivity(newIntent)

            val prefEditor:SharedPreferences.Editor = sharedPreferences.edit()
            prefEditor.putBoolean(showLoginScreen,false)
            prefEditor.apply()

            finish()

        }


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment2) as NavHostFragment
        navHostFragment.findNavController().setGraph(R.navigation.register_nav_graph,intent.extras)



    }
}