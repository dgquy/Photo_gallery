package com.example.photo_gallery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AlbumFragment extends Fragment implements IClickItemAlbumListener {
    private RecyclerView ryc_album;
    private List<Image> listImage;
    private View view;
    private androidx.appcompat.widget.Toolbar toolbar_album;
    private List<Album> listAlbum;
    private LinearLayout layout_bottom;
    private RecyclerView.Adapter mAdapter;
    private AlbumAdapter albumAdapter;
    private ProgressDialog progressDialog;


    // Dialog for Long Click
    private LinearLayout layout_bottom_delete;
    private LinearLayout layout_bottom_slide_show;
    private LinearLayout layout_bottom_edit_name;
    private TextView txtPath;
    private BottomSheetDialog bottomSheetDialog;


    private static int REQUEST_CODE_CREATE = 100;
    private static int REQUEST_CODE_DETAIL = 101;
    MySharedPreferences pref;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album, container, false);
        listImage = GetAllPhotoFromGallery.getAllImageFromGallery(view.getContext());
        toolbar_album = view.findViewById(R.id.toolbar_album);
        layout_bottom = view.findViewById(R.id.layout_bottom);
        progressDialog = new ProgressDialog(getContext());
        pref = new MySharedPreferences(getContext());
        toolbar_album.inflateMenu(R.menu.menu_top_album);
        toolbar_album.setTitle(getContext().getResources().getString(R.string.album));
        toolbar_album.setTitleTextColor(0xFFFFFFFF);
        int mauchude = Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        toolbar_album.setBackgroundColor(mauchude);
        toolBarEvents();
        // Ánh xạ rycycle album
        ryc_album = view.findViewById(R.id.ryc_album);
        //  eventsUpdateAlbum();
        setViewRyc();


        return view;
    }


    // Xử lí event của toolbar
    private void toolBarEvents() {
        toolbar_album.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.menuSearch) {
                    eventSearch(item);
                }
                //eventSearch(item);
                else if (id == R.id.menuAdd) {
                    openCreateAlbumActivity();

                }
                return true;
            }
        });
    }
