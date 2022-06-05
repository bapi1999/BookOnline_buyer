package com.sbdevs.bookonline.seller.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.fragment.app.commit
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.SetOptions
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.FragmentSlProfileBinding
import com.sbdevs.bookonline.seller.activities.SlAddBankDetailsActivity
import com.sbdevs.bookonline.seller.activities.SlAddProductActivity
import com.sbdevs.bookonline.seller.activities.SlBusinessDetailsActivity
import com.sbdevs.bookonline.seller.activities.SlEarningActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.Serializable

class SlProfileFragment : Fragment() {

    private var _binding: FragmentSlProfileBinding? = null
    private val binding get() = _binding!!
    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth
    private val user = firebaseAuth.currentUser

    private lateinit var aboutBusinessInput:TextInputLayout
    private lateinit var aboutBusinessText:TextView

    private lateinit var businessNameText:TextView
    private lateinit var businessTypeText:TextView
    private lateinit var verifyIcon:ImageView
    private lateinit var verifyText:TextView
    private lateinit var profileImage:CircleImageView
    private val gone = View.GONE
    private val visible = View.VISIBLE
    private var businessAddressMap:MutableMap<String,Any> = HashMap()
    private var businessContentList:ArrayList<String> = ArrayList()
    private var isAddressAvailable = false
    private var addressProfImage = ""
    private var about = ""

    private lateinit var hideDetailsFromUserSwitch:SwitchCompat


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlProfileBinding.inflate(inflater, container, false)
        val bottomBar = binding.bottomBar
        bottomBar.orderIcon.setImageResource(R.drawable.ic_order_icon_3_outline)
        bottomBar.productIcon.setImageResource(R.drawable.ic_outline_shopping_cart_24)
        bottomBar.earningIcon.setImageResource(R.drawable.ic_outline_payments_24)
        bottomBar.profileIcon.setImageResource(R.drawable.ic_account_circle_24)


        isUserVerified()
        getBankDetails()

