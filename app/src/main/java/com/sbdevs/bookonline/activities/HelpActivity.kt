package com.sbdevs.bookonline.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityHelpBinding
import com.sbdevs.bookonline.fragments.LoadingDialog

class HelpActivity : AppCompatActivity() {
    private lateinit var binding:ActivityHelpBinding

    private val firebaseFirestore = Firebase.firestore
    val firebaseAuth = Firebase.auth

    private lateinit var topicInput:TextInputLayout
    private lateinit var autoCompleteTopic:AutoCompleteTextView
    private lateinit var description:EditText

    private val loadingDialog = LoadingDialog()

    override fun onResume() {
        super.onResume()

        autoCompleteTopic = binding.autoCompleteTopic
        val stateList = resources.getStringArray(R.array.support_topics)
        val sateAdapter = ArrayAdapter(this,R.layout.item_dropdown,stateList)
        autoCompleteTopic.setAdapter(sateAdapter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

       topicInput = binding.topicInput
        description = binding.description



        binding.sendBtn.setOnClickListener{
            checkAllDetails()
        }
    }

    private fun checkDescription(): Boolean {
        val descriptionInput: String = description.text.toString()
        return if (descriptionInput.isEmpty()) {
           description.backgroundTintList = AppCompatResources.getColorStateList(this,R.color.red_500)
            false
        } else {
            description.backgroundTintList = AppCompatResources.getColorStateList(this,R.color.grey_500)
            true

        }
    }

    private fun checkTopic(): Boolean {
        val topic: String = autoCompleteTopic.text.toString()
        return if (topic.isEmpty()) {
            topicInput.isErrorEnabled = true
            topicInput.error = "Select an item"
            false
        } else {
//            buyerState.isErrorEnabled = false
            topicInput.error = null
            true
        }
    }

    private fun checkAllDetails() {
        if (!checkTopic() or !checkDescription()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show()
            return
        } else {
            loadingDialog.show(supportFragmentManager,"show")
            sendSupportToken()
        }
    }

    private fun sendSupportToken(){

        val valueMap:MutableMap<String,Any> = HashMap()
        valueMap["userId"] = firebaseAuth.currentUser!!.uid
        valueMap["topic"] = autoCompleteTopic.text.toString()
        valueMap["description"] =description.text.toString()
        valueMap["time"] = FieldValue.serverTimestamp()

        firebaseFirestore.collection("SUPPORT_REQUEST")
            .add(valueMap).addOnCompleteListener {
                loadingDialog.dismiss()
                binding.successContainer.visibility = View.VISIBLE
                description.text = null
            }.addOnFailureListener {
                binding.successContainer.visibility = View.GONE
                loadingDialog.dismiss()
                Log.e("sendSupportToken error","${it.message}")
                Toast.makeText(this,"${it.message}",Toast.LENGTH_LONG).show()
            }

    }




}