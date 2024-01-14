package com.example.photo_gallery;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class Scan_QR_Acitivity extends AppCompatActivity {
    ImageButton btnsave,btnout;
    Button btnscan,btnopen;
    TextView txtresult;
    String strresult="";
    MySharedPreferences pref;
    LinearLayout linearLayout;

    ActivityResultLauncher<ScanOptions> barLauncher=registerForActivityResult(new ScanContract(),result->{
        if(result.getContents()==null){
            Toast.makeText(this,"Cancelled",Toast.LENGTH_SHORT).show();
        }
        else{
            strresult=result.getContents();
            txtresult.setText(strresult);
            if (isUrl(strresult)) {
                btnopen.setVisibility(View.VISIBLE);
                btnopen.setOnClickListener(v -> openWebsite(strresult));
            } else {
                btnopen.setVisibility(View.GONE);
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr_acitivity);
        btnout=findViewById(R.id.btnout);
        btnsave=findViewById(R.id.btnsave);
        txtresult=findViewById(R.id.textResult);
        btnscan=findViewById(R.id.btnScan1);
        btnopen=findViewById(R.id.btnOpenWebsite);
        linearLayout= findViewById(R.id.layout_result);
        pref=new MySharedPreferences(Scan_QR_Acitivity.this);
        int mauchude= Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        linearLayout.setBackgroundColor(mauchude);
        btnscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showcamera();
            }
        });
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coppytoclipboard(strresult);
            }
        });
        btnout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private boolean isUrl(String text) {
        return text != null && (text.startsWith("http://") || text.startsWith("https://"));
    }

    private void openWebsite(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }


    private void coppytoclipboard(String text){
        ClipboardManager clipboard=(ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip=ClipData.newPlainText("copied data",text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Copied to clipboard !", Toast.LENGTH_SHORT).show();
    }
    private void showcamera(){
        ScanOptions scanOptions=new ScanOptions();
        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanOptions.setPrompt("Scan QR code");
        scanOptions.setCameraId(0);
        scanOptions.setBeepEnabled(false);
        scanOptions.setBarcodeImageEnabled(true);
        scanOptions.setOrientationLocked(false);
        barLauncher.launch(scanOptions);
    }

}