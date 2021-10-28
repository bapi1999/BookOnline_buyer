package com.sbdevs.bookonline.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.FragmentEditAccountBinding
import com.sbdevs.bookonline.databinding.FragmentMyAccountBinding
import de.hdodenhof.circleimageview.CircleImageView


class EditAccountFragment : Fragment() {

    private var _binding : FragmentEditAccountBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser
    lateinit var userImage: CircleImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditAccountBinding.inflate(inflater, container, false)


        return binding.root
    }

}