package com.example.photo_gallery;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;

public class SlideShowActivity extends AppCompatActivity {

    private SliderView sliderView;
    private ImageView img_back_slide_show;
    private Toolbar toolbar_slide;
    private List<Image> imageList;
    private Intent intent;
    Spinner spinner;
    private int currentSpeed = 3000;
    private List<SliderAnimations> effect = new ArrayList<SliderAnimations>();
    LinearLayout linearLayout;
    MySharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_show);
        intent = getIntent();
        mappingControls();
        event();

    }

    private void event() {
        addListAnim();
        setUpSlider(0);
        setUpToolBar();
        img_back_slide_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedSpeed = getSelectedSpeed(position);
                setSlideShowSpeed(selectedSpeed);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpToolBar() {
        toolbar_slide.inflateMenu(R.menu.menu_effect_slide);
        toolbar_slide.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.menu_effect1:
                        setUpSlider(0);
                        break;
                    case R.id.menu_effect2:
                        setUpSlider(1);
                        break;
                    case R.id.menu_effect3:
                        setUpSlider(2);
                        break;
                }
                return true;
            }
        });
    }
    private void setSlideShowSpeed(int speed) {
        sliderView.setScrollTimeInMillis(speed);
    }
    private int getSelectedSpeed(int position) {
        switch (position) {
            case 0:
                return 3000; // Tốc độ mặc định hoặc giá trị mong muốn của bạn
            case 1:
                return 5000; // Ví dụ: Tốc độ chậm
            case 2:
                return 1000; // Ví dụ: Tốc độ nhanh
            default:
                return 3000; // Giá trị mặc định
        }
    }

    private void addListAnim() {
        effect.add(SliderAnimations.HORIZONTALFLIPTRANSFORMATION);
        effect.add(SliderAnimations.ZOOMOUTTRANSFORMATION);
        effect.add(SliderAnimations.DEPTHTRANSFORMATION);
    }

    private void setUpSlider(int i) {
        ArrayList<String> imageList = intent.getStringArrayListExtra("data_slide");
        SlideShowAdapter slideShowAdapter = new SlideShowAdapter();
        slideShowAdapter.setData(imageList);
        sliderView.setSliderAdapter(slideShowAdapter);
        sliderView.startAutoCycle();
        sliderView.setSliderTransformAnimation(effect.get(i));
        setSlideShowSpeed(currentSpeed);
    }

    private void mappingControls() {
        sliderView = findViewById(R.id.sliderView);
        img_back_slide_show = findViewById(R.id.img_back_slide_show);
        toolbar_slide = findViewById(R.id.toolbar_slide);
        spinner=findViewById(R.id.spinnerSpeed);
        linearLayout= findViewById(R.id.header);
        pref=new MySharedPreferences(SlideShowActivity.this);
        int mauchude= Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        linearLayout.setBackgroundColor(mauchude);

    }
}