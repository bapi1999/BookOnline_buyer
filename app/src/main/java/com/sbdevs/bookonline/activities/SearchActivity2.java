package com.sbdevs.bookonline.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sbdevs.bookonline.R;
import com.sbdevs.bookonline.adapters.FirebaseAdapter2;
import com.sbdevs.bookonline.models.user.QueryModel2;

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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(SearchActivity2.this,SearchFilterActivity.class);
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
                Intent intent = new Intent(SearchActivity2.this,SearchFilterActivity.class);
                intent.putExtra("query",s);
                startActivity(intent);
                finish();
            }


        });




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