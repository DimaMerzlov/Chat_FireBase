package com.example.chatapp;

public class Message {
    private String author;
    private String textOfMessage;
    private Long date;
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Message(String author, String textOfMessage, long date,String imageUrl) {
        this.author = author;
        this.textOfMessage = textOfMessage;
        this.date=date;
        this.imageUrl=imageUrl;
    }

    public Message() {
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public String getTextOfMessage() {
        return textOfMessage;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTextOfMessage(String textOfMessage) {
        this.textOfMessage = textOfMessage;
    }
}
