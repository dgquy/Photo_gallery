package com.example.photo_gallery;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ShowImageToChoose extends AppCompatActivity {
    private ArrayList<String> albumListImgString;
    private List<Image> albumListImg;
    Toolbar toolbar;
    private RecyclerView rcvListImg;
    private AllImageAdapter listImgAdapter;
    private Intent intent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_img_to_choose);
        toolbar = findViewById(R.id.toolbar_item_choose);
        toolbar.inflateMenu(R.menu.menu_top_item_album);
        toolbar.setTitle("Choose an image");
//        toolbar.setTitleTextColor(0xFFFFFFFF);
        rcvListImg = findViewById(R.id.ryc_list_img_to_choose);

        setData();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rcvListImg.setLayoutManager(gridLayoutManager);
        listImgAdapter = new AllImageAdapter(albumListImg, this);
        rcvListImg.setAdapter(listImgAdapter);

        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setData() {
        albumListImgString = new ArrayList<>();
        albumListImg = new ArrayList<>();

        intent = getIntent();
        albumListImgString = intent.getStringArrayListExtra("dataToChoose");
        List<Image> list_temp = GetAllimagefromGallery.getAllImageFromGallery(this);
        for (int i = 0; i < albumListImgString.size(); i++) {
            for (int j = 0; j < list_temp.size(); j++) {
                if (albumListImgString.get(i).contains(list_temp.get(j).getThump()))
                    albumListImg.add(list_temp.get(j));
            }
        }
    }
}
