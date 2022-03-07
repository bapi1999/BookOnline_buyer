package com.sbdevs.bookonline.fragments.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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

    // do not use this
    //val user = firebaseAuth.currentUser

    lateinit var nameInput: TextInputLayout
    lateinit var email: TextInputLayout
    lateinit var phone:TextInputLayout
    lateinit var pass: TextInputLayout
    lateinit var confirmPass:TextInputLayout
    lateinit var errorTxt: TextView
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+"
    private val loadingDialog = LoadingDialog()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        nameInput = binding.signupLay.emailInput
        email = binding.signupLay.emailInput
        phone = binding.signupLay.mobileInput
        pass = binding.signupLay.passwordInput
        confirmPass = binding.signupLay.confirmPassInput

        binding.signupLay.loginText.setOnClickListener {
            val action = SignUpFragmentDirections.actionSignUpFragmentToLoginFragment()
            findNavController().navigate(action)
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signupLay.signupBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager,"show")
            checkAllDetails()
        }

        binding.signupLay.skipBtn.setOnClickListener{
            val mainActivityIntent = Intent(requireContext(),MainActivity::class.java)
            startActivity(mainActivityIntent)
            activity?.finish()
        }

    }

    private fun checkName(): Boolean {
        val nameString: String = nameInput.editText?.text.toString().trim()
        return if (nameString.isEmpty()) {
            nameInput.isErrorEnabled = true
            nameInput.error = "Field can't be empty"
            false
        } else {
            email.isErrorEnabled = false
            email.error = null
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


    private fun checkPhome(): Boolean {
        val phoneInput: String = phone.editText?.text.toString().trim()
        return if (phoneInput.isEmpty()) {
            phone.isErrorEnabled = true
            phone.error = "Field can't be empty"
            false
        } else {
            if(phoneInput.length==10){
                phone.isErrorEnabled = false
                phone.error = null
                true
            }else{
                phone.isErrorEnabled = true
                phone.error = "Must be 10 digit number"
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
    private fun checkConfirmPassword(): Boolean {
        val passInput: String = pass.editText?.text.toString().trim()
        val confirmPassInput: String = confirmPass.editText?.text.toString().trim()
        return if (passInput.isEmpty()) {
            confirmPass.isErrorEnabled = true
            confirmPass.error = "Field can't be empty"
            false
        } else {
            if (confirmPassInput == passInput){
                confirmPass.isErrorEnabled = false
                confirmPass.error = null
                true
            }else{
                confirmPass.isErrorEnabled = true
                confirmPass.error = "Doesn't match with password"
                false
            }

        }
    }

    private fun checkAllDetails() {
        if (!checkName() or !checkMail() or !checkPhome() or !checkPassword() or !checkConfirmPassword()) {
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
                    Log.e("login user","${it.message}")
                }



        }
    }
    private suspend fun createPaths(){

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
        userMap["signup_date"] = FieldValue.serverTimestamp()
        userMap["mobile_No"] = phone.editText?.text.toString().trim()
        userMap["profile"] = ""
        userMap["new_notification"] = FieldValue.serverTimestamp()

        if (firebaseAuth.currentUser!=null){
            val currentUser = firebaseAuth.currentUser!!.uid

            val docRef = firebaseFirestore.collection("USERS").document(currentUser)
            docRef.set(userMap)
                .addOnFailureListener {
                    Log.e("Create user","${it.message}")
                }.await()

            docRef.collection("NOTIFICATIONS").document("DUMMY").set(dummyMap)
                .addOnFailureListener {
                    Log.e("Create Notifications","${it.message}")
                }
                .await()

            val userRef =  docRef.collection("USER_DATA")

            userRef.document("MY_ADDRESSES").set(addressMap).await()
            userRef.document("MY_CART").set(listSizeMap).await()
            userRef.document("MY_WISHLIST").set(listSizeMap).await()

            withContext(Dispatchers.Main){
                val intent = Intent(context, MainActivity::class.java)
                startActivity(intent)
                Toast.makeText(context, "Successfully login", Toast.LENGTH_SHORT).show()
                loadingDialog.dismiss()
                activity?.finish()
            }

        }


    }


    private fun retrieveUserToken(){

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token:String = task.result
                val userId:String = FirebaseAuth.getInstance().currentUser!!.uid

                FirebaseDatabase.getInstance().getReference("Tokens")
                    .child(userId)
                    .setValue(token)

            }

        }

    }



}