package com.example.photo_gallery;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileInputStream;

public class scantext extends Fragment {

    Button btnout,btncoppy;
    TextView text_data;
    Context context = null;
    Bitmap bitmap;
    Uri yourImageUri;
    public static scantext newInstance(){
        return new scantext();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        String imagePath = null;
        if (bundle != null) {
            imagePath = bundle.getString("imagePath");
        }
        RelativeLayout layoutscan= (RelativeLayout) inflater.inflate(R.layout.fragment_scantext,null);
        btnout=layoutscan.findViewById(R.id.btnoutscan);
        btncoppy=layoutscan.findViewById(R.id.btncoppy);
        text_data=layoutscan.findViewById(R.id.text_data);
        context=getActivity();
        bitmap= getBitmap(imagePath);
        getTextformImage(bitmap);
        btnout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction().remove(scantext.this).commit();
                    if (getActivity() instanceof PictureActivity) {
                        ((PictureActivity) getActivity()).onScantextFragmentClosed();
                    }
                }
            }
        });
        btncoppy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String scan_text=text_data.getText().toString();
                coppytoclipboard(scan_text);
            }
        });
        return layoutscan;
    }

    private void getTextformImage(Bitmap bitmap){
        TextRecognizer recognizer=new TextRecognizer.Builder(context).build();
        if(!recognizer.isOperational()){
            Toast.makeText(context,"Error Occcured!!!",Toast.LENGTH_SHORT);
        }
        else{
             Frame frame=new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlockSparseArray=recognizer.detect(frame);
            StringBuilder stringBuilder=new StringBuilder();
            for(int i=0;i<textBlockSparseArray.size();i++){
                TextBlock textBlock=textBlockSparseArray.valueAt(i);
                stringBuilder.append(textBlock.getValue());
                stringBuilder.append("\n");

            }
            text_data.setText(stringBuilder.toString());
        }
    }
    private void coppytoclipboard(String text){
        ClipboardManager clipboard=(ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip=ClipData.newPlainText("copied data",text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Copied to clipboard !", Toast.LENGTH_SHORT).show();
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
}