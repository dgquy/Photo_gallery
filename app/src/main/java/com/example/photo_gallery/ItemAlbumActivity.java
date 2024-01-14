package com.example.photo_gallery;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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

    //----------------------------------
    private Album currAlbum;
    private ArrayList<String> myAlbum;
    private String path_folder;
    private RecyclerView ryc_album;
    private RecyclerView ryc_list_album;
    private Intent intent;
    private String album_name;
    Toolbar toolbar_item_album;
    private ItemAlbumAdapter itemAlbumAdapter;
    private ItemAlbumAdapter2 itemAlbumAdapter2;
    private ItemAlbumAdapter3 itemAlbumAdapter3;
    private int spanCount;
    private int isSecret = 0;
    private int duplicateImg = 0;
    private int isAlbum;
    AlbumFragment albumFragment;
    AlbumAdapter albumAdapter;
    private Image thumbChange = null;
    MySharedPreferences pref;

    private static final int REQUEST_CODE_PIC = 11;
    private static final int REQUEST_CODE_MULTI = 12;
    private static final int REQUEST_CODE_ADD = 13;
    private static final int REQUEST_CODE_SECRET = 14;
    private static final int REQUEST_AN_IMAGE = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_album);
        //----------------------------------
        currAlbum = getIntent().getParcelableExtra("selectedAlbum");
        isAlbum = getIntent().getIntExtra("ok", 1);
        Toast.makeText(ItemAlbumActivity.this, "SIZE OF ALBUM: " + currAlbum.getList().size(), Toast.LENGTH_LONG).show();

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


        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_ADD) {
            currAlbum = (Album) data.getParcelableExtra("albumResult");
            ArrayList<String> resultList = (ArrayList<String>) data.getStringArrayListExtra("addList");
            myAlbum.addAll(resultList);
            spanAction();
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_MULTI) {
            if (data != null) {
                int isMoved = data.getIntExtra("move", 0);
                if (isMoved == 1) {
                    ArrayList<String> resultList = data.getStringArrayListExtra("list_result");
                    String album_name_move_to = data.getStringExtra("album_name_move_to");
                    ArrayList<Image> move_list = data.getParcelableArrayListExtra("move_list");
                    // Xóa ảnh ở album hiện tại và thêm ảnh vào album được di chuyển ảnh tới
                    if (resultList != null) {
                        myAlbum.removeAll(resultList);

                        for (int i = 0; i < resultList.size(); i++) {
                            for (int j = 0; j < currAlbum.getList().size(); j++) {
                                if (resultList.get(i).equals(currAlbum.getList().get(j).getPath())) {
                                    currAlbum.getList().remove(j);
                                }
                            }
                        }

                        // Truyền currAlbum về lại cho Fragment
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("updatedAlbum", currAlbum);
                        resultIntent.putExtra("album_name_move_to", album_name_move_to);
                        resultIntent.putExtra("move_list", move_list);
                        resultIntent.putExtra("moved", 1);
                        setResult(Activity.RESULT_OK, resultIntent);

                        spanAction();
                        finish();

                    }
                } else if (isMoved == 2) {
                    Image gifImg = data.getParcelableExtra("result");
                    myAlbum.add(gifImg.getPath());
                    currAlbum.addItem(gifImg);
                    spanAction();
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
            thumbChange = data.getParcelableExtra("KEY_THUMBNAIL");
        }

    }

    private void spanAction() {
        if (spanCount == 1) {
            ryc_list_album.setAdapter(new ItemAlbumAdapter3(myAlbum));
        } else if (spanCount == 2) {
            ryc_list_album.setAdapter(new ItemAlbumAdapter2(myAlbum));
        } else {
            ryc_list_album.setAdapter(new ItemAlbumAdapter(myAlbum));
        }
    }

    private void setRyc() {
        album_name = currAlbum.getName();
        ryc_list_album.setLayoutManager(new GridLayoutManager(this, spanCount));
        itemAlbumAdapter = new ItemAlbumAdapter(myAlbum);
        if (spanCount == 1)
            ryc_list_album.setAdapter(new ItemAlbumAdapter3(myAlbum));
        else if (spanCount == 2)
            ryc_list_album.setAdapter(new ItemAlbumAdapter2(myAlbum));
        else
            ryc_list_album.setAdapter(new ItemAlbumAdapter(myAlbum));
    }

    private void animationRyc() {

    }


    private void events() {
        // Toolbar events
        pref = new MySharedPreferences(ItemAlbumActivity.this);
        int mauchude = Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));

        toolbar_item_album.inflateMenu(R.menu.menu_top_item_album);
        toolbar_item_album.setTitle(album_name);
        toolbar_item_album.setBackgroundColor(mauchude);
        if (isAlbum == 0) {
            toolbar_item_album.getMenu().findItem(R.id.menu_add_image).setVisible(false);
        } else
            toolbar_item_album.getMenu().findItem(R.id.menu_add_image).setVisible(true);
        // Xử lí event cho ic_back
        toolbar_item_album.setNavigationIcon(R.drawable.ic_back);
        toolbar_item_album.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Truyền currAlbum về lại cho Fragment
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedAlbum", currAlbum);
                setResult(Activity.RESULT_OK, resultIntent);
                if (thumbChange != null)
                    resultIntent.putExtra("thumbnailToChange", thumbChange);
                finish();
            }
        });

        // Toolbar options
        toolbar_item_album.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.change_span_count:
                        spanCountEvent();
                        break;
                    case R.id.menuChoose:
                        Intent intent_multi = new Intent(ItemAlbumActivity.this, ItemAlbumMultiSelectActivity.class);
                        // Truyền album đang chọn qua MultiActivity
                        intent_multi.putExtra("multiAlbum", currAlbum);
                        startActivityForResult(intent_multi, REQUEST_CODE_MULTI);
                        break;
                    case R.id.menu_add_image:

                        Intent intent_add = new Intent(ItemAlbumActivity.this, AddImageToAlbumActivity.class);
                        intent_add.putExtra("add2Album", currAlbum);
                        startActivityForResult(intent_add, REQUEST_CODE_ADD);

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

    public void changeThumbnail() {
        Intent currentIntent = new Intent(this, ShowImageToChoose.class);
        currentIntent.putExtra("dataToChoose", currAlbum);
        startActivityForResult(currentIntent, REQUEST_AN_IMAGE);
    }

    private void hideMenu() {
        toolbar_item_album.getMenu().findItem(R.id.menu_add_image).setVisible(false);
    }

    private void spanCountEvent() {
        if (spanCount == 1) {
            spanCount++;
            ryc_list_album.setLayoutManager(new GridLayoutManager(this, spanCount));
            ryc_list_album.setAdapter(itemAlbumAdapter2);
        } else if (spanCount < 4 && spanCount > 1) {
            spanCount++;
            ryc_list_album.setLayoutManager(new GridLayoutManager(this, spanCount));
            ryc_list_album.setAdapter(itemAlbumAdapter);
        } else if (spanCount == 4) {

            spanCount = 1;
            ryc_list_album.setLayoutManager(new LinearLayoutManager(this));
            ryc_list_album.setAdapter(itemAlbumAdapter3);

        }


        animationRyc();
        SharedPreferences sharedPref = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("span_count", spanCount);
        editor.commit();
    }


    private void setData() {

        //----------------------------------

        myAlbum = new ArrayList<>();
        for (int i = 0; i < currAlbum.getList().size(); i++) {
            myAlbum.add(currAlbum.getList().get(i).getThump());
        }
        path_folder = currAlbum.getPathFolder();

        //isSecret = intent.getIntExtra("isSecret", 0);
        // duplicateImg = intent.getIntExtra("duplicateImg",0);
        itemAlbumAdapter2 = new ItemAlbumAdapter2(myAlbum);
        itemAlbumAdapter3 = new ItemAlbumAdapter3(myAlbum);

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
