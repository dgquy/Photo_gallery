package com.example.photo_gallery;

import static android.app.PendingIntent.getActivity;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;

public class PictureActivity extends AppCompatActivity implements PictureInterface {

    private ViewPager viewPager_picture;
    private Toolbar toolbar_picture;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout frame_viewPager;
    private ArrayList<String> imageListThumb;
    private ArrayList<String> imageListPath;
    private Intent intent, intentsetting;
    private int pos;
    private SlideImageAdapter slideImageAdapter;
    private PictureInterface activityPicture;
    private String imgPath;
    private String imageName;
    private String thumb;
    private Bitmap imageBitmap;
    private String title, link, displayedLink, snippet;
    private RecyclerView resultsRV;
    private BottomSheetDialog bottomSheetDialog;
    private RecyclerView ryc_album;
    final SparseArray<Integer> menuIdTotoolBarPosition = new SparseArray<>();
    private static final int CHI_TIET_ID = 0;
    private static final int HINH_NEN_ID = 1;
    private static final int HINH_CHO_ID = 2;
    private static final int THEM_VAO_ALBUM = 3;
    RelativeLayout ln4;
    MySharedPreferences pref;
    public static Set<String> imageListFavor; //LE

    @Override
    protected void onResume() {
        super.onResume();
        imageListFavor = DataLocalManager.getListSet(); // LE
    }

