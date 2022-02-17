package com.sbdevs.bookonline.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.activities.MainActivity
import com.sbdevs.bookonline.activities.ProductActivity
import com.sbdevs.bookonline.activities.user.RegisterActivity
import com.sbdevs.bookonline.databinding.FragmentLoginDialogBinding
import com.sbdevs.bookonline.othercalss.SharedDataClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginDialogFragment : DialogFragment() {
    private var _binding:FragmentLoginDialogBinding ? = null
    private val binding get() = _binding!!

    val firebaseAuth = Firebase.auth
    lateinit var email: TextInputLayout
    lateinit var pass: TextInputLayout
    lateinit var errorTxt: TextView
    lateinit var newDummyText:TextView

    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginDialogBinding.inflate(inflater, container, false)


        errorTxt  = binding.errorMessageText
        errorTxt.visibility =View.GONE
        email = binding.emailInput
        pass = binding.passwordInput
        newDummyText = binding.textView31

        binding.signupText.setOnClickListener {
            val registerIntent = Intent(requireContext(), RegisterActivity::class.java)
            startActivity(registerIntent)
        }

        binding.forgotPassword.setOnClickListener {
            val registerIntent = Intent(requireContext(), RegisterActivity::class.java)
            startActivity(registerIntent)
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
//            activity?.recreate()
        }




        binding.loginBtn.setOnClickListener {
            checkAllDetails()
        }

        newDummyText.text = activity.toString()

        return binding.root
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
                ///email.error = null
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
                //pass.error = null
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
                        Toast.makeText(context, "Successfully login", Toast.LENGTH_SHORT).show()
                        if (SharedDataClass.currentACtivity == 1){
                            val intent = Intent(requireContext(),MainActivity::class.java)
                            startActivity(intent)
                        }else{
                            val productid = SharedDataClass.product_id
                            val intent = Intent(requireContext(),ProductActivity::class.java)
                            intent.putExtra("productId",productid)
                            startActivity(intent)
                            SharedDataClass.newLogin = true

                            val v = SharedDataClass
                            v.getCartListForOptionMenu()
                            dismiss()

                        }




                        activity?.finish()
                    }
                }catch (e:Exception){
                    withContext(Dispatchers.Main){
                        errorTxt.visibility = View.VISIBLE
                        Toast.makeText(context,e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
    }


}