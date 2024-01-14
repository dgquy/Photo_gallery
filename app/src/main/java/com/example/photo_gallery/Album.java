package com.example.photo_gallery;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Album implements Parcelable {
    private String pathFolder;
    private Image img;
    private String name;
    private List<Image> listImage;

    public Album(Image img, String name) {
        this.name = name;
        this.img = img;
        listImage = new ArrayList<>();
    }

    public Album(String _name, List<Image> _listImage, String _pathFolder) {
        this.name = _name;
        this.listImage = _listImage;
        this.pathFolder = _pathFolder;
        if (!_listImage.isEmpty()) {
            this.img = _listImage.get(0);
        }
    }

    protected Album(Parcel in) {
        pathFolder = in.readString();
        img = in.readParcelable(Image.class.getClassLoader());
        name = in.readString();
        listImage = in.createTypedArrayList(Image.CREATOR);
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    public void setPathFolder(String _pathFolder) {
        this.pathFolder = _pathFolder;
    }

    public String getPathFolder() {
        return pathFolder;
    }

    public Image getImg() {
        return img;
    }

    public String getName() {
        return name;
    }

    public void setName(String _name) {
        this.name = _name;
    }

    public void setImg(Image _img) {
        this.img = _img;
    }

    public List<Image> getList() {
        return listImage;
    }

    //public void addList(List<Image> list) {
    //   listImage = new ArrayList<>(list);
    // }
    public void addItem(Image _img) {
        listImage.add(_img);
    }

    public void setListImage(List<Image> _listImage) {
        this.listImage = _listImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(pathFolder);
        dest.writeParcelable(img, flags);
        dest.writeString(name);
        dest.writeTypedList(listImage);
    }
}

