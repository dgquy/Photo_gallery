package com.example.photo_gallery;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

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

public class CreateAlbumActivity extends AppCompatActivity implements ListTransInterface {
    private ImageView img_back_create_album;
    private ImageView btnTick;
    private EditText edtTitleAlbum;
    private RecyclerView rycAddAlbum;
    private List<Image> listImage;
    private ArrayList<Image> listImageSelected;
    LinearLayout linearLayout;
    MySharedPreferences pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_album);
        settingData();
        mappingControls();

        event();
    }

    private void settingData() {
        listImageSelected = new ArrayList<>();
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
                if (!TextUtils.isEmpty(edtTitleAlbum.getText())) {
                    if (edtTitleAlbum.getText().toString().contains("#")) {
                        Toast.makeText(getApplicationContext(), "Không được chứa kí tự #", Toast.LENGTH_SHORT).show();
                    } else {
                        CreateAlbumAsyncTask createAlbumAsyncTask = new CreateAlbumAsyncTask();
                        createAlbumAsyncTask.execute();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Title null", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*private void action() {
        Intent intent = new Intent(this, SlideShowActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }*/

    private void setViewRyc() {
        List<Image> tempListImage = GetAllPhotoFromGallery.getAllImageFromGallery(this);
        listImage = new ArrayList<>();
        String cut = "Pictures/";
        for (int i = 0; i < tempListImage.size(); i++) {
            if (!hasSlash(tempListImage.get(i).getPath(), cut)) {
                listImage.add(tempListImage.get(i));
            }
        }
        ImageSelectAdapter imageAdapter = new ImageSelectAdapter(this);
        imageAdapter.setListTransInterface(this);
        imageAdapter.setData(listImage);
        rycAddAlbum.setLayoutManager(new GridLayoutManager(this, 4));
        rycAddAlbum.setAdapter(imageAdapter);
    }

    public boolean hasSlash(String a, String b) {
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
        edtTitleAlbum = findViewById(R.id.edtTitleAlbum);
        rycAddAlbum = findViewById(R.id.rycAddAlbum);
        linearLayout= findViewById(R.id.header);
        pref=new MySharedPreferences(CreateAlbumActivity.this);
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

    public class CreateAlbumAsyncTask extends AsyncTask<Void, Integer, Album> {
        @Override
        protected Album doInBackground(Void... voids) {
            String albumName = edtTitleAlbum.getText().toString();
            final String albumPath = Environment.getExternalStorageDirectory() + File.separator + "Pictures" + File.separator + albumName;
            File directory = new File(albumPath);
            if (!directory.exists()) {
                directory.mkdirs();
                Log.e("File-no-exist", directory.getPath());
            }

            ArrayList<Image> newListImage = new ArrayList<>();
            String[] paths = new String[listImageSelected.size()];

            for (int i = 0; i < listImageSelected.size(); i++) {
                Image img = listImageSelected.get(i);
                String imagePath = img.getPath();
                // -------------------------------------
                // Chỉnh lại tên của image
                String imageFileName = albumName + "_" + getImageName(imagePath);

                // Create a copy of the image and save it to the album directory
                String destinationPath = albumPath + File.separator + imageFileName;
                copyImage(imagePath, destinationPath);

                // Add the copied image to the new list
                Image copiedImage = new Image(destinationPath, img.getThump(), img.getDateTaken());
                newListImage.add(copiedImage);

                paths[i] = destinationPath;
            }

            MediaScannerConnection.scanFile(getApplicationContext(), paths, null, null);
            Album newAlbum = new Album(albumName, newListImage, albumPath);
            return newAlbum;
        }

        // Helper method to copy an image file
        private void copyImage(String sourcePath, String destinationPath) {
            try {
                File sourceFile = new File(sourcePath);
                File destinationFile = new File(destinationPath);

                FileInputStream inStream = new FileInputStream(sourceFile);
                FileOutputStream outStream = new FileOutputStream(destinationFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }

                inStream.close();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // get Image Name from Image Path
        private String getImageName(String input) {
            if (input == null || input.isEmpty()) {
                return "";
            } else {
                int lastIndex = input.lastIndexOf("/");
                if (lastIndex != -1 && lastIndex < input.length() - 1) {
                    return input.substring(lastIndex + 1);
                } else {
                    return "";
                }
            }
        }

        @Override
        protected void onPostExecute(Album newAlbum) {
            super.onPostExecute(newAlbum);
            if (newAlbum != null) {

                Intent resultIntent = new Intent();
                resultIntent.putExtra("createdAlbum", newAlbum);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "The album creation failed", Toast.LENGTH_SHORT).show();
            }

        }
    }
}