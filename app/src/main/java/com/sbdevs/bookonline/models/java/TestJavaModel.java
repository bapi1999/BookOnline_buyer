package com.sbdevs.bookonline.models.java;

public class TestJavaModel {

    private String documentId;
    private String image;
    private String name;
    private String book_title;
    private Long price_selling;

    public TestJavaModel() {
    }


    public TestJavaModel(String documentId, String image, String name, String book_title, Long price_selling) {
        this.documentId = documentId;
        this.image = image;
        this.name = name;
        this.book_title = book_title;
        this.price_selling = price_selling;
    }


    public String getBook_title() {
        return book_title;
    }

    public void setBook_title(String book_title) {
        this.book_title = book_title;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPrice_selling() {
        return price_selling;
    }

    public void setPrice_selling(Long price_selling) {
        this.price_selling = price_selling;
    }
}