    @Override
    public void actionShow(boolean flag) {
        showNavigation(flag);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        //Fix Uri file SDK link: https://stackoverflow.com/questions/48117511/exposed-beyond-app-through-clipdata-item-geturi?answertab=oldest#tab-top
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        menuIdTotoolBarPosition.put(R.id.chitiet, CHI_TIET_ID);
        menuIdTotoolBarPosition.put(R.id.hinhnen, HINH_NEN_ID);
        menuIdTotoolBarPosition.put(R.id.hinhcho, HINH_CHO_ID);
        menuIdTotoolBarPosition.put(R.id.addalbum, THEM_VAO_ALBUM);
        ln4 = findViewById(R.id.ln4);
        bottomNavigationView = findViewById(R.id.bottom_picture);
        pref = new MySharedPreferences(this);

        //Favorite image
        DataLocalManager.init(getApplicationContext()); // LE
        imageListFavor = DataLocalManager.getListSet();

        mappingControls();
        toolbar_picture.inflateMenu(R.menu.menu_top_picture);
        toolbar_picture.setNavigationIcon(R.drawable.ic_back);
        toolbar_picture.setTitleTextColor(0xFFFFFFFF);
        int mauchude = Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        toolbar_picture.setBackgroundColor(mauchude);
        boolean darkmode = Boolean.parseBoolean(pref.updateMeUsingSavedStateData("darkmode"));
        if (darkmode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        bottomNavigationView.setBackgroundColor(mauchude);
        toolBarEvents();
        events();
    }

    private void events() {
        setDataIntent();
        setUpSilder();
        bottomNavigationViewEvents(); //LE
    }


    private void showNavigation(boolean flag) {
        if (!flag) {
            bottomNavigationView.setVisibility(View.INVISIBLE);
            toolbar_picture.setVisibility(View.INVISIBLE);
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
            toolbar_picture.setVisibility(View.VISIBLE);
        }
    }

    private void toolBarEvents() {
        toolbar_picture.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar_picture.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                int menuConstant = -1;
                menuConstant = menuIdTotoolBarPosition.get(id);
                if (menuConstant != -1) {
                    switch (menuConstant) {
                        case CHI_TIET_ID:

                            break;
                        case HINH_NEN_ID:
                            setWallpaper(imgPath);
                            break;
                        case HINH_CHO_ID:
                            setLockScreenWallpaper(imgPath);
                            break;
                        case THEM_VAO_ALBUM:

                            break;
                    }
                }
                return true;
            }
        });
    }

    private void setWallpaper(final String imagePath) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                // Decode the image from the file path with reduced size
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;  // Adjust this value based on your requirements

                // Attempt to reuse the Bitmap
                options.inMutable = true;
                Bitmap reusableBitmap = BitmapFactory.decodeFile(imagePath, options);

                if (reusableBitmap == null) {
                    options.inMutable = false; // Create a new Bitmap if reusing fails
                    return BitmapFactory.decodeFile(imagePath, options);
                } else {
                    return reusableBitmap;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                    try {
                        // Set the image as the wallpaper
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                        Toast.makeText(getApplicationContext(), "Wallpaper set successfully", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Failed to set wallpaper", Toast.LENGTH_SHORT).show();
                    } finally {
                        // Ensure that you recycle the bitmap to free up memory
                        bitmap.recycle();
                    }
                }
            }
        }.execute();
    }


    private void setLockScreenWallpaper(final String imagePath) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                // Decode the image from the file path with reduced size
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;  // Adjust this value based on your requirements

                // Attempt to reuse the Bitmap
                options.inMutable = true;
                Bitmap reusableBitmap = BitmapFactory.decodeFile(imagePath, options);

                if (reusableBitmap == null) {
                    options.inMutable = false; // Create a new Bitmap if reusing fails
                    return BitmapFactory.decodeFile(imagePath, options);
                } else {
                    return reusableBitmap;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                    try {
                        // Set the image as the lock screen wallpaper
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                        Toast.makeText(getApplicationContext(), "Lock screen wallpaper set successfully", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Failed to set lock screen wallpaper", Toast.LENGTH_SHORT).show();
                    } finally {
                        // Ensure that you recycle the bitmap to free up memory
                        bitmap.recycle();
                    }
                }
            }
        }.execute();
    }

    private void setUpSilder() {

        slideImageAdapter = new SlideImageAdapter();
        slideImageAdapter.setData(imageListThumb, imageListPath);
        slideImageAdapter.setContext(getApplicationContext());
        slideImageAdapter.setPictureInterface(activityPicture);
        viewPager_picture.setAdapter(slideImageAdapter);
        viewPager_picture.setCurrentItem(pos);

        viewPager_picture.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                thumb = imageListThumb.get(position);
                imgPath = imageListPath.get(position);
                setTitleToolbar(thumb.substring(thumb.lastIndexOf('/') + 1));
                if (!check(imgPath)) {
                    bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.ic_heart);
                } else {
                    bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.ic_heart_red);
                }
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void setDataIntent() {
        intent = getIntent();
        imageListPath = intent.getStringArrayListExtra("data_list_path");
        imageListThumb = intent.getStringArrayListExtra("data_list_thumb");
        pos = intent.getIntExtra("pos", 0);
        activityPicture = (PictureInterface) this;
    }

    private void mappingControls() {
        viewPager_picture = findViewById(R.id.viewPager_picture);
        bottomNavigationView.setItemIconTintList(null); //LE: doi mau vector heart
        toolbar_picture = findViewById(R.id.toolbar_picture);
        frame_viewPager = findViewById(R.id.frame_viewPager);
    }

    public void setTitleToolbar(String imageName) {
        this.imageName = imageName;
        toolbar_picture.setTitle(imageName);
    }

    public void showDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void bottomNavigationViewEvents() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //Uri targetUri = Uri.parse("file://" + thumb);
                Uri targetUri = Uri.parse("file://" + imgPath);
                switch (item.getItemId()) {

                    // LE
                    case R.id.sharePic: { // LE: OK
                        if (thumb.contains("gif")) {
                            Intent share = new Intent(Intent.ACTION_SEND);
                            share.setType("image/*");
                            share.putExtra(Intent.EXTRA_STREAM, targetUri);
                            startActivity(Intent.createChooser(share, "Share this image to your friends!"));
                        } else {
                            Drawable mDrawable = Drawable.createFromPath(imgPath);
                            Bitmap mBitmap = ((BitmapDrawable) mDrawable).getBitmap();
                            String path = MediaStore.Images.Media.insertImage(getContentResolver(), mBitmap, thumb.substring(thumb.lastIndexOf('/') + 1), null);
                            Uri uri = Uri.parse(path);
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("image/*");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            startActivity(Intent.createChooser(shareIntent, "Share Image"));
                        }
                        break;
                    }

                    // LE
                    case R.id.starPic: { // LE: OK

                        if (!imageListFavor.add(imgPath)) {
                            imageListFavor.remove(imgPath);
                        }

                        DataLocalManager.setListImg(imageListFavor);
                        Toast.makeText(PictureActivity.this, imageListFavor.size() + "", Toast.LENGTH_SHORT).show();
                        if (!check(imgPath)) {
                            bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.ic_heart);
                        } else {
                            bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.ic_heart_red);
                        }
                        break;
                    }

                    // LE: cân tạo album delete -> chờ Dương
                    case R.id.deletePic: {
                        AlertDialog.Builder builder = new AlertDialog.Builder(PictureActivity.this);

                        builder.setTitle("Confirm");
                        builder.setMessage("Do you want to delete this image?");

                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                //Uri  targetUrile = Uri.parse("file://" + imgPath);
                                File file = new File(targetUri.getPath());

                                if (file.exists()) {
                                    if (ContextCompat.checkSelfPermission(PictureActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            == PackageManager.PERMISSION_GRANTED) {
                                        if (!file.canWrite()) {
                                            Toast.makeText(PictureActivity.this, "can't write", Toast.LENGTH_SHORT).show();
                                            file.setWritable(true);
                                        }
                                        if (file.canWrite()) {
                                            Toast.makeText(PictureActivity.this, "able write", Toast.LENGTH_SHORT).show();
                                        }
                                        if (file.delete()) {
                                            // Deletion successful
                                            GetAllimagefromGallery.removeImageFromAllImages(targetUri.getPath());
                                            Toast.makeText(PictureActivity.this, targetUri.getPath() + "Delete successfully: ", Toast.LENGTH_LONG).show();
                                        } else {
                                            // Error while deleting
                                            Toast.makeText(PictureActivity.this, targetUri.getPath() + "Delete failed: File in use or permission issue", Toast.LENGTH_LONG).show();
                                        }
                                        //int deletedRows = PictureActivity.this.getContentResolver().delete(targetUri, null, null);
                                        //if(deletedRows>0){
                                        //    Toast.makeText(PictureActivity.this, "xoa r", Toast.LENGTH_LONG).show();
                                        //}
                                    }
                                } else {
                                    // File doesn't exist
                                    Toast.makeText(PictureActivity.this, "File not exist: " + targetUri.getPath(), Toast.LENGTH_SHORT).show();
                                }
                                finish();
                                dialog.dismiss();
                            }
                        });

                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Do nothing
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();

                        break;
                    }
                }
                return true;
            }
        });
    }

    private boolean check(String imgPath) {
        for (String img : imageListFavor) {
            if (img.equals(imgPath)) {
                return true;
            }
        }
        return false;
    }
}