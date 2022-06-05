package com.sbdevs.bookonline.adapters.java;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sbdevs.bookonline.R;
import com.sbdevs.bookonline.activities.ProductActivity;
import com.sbdevs.bookonline.models.java.SearchJavaModel;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;


public class SearchFilterJavaAdapter extends RecyclerView.Adapter<SearchFilterJavaAdapter.ViewHolder> {

    private final ArrayList<SearchJavaModel> list;

    public SearchFilterJavaAdapter(ArrayList<SearchJavaModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public SearchFilterJavaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_filter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchFilterJavaAdapter.ViewHolder holder, int position) {
        holder.setProductData(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productRealPrice;
        TextView percentOff;

        TextView avgRatingText;
        TextView totalRatingsText;
        ImageView outOfStockIcon;
        TextView bookTypeText;
        TextView bookConditionText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productRealPrice = itemView.findViewById(R.id.product_real_price);
            percentOff = itemView.findViewById(R.id.percent_off);

            avgRatingText = itemView.findViewById(R.id.mini_product_rating);
            totalRatingsText = itemView.findViewById(R.id.mini_totalNumberOf_ratings);
            outOfStockIcon = itemView.findViewById(R.id.outofstock_icon);
            bookTypeText = itemView.findViewById(R.id.book_type);
            bookConditionText = itemView.findViewById(R.id.product_condition);

        }

        private void setProductData(SearchJavaModel model) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent productIntent =new Intent(itemView.getContext(), ProductActivity.class);
                    productIntent.putExtra("productId",model.getProductId());
                    itemView.getContext().startActivity(productIntent);
                }
            });

            productName.setText(model.getBook_title());
            String url = model.getProductImage_List().get(0);
            Long stockQty = model.getIn_stock_quantity();
            Long priceOriginal = model.getPrice_original();
            Long priceSelling = model.getPrice_selling();

            Long ratingTotal = model.getRating_total();
            avgRatingText.setText(model.getRating_avg());
            totalRatingsText.setText("(" + ratingTotal + " ratings )");




            Picasso.get().load(url).placeholder(R.drawable.as_square_placeholder)
                    .resize(200, 200).centerCrop()
                    .into(productImage);

            if (priceOriginal == 0L) {
                productPrice.setText(priceSelling.toString());
                productRealPrice.setVisibility(View.GONE);
                percentOff.setVisibility(View.GONE);

            } else {

                int price = Math.toIntExact(priceSelling);
                int realPriceInt = Math.toIntExact(priceOriginal);

                int percent = (100 * (realPriceInt - price)) / (realPriceInt);

                productPrice.setText(priceSelling.toString());
                productRealPrice.setText(priceOriginal.toString());
                percentOff.setText(percent + "% off");

            }

            if (stockQty == 0L) {
                outOfStockIcon.setVisibility(View.VISIBLE);
            } else {
                outOfStockIcon.setVisibility(View.GONE);
            }

            String conditionString  = "";
            switch (model.getBook_condition()){
                case "new_condition":{
                    conditionString = "Good";
                    break;
                }
                case "almost_new":{
                    conditionString = "Almost new";
                    break;
                }
                case "slightly_damaged":{
                    conditionString = "Slightly damaged";
                    break;
                }
                case "fully_damaged":{
                    conditionString = "Fully damaged";
                    break;
                }
                default:
                    conditionString = "";
            }

            bookConditionText.setText(conditionString);

            String typeString = "" ;
            switch (model.getBook_type()){

                case "new_printed":
                case "old_printed": {
                    typeString = "New";
                    break;
                }
                case "used":{
                    typeString = "Used";
                    break;
                }
                case "refurbished":{
                    typeString = "Refurbished";
                    break;
                }

            }



            if (model.getBook_printed_ON() != 0L) {
                String st = typeString+ "(" + model.getBook_printed_ON() + ")";
                bookTypeText.setText(st);
            }else {
                bookTypeText.setText(typeString);
            }



        }
    }
}
