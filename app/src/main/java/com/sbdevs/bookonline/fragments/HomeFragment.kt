package com.sbdevs.bookonline.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.HomeAdapter
import com.sbdevs.bookonline.databinding.FragmentHomeBinding
import com.sbdevs.bookonline.models.HomeModel
import com.sbdevs.bookonline.othercalss.SharedDataClass
import com.sbdevs.bookonline.viewModels.HomeViewModel


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val firebaseFirestore = Firebase.firestore

    private var uiViewLIst:MutableList<HomeModel> = ArrayList()
    private var homeAdapter: HomeAdapter = HomeAdapter(uiViewLIst)
//    private var isReachLast:Boolean = false

    private val loadingDialog  = LoadingDialog()
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var  homeRecycler:RecyclerView

    private lateinit var newView: HomeViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        loadingDialog.show(childFragmentManager,"Show")

        newView = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        swipeRefreshLayout = binding.refreshLayout

        homeRecycler = binding.homeRecycler
        homeRecycler.isNestedScrollingEnabled = false
        homeRecycler.layoutManager = LinearLayoutManager(context)

        //binding.textView40.text = SharedDataClass.uiViewLIst.size.toString()

        if (SharedDataClass.uiViewLIst.isEmpty()){

            //Toast.makeText(requireContext(),"1 method",Toast.LENGTH_SHORT).show()
            SharedDataClass.getHomePageData( binding.progressBar2,loadingDialog )

            //homeRecycler.adapter = SharedDataClass.homeAdapter
        }else{
            uiViewLIst = SharedDataClass.uiViewLIst
            homeAdapter = HomeAdapter(uiViewLIst)
            //Toast.makeText(requireContext(),"2 method",Toast.LENGTH_SHORT).show()
            //homeRecycler.adapter = homeAdapter
            loadingDialog.dismiss()
        }

        homeRecycler.adapter = SharedDataClass.homeAdapter


        binding.textView40.text = SharedDataClass.dbCartList.toString()




        swipeRefreshLayout.setOnRefreshListener {
//            swipeRefreshLayout.isRefreshing = true
//            loadUi()
            refreshFragment()
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var st = ""

        homeRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {

                    st = "list size${SharedDataClass.uiViewLIst.size} / reach last ${SharedDataClass.isReachLast}"
                    binding.textView40.text = st
                    SharedDataClass.getHomePageData(binding.progressBar2,loadingDialog)

//                    if (SharedDataClass.isReachLast){
//                        Log.w("Query item","Last item is reached already")
//                        binding.progressBar2.visibility = View.GONE
//                    }else{
//                        binding.progressBar2.visibility = View.VISIBLE
//                        Log.e("last query", "${SharedDataClass.lastResult.toString()}")
//
//                    }

                }

            }

        })


    }

    private fun refreshFragment(){
        val navController: NavController = requireActivity().findNavController(R.id.nav_host_fragment)
        navController.run {
            popBackStack()
            navigate(R.id.homeFragment)
        }
    }


}