package com.sbdevs.bookonline.activities.java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sbdevs.bookonline.R;
import com.sbdevs.bookonline.adapters.java.SearchFilterJavaAdapter;
import com.sbdevs.bookonline.databinding.ActivitySearchFilterJavaBinding;
import com.sbdevs.bookonline.fragments.LoadingDialog;
import com.sbdevs.bookonline.models.java.SearchJavaModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class SearchFilterJavaActivity extends AppCompatActivity {

    private ActivitySearchFilterJavaBinding binding;
    private BottomSheetDialog bottomSheetDialog;

    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    private RecyclerView searchRecycler;
    private ArrayList<SearchJavaModel> allSearchList  = new ArrayList<SearchJavaModel>();
    private SearchFilterJavaAdapter searchFilterAdapter ;

    private final ArrayList<String> yearList = new ArrayList<String>();
    private final ArrayList<String> tags = new ArrayList<String>();
    private ArrayList<String> subTagList = new ArrayList<String>();
    private Query.Direction  priceDirection = Query.Direction.ASCENDING;

    private HashMap<String,String> mainFilterMap = new HashMap<>();
    private HashMap<String ,String> subFilterMap = new HashMap<>();

    private DocumentSnapshot lastResult;
    private Boolean isReachLast = false;
    private Boolean priceRageIsApplied = false;
    Long lowerLimit = 0L;
    Long upperLimit = 1000L;
    private int searchCode = 0;

    private final int visible = View.VISIBLE;
    private final int gone = View.GONE;
    private final LoadingDialog loadingDialog = new LoadingDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchFilterJavaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        setSupportActionBar(binding.toolbar2);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialog);
        View view = getLayoutInflater().inflate(R.layout.ar_search_filter_bottom_sheet_2, null);
        bottomSheetDialog.setContentView(view);
        dialogFunction(bottomSheetDialog);

        searchRecycler = binding.searchRecycler;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        searchRecycler.setLayoutManager(layoutManager);

        String from = getIntent().getStringExtra("from");
        if (Objects.equals(from, "ActionString")){

            String name = getIntent().getStringExtra("queryTitle");
            binding.queryText.setText(name);
            ArrayList<String> queryList = getIntent().getStringArrayListExtra("queryList");
            tags.addAll(queryList);
            Log.e("list",tags.toString());

        }else {
            String query = getIntent().getStringExtra("query");
            binding.queryText.setText(query);
            List<String> queryList  = Arrays.asList(query.toLowerCase().split(" "));

            if (queryList.size()>10){
                List<String> first9Item = queryList.stream().limit(9).map(String::trim).collect(Collectors.toList());
                tags.addAll(first9Item);
            }else {
                tags.addAll(queryList);
            }
        }



        searchFilterAdapter = new SearchFilterJavaAdapter(allSearchList);
        searchRecycler.setAdapter(searchFilterAdapter);

        loadingDialog.show(getSupportFragmentManager(),"show");
        queryEqualCount0();




        binding.queryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchFilterJavaActivity.this, SearchActivity2.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        searchRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {

                    if (isReachLast){
                        Log.w("Query item","Last item is reached already");
                        binding.progressBar2.setVisibility(gone);
                    }else{
                        binding.progressBar2.setVisibility(visible);

                        switch (searchCode){
                            case 0:{
                                queryEqualCount0();
                                break;
                            }
                            case 1: {
                                queryEqualCount1(subFilterMap);
                                break;
                            }
                            case 2 : {
                                queryEqualCount2(subFilterMap);
                                break;
                            }

                        }
                    }
                }
            }


        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
