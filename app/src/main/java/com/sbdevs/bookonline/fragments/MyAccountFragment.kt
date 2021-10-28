package com.sbdevs.bookonline.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.MyAddressActivity
import com.sbdevs.bookonline.databinding.FragmentMyAccountBinding
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MyAccountFragment : Fragment() {
    private var _binding :FragmentMyAccountBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    val firebaseAuth = FirebaseAuth.getInstance()
    private val user = firebaseAuth.currentUser
    lateinit var userImage:CircleImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyAccountBinding.inflate(inflater, container, false)
        userImage = binding.lay1.userImage
        lifecycleScope.launch(Dispatchers.IO){
            getMyAccount()
        }

        binding.lay3.myAddress.setOnClickListener {
            val intent = Intent(context,MyAddressActivity::class.java)
            intent.putExtra("from",1)
            //1 = from MyAccountFragment 2 = OrderDetailsFRagment
            startActivity(intent)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lay1.editProfileBtn.setOnClickListener{
//            Toast.makeText(context,"clicked",Toast.LENGTH_SHORT).show()
            val action = MyAccountFragmentDirections.actionMyAccountFragmentToEditAccountFragment()
            findNavController().navigate(action)
        }

        binding.logout.setOnClickListener {
            Toast.makeText(context,"logout",Toast.LENGTH_SHORT).show()
            firebaseAuth.signOut()
        }

    }

    fun getMyAccount(){
        val lay1 = binding.lay1
        firebaseFirestore.collection("USERS").document(user!!.uid)
            .get().addOnSuccessListener {
                val title = it.getString("name")
                val gmail = it.getString("email")
                val mobile = it.getString("mobile_No")
                val profile = it.get("profile").toString().trim()
                val isSeller = it.getBoolean("Is_seller")
                val preference:ArrayList<String> = it.get("preference") as ArrayList<String>

                if (title!=""){
                    lay1.userName.text = title
                }else{
                    lay1.userName.text = "No Name"
                }


                lay1.userMail.text = gmail
                lay1.userPhone.text = mobile
                if (profile!=""){
                    Glide.with(context!!).load(profile).placeholder(R.drawable.as_user_placeholder).into(userImage)
                }




            }
    }

}