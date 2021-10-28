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
import androidx.recyclerview.widget.LinearLayoutManager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.HomeAdapter
import com.sbdevs.bookonline.adapters.MiniCategoryAdapter
import com.sbdevs.bookonline.databinding.FragmentHomeBinding
import com.sbdevs.bookonline.models.HomeModel
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val firebaseFirestore = Firebase.firestore
    val firebaseAuth = FirebaseAuth.getInstance()
    private var uiViewLIst:List<HomeModel> = ArrayList()
    private lateinit var homeAdapter: HomeAdapter

    private lateinit var miniCategoryAdapter: MiniCategoryAdapter

    lateinit var loadingDialog :Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater,container,false)

        loadingDialog = Dialog(activity!!)
        loadingDialog.setContentView(R.layout.le_loading_progress_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window!!.setBackgroundDrawable(
            AppCompatResources.getDrawable(activity!!.applicationContext,R.drawable.s_shape_bg_2)
        )
        loadingDialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        loadingDialog.show()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
            withContext(Dispatchers.IO){
                loadUi()

            }
            withContext(Dispatchers.Main){
                delay(2000)
                loadingDialog.dismiss()
            }
        }








//        val categorylist1 = cateGoryList()
//        var categoryRecyclerView = binding.categoryRecyclerHome
//        categoryRecyclerView.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
//        miniCategoryAdapter = MiniCategoryAdapter(categorylist1)
//        categoryRecyclerView.adapter = miniCategoryAdapter


        var recyclerView = binding.homeRecycler
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = LinearLayoutManager(context)
        homeAdapter = HomeAdapter(uiViewLIst);
        recyclerView.adapter = homeAdapter


        return binding.root
    }

    private fun loadUi() = CoroutineScope(Dispatchers.IO).launch{
        firebaseFirestore.collection("HOMEPAGE").orderBy("index", Query.Direction.ASCENDING)
            .get().addOnCompleteListener {
                if (it.isSuccessful){

                    uiViewLIst = it.result!!.toObjects(HomeModel::class.java)
                    homeAdapter.homeModelList =uiViewLIst
                    homeAdapter.notifyDataSetChanged()
                }else{
                    Toast.makeText(context,it.exception?.message, Toast.LENGTH_LONG).show()
                }
            }.await()
    }

    private fun cateGoryList():ArrayList<String>{
        var categoryId:ArrayList<String> = ArrayList<String>()
        categoryId.add("All Category")
        firebaseFirestore.collection("CATEGORIES").orderBy("index")
            .get().addOnSuccessListener {
                for (snapshot :QueryDocumentSnapshot in it){
                    val category:String = snapshot.id
                    categoryId.add(category)
                }
                miniCategoryAdapter.notifyDataSetChanged()
            }
        return categoryId
    }



}