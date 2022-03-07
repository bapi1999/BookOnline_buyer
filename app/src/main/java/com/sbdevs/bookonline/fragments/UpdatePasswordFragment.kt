package com.sbdevs.bookonline.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.FragmentUpdatePasswordBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UpdatePasswordFragment : Fragment() {
    private var _binding:FragmentUpdatePasswordBinding? = null
    private val binding get() = _binding!!
    val firebaseFirestore = Firebase.firestore
    val firebaseAuth = Firebase.auth
    val user = firebaseAuth.currentUser!!
    lateinit var oldPasswordInput:TextInputLayout
    lateinit var newPasswordInput:TextInputLayout

    lateinit var loadingDialog : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdatePasswordBinding.inflate(inflater, container, false)

        loadingDialog = Dialog(activity!!)
        loadingDialog.setContentView(R.layout.le_loading_progress_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(activity!!.applicationContext,R.drawable.s_shape_bg_2)
        )
        loadingDialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)



        oldPasswordInput = binding.oldPasswordInput
        newPasswordInput = binding.newPasswordInput

        binding.updateBtn.setOnClickListener {
            loadingDialog.show()
            checkAllDetails()
        }

        return binding.root
    }

    private fun checkOldPassword(): Boolean {
        val passInput: String = oldPasswordInput.editText?.text.toString().trim()
        return if (passInput.isEmpty()) {
            oldPasswordInput.error = "Field can't be empty"
            false
        } else {
            if (passInput.length<8){
                oldPasswordInput.error = "must be at least 8 character"
                false
            }else{
                oldPasswordInput.error = null
                true
            }

        }
    }

    private fun checkNewPassword(): Boolean {
        val newPassInput: String = newPasswordInput.editText?.text.toString().trim()
        val oldPassInput: String = oldPasswordInput.editText?.text.toString().trim()
        return if (newPassInput.isEmpty()) {
            newPasswordInput.error = "Field can't be empty"
            false
        } else {
            if (newPassInput.length<8){

                newPasswordInput.error = "must be at least 8 character"
                false
            }else{
                if (newPassInput == oldPassInput){
                    newPasswordInput.error = getString(R.string.new_password_match_oldpass)
                    false
                }else{
                    newPasswordInput.error = null
                    true
                }

            }

        }
    }

    private fun checkAllDetails() {
        val newPassInput: String = newPasswordInput.editText?.text.toString().trim()
        val oldPassInput: String = oldPasswordInput.editText?.text.toString().trim()
        if (!checkNewPassword() or !checkOldPassword()) {
            loadingDialog.dismiss()
            return
        } else {
            lifecycleScope.launch(Dispatchers.IO){
                val authCredential = EmailAuthProvider.getCredential(user.email.toString(),oldPassInput)
                user.reauthenticate(authCredential).addOnSuccessListener {
                    user.updatePassword(newPassInput).addOnSuccessListener {
                        Toast.makeText(context,"Password updated successfully",Toast.LENGTH_LONG).show()
                        loadingDialog.dismiss()
                    }.addOnFailureListener {
                        loadingDialog.dismiss()
                        Toast.makeText(context,it.message, Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener {
                    loadingDialog.dismiss()
                    Toast.makeText(context,it.message, Toast.LENGTH_LONG).show()
                }
            }

        }
    }


}