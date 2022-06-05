package com.sbdevs.bookonline.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.adapters.HomeAdapter
import com.sbdevs.bookonline.databinding.FragmentHomeBinding
import com.sbdevs.bookonline.models.HomeModel
import com.sbdevs.bookonline.othercalss.SharedDataClass

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val firebaseDatabase = SharedDataClass.database

    private lateinit var homeRecycler: RecyclerView
    private var uiViewLIst: MutableList<HomeModel> = ArrayList()
    private var newUiViewLIst: MutableList<HomeModel> = ArrayList()
    private var homeAdapter: HomeAdapter = HomeAdapter(uiViewLIst)
    private var isReachLast: Boolean = false

    var fromPos = 0
    var toPos = 4
    var totalCount = -1
    var mod = 0

    private val loadingDialog = LoadingDialog()
//    lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)


//        swipeRefreshLayout = binding.refreshLayout

        homeRecycler = binding.homeRecycler
        homeRecycler.isNestedScrollingEnabled = false
        homeRecycler.layoutManager = LinearLayoutManager(context)
        homeRecycler.isNestedScrollingEnabled = false
        homeRecycler.adapter = homeAdapter

        loadingDialog.show(childFragmentManager, "Show")
        getHomeUiList()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (isReachLast) {
                        Log.w("Query item", "Last item is reached already")
                        binding.progressBar2.visibility = View.GONE

                    } else {
                        binding.progressBar2.visibility = View.VISIBLE
                        Log.e("List", "last item not reached")
                        paginateData(uiViewLIst, totalCount, mod)


                    }
                }
            }
        })


    }

    private fun getHomeUiList() {
        firebaseDatabase.child("HomeUI").get()
            .addOnSuccessListener {

                for (snapShot in it.children) {
                    val element = snapShot.getValue(HomeModel::class.java)
                    if (element != null) {
                        uiViewLIst.add(element)
                    }
                }

//                homeAdapter.homeModelList = uiViewLIst
//                homeAdapter.notifyDataSetChanged()

                totalCount = uiViewLIst.size / 5
                mod = uiViewLIst.size % 5
                Log.e("getHomeUiList", "take off")
                paginateData(uiViewLIst, totalCount, mod)

                loadingDialog.dismiss()
            }
            .addOnFailureListener {
                Log.e("HomeFragment-Error in get home ui", "${it.message}")
                loadingDialog.dismiss()
            }
    }


    private fun paginateData(homeList: MutableList<HomeModel>, divCount: Int, extra: Int) {
        Log.e("paginateData", "take off")
        val kh: MutableList<HomeModel> = ArrayList()

        if (divCount == 0 && extra > 0) {
            for (i in fromPos until (fromPos + extra)) {
                kh.add(homeList[i])
            }
//                mod = 0
            isReachLast = true
            newUiViewLIst.addAll(kh)
            homeAdapter.homeModelList = newUiViewLIst
            homeAdapter.notifyItemRangeInserted(fromPos, extra)

        } else if (divCount > 0) {

            for (i in fromPos..toPos) {
                kh.add(homeList[i])
            }
            newUiViewLIst.addAll(kh)
            homeAdapter.homeModelList = newUiViewLIst
            homeAdapter.notifyItemRangeInserted(fromPos, 5)

            fromPos += 5
            toPos += 5

            totalCount -= 1

        } else {
            Log.w("divident", "divident cant be -ve")

            isReachLast = true
        }
        binding.progressBar2.visibility = View.GONE


    }


}