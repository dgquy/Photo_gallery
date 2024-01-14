package com.example.photo_gallery;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShowImageToChoose extends AppCompatActivity {
    private List<Image> albumListImg;
    Toolbar toolbar;
    private RecyclerView rcvListImg;
    private AllImageAdapter listImgAdapter;
    MySharedPreferences pref;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_img_to_choose);
        toolbar = findViewById(R.id.toolbar_item_choose);
        pref=new MySharedPreferences(ShowImageToChoose.this);
        int mauchude= Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        toolbar.setBackgroundColor(mauchude);
        toolbar.inflateMenu(R.menu.menu_top_item_album);
        toolbar.setTitle("Choose an image");
        toolbar.setTitleTextColor(0xFFFFFFFF);
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
        Album temp = getIntent().getParcelableExtra("dataToChoose");
        albumListImg = temp.getList();
    }
}
