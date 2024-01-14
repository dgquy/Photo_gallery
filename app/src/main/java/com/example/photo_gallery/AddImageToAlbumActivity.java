package com.example.photo_gallery;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddImageToAlbumActivity extends AppCompatActivity implements ListTransInterface {

    private Album currAddAlbum;
    private ImageView img_back_create_album;
    private ImageView btnTick;
    private RecyclerView rycAddAlbum;
    private List<Image> listImage;
    private ArrayList<Image> listImageSelected;
    private String path_folder;
    private ArrayList<String> myAlbum;
    private String album_name;
    LinearLayout linearLayout;
    MySharedPreferences pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image_album);
        currAddAlbum= getIntent().getParcelableExtra("add2Album");
        settingData();
        mappingControls();
        event();
    }
    private void settingData() {
        listImageSelected = new ArrayList<>();
        path_folder = currAddAlbum.getPathFolder();
        album_name = currAddAlbum.getName();
        myAlbum= new ArrayList<>();
        for(int i=0; i<currAddAlbum.getList().size(); i++){
            myAlbum.add(currAddAlbum.getList().get(i).getThump());
        }
    }
    private void event() {
        img_back_create_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setViewRyc();

        btnTick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddAlbumAsyncTask createAlbumAsyncTask = new AddAlbumAsyncTask();
                createAlbumAsyncTask.execute();
            }
        });
    }

   // Thiết lập danh sách ảnh để chọn
    private void setViewRyc() {
        List<Image> tempListImage= GetAllPhotoFromGallery.getAllImageFromGallery(this);
        listImage= new ArrayList<>();
        String cut="Pictures/";
        for (int i = 0; i < tempListImage.size(); i++) {
            if(!hasSlash(tempListImage.get(i).getPath(),cut)){
                listImage.add(tempListImage.get(i));
            }
        }
        ImageSelectAdapter imageAdapter = new ImageSelectAdapter(this);
        imageAdapter.setListTransInterface(this);
        imageAdapter.setData(listImage);
        rycAddAlbum.setLayoutManager(new GridLayoutManager(this, 4));
        rycAddAlbum.setAdapter(imageAdapter);
    }
    public  boolean hasSlash(String a, String b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return false;
        }
        int indexOfB = a.indexOf(b);
        if (indexOfB != -1 && indexOfB + b.length() < a.length()) {
            String remainingString = a.substring(indexOfB + b.length());
            return remainingString.contains("/");
        } else {
            return false;
        }
    }
    private void mappingControls() {
        img_back_create_album = findViewById(R.id.img_back_create_album);
        btnTick = findViewById(R.id.btnTick);
        rycAddAlbum = findViewById(R.id.rycAddAlbum);
        linearLayout= findViewById(R.id.viewln);
        pref=new MySharedPreferences(AddImageToAlbumActivity.this);
        int mauchude= Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        linearLayout.setBackgroundColor(mauchude);
    }

    @Override
    public void addList(Image img) {
        listImageSelected.add(img);
    }
    public void removeList(Image img) {
        listImageSelected.remove(img);
    }
    public class AddAlbumAsyncTask extends AsyncTask<Void, Integer, Void> {
        ArrayList<String> list;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            list = new ArrayList<>();


        }

        //                File imgFile = new File(img.getPath());
        //                File desImgFile = new File(album.getPathFolder(),album.getName()+"_"+imgFile.getName());
        //                list.add(desImgFile.getPath());

        @Override
        protected Void doInBackground(Void... voids) {
            String albumName = currAddAlbum.getName();
            String albumPath= currAddAlbum.getPathFolder();
            ArrayList<Image> addListImage = new ArrayList<>();
            String[] paths = new String[listImageSelected.size()];

            int k=0;
            int i = 0;

            for (Image img : listImageSelected) {
                File imgFile = new File(img.getPath());
                File desImgFile = new File(albumPath, albumName + "_" + imgFile.getName());

                try {
                    FileInputStream fis = new FileInputStream(imgFile);
                    FileOutputStream fos = new FileOutputStream(desImgFile);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }

                    fis.close();
                    fos.close();

                    paths[i] = desImgFile.getPath();
                    list.add(desImgFile.getPath());
                    addListImage.add(new Image(desImgFile.getPath(), desImgFile.getPath(), img.getDateTaken()));

                } catch (IOException e) {
                    e.printStackTrace();
                }

                i++;
            }


            MediaScannerConnection.scanFile(getApplicationContext(), paths, null, null);
            currAddAlbum.getList().addAll(addListImage);
            return null;
        }





        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("albumResult", currAddAlbum);
            resultIntent.putStringArrayListExtra("addList", list);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }
}

