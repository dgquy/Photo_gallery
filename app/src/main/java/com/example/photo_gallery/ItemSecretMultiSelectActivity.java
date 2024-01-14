package com.example.photo_gallery;


import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photo_gallery.Image;
import com.example.photo_gallery.ImageSelectAdapter;
import com.example.photo_gallery.ListTransInterface;
import com.example.photo_gallery.SlideShowActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ItemSecretMultiSelectActivity extends AppCompatActivity implements ListTransInterface {
    private ArrayList<String> myAlbum;
    private RecyclerView ryc_album;
    private RecyclerView ryc_list_album;
    private Intent intent;
    private String album_name;
    Toolbar toolbar_item_album;
    private ArrayList<Image> listImageSelected;
    private static int REQUEST_CODE_SLIDESHOW = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_album);
        intent = getIntent();
        setUpData();
        mappingControls();
        setData();
        setRyc();
        events();
    }

    private void setUpData() {
        listImageSelected = new ArrayList<>();
    }

    private void setRyc() {
        album_name = intent.getStringExtra("name_1");
        ryc_list_album.setLayoutManager(new GridLayoutManager(this, 3));
        ImageSelectAdapter imageSelectAdapter = new ImageSelectAdapter(ItemSecretMultiSelectActivity.this);
        List<Image> listImg = new ArrayList<>();
        for (int i = 0; i < myAlbum.size(); i++) {
            Image img = new Image();
            img.setThump(myAlbum.get(i));
            img.setPath(myAlbum.get(i));
            listImg.add(img);
        }
        imageSelectAdapter.setData(listImg);
        imageSelectAdapter.setListTransInterface(this);
        ryc_list_album.setAdapter(imageSelectAdapter);
    }


    private void events() {
        // Toolbar events
        toolbar_item_album.inflateMenu(R.menu.menu_top_multi_secret);
        toolbar_item_album.setTitle(album_name);

        // Show back button
        toolbar_item_album.setNavigationIcon(R.drawable.ic_back);
        toolbar_item_album.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Toolbar options
        toolbar_item_album.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.menuMultiDelete:
                        deleteEvents();
                        break;
                    case R.id.menuSlideshow:
                        slideShowEvents();
                        break;
                    case R.id.menu_restore:
                        restoreEvent();
                        break;
                }

                return true;
            }
        });
    }

    private void restoreEvent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ItemSecretMultiSelectActivity.this);

        builder.setTitle("Confirm");
        builder.setMessage("Do you want to hide/show this image?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                RestoreAsync restoreAsync = new RestoreAsync();
                restoreAsync.execute();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteEvents() {
        for (int i = 0; i < listImageSelected.size(); i++) {
            Uri targetUri = Uri.parse("file://" + listImageSelected.get(i).getPath());
            File file = new File(targetUri.getPath());
            if (file.exists()) {
                //GetAllPhotoFromGallery.removeImageFromAllImages(targetUri.getPath()); This is secret
                // I can't easily test it TODO/FIXME
                file.delete();
            }
        }
        setResult(RESULT_OK);
        finish();
    }

    private void slideShowEvents() {
        Intent intent = new Intent(ItemSecretMultiSelectActivity.this, SlideShowActivity.class);
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < listImageSelected.size(); i++) {
            list.add(listImageSelected.get(i).getThump());
        }
        intent.putStringArrayListExtra("data_slide", list);
        intent.putExtra("name", "Slide Show");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, REQUEST_CODE_SLIDESHOW);
    }

    private void setData() {
        myAlbum = intent.getStringArrayListExtra("data_1");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void mappingControls() {
        ryc_list_album = findViewById(R.id.ryc_list_album);
        toolbar_item_album = findViewById(R.id.toolbar_item_album);

    }

    @Override
    public void addList(Image img) {
        listImageSelected.add(img);
    }

    public void removeList(Image img) {
        listImageSelected.remove(img);
    }

    public class RestoreAsync extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            //String outputPath = Environment.getExternalStorageDirectory() + File.separator + "DCIM" + File.separator + "Restore";
            //File folder = new File(outputPath);
            //if (!folder.exists()) {
            //    folder.mkdir();
            //}
            //String[] paths = new String[listImageSelected.size()];
            //for (int i = 0; i < listImageSelected.size(); i++) {
            //    Image img = listImageSelected.get(i);
            //    File imgFile = new File(img.getPath());
            //    File desImgFile = new File(outputPath, imgFile.getName());
            //    imgFile.renameTo(desImgFile);
            //    imgFile.deleteOnExit();
            //    desImgFile.getPath();
            //    paths[i] = desImgFile.getPath();
            //}
            //MediaScannerConnection.scanFile(getApplicationContext(), paths, null, null);

            // lee
            List<Uri> urisToRemove = new ArrayList<>();

            for (Image image : listImageSelected) {
                Uri imageUri = Uri.fromFile(new File(image.getPath()));

                // Get the source file from the image path
                File sourceFile = new File(image.getPath());

                // Specify the destination folder relative to the "Trash" directory
                File destinationFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "");
                File destinationFile = new File(destinationFolder, sourceFile.getName());

                // Move the file to the destination folder if it exists
                if (sourceFile.exists() && sourceFile.renameTo(destinationFile)) {
                    urisToRemove.add(imageUri);
                }
                GetAllimagefromGallery.updateAllImagesFromUris(ItemSecretMultiSelectActivity.this, urisToRemove, true);
                GetAllimagefromGallery.refreshAllImages();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            setResult(RESULT_OK);

            finish();
        }
    }

}
