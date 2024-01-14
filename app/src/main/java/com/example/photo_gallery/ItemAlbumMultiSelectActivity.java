package com.example.photo_gallery;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemAlbumMultiSelectActivity extends AppCompatActivity implements ListTransInterface, SubInterface {
    private Album currMultiAlbum;
    private ArrayList<String> myAlbum;
    private RecyclerView ryc_album;
    private RecyclerView ryc_list_album;
    private Intent intent;
    private String album_name;
    private String album_name_move_to;
    private String path_folder;
    Toolbar toolbar_item_album;

    private BottomSheetDialog bottomSheetDialog;
    private ArrayList<Image> listImageSelected;
    MySharedPreferences pref;
    private static int REQUEST_CODE_SLIDESHOW = 101;
    private static int REQUEST_CODE_GIF = 102;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_album);
        currMultiAlbum= getIntent().getParcelableExtra("multiAlbum");

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
        album_name = currMultiAlbum.getName();
        path_folder = currMultiAlbum.getPathFolder();
        ryc_list_album.setLayoutManager(new GridLayoutManager(this, 3));
        ImageSelectAdapter imageSelectAdapter = new ImageSelectAdapter(ItemAlbumMultiSelectActivity.this);
        List<Image> listImg = new ArrayList<>();
        for(int i =0 ; i< myAlbum.size();i++) {
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
        toolbar_item_album.inflateMenu(R.menu.menu_top_multi_album);
        toolbar_item_album.setTitle(album_name);
        pref=new MySharedPreferences(ItemAlbumMultiSelectActivity.this);
        int mauchude= Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        toolbar_item_album.setBackgroundColor(mauchude);
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
                    case R.id.menuSlideshow:
                        slideShowEvents();
                        break;
                    case R.id.menu_move_image:
                        moveEvent();
                        break;
                    case R.id.menuGif:
                        gifEvents();
                        break;
                }

                return true;
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GIF && resultCode==RESULT_OK) {
                if (data != null) {
                    Image gifImg = (Image) data.getSerializableExtra("gifImage");
                    Intent resultIntent = new Intent();

                    resultIntent.putExtra("result", gifImg);
                    resultIntent.putExtra("move", 2);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }

        }
    }
    private void gifEvents() {
        Toast.makeText(getApplicationContext(),"App sẽ loại bỏ ảnh gif có trong danh sách chọn", Toast.LENGTH_SHORT).show();
        ArrayList<String> list_send_gif = new ArrayList<>();
        for(int i =0;i<listImageSelected.size();i++) {
            if(!listImageSelected.get(i).getPath().contains(".gif"))
                list_send_gif.add(listImageSelected.get(i).getPath());
        }
        if(list_send_gif.size()!=0) {
            inputDialog(list_send_gif);

        }
        else
            Toast.makeText(getApplicationContext(),"Danh sách trống", Toast.LENGTH_SHORT).show();
    }

    private void inputDialog(ArrayList<String> list_send_gif) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ItemAlbumMultiSelectActivity.this);
        alertDialog.setTitle("Nhập khoảng delay");
        alertDialog.setMessage("Delay: ");
        final EditText input = new EditText(ItemAlbumMultiSelectActivity.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(!TextUtils.isEmpty(input.getText())) {
                    Intent intent_gif = new Intent(ItemAlbumMultiSelectActivity.this, GifShowActivity.class);
                    intent_gif.putExtra("delay", Integer.valueOf(input.getText().toString()));
                    intent_gif.putStringArrayListExtra("list", list_send_gif);
                    intent_gif.putExtra("albumName", album_name);
                    startActivityForResult(intent_gif, REQUEST_CODE_GIF);
                    dialogInterface.cancel();
                }
                else
                    Toast.makeText(getApplicationContext(),"Mời nhập đầy đủ", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();
    }

    private void moveEvent() {
        openBottomDialog();
    }
    private void openBottomDialog() {
        View viewDialog = LayoutInflater.from(ItemAlbumMultiSelectActivity.this).inflate(R.layout.layout_bottom_sheet_add_to_album, null);
        ryc_album = viewDialog.findViewById(R.id.ryc_album);
        ryc_album.setLayoutManager(new GridLayoutManager(this, 2));

        bottomSheetDialog = new BottomSheetDialog(ItemAlbumMultiSelectActivity.this);
        bottomSheetDialog.setContentView(viewDialog);
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();

    }
    private void deleteEvents() {
        for(int i=0;i<listImageSelected.size();i++) {
            Uri targetUri = Uri.parse("file://" + listImageSelected.get(i).getPath());
            File file = new File(targetUri.getPath());
            if (file.exists()){
                GetAllPhotoFromGallery.removeImageFromAllImages(targetUri.getPath());
                file.delete();
            }
            if(i==listImageSelected.size()-1) {
                setResult(RESULT_OK);
                finish();
            };
        }
    }
    private void slideShowEvents() {
        Intent intent = new Intent(ItemAlbumMultiSelectActivity.this, SlideShowActivity.class);
        ArrayList<String> list = new ArrayList<>();
        for(int i=0;i<listImageSelected.size();i++) {
            list.add(listImageSelected.get(i).getThump());
        }
        intent.putStringArrayListExtra("data_slide", list);
        intent.putExtra("name", "Slide Show");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setData() {
        myAlbum= new ArrayList<>();
        for(int i=0; i<currMultiAlbum.getList().size(); i++){
            myAlbum.add(currMultiAlbum.getList().get(i).getThump());
        }
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

    @Override
    public void add(Album album) {
        album_name_move_to= album.getName();
        AddAlbumAsync addAlbumAsync = new AddAlbumAsync();
        addAlbumAsync.setAlbum(album);
        addAlbumAsync.execute();
    }

    public class MyAsyncTask extends AsyncTask<Void, Integer, Void> {
        private AlbumSheetAdapter albumSheetAdapter;
        private List<Album> listAlbum;
        @Override
        protected Void doInBackground(Void... voids) {
            List<Image> listImage = GetAllPhotoFromGallery.getAllImageFromGallery(ItemAlbumMultiSelectActivity.this);
            listAlbum = getListAlbum(listImage);
            if(path_folder!=null)
                for(int i =0;i<listAlbum.size();i++) {
                    if(path_folder.equals(listAlbum.get(i).getPathFolder())) {
                        listAlbum.remove(i);
                        break;
                    }
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            albumSheetAdapter = new AlbumSheetAdapter(listAlbum, ItemAlbumMultiSelectActivity.this);
            albumSheetAdapter.setSubInterface(ItemAlbumMultiSelectActivity.this);
            ryc_album.setAdapter(albumSheetAdapter);
            bottomSheetDialog.show();
        }
        @NonNull
        private List<Album> getListAlbum(List<Image> listImage) {
            List<String> ref = new ArrayList<>();
            List<Album> listAlbum = new ArrayList<>();

            for (int i = 0; i < listImage.size(); i++) {
                String[] _array = listImage.get(i).getThump().split("/");
                String _pathFolder = listImage.get(i).getThump().substring(0, listImage.get(i).getThump().lastIndexOf("/"));
                String _name = _array[_array.length - 2];
                if (!ref.contains(_pathFolder)) {
                    ref.add(_pathFolder);
                    Album token = new Album(listImage.get(i), _name);
                    token.setPathFolder(_pathFolder);
                    token.addItem(listImage.get(i));
                    listAlbum.add(token);
                } else {
                    listAlbum.get(ref.indexOf(_pathFolder)).addItem(listImage.get(i));
                }
            }

            return listAlbum;
        }
    }
    public class AddAlbumAsync extends AsyncTask<Void, Integer, Void> {
        Album album;

        ArrayList<String> list;
        ArrayList<Image> move_list;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            list = new ArrayList<>();
            move_list= new ArrayList<>();
        }
        public void setAlbum(Album album) {
            this.album = album;
        }








        @Override
        protected Void doInBackground(Void... voids) {
            String[] paths = new String[listImageSelected.size()];
            int i =0;
            for (Image img :listImageSelected){
                File imgFile = new File(img.getPath());
                File desImgFile = new File(album.getPathFolder(),album.getName()+"_"+imgFile.getName());
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
                    list.add(imgFile.getPath());
                    move_list.add(new Image(desImgFile.getPath(), desImgFile.getPath(), img.getDateTaken()));
                    // Xóa tệp gốc sau khi sao chép
                    Uri targetUri = Uri.parse("file://" + img.getPath());
                    File file = new File(targetUri.getPath());
                    if (file.exists()){
                        GetAllPhotoFromGallery.removeImageFromAllImages(targetUri.getPath());
                        file.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                i++;
            }
            MediaScannerConnection.scanFile(getApplicationContext(),paths, null, null);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            bottomSheetDialog.cancel();
            Intent resultIntent = new Intent();

            resultIntent.putStringArrayListExtra("list_result", list);
            resultIntent.putParcelableArrayListExtra("move_list", move_list);
            resultIntent.putExtra("album_name_move_to", album_name_move_to);
            resultIntent.putExtra("move", 1);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}