//        return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.search_filter: {
                bottomSheetDialog.show();

            }
            break;
            case android.R.id.home: {
                finish();
            }
            break;
            default:
                Log.i("", "");
        }

        return super.onOptionsItemSelected(item);
    }

    private void dialogFunction(BottomSheetDialog dialog) {

        AppCompatButton applyBtn = dialog.findViewById(R.id.apply_btn);
        ChipGroup typeChipGroup= dialog.findViewById(R.id.type_chipGroup);
        ChipGroup conditionChipGroup = dialog.findViewById(R.id.condition_chipGroup);
        RadioGroup relevanceRadioGroup = dialog.findViewById(R.id.relevance_radioGroup);
        ChipGroup yearChipGroup= dialog.findViewById(R.id.print_chipGroup) ;
        RadioGroup priceRadioGroup = dialog.findViewById(R.id.price_radioGroup) ;
        RangeSlider priceRaneSlider = dialog.findViewById(R.id.price_range_slider) ;
        LinearLayout priceRangeTextCOntainer = dialog.findViewById(R.id.price_range_text_container) ;
        TextView lowerInput = dialog.findViewById(R.id.lower_input) ;
        TextView upperInput= dialog.findViewById(R.id.upper_input) ;

        if (yearChipGroup != null) {
            chipListenerForYear(yearChipGroup);
        }
        assert typeChipGroup != null;
        chipListenerForType(typeChipGroup);
        assert conditionChipGroup != null;
        chipListenerForCondition(conditionChipGroup);

        relevanceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.relevance_chip2) {
                    priceDirection = Query.Direction.DESCENDING;
                } else {
                    priceDirection = Query.Direction.ASCENDING;
                }
            }
        });



        priceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.price_radio1 : {
                        priceRaneSlider.setVisibility(gone);
                        priceRangeTextCOntainer.setVisibility(gone);
                        priceRageIsApplied = false;
                        break;
                    }
                    case  R.id.price_radio2 : {
                        priceRaneSlider.setVisibility(visible);
                        priceRangeTextCOntainer.setVisibility(visible);
                        priceRageIsApplied = true;
                        break;
                    }
                }
            }
        });


        assert priceRaneSlider != null;
        priceRaneSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                lowerLimit = slider.getValues().get(0).longValue();
                upperLimit = slider.getValues().get(1).longValue();

                if (lowerLimit.equals(upperLimit)){
                    lowerInput.setBackgroundTintList(AppCompatResources.getColorStateList(SearchFilterJavaActivity.this,R.color.red_700));
                    upperInput.setBackgroundTintList(AppCompatResources.getColorStateList(SearchFilterJavaActivity.this,R.color.red_700));
                    applyBtn.setEnabled(false);
                    applyBtn.setBackgroundTintList(AppCompatResources.getColorStateList(SearchFilterJavaActivity.this,R.color.grey_500));
                }else if (upperLimit.equals(lowerLimit)){
                    lowerInput.setBackgroundTintList(AppCompatResources.getColorStateList(SearchFilterJavaActivity.this,R.color.red_700));
                    upperInput.setBackgroundTintList(AppCompatResources.getColorStateList(SearchFilterJavaActivity.this,R.color.red_700));
                    applyBtn.setEnabled(false);
                    applyBtn.setBackgroundTintList(AppCompatResources.getColorStateList(SearchFilterJavaActivity.this,R.color.grey_500));
                }else{
                    lowerInput.setBackgroundTintList(AppCompatResources.getColorStateList(SearchFilterJavaActivity.this,R.color.grey_700));
                    upperInput.setBackgroundTintList(AppCompatResources.getColorStateList(SearchFilterJavaActivity.this,R.color.grey_700));
                    applyBtn.setEnabled(true);
                    applyBtn.setBackgroundTintList(AppCompatResources.getColorStateList(SearchFilterJavaActivity.this,R.color.blueLink));
                }
                lowerInput.setText(lowerLimit.toString());
                upperInput.setText(upperLimit.toString());

            }
        });


        assert applyBtn != null;
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subFilterMap.clear();
                searchFilterAdapter.notifyItemRangeRemoved(0,allSearchList.size());
                allSearchList.clear();
                lastResult = null;

                subFilterMap.putAll(mainFilterMap);

                switch (subFilterMap.size()){
                    case 0:{
                        queryEqualCount0();
                        searchCode = 0;
                        break;
                    }
                    case 1 :{
                        queryEqualCount1(subFilterMap);
                        searchCode = 1;
                        break;
                    }
                    case 2:{
                        queryEqualCount2(subFilterMap);
                        searchCode = 2;
                        break;
                    }
                }

                dialog.dismiss();

            }
        }); {

        }



    }


    private void chipListenerForType(ChipGroup chipGroup ) {
        for (int i= 0; i< chipGroup.getChildCount();i++) {
            Chip chip  = (Chip) chipGroup.getChildAt(i);

            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        mainFilterMap.put(buttonView.getTag().toString(),"book_type");
                    } else {
                        mainFilterMap.remove(buttonView.getTag().toString(),"book_type");
                    }


                }
            });
        }
    }

    private void chipListenerForYear(ChipGroup chipGroup) {

        for (int i= 0; i< chipGroup.getChildCount();i++) {
            Chip chip  = (Chip) chipGroup.getChildAt(i);

            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {

                        yearList.add(buttonView.getTag().toString());
                    } else {
                        yearList.remove(buttonView.getTag().toString());
                    }


                }
            });
        }


    }

    private void chipListenerForCondition(ChipGroup chipGroup) {
        for (int i= 0; i< chipGroup.getChildCount();i++) {
            Chip chip  = (Chip) chipGroup.getChildAt(i);

            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        mainFilterMap.put(buttonView.getTag().toString(),"book_condition");
                    } else {
                        mainFilterMap.remove(buttonView.getTag().toString(),"book_condition");
                    }


                }
            });


        }
    }