        businessNameText = binding.lay1.businessName
        businessTypeText = binding.layBusiness.businessType
        verifyText = binding.lay1.verifyText
        verifyIcon = binding.lay1.verifyIcon
        profileImage = binding.lay1.userImage
        aboutBusinessText = binding.layBusiness.aboutBusinessText
        aboutBusinessInput = binding.layBusiness.businessAboutInput
        hideDetailsFromUserSwitch = binding.layBusiness.switch1



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomBar = binding.bottomBar
        bottomBar.orderContainer.setOnClickListener {
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.main_frame_layout, SlOrderFragment())
                addToBackStack("sl_order")
            }
        }

        bottomBar.productContainer.setOnClickListener {
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.main_frame_layout, SlMyProductFragment())
                addToBackStack("sl_product")
            }
        }


        bottomBar.addProductContainer.setOnClickListener {
            val newIntent = Intent(requireContext(), SlAddProductActivity::class.java)
            startActivity(newIntent)
            bottomBar.addProductContainer.isClickable = false
        }


        bottomBar.earningContainer.setOnClickListener {
            val newIntent = Intent(requireContext(), SlEarningActivity::class.java)
            startActivity(newIntent)
            bottomBar.earningContainer.isClickable = false
        }




        binding.addNewUpi.setOnClickListener {
            val bankIntent = Intent (requireContext(),SlAddBankDetailsActivity::class.java)
            startActivity(bankIntent)
        }

        binding.editUpi.setOnClickListener {
            val bankIntent = Intent (requireContext(),SlAddBankDetailsActivity::class.java)
            startActivity(bankIntent)
        }


        binding.lay1.editProfileBtn.setOnClickListener {
            if (isAddressAvailable){
                val editIntent = Intent(context, SlBusinessDetailsActivity::class.java)
                editIntent.putStringArrayListExtra("businessContentList",businessContentList)
                editIntent.putExtra("address",businessAddressMap as Serializable)
                editIntent.putExtra("addressImage",addressProfImage)
                editIntent.putExtra("cameFrom","edit")
                startActivity(editIntent)
            }else{
                val newIntent = Intent(context, SlBusinessDetailsActivity::class.java)
                startActivity(newIntent)
            }
        }


        binding.layBusiness.editAboutBtn.setOnClickListener {
            binding.layBusiness.editAboutContent.visibility = visible
            aboutBusinessInput.editText?.setText(about)
        }


        binding.layBusiness.updateAboutBtn.setOnClickListener {
            val aboutString = aboutBusinessInput.editText?.text.toString()
            if (aboutString.isNotEmpty()){
                val updateMap :MutableMap<String,Any> = HashMap<String,Any>()
                updateMap["about_business"] = aboutString

                firebaseFirestore.collection("USERS")
                    .document(user!!.uid).collection("SELLER_DATA")
                    .document("BUSINESS_DETAILS").update(updateMap)
                    .addOnSuccessListener {
                        aboutBusinessText.text = aboutString
                        binding.layBusiness.editAboutContent.visibility = gone
                    }
                    .addOnFailureListener {
                        Log.e("Update About error","${it.message}")
                    }
            }
        }


        hideDetailsFromUserSwitch.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked){
                hideAddressContact(true)
            }else{
                hideAddressContact(false)
            }
        }



    }


    override fun onResume() {
        super.onResume()
        binding.bottomBar.earningContainer.isClickable = true
        binding.bottomBar.addProductContainer.isClickable = true

    }


    private fun isUserVerified(){
        firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")
            .document("BUSINESS_DETAILS").get()
            .addOnSuccessListener {

                val isBusinessAdded = it.getBoolean("Is_BusinessDetail_Added")!!
                val isVerified = it.getBoolean("is_address_verified")!!

                if (isBusinessAdded){

                    val businessType = it.get("Business_type")!!.toString()
                    val businessName = it.get("Business_name")!!.toString()
                    val businessPhone= it.get("Business_phone")!!.toString()
                    val businessProfile= it.get("Business_profile")!!.toString()
                    val hideStatus= it.getBoolean("hide_address_contact")!!
                    about = it.get("about_business")!!.toString()
                    addressProfImage= it.getString("Address_prof_image")!!.toString()

                    hideDetailsFromUserSwitch.isChecked = hideStatus
                    if (hideStatus){
                        hideDetailsFromUserSwitch.text = "Hidden from user"
                    }else{
                        hideDetailsFromUserSwitch.text = "Visible to user"
                    }

                    businessContentList.add(0,businessType)
                    businessContentList.add(1,businessName)
                    businessContentList.add(2,businessPhone)

                    businessAddressMap = (it.get("address") as MutableMap<String, Any>?)!!

                    isAddressAvailable = businessAddressMap.isNotEmpty()

                    val addlissLine1 = businessAddressMap["Address_line_1"].toString()
                    val town = businessAddressMap["Town_Vill"].toString()
                    val pincode= businessAddressMap["PinCode"].toString()
                    val state = businessAddressMap["State"].toString()

                    if (about.isEmpty()){
                        aboutBusinessText.text = "null"
                    }else{
                        aboutBusinessText.text = about
                    }

                    if (businessProfile!=""){
                        Glide.with(requireContext()).load(businessProfile).placeholder(R.drawable.as_user_placeholder).into(profileImage)
                    }

                    if (isVerified){
                        verifyText.text = "verified"
                        binding.lay1.verifyContainer.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.successGreen)
                        verifyIcon.setImageResource(R.drawable.ic_check_circle_outline_24)
                        verifyIcon.visibility = visible
                    }else{
                        verifyText.text = "reviewing..."
                        verifyIcon.visibility = View.GONE
                        binding.lay1.verifyContainer.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),
                            R.color.amber_900
                        )

                    }

                    businessNameText.text = businessName
                    businessTypeText.text = businessType

                    binding.layBusiness.businessPhone.text = businessPhone
                    binding.layBusiness.buyerAddress.text = addlissLine1
                    binding.layBusiness.buyerTownAndPin.text = "$town, $pincode"
                    binding.layBusiness.buyerState.text = state



                }else{

                    verifyText.text = "Not verified"
                    verifyIcon.visibility = View.GONE
                    binding.lay1.verifyContainer.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_700)

                }


            }
    }

    private fun getBankDetails(){
        firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")
            .document("BANK_DETAILS").get()
            .addOnSuccessListener {
                val isBankAdded = it.getBoolean("Is_BankDetail_Added")!!
                if (isBankAdded){
                    binding.upiContainer.visibility = visible
                    binding.editUpi.visibility = visible
                    binding.addNewUpi.visibility = gone
                    binding.warningBankText.visibility =gone

                    val upiId = it.getString("UPI_id").toString()
                    binding.uipIdText.text = upiId


                }else{
                    binding.upiContainer.visibility = gone
                    binding.editUpi.visibility = gone
                    binding.addNewUpi.visibility = visible
                    binding.warningBankText.visibility = visible
                }
            }
    }

    private fun hideAddressContact(hidden :Boolean){

        val updateMap:MutableMap<String,Any> = HashMap()

        updateMap["hide_address_contact"] = hidden

        firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")
            .document("BUSINESS_DETAILS").set(updateMap, SetOptions.merge())
            .addOnSuccessListener {
                hideDetailsFromUserSwitch.isChecked = hidden
                if (hidden){
                    hideDetailsFromUserSwitch.text = "Hidden from user"
                }else{
                    hideDetailsFromUserSwitch.text = "Visible to user"
                }
            }
            .addOnFailureListener {
                Log.e("update hide status error","${it.message}")
            }

    }


}