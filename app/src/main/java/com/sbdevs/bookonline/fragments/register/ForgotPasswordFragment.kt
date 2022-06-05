package com.sbdevs.bookonline.fragments.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.FragmentForgotPasswordBinding

class ForgotPasswordFragment : Fragment() {
    private var _binding:FragmentForgotPasswordBinding ?=null
    private val binding get() = _binding!!
    val firebaseAuth = Firebase.auth
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+"

    lateinit var recoveryMassege:TextView
    lateinit var progress:ProgressBar
    lateinit var emailEditText:EditText
    private val gone = View.GONE
    private val visible = View.VISIBLE


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater,container,false)
        recoveryMassege = binding.textView46
        progress = binding.recoveryProgressBar

        emailEditText = binding.recoveryEmailAddress

        binding.resetPasswordBtn.setOnClickListener {
            validateEmail()
        }

        return binding.root
    }



    private fun validateEmail(){
        val email:String =  emailEditText.text.toString().trim().lowercase()
        if (email.isEmpty()){
            emailEditText.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.red_a700)
            emailEditText.requestFocus()
        } else {
            if(email.matches(emailPattern.toRegex())){
                progress.visibility =visible
                emailEditText.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.purple_500)
                forgotPassWord(email)

            }else{
                emailEditText.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.red_a700)
                emailEditText.requestFocus()
            }

        }
    }


    private fun forgotPassWord(email:String){

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                binding.messageContainer.visibility  = visible
                binding.mailImage.imageTintList = AppCompatResources.getColorStateList(requireContext(),R.color.successGreen)
            }
            .addOnFailureListener {
                binding.messageContainer.visibility  = visible
                recoveryMassege.text = "Failed to send email"
                binding.mailImage.imageTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_700)
            }
        progress.visibility =gone
    }

}