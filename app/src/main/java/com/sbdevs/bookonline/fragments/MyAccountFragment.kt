package com.sbdevs.bookonline.fragments

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.MyAddressActivity
import com.sbdevs.bookonline.databinding.FragmentMyAccountBinding
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MyAccountFragment : Fragment() {
    private var _binding :FragmentMyAccountBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    val firebaseAuth = Firebase.auth
    private val user = firebaseAuth.currentUser
    lateinit var userImage:CircleImageView

    var profilePicture:String = ""
    var buyerName:String =""
    var mobileNumber = ""

    lateinit var loadingDialog :Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAccountBinding.inflate(inflater, container, false)
        userImage = binding.lay1.userImage


        loadingDialog = Dialog(activity!!)
        loadingDialog.setContentView(R.layout.le_loading_progress_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(activity!!.applicationContext,R.drawable.s_shape_bg_2)
        )
        loadingDialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        loadingDialog.show()

        lifecycleScope.launch(Dispatchers.IO){
            getMyAccount()
            delay(1000)
            loadingDialog.dismiss()
        }

        binding.lay3.myAddressLay.setOnClickListener {
            val intent = Intent(context,MyAddressActivity::class.java)
            intent.putExtra("from",1)
            //1 = from MyAccountFragment 2 = OrderDetailsFRagment
            startActivity(intent)
        }




        binding.lay3.myWishlistLay.setOnClickListener{
            val action = MyAccountFragmentDirections.actionMyAccountFragmentToMyWishlistFragment()
            findNavController().navigate(action)
        }


        binding.lay3.myCartLay.setOnClickListener{
            val action = MyAccountFragmentDirections.actionMyAccountFragmentToMyCartFragment()
            findNavController().navigate(action)
        }



        binding.lay3.myOrderLay.setOnClickListener{

        }

        binding.lay3.updatePasswordLay.setOnClickListener {
            val action = MyAccountFragmentDirections.actionMyAccountFragmentToUpdatePasswordFragment()
            findNavController().navigate(action)
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.lay1.imageView20.setOnClickListener {
            goToEditdFragment()
        }
        binding.lay1.editProfileBtn.setOnClickListener{
           goToEditdFragment()
        }




        binding.logout.setOnClickListener {
            Toast.makeText(context,"logout",Toast.LENGTH_SHORT).show()
            firebaseAuth.signOut()
        }

    }

    fun goToEditdFragment(){
        val action = MyAccountFragmentDirections.actionMyAccountFragmentToEditAccountFragment(profilePicture,buyerName,mobileNumber)
        findNavController().navigate(action)
    }




    private fun getMyAccount(){
        val lay1 = binding.lay1
        val userRef = firebaseFirestore.collection("USERS").document(user!!.uid).get()

        userRef.addOnSuccessListener {
                val title = it.getString("name").toString()
                val gmail = it.getString("email")
                val mobile = it.getString("mobile_No").toString()
                val profile = it.get("profile").toString().trim()

                if (title!=""){
                    lay1.userName.text = title
                }else{
                    lay1.userName.text = "No Name"
                }

                profilePicture = profile
                buyerName = title
                mobileNumber= mobile



                lay1.userMail.text = gmail
                lay1.userPhone.text = mobile

                if (profile!=""){
                    binding.lay1.textView57.visibility = View.GONE
                    Glide.with(context!!).load(profile).placeholder(R.drawable.as_user_placeholder).into(userImage)
                }else{
                    binding.lay1.textView57.visibility = View.VISIBLE
                }

            }

        userRef.addOnFailureListener {
            it.message?.let { it1 -> Log.e(TAG, it1) }
        }
    }

}