package com.example.photo_gallery;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.util.ArrayList;

public class Adapter_hashtag extends ArrayAdapter<Hashtag> {
    Activity context;
    int id_layout;
    ArrayList<Hashtag> mylist;
    SQLdemo mydatabase;
    PictureActivity activity = (PictureActivity) getContext();
    public Adapter_hashtag(Context content, int id_layout, ArrayList<Hashtag> mylist) {
        super(content, id_layout, mylist);
        this.context = (Activity) content;
        this.id_layout = id_layout;
        this.mylist = mylist;
        this.mydatabase = new SQLdemo(content); // Initialize mydatabase
    }
    //Gọi hàm getView để tiến hành sắp xếp dữ liệu

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //Tạo layout inflacter
        LayoutInflater myflacter= context.getLayoutInflater();
        convertView=myflacter.inflate(id_layout,null);
        Hashtag hashtag=mylist.get(position);
        TextView name=convertView.findViewById(R.id.edthashtag);
        name.setText(hashtag.getHashtag());
        ImageButton deleteButton = convertView.findViewById(R.id.btndelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the item from the list
                Hashtag deletehashtag=mylist.get(position);
                mydatabase.drophashtag(deletehashtag.getHashtag());
                mylist.remove(position);
                activity.updatehashtagview(mylist.size());
                // Notify the adapter that the data set has changed*/
                notifyDataSetChanged();
            }
        });
        return  convertView;
    }
}
