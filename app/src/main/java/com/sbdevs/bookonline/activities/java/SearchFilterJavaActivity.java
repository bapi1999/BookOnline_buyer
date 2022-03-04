package com.sbdevs.bookonline.activities.java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sbdevs.bookonline.R;
import com.sbdevs.bookonline.adapters.java.SearchFilterJavaAdapter;
import com.sbdevs.bookonline.databinding.ActivitySearchFilterJavaBinding;
import com.sbdevs.bookonline.fragments.LoadingDialog;
import com.sbdevs.bookonline.models.java.SearchJavaModel;
import java.util.ArrayList;
import java.util.Objects;

public class SearchFilterJavaActivity extends AppCompatActivity {

    private ActivitySearchFilterJavaBinding binding;
    private BottomSheetDialog bottomSheetDialog;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    private RecyclerView searchRecycler;
    private ArrayList<SearchJavaModel> allSearchList  = new ArrayList();
    private SearchFilterJavaAdapter searchFilterAdapter;

    private ArrayList<String> typeList =new ArrayList();
    private ArrayList<String> conditionList =new ArrayList();
    private ArrayList<String> yearList = new ArrayList();
    private ArrayList<String> tags = new ArrayList();
    private Query.Direction  priceDirection = Query.Direction.ASCENDING;


    private DocumentSnapshot lastResult;
    private Long inStockOrder = 0L;
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
        ChipGroup relevanceChipGroup = dialog.findViewById(R.id.relevance_chipGroup);
        ChipGroup yearChipGroup= dialog.findViewById(R.id.print_chipGroup) ;
        RadioGroup priceRadioGroup = dialog.findViewById(R.id.price_radioGroup) ;
        RangeSlider priceRaneSlider = dialog.findViewById(R.id.price_range_slider) ;
        LinearLayout priceRangeTextCOntainer = dialog.findViewById(R.id.price_range_text_container) ;
        TextView lowerInput = dialog.findViewById(R.id.lower_input) ;
        TextView upperInput= dialog.findViewById(R.id.upper_input) ;



    }

}