// SEARCH

    private void eventSearch(@NonNull MenuItem item) {
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Type to search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                List<Album> lisAlbumSearch = new ArrayList<>();

                for (Album album : listAlbum) {
                    if (album.getName().toLowerCase().contains(s) || album.getName().contains(s)) {
                        lisAlbumSearch.add(album);
                    }
                }

                if (lisAlbumSearch.size() != 0) {
                    albumAdapter.setData(lisAlbumSearch);
                    synchronized (AlbumFragment.this) {
                        AlbumFragment.this.notifyAll();
                    }
                } else {
                    Toast.makeText(getContext(), "Searched album not found!", Toast.LENGTH_LONG).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
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
                albumAdapter.setData(listAlbum);
                synchronized (AlbumFragment.this) {
                    AlbumFragment.this.notifyAll();
                }
                return true;
            }
        });
    }


    private void eventsUpdateAlbum() {
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        pref = new MySharedPreferences(getContext());
        int mauchude = Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        toolbar_album.setBackgroundColor(mauchude);
        listImage = GetAllPhotoFromGallery.getAllImageFromGallery(view.getContext());
        listAlbum = getListAlbum(listImage);
        albumAdapter.setData(listAlbum);
        albumAdapter.notifyDataSetChanged();
    }

    private void setViewRyc() {

        setAdapter(listAlbum);

        //albumAdapter.setData(listAlbum);
    }

    public void setAdapter(List<Album> list) {
        albumAdapter = new AlbumAdapter(this, list, this);
        ryc_album.setAdapter(albumAdapter);
        albumAdapter.notifyDataSetChanged();
        ryc_album.setLayoutManager(new GridLayoutManager(view.getContext(), 2));

    }

    private void openCreateAlbumActivity() {
        Intent intent = new Intent(getActivity(), CreateAlbumActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, REQUEST_CODE_CREATE);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CREATE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.hasExtra("createdAlbum")) {

                Album createdAlbum = data.getParcelableExtra("createdAlbum");
                listAlbum.add(createdAlbum);
                ryc_album.setAdapter(new AlbumAdapter(this, listAlbum, this));
                albumAdapter.notifyItemInserted(listAlbum.size() - 1);
                onClickToDetail(listAlbum.size() - 1);

                // ryc_album.scrollToPosition(listAlbum.size()-1);
            }
            //eventsUpdateAlbum();
        }


        // ------------------------------------------------------------------------------------
        // Khi thêm ảnh vào một album và cập nhật lại list Album
        else if (requestCode == REQUEST_CODE_DETAIL && resultCode == Activity.RESULT_OK) {

            if (data != null) {
                Album updatedAlbum = data.getParcelableExtra("updatedAlbum");
                int isMoved = data.getIntExtra("moved", 0);


                int pos = -1;
                int pos1 = -1;

                for (int i = 0; i < listAlbum.size(); i++) {
                    if (listAlbum.get(i).getName().equals(updatedAlbum.getName())) {
                        pos = i;
                        listAlbum.set(i, updatedAlbum);
                        break;
                    }
                }

                // Duong
                Image thumbToChange = null;
                thumbToChange = data.getParcelableExtra("thumbnailToChange");
                if (thumbToChange != null)
                    listAlbum.get(pos).setImg(thumbToChange);

                if (isMoved == 0) {
                    Toast.makeText(getContext(), "KHONG CO MOVE", Toast.LENGTH_LONG).show();

                }
                if (isMoved == 1) {

                    String album_name_move_to = data.getStringExtra("album_name_move_to");
                    ArrayList<Image> move_list = data.getParcelableArrayListExtra("move_list");
                    for (int i = 0; i < listAlbum.size(); i++) {
                        if (listAlbum.get(i).getName().equals(album_name_move_to)) {
                            listAlbum.get(i).getList().addAll(move_list);
                            pos1 = i;
                            break;
                        }
                    }
                    Toast.makeText(getContext(), "NHAN DUOC MOVE TOI  " + album_name_move_to, Toast.LENGTH_LONG).show();
                    Toast.makeText(getContext(), "SIZE LIST TOI  " + move_list.size(), Toast.LENGTH_LONG).show();


                    for (int j = 0; j < move_list.size(); j++) {
                        File desImgFile = new File(move_list.get(j).getPath());
                        if (desImgFile.exists()) {
                            Toast.makeText(getContext(), "PATH FILE: " + j + "  : " + desImgFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        }

                    }

                    if (pos1 != -1) {

                        albumAdapter = new AlbumAdapter(this, listAlbum, this);
                        ryc_album.setAdapter(albumAdapter);
                        albumAdapter.notifyDataSetChanged();
                        albumAdapter.notifyItemChanged(pos, updatedAlbum);
                        albumAdapter.notifyItemChanged(pos1);
                        onClickToDetail(pos1);

                        /*ryc_album.setAdapter(new AlbumAdapter(this, listAlbum, this));
                        albumAdapter.notifyItemChanged(pos1);
                        onClickToDetail(pos1);*/


                    }

                }

                if (pos != -1) {
                    ryc_album.setAdapter(new AlbumAdapter(this, listAlbum, this));
                    albumAdapter.notifyItemChanged(pos, updatedAlbum);
                }


            }
        }
    }

    @NonNull
    private List<Album> getListAlbum(List<Image> listImage) {
        List<String> ref = new ArrayList<>();
        List<Album> listAlbum = new ArrayList<>();

        for (int i = 0; i < listImage.size(); i++) {
            String[] _array = listImage.get(i).getThump().split("/");
            String _pathFolder = listImage.get(i).getThump().substring(0, listImage.get(i).getThump().lastIndexOf("/"));
            String _name = _array[_array.length - 2];
            if(_name.equals("Trash"))
                continue;
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

    public void updateAlbumItem(Album updateAlbum) {
        Toast.makeText(getContext(), "updating is running....", Toast.LENGTH_LONG).show();

        if (listAlbum != null) {
            int pos = -1;
            for (int i = 0; i < listAlbum.size(); i++) {
                if (listAlbum.get(i).getName().equals(updateAlbum.getName())) {
                    //    yourList.set(position, modifiedObject); // Update the list directly
                    pos = i;
                    listAlbum.set(i, updateAlbum);
                    break;
                }
            }
            if (albumAdapter != null) {
                if (pos != -1) {
                    albumAdapter.updateAlbumList(listAlbum);
                    eventsUpdateAlbum();
                }

            }
        }
    }

    public void addNewAlbum(Album newAlbum) {
        if (listAlbum != null) {
            listAlbum.add(newAlbum);
            if (albumAdapter != null) {
                albumAdapter.notifyItemInserted(listAlbum.size() - 1);
                ryc_album.scrollToPosition(listAlbum.size() - 1);
                //eventsUpdateAlbum();
            }
        }
    }

    @Override
    public void onClickItemAlbum(int pos) {
        onClickToDetail(pos);
    }

    private void onClickToDetail(int pos) {

        Intent intent = new Intent(getContext(), ItemAlbumActivity.class);
        // Truyền album đang chọn qua ItemAlbumActivity
        intent.putExtra("selectedAlbum", listAlbum.get(pos));
        intent.putExtra("ok", 1);
        startActivityForResult(intent, REQUEST_CODE_DETAIL);

    }

    @Override
    public void onLongClickItemAlbum(int pos) {

        openBottomDialog();
        txtPath.setText(listAlbum.get(pos).getPathFolder());
        txtPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), listAlbum.get(pos).getPathFolder(), Toast.LENGTH_SHORT).show();
                bottomSheetDialog.cancel();
            }
        });

        layout_bottom_slide_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideShowEvents(listAlbum.get(pos));

            }
        });
        layout_bottom_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteEvents(pos);
                Toast.makeText(getContext(), "Deleted album", Toast.LENGTH_SHORT).show();


            }
        });
        layout_bottom_edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editNameEvents(pos);
            }
        });

    }

    private void openBottomDialog() {
        View viewDialog = LayoutInflater.from(getContext()).inflate(R.layout.layout_bottom_sheet_album, null);
        layout_bottom_slide_show = viewDialog.findViewById(R.id.layout_bottom_slide_show);
        layout_bottom_delete = viewDialog.findViewById(R.id.layout_bottom_delete);
        layout_bottom_edit_name = viewDialog.findViewById(R.id.layout_bottom_edit_name);
        txtPath = viewDialog.findViewById(R.id.txtPath);


        bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(viewDialog);
        bottomSheetDialog.show();
    }

    private void deleteEvents(int pos) {
        Album ref = listAlbum.get(pos);
        // Delete images in the ref album's listImage
        for (Image image : ref.getList()) {
            File imageFile = new File(image.getPath());
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
        // Delete the folder associated with the ref album
        File albumFolder = new File(ref.getPathFolder());
        if (albumFolder.exists() && albumFolder.isDirectory()) {
            File[] files = albumFolder.listFiles();


            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            albumFolder.delete();
        }

        // Remove the album from the list and notify the adapter
        listAlbum.remove(pos);
        ryc_album.setAdapter(new AlbumAdapter(this, listAlbum, this));
        albumAdapter.notifyDataSetChanged();
        //albumAdapter.notifyItemRemoved(pos);

        // Dismiss the bottom sheet dialog
        bottomSheetDialog.cancel();
    }

    private void editNameEvents(int pos) {
        Album ref = listAlbum.get(pos);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.layout_edit_name_album, null);
        alertDialogBuilder.setView(dialogView);

        EditText editTextAlbumName = dialogView.findViewById(R.id.editTextAlbumName);
        Button buttonSave = dialogView.findViewById(R.id.buttonSave);

        // Set the current album name in the EditText
        editTextAlbumName.setText(ref.getName());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newAlbumName = editTextAlbumName.getText().toString().trim();
                List<Image> listImage = ref.getList();
                if (!newAlbumName.isEmpty()) {

                    final String albumPath = Environment.getExternalStorageDirectory() + File.separator + "Pictures" + File.separator + newAlbumName;
                    File directory = new File(albumPath);
                    if (!directory.exists()) {
                        directory.mkdirs();
                        Log.e("File-no-exist", directory.getPath());
                    }

                    ArrayList<Image> newListImage = new ArrayList<>();
                    String[] paths = new String[listImage.size()];

                    for (int i = 0; i < listImage.size(); i++) {
                        Image img = listImage.get(i);
                        String imagePath = img.getPath();
                        String imageFileName = newAlbumName + "_image_" + i + ".jpg";

                        // Create a copy of the image and save it to the album directory
                        String destinationPath = albumPath + File.separator + imageFileName;
                        copyImage(imagePath, destinationPath);

                        // Add the copied image to the new list
                        Image copiedImage = new Image(destinationPath, img.getThump(), img.getDateTaken());
                        newListImage.add(copiedImage);

                        paths[i] = destinationPath;
                    }

                    MediaScannerConnection.scanFile(getContext().getApplicationContext(), paths, null, null);
                    Album newAlbum = new Album(newAlbumName, newListImage, albumPath);
                    deleteEvents(pos);
                    listAlbum.add(pos, newAlbum);
                    changge(listAlbum);
                    // albumAdapter.notifyItemChanged(pos);
                    alertDialog.dismiss();
                    //return newAlbum;

                } else {
                    Toast.makeText(getContext(), "Please enter a valid name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void slideShowEvents(Album ref) {
        Intent intent = new Intent(getContext(), SlideShowActivity.class);
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < ref.getList().size(); i++) {
            list.add(ref.getList().get(i).getThump());
        }
        intent.putStringArrayListExtra("data_slide", list);
        intent.putExtra("name", "Slide Show");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void changge(List<Album> list) {
        ryc_album.setAdapter(new AlbumAdapter(this, list, this));
        albumAdapter.notifyDataSetChanged();
    }

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

    // ASYNCTASK
    public class MyAsyncTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            listImage = GetAllPhotoFromGallery.getAllImageFromGallery(view.getContext());
            listAlbum = getListAlbum(listImage);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            albumAdapter.setData(listAlbum);
            progressDialog.cancel();
        }
    }
}
