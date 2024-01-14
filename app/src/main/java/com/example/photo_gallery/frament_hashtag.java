package com.example.photo_gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.photo_gallery.ml.MobilenetV110224Quant;
import com.google.android.material.textfield.TextInputLayout;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class frament_hashtag extends Fragment {
    EditText enterhashtag;
    TextInputLayout enterField;
    Context context = null;
    Button add;
    Button exit;
    Button autohash;
    ListView listhashtag;
    Adapter_hashtag myadapter;
    ArrayList<Hashtag> mylist;
    TextView textView;
    int n=0;
    SQLdemo mydatabase;
    PictureActivity activity;
    Image image;
    Bitmap bitmap;
    String[] labels;
    int cnt=0;
    public static frament_hashtag newInstance() {
        return new frament_hashtag();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        RelativeLayout layouthashtag=(RelativeLayout)inflater.inflate(R.layout.layout_frament_hashtag,null);
        textView=layouthashtag.findViewById(R.id.textViewhashtag);
        add=layouthashtag.findViewById(R.id.btnCreateHashtag);
        exit=layouthashtag.findViewById(R.id.btnexit);
        autohash=layouthashtag.findViewById(R.id.btnautohash);
        enterhashtag=layouthashtag.findViewById(R.id.enterHashtag);
        listhashtag=layouthashtag.findViewById(R.id.listhashtag);
        mylist=new ArrayList<>();
        enterField=layouthashtag.findViewById(R.id.enterField);
        context=getActivity();
        activity= (PictureActivity) getActivity();
        image=activity.getImage();
        String path=image.getPath();
        mydatabase=new SQLdemo(context);
        mylist=mydatabase.readDatatoList(path);
        n=mylist.size();
        updateTextView(mylist.size());
        myadapter=new Adapter_hashtag(context, R.layout.layout_item_hashtag,mylist);
        listhashtag.setAdapter(myadapter);
        labels=new String[1001];
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("labels.txt")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                labels[cnt]=line;
                cnt++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            context = getActivity(); // use this reference to invoke main callbacks
        } catch (IllegalStateException e) {
            throw new IllegalStateException("MainActivity must implement callbacks");
        }
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kết thúc Fragment khi nhấn nút exit
                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction().remove(frament_hashtag.this).commit();
                }
            }
        });
        autohash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MobilenetV110224Quant model = MobilenetV110224Quant.newInstance(context);

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.UINT8);

                    bitmap=getBitmap(image.getPath());
                    Bitmap resized=Bitmap.createScaledBitmap(bitmap,224,224,true);
                    inputFeature0.loadBuffer(TensorImage.fromBitmap(resized).getBuffer());

                    // Runs model inference and gets result.
                    MobilenetV110224Quant.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                    String autohashtmp=labels[getMax(outputFeature0.getFloatArray())];
                    if(!mydatabase.checkExist(autohashtmp, image.getPath())){
                        n++;
                        mylist.add(new Hashtag(autohashtmp,image));
                        myadapter=new Adapter_hashtag(context, R.layout.layout_item_hashtag,mylist);
                        mydatabase.insertdata(image,autohashtmp);
                        listhashtag.setAdapter(myadapter);
                        textView.setText("");
                    }else{
                        enterField.setError("Tính năng này đã được sử dụng trước đó!");
                    }
                    // Releases model resources if no longer used.
                    model.close();
                } catch (IOException e) {
                    // TODO Handle the exception
                }
            }
        });

        enterhashtag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Không cần xử lý trước khi thay đổi văn bản
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Không cần xử lý khi văn bản thay đổi
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Đặt lại màu nền và thông báo lỗi khi người dùng bắt đầu nhập lại
                enterField.setError(null);
                enterField.setBoxBackgroundColor(0xFFFFFFFF);
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text="";
                text = String.valueOf(enterhashtag.getText());
                enterhashtag.setText("");
                if (mydatabase.checkExist(text, image.getPath())) {
                    enterField.setError("Hashtag đã tồn tại");
                }
                else if (containsSpecialCharacters(text)){
                    enterField.setError("Hashtag không hợp lệ");
                }
                else{
                    n++;
                    mylist.add(new Hashtag(text,image));
                    myadapter=new Adapter_hashtag(context, R.layout.layout_item_hashtag,mylist);
                    mydatabase.insertdata(image,text);
                    listhashtag.setAdapter(myadapter);
                    textView.setText("");
                }
            }
        });
        return layouthashtag;
    }

    int getMax(float[] arr){
        int max=0;
        for(int i=0;i<arr.length;i++){
            if(arr[i]>arr[max])max=i;
        }
        return max;
    }
    public Bitmap getBitmap(String path) {
        Bitmap bitmap = null;
        try {
            File f = new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    public static boolean containsSpecialCharacters(String text) {
        // Biểu thức chính quy để kiểm tra ký tự đặc biệt
        String specialCharacterPattern = "[^a-zA-Z0-9]";

        // Tạo Pattern
        Pattern pattern = Pattern.compile(specialCharacterPattern);

        // Tạo Matcher
        Matcher matcher = pattern.matcher(text);

        // Kiểm tra xem có ký tự đặc biệt hay không
        return matcher.find();
    }
    public void updateTextView(int size) {
        if (size==0) {
            textView.setText("Không có hashtag nào");
        } else {
            textView.setText("");
        }
    }
}