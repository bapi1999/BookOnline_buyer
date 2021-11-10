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
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.HomeAdapter
import com.sbdevs.bookonline.adapters.CategoryAdapter
import com.sbdevs.bookonline.databinding.FragmentHomeBinding
import com.sbdevs.bookonline.models.HomeModel
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val firebaseFirestore = Firebase.firestore
    val firebaseAuth = Firebase.auth
    private var uiViewLIst:List<HomeModel> = ArrayList()
    private lateinit var homeAdapter: HomeAdapter

    lateinit var loadingDialog :Dialog
    lateinit var swipeRefreshLayout: SwipeRefreshLayout


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

        swipeRefreshLayout = binding.refreshLayout

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
            withContext(Dispatchers.IO){
                loadUi()

            }
            withContext(Dispatchers.Main){
               delay(100)
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

        swipeRefreshLayout.setOnRefreshListener {
//            swipeRefreshLayout.isRefreshing = true
//            loadUi()
            refreshFragment()
        }

        return binding.root
    }

    private fun loadUi() = CoroutineScope(Dispatchers.IO).launch{
        firebaseFirestore.collection("HOMEPAGE").orderBy("index", Query.Direction.ASCENDING)
            .get().addOnCompleteListener {
                if (it.isSuccessful){

                    uiViewLIst = it.result!!.toObjects(HomeModel::class.java)
                    homeAdapter.homeModelList =uiViewLIst
                    homeAdapter.notifyDataSetChanged()
                    swipeRefreshLayout.isRefreshing = false
                }else{
                    Toast.makeText(context,it.exception?.message, Toast.LENGTH_LONG).show()
                }
            }.await()
        delay(1500)
    }

    fun refreshFragment(){
        val navController: NavController = requireActivity().findNavController(R.id.nav_host_fragment)
        navController.run {
            popBackStack()
            navigate(R.id.homeFragment)
        }
    }


}