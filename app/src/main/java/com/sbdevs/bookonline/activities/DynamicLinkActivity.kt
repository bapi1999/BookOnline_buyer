package com.sbdevs.bookonline.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityDynamicLinkBinding
import com.sbdevs.bookonline.othercalss.Constants
import org.json.JSONException
import org.json.JSONObject

class DynamicLinkActivity : AppCompatActivity() {
    private lateinit var binding:ActivityDynamicLinkBinding
    private var productId = "abs"
    private var sellertoken = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDynamicLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receivedDynamicLink()

    }



    private fun receivedDynamicLink(){
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    Log.e( "getDynamicLink:Success", "$deepLink")
                    productId = deepLink!!.getQueryParameter("productid").toString()
                    if (!productId.isNullOrEmpty()){
                        val productIntent = Intent(this, ProductActivity::class.java)
                        productIntent.putExtra("productId",productId)
                        startActivity(productIntent)
                        Log.e( "productId :Success", "$productId")

                    }


                }
            }
            .addOnFailureListener(this) { e ->
                Log.e( "getDynamicLink:onFailure", "${e.message}")
            }
    }


}