//TODO #######################################################################################################################
//Filter methods
//TODO#######################################################################################################################

    private void queryEqualCount0(){

        ArrayList<SearchJavaModel>  searchList = new ArrayList<SearchJavaModel> ();
        subTagList.addAll(tags);
        subTagList.addAll(yearList);

        Query filterTask;
        if (lastResult == null){
            if (!priceRageIsApplied){
                filterTask = firebaseFirestore.collection("PRODUCTS")
                        .whereArrayContainsAny("tags", subTagList)
                        .orderBy("price_selling",priceDirection);
            } else{
                filterTask = firebaseFirestore.collection("PRODUCTS")
                        .whereArrayContainsAny("tags", tags)
                        .whereGreaterThan("price_selling",lowerLimit)
                        .whereLessThan("price_selling",upperLimit)
                        .orderBy("price_selling",priceDirection);
            }
        }
        else{
            if (!priceRageIsApplied){
                filterTask = firebaseFirestore.collection("PRODUCTS")
                        .whereArrayContainsAny("tags", tags)
                        .orderBy("price_selling",priceDirection)
                        .startAfter(lastResult);
            } else{
                filterTask = firebaseFirestore.collection("PRODUCTS")
                        .whereArrayContainsAny("tags", tags)
                        .whereGreaterThan("price_selling",lowerLimit)
                        .whereLessThan("price_selling",upperLimit)
                        .orderBy("price_selling",priceDirection)
                        .startAfter(lastResult);
            }
        }

        filterTask.limit(10L).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    List<DocumentSnapshot> allDocumentSnapshot = task.getResult().getDocuments();

                    Log.e("QuerySnapshot ","size "+allDocumentSnapshot.size()+"");

                    if (allDocumentSnapshot.isEmpty()){

                        isReachLast = true;
                        if (allSearchList.isEmpty()){
                            searchRecycler.setVisibility(gone);
                            binding.progressBar2.setVisibility(gone);
                            binding.noResultFoundText.setVisibility(visible);
                        }

                    }else{
                        for (DocumentSnapshot documentSnapshot : allDocumentSnapshot) {
                            String productId = documentSnapshot.getId();
                            String productName = documentSnapshot.getString("book_title");
                            ArrayList<String> productImgList =  (ArrayList<String>) documentSnapshot.get("productImage_List");
                            Long stockQty  = documentSnapshot.getLong("in_stock_quantity");
                            String avgRating = documentSnapshot.getString("rating_avg");
                            Long totalRatings = documentSnapshot.getLong("rating_total");
                            Long priceOriginal = documentSnapshot.getLong("price_original");
                            Long priceSelling = documentSnapshot.getLong("price_selling");
                            Long printedYear = documentSnapshot.getLong("book_printed_ON");
                            String bookCondition = documentSnapshot.getString("book_condition");
                            String bookType = documentSnapshot.getString("book_type");

                            searchList.add(new SearchJavaModel(productId, productName, productImgList, priceOriginal, priceSelling,
                                    stockQty, avgRating, totalRatings, bookCondition, bookType, printedYear));
                        }

                        allSearchList.addAll(searchList);
                        searchRecycler.setVisibility(visible);
                        binding.progressBar2.setVisibility(visible);
                        binding.noResultFoundText.setVisibility(gone);

                        if (lastResult == null ){
                            searchFilterAdapter.notifyItemRangeInserted(0,searchList.size());
                        }else{
                            searchFilterAdapter.notifyItemRangeInserted((allSearchList.size()-1),searchList.size());
                        }

                        if (!allDocumentSnapshot.isEmpty()){
                            DocumentSnapshot lastR = allDocumentSnapshot.get(allDocumentSnapshot.size() - 1);
                            lastResult = lastR;
                        }
                        binding.progressBar2.setVisibility(gone);

                        isReachLast = allDocumentSnapshot.size() < 10;
                    }



                    subTagList.clear();
                    loadingDialog.dismiss();

                }else {
                    Log.e("get search query 0", "${it.message}");
                    loadingDialog.dismiss();
                    subTagList.clear();
                }

            }
        });

    }

    private void queryEqualCount1(HashMap<String ,String> subMap){

        ArrayList<SearchJavaModel>  searchList = new ArrayList<SearchJavaModel> ();

        Set<String> keys = subMap.keySet();
        List<String> listKeys = new ArrayList<String>(keys);
        Collection<String> values = subMap.values();
        List<String> listValues = new ArrayList<String>(values);
        subTagList.addAll(tags);
        subTagList.addAll(yearList);

        Query filterTask;
        if (lastResult == null){
            if (!priceRageIsApplied){
                filterTask = firebaseFirestore.collection("PRODUCTS")
                        .whereArrayContainsAny("tags", subTagList)
                        .whereEqualTo(listValues.get(0),listKeys.get(0))
                        .orderBy("price_selling",priceDirection);
            } else{
                filterTask = firebaseFirestore.collection("PRODUCTS")
                        .whereArrayContainsAny("tags", tags)
                        .whereEqualTo(listValues.get(0),listKeys.get(0))
                        .whereGreaterThan("price_selling",lowerLimit)
                        .whereLessThan("price_selling",upperLimit)
                        .orderBy("price_selling",priceDirection);
            }
        }
        else{
            if (!priceRageIsApplied){
                filterTask = firebaseFirestore.collection("PRODUCTS")
                        .whereArrayContainsAny("tags", tags)
                        .whereEqualTo(listValues.get(0),listKeys.get(0))
                        .orderBy("price_selling",priceDirection)
                        .startAfter(lastResult);
            } else{
                filterTask = firebaseFirestore.collection("PRODUCTS")
                        .whereArrayContainsAny("tags", tags)
                        .whereEqualTo(listValues.get(0),listKeys.get(0))
                        .whereGreaterThan("price_selling",lowerLimit)
                        .whereLessThan("price_selling",upperLimit)
                        .orderBy("price_selling",priceDirection)
                        .startAfter(lastResult);
            }
        }

        filterTask.limit(10L).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    List<DocumentSnapshot> allDocumentSnapshot = task.getResult().getDocuments();
                    Log.e("QuerySnapshot size","${allDocumentSnapshot.size}");


                    if (allDocumentSnapshot.isEmpty()){
                        isReachLast = true;
                        if (allSearchList.isEmpty()){
                            searchRecycler.setVisibility(gone);
                            binding.progressBar2.setVisibility(gone);
                            binding.noResultFoundText.setVisibility(visible);
                        }
                    }else{
                        for (DocumentSnapshot documentSnapshot : allDocumentSnapshot) {
                            String productId = documentSnapshot.getId();
                            String productName = documentSnapshot.getString("book_title");
                            ArrayList<String> productImgList = (ArrayList<String>) documentSnapshot.get("productImage_List");
                            Long stockQty  = documentSnapshot.getLong("in_stock_quantity");
                            String avgRating = documentSnapshot.getString("rating_avg");
                            Long totalRatings = documentSnapshot.getLong("rating_total");
                            Long priceOriginal = documentSnapshot.getLong("price_original");
                            Long priceSelling = documentSnapshot.getLong("price_selling");
                            Long printedYear = documentSnapshot.getLong("book_printed_ON");
                            String bookCondition = documentSnapshot.getString("book_condition");
                            String bookType = documentSnapshot.getString("book_type");

                            searchList.add(new SearchJavaModel(productId, productName, productImgList, priceOriginal, priceSelling,
                                    stockQty, avgRating, totalRatings, bookCondition, bookType, printedYear));
                        }

                        allSearchList.addAll(searchList);
                        searchRecycler.setVisibility(visible);
                        binding.progressBar2.setVisibility(visible);
                        binding.noResultFoundText.setVisibility(gone);

                        if (lastResult == null ){
                            searchFilterAdapter.notifyItemRangeInserted(0,searchList.size());
                        }else{
                            searchFilterAdapter.notifyItemRangeInserted((allSearchList.size()-1),searchList.size());
                        }

                        if (!allDocumentSnapshot.isEmpty()){
                            DocumentSnapshot lastR = allDocumentSnapshot.get(allDocumentSnapshot.size() - 1);
                            lastResult = lastR;
                        }

                        binding.progressBar2.setVisibility(gone);

                        isReachLast = allDocumentSnapshot.size() < 10;
                    }

                    subTagList.clear();
                    loadingDialog.dismiss();


                }else {
                    Log.e("get search query 0", "${it.message}");
                    loadingDialog.dismiss();
                    subTagList.clear();
                }

            }
        });

    }

    private void queryEqualCount2(HashMap<String ,String> subMap){

        ArrayList<SearchJavaModel>  searchList = new ArrayList<SearchJavaModel> ();
        Set<String> keys = subMap.keySet();
        List<String> listKeys = new ArrayList<String>(keys);
        Collection<String> values = subMap.values();
        List<String> listValues = new ArrayList<String>(values);
        subTagList.addAll(tags);
        subTagList.addAll(yearList);

        Query filterTask;
        if (lastResult == null){
            if (!priceRageIsApplied){
                filterTask = firebaseFirestore.collection("PRODUCTS")
                        .whereArrayContainsAny("tags", subTagList)
                        .whereEqualTo(listValues.get(0),listKeys.get(0))
                        .whereEqualTo(listValues.get(1),listKeys.get(1))
                        .orderBy("price_selling",priceDirection);
            } else{
                filterTask = firebaseFirestore.collection("PRODUCTS")
                        .whereArrayContainsAny("tags", tags)
                        .whereEqualTo(listValues.get(0),listKeys.get(0))
                        .whereEqualTo(listValues.get(1),listKeys.get(1))
                        .whereGreaterThan("price_selling",lowerLimit)
                        .whereLessThan("price_selling",upperLimit)
                        .orderBy("price_selling",priceDirection);
            }
        }
        else{
            if (!priceRageIsApplied){
                filterTask = firebaseFirestore.collection("PRODUCTS")
                        .whereArrayContainsAny("tags", tags)
                        .whereEqualTo(listValues.get(0),listKeys.get(0))
                        .whereEqualTo(listValues.get(1),listKeys.get(1))
                        .orderBy("price_selling",priceDirection)
                        .startAfter(lastResult);
            } else{
                filterTask = firebaseFirestore.collection("PRODUCTS")
                        .whereArrayContainsAny("tags", tags)
                        .whereEqualTo(listValues.get(0),listKeys.get(0))
                        .whereEqualTo(listValues.get(1),listKeys.get(1))
                        .whereGreaterThan("price_selling",lowerLimit)
                        .whereLessThan("price_selling",upperLimit)
                        .orderBy("price_selling",priceDirection)
                        .startAfter(lastResult);
            }
        }

        filterTask.limit(10L).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    List<DocumentSnapshot> allDocumentSnapshot = task.getResult().getDocuments();
                    Log.e("QuerySnapshot size","${allDocumentSnapshot.size}");

                    if (allDocumentSnapshot.isEmpty()){

                        isReachLast = true;
                        if (allSearchList.isEmpty()){
                            searchRecycler.setVisibility(gone);
                            binding.progressBar2.setVisibility(gone);
                            binding.noResultFoundText.setVisibility(visible);
                        }
                    }
                    else{
                        for (DocumentSnapshot documentSnapshot : allDocumentSnapshot) {
                            String productId = documentSnapshot.getId();
                            String productName = documentSnapshot.getString("book_title");
                            ArrayList<String> productImgList = (ArrayList<String>) documentSnapshot.get("productImage_List");
                            Long stockQty  = documentSnapshot.getLong("in_stock_quantity");
                            String avgRating = documentSnapshot.getString("rating_avg");
                            Long totalRatings = documentSnapshot.getLong("rating_total");
                            Long priceOriginal = documentSnapshot.getLong("price_original");
                            Long priceSelling = documentSnapshot.getLong("price_selling");
                            Long printedYear = documentSnapshot.getLong("book_printed_ON");
                            String bookCondition = documentSnapshot.getString("book_condition");
                            String bookType = documentSnapshot.getString("book_type");

                            searchList.add(new SearchJavaModel(productId, productName, productImgList, priceOriginal, priceSelling,
                                    stockQty, avgRating, totalRatings, bookCondition, bookType, printedYear));
                        }

                        allSearchList.addAll(searchList);
                        searchRecycler.setVisibility(visible);
                        binding.progressBar2.setVisibility(visible);
                        binding.noResultFoundText.setVisibility(gone);

                        if (lastResult == null ){
                            searchFilterAdapter.notifyItemRangeInserted(0,searchList.size());
                        }else{
                            searchFilterAdapter.notifyItemRangeInserted((allSearchList.size()-1),searchList.size());
                        }

                        if (!allDocumentSnapshot.isEmpty()){
                            DocumentSnapshot lastR = allDocumentSnapshot.get(allDocumentSnapshot.size() - 1);
                            lastResult = lastR;
                        }

                        binding.progressBar2.setVisibility(gone);

                        isReachLast = allDocumentSnapshot.size() < 10;
                    }


                    subTagList.clear();
                    loadingDialog.dismiss();

                }else {
                    Log.e("get search query 0", "${it.message}");
                    loadingDialog.dismiss();
                    subTagList.clear();
                }

            }
        });

    }


}