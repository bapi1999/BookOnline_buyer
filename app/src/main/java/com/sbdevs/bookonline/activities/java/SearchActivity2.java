package com.sbdevs.bookonline.activities.java;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sbdevs.bookonline.R;
import com.sbdevs.bookonline.activities.SearchFilterActivity;
import com.sbdevs.bookonline.adapters.java.FirebaseAdapter2;
import com.sbdevs.bookonline.models.java.QueryModel2;

public class SearchActivity2 extends AppCompatActivity {
    SearchView searchView;
    RecyclerView searchRecycler;
    FirebaseAdapter2 adapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search2);

        searchView = findViewById(R.id.search_view);
        searchRecycler = findViewById(R.id.search_recycler);
        searchRecycler.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(SearchActivity2.this, SearchFilterJavaActivity.class);
                intent.putExtra("query",query);
                startActivity(intent);
                finish();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchNewData(newText);
                return false;
            }
        });



        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("for_test")
                .limitToLast(50);

        FirebaseRecyclerOptions<QueryModel2> options =
                new FirebaseRecyclerOptions.Builder<QueryModel2>()
                        .setQuery(query, QueryModel2.class)
                        .build();



        adapter2 = new FirebaseAdapter2(options);
        searchRecycler.setAdapter(adapter2);


        adapter2.setOnItemClickListener(new FirebaseAdapter2.SearchQueryClickListener() {
            @Override
            public void OnQueryClick(int position, String s) {
                Intent intent = new Intent(SearchActivity2.this,SearchFilterJavaActivity.class);
                intent.putExtra("query",s);
                startActivity(intent);
                finish();
            }


        });




    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }else {
            Log.i("","");
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchNewData(String s){
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("for_test")
                .orderByChild("name")
                .startAt(s)
                .endAt(s+"\uf8ff")
                .limitToLast(50);

        FirebaseRecyclerOptions<QueryModel2> options =
                new FirebaseRecyclerOptions.Builder<QueryModel2>()
                        .setQuery(query, QueryModel2.class)
                        .build();



        adapter2 = new FirebaseAdapter2(options);
        adapter2.startListening();
        searchRecycler.setAdapter(adapter2);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter2.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter2.stopListening();
    }
}