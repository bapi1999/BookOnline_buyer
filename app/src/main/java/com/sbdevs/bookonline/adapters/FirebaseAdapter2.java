package com.sbdevs.bookonline.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.sbdevs.bookonline.R;
import com.sbdevs.bookonline.activities.SearchActivity2;
import com.sbdevs.bookonline.activities.SearchFilterActivity;
import com.sbdevs.bookonline.models.QueryModel2;

public class FirebaseAdapter2 extends FirebaseRecyclerAdapter<QueryModel2,FirebaseAdapter2.ViewHolder> {

    private SearchQueryClickListener queryClickListener;

    public interface SearchQueryClickListener{
        void OnQueryClick(int position,String s);
    }

    public void setOnItemClickListener(SearchQueryClickListener listener){
        queryClickListener = listener;
    }

    public FirebaseAdapter2(@NonNull FirebaseRecyclerOptions<QueryModel2> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FirebaseAdapter2.ViewHolder holder, int position, @NonNull QueryModel2 model) {
        //holder.queryText.setText(model.getName());
        holder.setProductData(model.getName(),queryClickListener);
    }

    @NonNull
    @Override
    public FirebaseAdapter2.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.le_search_query_item, parent, false);
        return new ViewHolder(view,queryClickListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView queryText;


        public ViewHolder(@NonNull View itemView,SearchQueryClickListener listener) {
            super(itemView);
            queryText=(TextView) itemView.findViewById(R.id.query_text);

        }

        private void setProductData(String title,SearchQueryClickListener listener){
            queryText.setText(title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position =getAbsoluteAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.OnQueryClick(position,title);
                        }
                    }

                }
            });
        }

    }
}
