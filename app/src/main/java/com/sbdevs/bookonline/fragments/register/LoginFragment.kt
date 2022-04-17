package com.sbdevs.bookonline.fragments.register

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.MainActivity
import com.sbdevs.bookonline.databinding.FragmentLoginBinding
import com.sbdevs.bookonline.othercalss.SharedDataClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {
    private var _binding:FragmentLoginBinding?=null
    private val binding get() = _binding!!

    val firebaseAuth = Firebase.auth
    lateinit var email:TextInputLayout
    lateinit var pass:TextInputLayout
    lateinit var errorTxt:TextView

    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+"

    lateinit var loadingDialog : Dialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        errorTxt  = binding.loginLay.errorMessageText
        errorTxt.visibility =View.GONE
        email = binding.loginLay.emailInput
        pass = binding.loginLay.passwordInput
        binding.loginLay.signupText.setOnClickListener {
            val action  = LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            findNavController().navigate(action)
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginLay.loginBtn.setOnClickListener {
            checkAllDetails()
        }

        binding.loginLay.forgotPassword.setOnClickListener {
            val action1 = LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment()
            findNavController().navigate(action1)
        }

        binding.loginLay.skipBtn.setOnClickListener{

            val mainActivityIntent = Intent(requireContext(),MainActivity::class.java)
            startActivity(mainActivityIntent)

            activity?.finish()
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


    private fun checkAllDetails() {
        if (!checkMail() or !checkPassword()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        } else {
            lifecycleScope.launch(Dispatchers.IO){
                try {
                    firebaseAuth.signInWithEmailAndPassword(email.editText?.text.toString().trim(),pass.editText?.text.toString()).await()
                    withContext(Dispatchers.Main){
                        retrieveUserToken()
                        Toast.makeText(context, "Successfully login", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context,MainActivity::class.java)
                        startActivity(intent)

//                        val v = SharedDataClass
//                        v.getCartListForOptionMenu()

                        activity?.finish()
                    }
                }catch (e:Exception){
                    withContext(Dispatchers.Main){
                        errorTxt.visibility = View.VISIBLE
                        Toast.makeText(context,e.message,Toast.LENGTH_LONG).show()
                    }
                }
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