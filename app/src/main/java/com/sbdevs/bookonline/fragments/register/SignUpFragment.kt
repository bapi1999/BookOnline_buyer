package com.sbdevs.bookonline.fragments.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.MainActivity
import com.sbdevs.bookonline.activities.WebViewActivity
import com.sbdevs.bookonline.databinding.FragmentLoginBinding
import com.sbdevs.bookonline.databinding.FragmentSignUpBinding
import com.sbdevs.bookonline.fragments.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding?=null
    private val binding get() = _binding!!
    private val firebaseFirestore = Firebase.firestore
    val firebaseAuth = Firebase.auth

    lateinit var nameInput: TextInputLayout
    lateinit var email: TextInputLayout

    lateinit var pass: TextInputLayout
    lateinit var errorTxt: TextView
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+"
    private val loadingDialog = LoadingDialog()
    private var fromIntent = 0
    private lateinit var termAndPolicyBox: CheckBox

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        nameInput = binding.signupLay.emailInput
        email = binding.signupLay.emailInput
        errorTxt = binding.signupLay.errorMessageText
        pass = binding.signupLay.passwordInput
        termAndPolicyBox = binding.signupLay.checkBox5

        fromIntent = requireActivity().intent.getIntExtra("from",0)

        binding.signupLay.loginText.setOnClickListener {
            val action = SignUpFragmentDirections.actionSignUpFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        termAndPolicyBox.setOnCheckedChangeListener { buttonView, isChecked ->
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.teal_200)
        }

        binding.signupLay.termsConditionText.setOnClickListener {
            val myIntent = Intent(requireContext(), WebViewActivity::class.java)
            myIntent.putExtra("PolicyCode",1)// 1 = Terms and services
            startActivity(myIntent)
        }
        binding.signupLay.privacyPolicyText.setOnClickListener {
            val myIntent = Intent(requireContext(), WebViewActivity::class.java)
            myIntent.putExtra("PolicyCode",2)// 2 = Privacy Policy
            startActivity(myIntent)
        }
        binding.signupLay.returnPolicyText.setOnClickListener {
            val myIntent = Intent(requireContext(), WebViewActivity::class.java)
            myIntent.putExtra("PolicyCode",3)//3 = Return Policy
            startActivity(myIntent)
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signupLay.signupBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager,"show")
            checkAllDetails()
        }

    }

    private fun checkName(): Boolean {
        val nameString: String = nameInput.editText?.text.toString().trim()
        return if (nameString.isEmpty()) {
            nameInput.isErrorEnabled = true
            nameInput.error = "Field can't be empty"
            false
        } else {
            nameInput.isErrorEnabled = false
            nameInput.error = null
            true

        }
    }

    private fun checkMail(): Boolean {
        val emailInput: String = email.editText?.text.toString().trim()
        return if (emailInput.isEmpty()) {
            email.isErrorEnabled = true
            email.error = "Field can't be empty"
            false
        } else {
            if(emailInput.matches(emailPattern.toRegex())){
                email.isErrorEnabled = false
                email.error = null
                true
            }else{
                email.isErrorEnabled = true
                email.error = "Please enter a valid email address"
                false
            }

        }
    }



    private fun checkPassword(): Boolean {
        val passInput: String = pass.editText?.text.toString().trim()
        return if (passInput.isEmpty()) {
            pass.isErrorEnabled = true
            pass.error = "Field can't be empty"
            false
        } else {
            if (passInput.length<8){
                pass.isErrorEnabled = true
                pass.error = "must be at least 8 character"
                false
            }else{
                pass.isErrorEnabled = false
                pass.error = null
                true
            }

        }
    }

    private fun checkTermsAndPolicyBox(): Boolean {
        return if (termAndPolicyBox.isChecked) {
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.teal_200)
            true
        } else {
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_700)
            false
        }
    }

    private fun checkAllDetails() {
        if (!checkName() or !checkMail() or !checkPassword() or !checkTermsAndPolicyBox()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            loadingDialog.dismiss()
            return
        } else {
            firebaseAuth
                .createUserWithEmailAndPassword(email.editText?.text.toString().trim(),pass.editText?.text.toString())
                .addOnSuccessListener {
                    lifecycleScope.launch(Dispatchers.IO){
                        retrieveUserToken()
                        createPaths()

                    }
                }
                .addOnFailureListener {
                    loadingDialog.dismiss()
                    errorTxt.text = "${it.message}"
                    errorTxt.visibility = View.VISIBLE
                    Log.e("login user","${it.message}")
                }



        }
    }
    private suspend fun createPaths(){

        val timstamp1 = FieldValue.serverTimestamp()

        val listSizeMap: MutableMap<String, Any> = HashMap()
        listSizeMap["listSize"] = 0L

        val addressMap: MutableMap<String, Any> = HashMap()
        addressMap["select_No"] = 0L

        val dummyMap: MutableMap<String, Any> = HashMap()
        dummyMap["DUMMY"] = "dummy"

        val userMap: MutableMap<String, Any> = HashMap()
        userMap["name"] = nameInput.editText?.text.toString().trim()
        userMap["email"] = email.editText?.text.toString().trim()
        userMap["Is_user"] = true
        userMap["Is_seller"] = false
        userMap["signup_date"] = timstamp1
        userMap["profile"] = ""
        userMap["new_notification_user"] = timstamp1

        if (firebaseAuth.currentUser!=null){
            val currentUser = firebaseAuth.currentUser!!.uid

            val docRef = firebaseFirestore.collection("USERS").document(currentUser)

            docRef.set(userMap)
                .addOnFailureListener {
                    Log.e("Create user","${it.message}")
                }.await()

            docRef.collection("USER_NOTIFICATIONS").document("DUMMY").set(dummyMap)
                .addOnFailureListener {
                    Log.e("Create Notifications","${it.message}")
                }
                .await()

            val userRef =  docRef.collection("USER_DATA")

            userRef.document("MY_ADDRESSES").set(addressMap).await()
            userRef.document("MY_CART").set(listSizeMap).await()
            userRef.document("MY_WISHLIST").set(listSizeMap).await()

            withContext(Dispatchers.Main){
                if(fromIntent==1){
                    val intent = Intent(context, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }else{
                    activity?.finish()
                }
                loadingDialog.dismiss()
            }

        }


    }


    private fun retrieveUserToken(){

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token:String = task.result
                val userId:String = FirebaseAuth.getInstance().currentUser!!.uid

                FirebaseDatabase.getInstance().getReference("Buyer_Tokens")
                    .child(userId)
                    .setValue(token)

            }

        }

    }



}