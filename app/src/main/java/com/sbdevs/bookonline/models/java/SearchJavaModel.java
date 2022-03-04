package com.sbdevs.bookonline.models.java;

import java.util.ArrayList;

public class SearchJavaModel {
    private String productId ;
    private String book_title;
    private ArrayList<String> productImage_List;
    private Long price_original;
    private Long price_selling;
    private Long in_stock_quantity;
    private String rating_avg;
    private Long rating_total;
    private String book_condition;
    private String book_type;
    private Long book_printed_ON;

    public SearchJavaModel() {
    }

    public SearchJavaModel(String productId, String book_title, ArrayList<String> productImage_List, Long price_original, Long price_selling, Long in_stock_quantity, String rating_avg, Long rating_total, String book_condition, String book_type, Long book_printed_ON) {
        this.productId = productId;
        this.book_title = book_title;
        this.productImage_List = productImage_List;
        this.price_original = price_original;
        this.price_selling = price_selling;
        this.in_stock_quantity = in_stock_quantity;
        this.rating_avg = rating_avg;
        this.rating_total = rating_total;
        this.book_condition = book_condition;
        this.book_type = book_type;
        this.book_printed_ON = book_printed_ON;
    }


    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getBook_title() {
        return book_title;
    }

    public void setBook_title(String book_title) {
        this.book_title = book_title;
    }

    public ArrayList<String> getProductImage_List() {
        return productImage_List;
    }

    public void setProductImage_List(ArrayList<String> productImage_List) {
        this.productImage_List = productImage_List;
    }

    public Long getPrice_original() {
        return price_original;
    }

    public void setPrice_original(Long price_original) {
        this.price_original = price_original;
    }

    public Long getPrice_selling() {
        return price_selling;
    }

    public void setPrice_selling(Long price_selling) {
        this.price_selling = price_selling;
    }

    public Long getIn_stock_quantity() {
        return in_stock_quantity;
    }

    public void setIn_stock_quantity(Long in_stock_quantity) {
        this.in_stock_quantity = in_stock_quantity;
    }

    public String getRating_avg() {
        return rating_avg;
    }

    public void setRating_avg(String rating_avg) {
        this.rating_avg = rating_avg;
    }

    public Long getRating_total() {
        return rating_total;
    }

    public void setRating_total(Long rating_total) {
        this.rating_total = rating_total;
    }

    public String getBook_condition() {
        return book_condition;
    }

    public void setBook_condition(String book_condition) {
        this.book_condition = book_condition;
    }

    public String getBook_type() {
        return book_type;
    }

    public void setBook_type(String book_type) {
        this.book_type = book_type;
    }

    public Long getBook_printed_ON() {
        return book_printed_ON;
    }

    public void setBook_printed_ON(Long book_printed_ON) {
        this.book_printed_ON = book_printed_ON;
    }
}
