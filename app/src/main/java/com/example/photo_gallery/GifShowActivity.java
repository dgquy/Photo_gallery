package com.example.photo_gallery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class GifShowActivity extends AppCompatActivity {
    private ImageView img_back_slide_show;
    private ImageButton btnSave;
    private ImageView view;
    private Intent intent;
    private ArrayList<String> list;
    private ArrayList<Bitmap> bitmaps;
    private ProgressDialog progressDialog;
    private byte[] saveData;
    private int delay;
    private String album_name;
    LinearLayout linearLayout;
    MySharedPreferences pref;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_show);
        progressDialog = new ProgressDialog(this);
        mappingControls();
        setUpDataView();
        events();
    }

    private void events() {
        img_back_slide_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveGif();
            }
        });
        Glide.with(getApplicationContext()).load(saveData).into(view);
    }

    public byte[] generateGIF() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(bos);
        encoder.setDelay(delay);
        for (Bitmap bitmap : bitmaps) {
            encoder.addFrame(bitmap);
        }
        encoder.finish();
        return bos.toByteArray();
    }

    public void saveGif() {
        try {
            progressDialog.show();
            Random generator = new Random();
            String ref = String.valueOf(generator.nextInt());

            String[] paths = new String[1];
            paths[0] = Environment.getExternalStorageDirectory()
                    + File.separator+"Pictures"+ File.separator+album_name+File.separator+"gif" + ref+".gif";

            FileOutputStream outStream = new FileOutputStream(paths[0]);
            outStream.write(saveData);
            outStream.close();
            MediaScannerConnection.scanFile(getApplicationContext(),paths, null, null);
            Calendar calendar = Calendar.getInstance();
            Date currentDate = calendar.getTime();

            // Chuyển ngày thành chuỗi (String)
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = formatter.format(currentDate);

            Image gifImg= new Image(paths[0], paths[0], formattedDate);
            Intent resultIntent= new Intent();
            resultIntent.putExtra("gifImage",gifImg );
            setResult(Activity.RESULT_OK, resultIntent);


            progressDialog.cancel();
            Toast.makeText(getApplicationContext(), "Lưu file thành công", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    private void setUpDataView() {
        intent = getIntent();
        list = intent.getStringArrayListExtra("list");
        delay = intent.getIntExtra("delay", 100);
        album_name= intent.getStringExtra("albumName");
        bitmaps = new ArrayList<>();
        for(String token: list) {
            Bitmap myBitmap = BitmapFactory.decodeFile(token);
            bitmaps.add(myBitmap);
        }
        saveData = generateGIF();
    }
    private void mappingControls() {
        img_back_slide_show = findViewById(R.id.img_back_slide_show);
        view = findViewById(R.id.view);
        btnSave = findViewById(R.id.btnSave);
        linearLayout= findViewById(R.id.header);
        pref=new MySharedPreferences(GifShowActivity.this);
        int mauchude= Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        linearLayout.setBackgroundColor(mauchude);
    }
}