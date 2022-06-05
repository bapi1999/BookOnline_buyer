package com.sbdevs.bookonline.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.activities.user.CartActivity
import com.sbdevs.bookonline.activities.user.MyAddressActivity
import com.sbdevs.bookonline.activities.user.RegisterActivity
import com.sbdevs.bookonline.databinding.FragmentMyAccountBinding
import com.sbdevs.bookonline.fragments.register.LoginDialogFragment
import com.sbdevs.bookonline.othercalss.SharedDataClass
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class MyAccountFragment : Fragment() {
    private var _binding :FragmentMyAccountBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    lateinit var userImage:CircleImageView

    var profilePicture:String = ""
    var buyerName:String =""

    private var loadingDialog = LoadingDialog()
    private val gone = View.GONE
    private val visible = View.VISIBLE


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAccountBinding.inflate(inflater, container, false)
        userImage = binding.lay1.userImage


        loadingDialog.show(childFragmentManager,"Show")

        if( Firebase.auth.currentUser != null){
            binding.myAccountScroll.visibility = View.VISIBLE
            binding.notLoginContainer.visibility = View.GONE

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
                getMyAccount()
                getAddress()
            }

        }else{
            binding.myAccountScroll.visibility = View.GONE
            binding.notLoginContainer.visibility = View.VISIBLE
            loadingDialog.dismiss()

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.lay1.imageView20.setOnClickListener {
            goToEditdFragment()
        }
        binding.lay1.editProfileBtn.setOnClickListener {
            goToEditdFragment()
        }

        binding.notLoginLay.loginOrSignupBtn.setOnClickListener {
            val registerIntent = Intent(requireContext(), RegisterActivity::class.java)
            registerIntent.putExtra("from", 2)// 1 = from splash/ 2 = from other class
            startActivity(registerIntent)
            SharedDataClass.newLogin1 = true
        }


        binding.lay3.myAddressLay.setOnClickListener {
            val intent = Intent(context, MyAddressActivity::class.java)
            intent.putExtra("from",1)
            //1 = from MyAccountFragment 2 = OrderDetailsFRagment
            startActivity(intent)
        }

        binding.lay3.myWishlistLay.setOnClickListener{
            val action = MyAccountFragmentDirections.actionMyAccountFragmentToMyWishlistFragment()
            findNavController().navigate(action)
        }

        binding.lay3.myCartLay.setOnClickListener{
            val cartIntent = Intent(requireContext(), CartActivity::class.java)
            startActivity(cartIntent)
        }

        binding.lay3.myOrderLay.setOnClickListener{
            val action = MyAccountFragmentDirections.actionMyAccountFragmentToMyOrderFragment()
            findNavController().navigate(action)
        }

        binding.lay3.updatePasswordLay.setOnClickListener {
            val action = MyAccountFragmentDirections.actionMyAccountFragmentToUpdatePasswordFragment()
            findNavController().navigate(action)
        }




        binding.logout.setOnClickListener {


            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    logOutUser()

                }
                withContext(Dispatchers.Main){

                    SharedDataClass.dbCartList.clear()
                    SharedDataClass.cartNumber = 0
                    SharedDataClass.dbWishList.clear()
                    SharedDataClass.isSeller = false

                    binding.myAccountScroll.visibility = View.GONE
                    binding.notLoginContainer.visibility = View.VISIBLE

                }
            }
        }




    }



    private fun goToEditdFragment(){
        val action = MyAccountFragmentDirections.actionMyAccountFragmentToEditAccountFragment(profilePicture,buyerName,"00")
        findNavController().navigate(action)
    }




    private suspend fun getMyAccount(){
        val lay1 = binding.lay1
        val userRef = firebaseFirestore.collection("USERS").document(Firebase.auth.currentUser!!.uid).get()

        userRef.addOnSuccessListener {
                val title = it.getString("name").toString()
                val gmail = it.getString("email")
                val profile = it.get("profile").toString().trim()

                if (title!=""){
                    lay1.userName.text = title
                }else{
                    lay1.userName.text = "No Name"
                }

                profilePicture = profile
                buyerName = title



                lay1.userMail.text = gmail


                if (profile!=""){
                    Glide.with(requireContext()).load(profile).placeholder(R.drawable.as_user_placeholder).into(userImage)
                }


            loadingDialog.dismiss()
            }.addOnFailureListener {
           Log.e("User","${it.message}")
            loadingDialog.dismiss()
        }.await()
    }


    private fun getAddress(){

        val lay2 =  binding.lay2

        firebaseFirestore.collection("USERS").document(Firebase.auth.currentUser!!.uid).collection("USER_DATA")
            .document("MY_ADDRESSES").addSnapshotListener { value, error ->
                error?.let {
                    Log.e("Get address","${it.message}")

                    lay2.haveAddressContainer.visibility = gone
                    lay2.editAddressBtn.visibility = gone
                    lay2.noAddressContainer.visibility = visible


                    return@addSnapshotListener
                }

                value?.let {
                    val position: Long = it.getLong("select_No")!!
                    val x = it.get("address_list")


                    if (x != null){

                        var addressList = x as ArrayList<MutableMap<String, Any>>

                        if (addressList.size != 0){

                            val group:MutableMap<String,Any> = addressList[position.toInt()]

                            val buyerName:String = group["name"].toString()
                            val buyerAddress1:String = group["address1"].toString()
                            val buyerAddress2:String = group["address2"].toString()
                            val buyerAddressType:String = group["address_type"].toString()
                            val buyerTown:String = group["city_vill"].toString()
                            val buyerPinCode:String = group["pincode"].toString()
                            val buyerState:String = group["state"].toString()
                            val buyerPhone:String = group["phone"].toString()

                            val addressBuilder  = StringBuilder()
                            addressBuilder.append(buyerAddress1).append(", ").append(buyerAddress2)

                            val townPinBuilder  = StringBuilder()
                            townPinBuilder.append(buyerTown).append(", ").append(buyerPinCode)

                            lay2.buyerName.text = buyerName
                            lay2.buyerAddress.text = addressBuilder.toString()
                            lay2.buyerTownAndPin.text =townPinBuilder.toString()
                            lay2.buyerState.text = buyerState
                            lay2.buyerPhone.text = buyerPhone

                            lay2.haveAddressContainer.visibility = visible
                            lay2.editAddressBtn.visibility = visible
                            lay2.noAddressContainer.visibility = gone

                        }else{
                            lay2.haveAddressContainer.visibility = gone
                            lay2.editAddressBtn.visibility = gone
                            lay2.noAddressContainer.visibility = visible
                        }

                    }else{
                        lay2.haveAddressContainer.visibility = gone
                        lay2.editAddressBtn.visibility = gone
                        lay2.noAddressContainer.visibility = visible

                    }


                }
            }

    }


    private fun logOutUser(){
        val userId = FirebaseAuth.getInstance().currentUser
        if (userId != null){
            deleteToken(userId.uid)
            Firebase.auth.signOut()

        }
    }
    private fun deleteToken(uid:String){

        FirebaseDatabase.getInstance()
            .getReference("Buyer_Tokens")
            .child(uid)
            .removeValue()

    }

}