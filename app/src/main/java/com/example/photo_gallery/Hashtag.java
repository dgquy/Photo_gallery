package com.example.photo_gallery;


public class Hashtag {
    String hashtag;
    Image image;

    public Hashtag(String hashtag, Image image) {
        this.hashtag = hashtag;
        this.image = image;
    }

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
