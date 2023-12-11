package com.example.photo_gallery;

public class Image {
    private String path;
    private String thump;
    private String dateTaken;
    private String hashtag;

    public Image(String path, String thump, String dateTaken) {
        this.path = path;
        this.thump = thump;
        this.dateTaken = dateTaken;
    }

    public Image(Image _image) {
        this.path = _image.path;
        this.thump = _image.thump;
        this.dateTaken = _image.dateTaken;
        this.hashtag = _image.hashtag;
    }

    public Image() {

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getThump() {
        return thump;
    }

    public void setThump(String thump) {
        this.thump = thump;
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(String dateTaken) {
        this.dateTaken = dateTaken;
    }

    public String getHashtag() {
        return hashtag;
    }
}
