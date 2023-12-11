package com.example.photo_gallery;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class ItemAlbumActivity extends AppCompatActivity {

    private ArrayList<String> myAlbum;
    private String path_folder;
    private RecyclerView ryc_list_album;
    private Intent intent;
    private String album_name;
    Toolbar toolbar_item_album;
    private ItemAlbumAdapter itemAlbumAdapter;
    private int spanCount;
    private int isSecret;
    private int duplicateImg;
    private int isAlbum;
    private static final int REQUEST_CODE_PIC = 10;
    private static final int REQUEST_CODE_CHOOSE = 55;
    private static final int REQUEST_CODE_ADD = 56;
    private static final int REQUEST_CODE_SECRET = 57;
    private static final int REQUEST_AN_IMAGE = 100;
    private static final String KEY_THUMBNAIL_RESULT = "KEY_THUMBNAIL_RESULT";
    MySharedPreferences pref;
    private int posCurrentAlbum;
    private String thumbResult;
    private Intent resultIntent;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_album);
        intent = getIntent();
        setUpSpanCount();
        mappingControls();
        setData();
        setRyc();
        events();
    }

    private void setUpSpanCount() {
        SharedPreferences sharedPref = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        spanCount = sharedPref.getInt("span_count", 3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, requestCode + " " + resultCode, Toast.LENGTH_SHORT).show();

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_ADD) {
            ArrayList<String> resultList = data.getStringArrayListExtra("list_result");
            if (resultList != null) {
                myAlbum.addAll(resultList);
                spanAction();
            }
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CHOOSE) {
            if (data != null) {
                int isMoved = data.getIntExtra("move", 0);
                if (isMoved == 1) {
                    ArrayList<String> resultList = data.getStringArrayListExtra("list_result");
                    if (resultList != null) {
                        myAlbum.remove(resultList);
                        spanAction();
                    }
                }
            }
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SECRET) {
            MyAsyncTask myAsyncTask = new MyAsyncTask();
            myAsyncTask.execute();
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PIC) {
            String path_img = data.getStringExtra("path_img");
            if (isSecret == 1) {
                myAlbum.remove(path_img);
                spanAction();
            } else if (duplicateImg == 2) {
                myAlbum.remove(path_img);
                spanAction();
            }
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_AN_IMAGE) {
            thumbResult = data.getStringExtra("KEY_THUMBNAIL");
            resultIntent = new Intent();
            resultIntent.putExtra(KEY_THUMBNAIL_RESULT, thumbResult);
            resultIntent.putExtra("this_position", posCurrentAlbum);
            setResult(RESULT_OK, resultIntent);
        }
    }

    private void spanAction() {
        ryc_list_album.setAdapter(new ItemAlbumAdapter(myAlbum));
    }

    private void setRyc() {
        album_name = intent.getStringExtra("name");
        ryc_list_album.setLayoutManager(new GridLayoutManager(this, spanCount));
        itemAlbumAdapter = new ItemAlbumAdapter(myAlbum);
        ryc_list_album.setAdapter(new ItemAlbumAdapter(myAlbum));
    }

    // Ghi đè phương thức của nút back
//    @Override
//    public void onBackPressed() {
//        Intent resultIntent = new Intent();
//        resultIntent.putExtra(KEY_THUMBNAIL_RESULT, thumbResult);
//        resultIntent.putExtra("this_position", posCurrentAlbum);
//        setResult(RESULT_OK, resultIntent);
//
//        //Toast.makeText(this, "Changed thumbnail successfully!", Toast.LENGTH_SHORT).show();
//        super.onBackPressed();
//    }

    //////////////////////////////////////////////////////////
    private void events() {
        // Toolbar events
        toolbar_item_album.inflateMenu(R.menu.menu_top_item_album);
        toolbar_item_album.setTitle(album_name);
        toolbar_item_album.setTitleTextColor(0xFFFFFFFF);
        pref = new MySharedPreferences(this);
        int mauchude = Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        toolbar_item_album.setBackgroundColor(mauchude);
        if (isAlbum == 0) {
            toolbar_item_album.getMenu().findItem(R.id.menu_add_image).setVisible(false);
        } else
            toolbar_item_album.getMenu().findItem(R.id.menu_add_image).setVisible(true);

        // Show back button
        toolbar_item_album.setNavigationIcon(R.drawable.ic_back);
        toolbar_item_album.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Xử lý các sự kiện nhấn trên thanh menu
        toolbar_item_album.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.change_span_count:
                        // TODO:
                        break;
                    case R.id.menuChoose:
                        // TODO:
                        break;
                    case R.id.album_item_slideshow:
                        //TODO:
                        break;
                    case R.id.menu_add_image:
                        //TODO:
                        break;
                    case R.id.change_thumb:
                        changeThumbnail();
                        break;
                }
                return true;
            }
        });

        if (isSecret == 1)
            hideMenu();
    }

    // TODO:
    // Đổi ảnh bìa
    public void changeThumbnail() {
        Intent currentIntent = new Intent(this, ShowImageToChoose.class);
        currentIntent.putStringArrayListExtra("dataToChoose", myAlbum);
        startActivityForResult(currentIntent, REQUEST_AN_IMAGE);
    }

    private void hideMenu() {
        toolbar_item_album.getMenu().findItem(R.id.menu_add_image).setVisible(false);
    }

    private void setData() {
        myAlbum = intent.getStringArrayListExtra("data");
        path_folder = intent.getStringExtra("path_folder");
        isSecret = intent.getIntExtra("isSecret", 0);
        duplicateImg = intent.getIntExtra("duplicateImg", 0);
        posCurrentAlbum = intent.getIntExtra("position", 0);
        //itemAlbumAdapter2 = new ItemAlbumAdapter2(myAlbum);
        isAlbum = intent.getIntExtra("ok", 0);
        //itemAlbumAdapter3 = new ItemAlbumAdapter3(myAlbum);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();
    }

    private void mappingControls() {
        ryc_list_album = findViewById(R.id.ryc_list_album);
        toolbar_item_album = findViewById(R.id.toolbar_item_album);
    }

    public class MyAsyncTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < myAlbum.size(); i++) {
                File file = new File(myAlbum.get(i));
                if (!file.exists()) {
                    myAlbum.remove(i);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            spanAction();
        }
    }
}

