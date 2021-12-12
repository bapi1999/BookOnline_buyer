package com.sbdevs.bookonline.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.HomeAdapter
import com.sbdevs.bookonline.databinding.FragmentHomeBinding
import com.sbdevs.bookonline.models.HomeModel
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val firebaseFirestore = Firebase.firestore

    private var uiViewLIst:List<HomeModel> = ArrayList()
    private lateinit var homeAdapter: HomeAdapter

    private val loadingDialog  = LoadingDialog()
    lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        loadingDialog.show(childFragmentManager,"Show")

        swipeRefreshLayout = binding.refreshLayout

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
            withContext(Dispatchers.IO){
                firebaseFirestore.collection("HOMEPAGE").orderBy("index", Query.Direction.ASCENDING)
                    .get().addOnSuccessListener{
                        Toast.makeText(context,"Success home page", Toast.LENGTH_LONG).show()
                        uiViewLIst = it.toObjects(HomeModel::class.java)
                        homeAdapter.homeModelList =uiViewLIst
                        homeAdapter.notifyDataSetChanged()
                        swipeRefreshLayout.isRefreshing = false

                    }.addOnFailureListener{
                        Toast.makeText(context,it.message, Toast.LENGTH_LONG).show()
                        Log.e("HomeFragment","Failed to load home ${it.message}",it.cause)
                    }.await()

                delay(1500)

            }
            withContext(Dispatchers.Main){
               delay(100)
                loadingDialog.dismiss()
            }
        }




        val recyclerView = binding.homeRecycler
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = LinearLayoutManager(context)
        homeAdapter = HomeAdapter(uiViewLIst);
        recyclerView.adapter = homeAdapter

        swipeRefreshLayout.setOnRefreshListener {
//            swipeRefreshLayout.isRefreshing = true
//            loadUi()
            refreshFragment()
        }

        return binding.root
    }

    private fun refreshFragment(){
        val navController: NavController = requireActivity().findNavController(R.id.nav_host_fragment)
        navController.run {
            popBackStack()
            navigate(R.id.homeFragment)
        }
    }


}