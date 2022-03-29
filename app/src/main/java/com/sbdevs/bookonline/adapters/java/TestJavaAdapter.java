package com.sbdevs.bookonline.adapters.java;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sbdevs.bookonline.R;
import com.sbdevs.bookonline.models.java.TestJavaModel;

import java.util.List;

public class TestJavaAdapter extends RecyclerView.Adapter<TestJavaAdapter.ViewHolder>  {
    private List<TestJavaModel> list;

    public TestJavaAdapter(List<TestJavaModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public TestJavaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_donate,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestJavaAdapter.ViewHolder holder, int position) {

        String resource = list.get(position).getImage();
        String title = list.get(position).getName();
        Long price = list.get(position).getPrice_selling();

        holder.setProductData(resource,title,price);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
//        ImageView productImg;
//        TextView productTitle;
//        TextView productPrice;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
//            productImg = itemView.findViewById(R.id.product_image);
//            productTitle = itemView.findViewById(R.id.product_name);
//            productPrice = itemView.findViewById(R.id.product_price);
        }

        private void setProductData( String resource, String title, Long price){
//            Glide.with(itemView.getContext()).load(resource).apply(new RequestOptions()
//                    .placeholder(R.drawable.as_square_placeholder)).into(productImg);
//            productTitle.setText(title);
//            productPrice.setText("Rs."+price+"/-");
        }

    }
}
