package com.sbdevs.bookonline.fragments

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.FragmentEditAccountBinding

import com.google.firebase.storage.StorageReference
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.collections.HashMap


class EditAccountFragment : Fragment() {

    private var _binding: FragmentEditAccountBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser
    private val firebaseStorage = Firebase.storage
    private var storageReference1: StorageReference = firebaseStorage.reference

    private val args: EditAccountFragmentArgs by navArgs()
    private lateinit var buyerInput: TextInputLayout
    private lateinit var mobileInput: TextInputLayout
    private lateinit var image: ImageView

    var fileUri: Uri? = null

    lateinit var loadingDialog: Dialog

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                fileUri = data?.data!!

                Glide.with(this).load(fileUri)
                    .placeholder(R.drawable.as_square_placeholder).into(image)
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }


    //TODO- ON CREATE METHOD =====================================
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditAccountBinding.inflate(inflater, container, false)

        loadingDialog = Dialog(activity!!)
        loadingDialog.setContentView(R.layout.le_loading_progress_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(activity!!.applicationContext, R.drawable.s_shape_bg_2)
        )
        loadingDialog.window!!.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )


        val profilePicture: String = args.profilePic
        val buyerName: String = args.buyerName
        val mobileNumber: String = args.mobileNumber

        buyerInput = binding.buyerNameInput

        mobileInput = binding.mobileInput

        buyerInput.editText?.setText(buyerName)
        mobileInput.editText?.setText(mobileNumber)

        image = binding.profilePicture

        Glide.with(this).load(profilePicture)
            .placeholder(R.drawable.as_square_placeholder).into(image)


        binding.imageSelect.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()            //Crop image(Optional), Check Customization for more option
                .compress(400)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    700,
                    700
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }


        binding.cancelButton.setOnClickListener {
            returnToMyAccount()
        }

        binding.updateBtn.setOnClickListener {
            loadingDialog.show()
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    checkAllDetails()
                    fileUri?.let {
                        uploadThumbnail()
                    }
                }
                withContext(Dispatchers.Main) {
                    delay(1000)
                    loadingDialog.dismiss()
                    returnToMyAccount()
                }
            }

        }


        return binding.root
    }

    private fun returnToMyAccount() {
        val action = EditAccountFragmentDirections.actionEditAccountFragmentToMyAccountFragment()
        findNavController().navigate(action)

    }

    private fun checkPhome(): Boolean {
        val phoneInput: String = mobileInput.editText?.text.toString().trim()
        return if (phoneInput.isEmpty()) {
            mobileInput.isErrorEnabled = true
            mobileInput.error = "Field can't be empty"
            false
        } else {
            if (phoneInput.length == 10) {
                mobileInput.isErrorEnabled = false
                mobileInput.error = null
                true
            } else {
                mobileInput.isErrorEnabled = true
                mobileInput.error = "Must be 10 digit number"
                false
            }

        }
    }

    private fun checkAllDetails() {
        if (!checkPhome()) {
            return
        } else {
            uploadPersonalDataDb()
        }
    }

    private fun uploadPersonalDataDb() {
        val updateMap: MutableMap<String, Any> = HashMap()
        updateMap["name"] = buyerInput.editText!!.text.toString()
        updateMap["mobile_No"] = mobileInput.editText!!.text.toString()

        firebaseFirestore.collection("USERS")
            .document(user!!.uid).update(updateMap)
    }


    private fun uploadThumbnail() {
        val mRef: StorageReference =
            storageReference1.child("image/" + user!!.uid + "/").child(generateImageName())
        fileUri?.let {
            mRef.putFile(it)
                .addOnCompleteListener {
                    val downloadUrl = mRef.downloadUrl
                    downloadUrl.addOnSuccessListener { value ->
                        val uploadThumbMap: MutableMap<String, Any> = HashMap()
                        uploadThumbMap["profile"] = value.toString().trim()

                        firebaseFirestore.collection("USERS")
                            .document(user.uid).update(uploadThumbMap)
                    }
                    downloadUrl.addOnFailureListener { exp ->
                        Log.e(TAG, exp.message!!)
                    }


                }


        }
    }

//    fun GetFileName(uri: Uri): String? { // for image names
//        var result: String? = null
//        if (uri.scheme == "content") {
//            val cursor: Cursor = getContentResolver().query(uri, null, null, null, null)
//            try {
//                if (cursor != null && cursor.moveToFirst()) {
//                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
//                }
//            } finally {
//                cursor.close()
//            }
//        }
//        if (result == null) {
//            result = uri.path
//            val cut = result!!.lastIndexOf('/')
//            if (cut != -1) {
//                result = result.substring(cut + 1)
//            }
//        }
//        return result
//    }

    private fun generateImageName(): String {

        val timeString = LocalDateTime.now().toString()
        val userString = user!!.uid.toString()
        val docBuilder: StringBuilder = StringBuilder()
        docBuilder.append(timeString).append(userString)
        return docBuilder.toString().replace(".", "_").replace("-", "_").replace(":", "_")
    }


}