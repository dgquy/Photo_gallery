package com.example.photo_gallery;


import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PhotoFragment extends Fragment {
    private RecyclerView recyclerView;
    MainActivity main;
    private CategoryAdapter categoryAdapter;
    private androidx.appcompat.widget.Toolbar toolbar_photo;
    private Boolean flag = false;
    private List<Category> listImg;
    private List<Image> imageList;
    private ArrayList<String> list_searchA;
    private static int REQUEST_CODE_MULTI = 40;
    final SparseArray<Integer> menuIdTotoolBarPosition = new SparseArray<>();
    private Context context;
    private static final int MENU_SEARCH_ID = 0;
    private static final int MENU_CAMERA_ID = 1;
    private static final int MENU_CHOOSE_ID = 2;
    private static final int MENU_SETTING_ID = 3;
    private static final int MENU_SORT = 4;
    private static final int MENU_SORT_BY_DATE = 5;
    private static final int MENU_SORT_BY_MONTH = 6;
    private static final int MENU_QR_SCAN = 7;
    MySharedPreferences pref;

    public static Set<String> imageListFavor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        context = view.getContext();
        recyclerView = view.findViewById(R.id.rcv_category);
        toolbar_photo = view.findViewById(R.id.toolbar_photo);
        menuIdTotoolBarPosition.put(R.id.menuSearch, MENU_SEARCH_ID);
        menuIdTotoolBarPosition.put(R.id.menuCamera, MENU_CAMERA_ID);
        menuIdTotoolBarPosition.put(R.id.menuSettings, MENU_SETTING_ID);
        /////Sort by date or month - QD
        menuIdTotoolBarPosition.put(R.id.sort_list, MENU_SORT);
        menuIdTotoolBarPosition.put(R.id.sort_date, MENU_SORT_BY_DATE);
        menuIdTotoolBarPosition.put(R.id.sort_month, MENU_SORT_BY_MONTH);
        menuIdTotoolBarPosition.put(R.id.QRscan, MENU_QR_SCAN);

        main = (MainActivity) getActivity();
        pref = new MySharedPreferences(getContext());
        toolbar_photo.inflateMenu(R.menu.menu_top);
        toolbar_photo.setTitle(getContext().getResources().getString(R.string.photo));
        toolbar_photo.setTitleTextColor(0xFFFFFFFF);
        int mauchude = Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        toolbar_photo.setBackgroundColor(mauchude);
        toolBarEvents();
        setRyc(getListCategory());
        return view;
    }

    private void toolBarEvents() {
        toolbar_photo.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                int menuConstant = -1;
                menuConstant = menuIdTotoolBarPosition.get(id);
                if (menuConstant != -1) {
                    // Sử dụng switch để xử lý chức năng tương ứng với hằng số
                    switch (menuConstant) {
                        case MENU_SEARCH_ID:
                            searchImage(item);
                            break;
                        case MENU_CAMERA_ID:
                            takenImg();
                            break;

                        case MENU_SETTING_ID:
                            Intent intent = new Intent(getContext(), SettingsActivity.class);
                            startActivity(intent);
                            break;
                        case MENU_SORT_BY_DATE:
                            sortByDate();
                            break;
                        case MENU_SORT_BY_MONTH:
                            sortByMonth();
                            break;
                        case MENU_QR_SCAN:
                            Intent intent1 = new Intent(getContext(), Scan_QR_Acitivity.class);
                            startActivity(intent1);
                            break;
                    }
                }
                return true;
            }

        });

    }

    public void sortByDate() {
        setRyc(getListCategory());
    }

    // Sort by month
    public void sortByMonth() {
        setRyc(getListImageByMonth());
    }

    private List<Category> getListImageByMonth() {
        List<Category> listCategory = new ArrayList<>();
        List<Image> listImg = GetAllimagefromGallery.getAllImageFromGallery(getContext());

        int categoryCount = 0;

        try {
            listCategory.add(new Category(parseDateToMonth(listImg.get(0).getDateTaken()), new ArrayList<>()));
            listCategory.get(categoryCount).addListGirl(imageList.get(0));
            for (int i = 1; i < imageList.size(); i++) {
                if (!parseDateToMonth(imageList.get(i).getDateTaken()).equals(parseDateToMonth(imageList.get(i - 1).getDateTaken()))) {
                    listCategory.add(new Category(parseDateToMonth(imageList.get(i).getDateTaken()), new ArrayList<>()));
                    categoryCount++;
                }
                listCategory.get(categoryCount).addListGirl(imageList.get(i));
            }
            return listCategory;
        } catch (Exception e) {
            return null;
        }
    }

    private String parseDateToMonth(String dateTaken) {
        int pos = 0;
        for (int i = 0; i < dateTaken.length(); i++) {
            if (dateTaken.charAt(i) == '-') {
                pos = i;
                break;
            }
        }

        return dateTaken.substring(pos + 1, dateTaken.length());
    }

    private void setRyc(List<Category> listCategory) {
        categoryAdapter = new CategoryAdapter(getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        categoryAdapter.setData(listCategory);
        recyclerView.setAdapter(categoryAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();
        categoryAdapter.setData(listImg);
        // Đọc giá trị màu từ SharedPreferences
        int mauchude = Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        // Cập nhật màu chủ đề cho toolbar_photo
        toolbar_photo.setBackgroundColor(mauchude);
        toolBarEvents();
    }

    @Override
    public void onStop() {
        super.onStop();
        flag = true;
    }

    //Camera
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int PICTURE_RESULT = 1;
    private Uri imageUri;
    private String imageurl;
    private Bitmap thumbnail;

    private void takenImg() {
        int permissionCheckCamera = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);

        if (permissionCheckCamera != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }

    }

    void requestCamera() {
        int permissionCheckCamera = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);

        if (permissionCheckCamera != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String timestamp = sdf.format(new Date());
        String imageFileName = "IMG_" + timestamp + ".jpg";

        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getActivity().getApplicationContext().getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, PICTURE_RESULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getActivity(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case PICTURE_RESULT:
                if (requestCode == PICTURE_RESULT && resultCode == Activity.RESULT_OK) {
                    try {
                        thumbnail = MediaStore.Images.Media.getBitmap(
                                getActivity().getApplicationContext().getContentResolver(), imageUri);

                        imageurl = getRealPathFromURI(imageUri);
                        GetAllimagefromGallery.updateAllImagesFromUris(this.context, Collections.singletonList(imageUri), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
        if (requestCode == REQUEST_CODE_MULTI && resultCode == Activity.RESULT_OK ) {
            MyAsyncTask myAsyncTask = new MyAsyncTask();
            myAsyncTask.execute();
        }
    }

    private void searchImage(@NonNull MenuItem item) {
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Enter a hashtag to search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String name) {
                SQLdemo database = new SQLdemo(getContext());
                List<Hashtag> listHashtag = database.getListHashtag();

                List<Image> imgList = new ArrayList<>();
                for (Hashtag _hashtag : listHashtag) {
                    if (_hashtag.getHashtag().toLowerCase().contains(name) || _hashtag.getHashtag().contains(name)) {
                        imgList.add(_hashtag.getImage());
                    }
                }

                if (imgList != null) {
                    List<Category> listResult = new ArrayList<>();
                    listResult.add(new Category("", imgList));
                    categoryAdapter.setData(listResult);
                } else {
                    Toast.makeText(main, "Searched image(s) not found!", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                categoryAdapter.setData(listImg);
                synchronized (PhotoFragment.this) {
                    PhotoFragment.this.notifyAll();
                }
                return true;
            }
        });
    }


    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @NonNull
    private List<Category> getListCategory() {
        List<Category> categoryList = new ArrayList<>();
        int categoryCount = 0;
        imageList = GetAllimagefromGallery.getAllImageFromGallery(getContext());

        try {
            categoryList.add(new Category(imageList.get(0).getDateTaken(), new ArrayList<>()));
            categoryList.get(categoryCount).addListGirl(imageList.get(0));
            for (int i = 1; i < imageList.size(); i++) {
                if (!imageList.get(i).getDateTaken().equals(imageList.get(i - 1).getDateTaken())) {
                    categoryList.add(new Category(imageList.get(i).getDateTaken(), new ArrayList<>()));
                    categoryCount++;
                }
                categoryList.get(categoryCount).addListGirl(imageList.get(i));
            }
            return categoryList;
        } catch (Exception e) {
            return null;
        }

    }

    public class MyAsyncTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            listImg = getListCategory();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            categoryAdapter.setData(listImg);
        }
    }
}

