package com.sbdevs.bookonline.fragments.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.adapters.CategoryAdapter
import com.sbdevs.bookonline.databinding.FragmentCategoryBinding

class CategoryFragment : Fragment() {
    private var _binding:FragmentCategoryBinding?= null
    private val binding get () = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private var categoryList:ArrayList<String> = ArrayList()
    lateinit var adapter:CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)

        val recyclerView = binding.categoryRecycler
//        recyclerView.layoutManager =
        adapter = CategoryAdapter(categoryList)
        recyclerView.adapter = adapter
        return binding.root
    }
//    fun getAllCategory(){
//        firebaseFirestore.collection().document().get().addOnCompleteListener {
//
//            adapter.list = categoryList
//            adapter.notifyDataSetChanged()
//
//        }
//    }


}