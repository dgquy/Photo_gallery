package com.example.photo_gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;

public class SlideShowAdapter extends SliderViewAdapter<SlideShowAdapter.SliderViewHolder> {
    private ArrayList<String> imageList;

    public void setData(ArrayList<String> imageList) {
        this.imageList = imageList;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public SliderViewHolder onCreateViewHolder(ViewGroup parent) {
        return new SliderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slide_show, parent, false));
    }

    @Override
    public void onBindViewHolder(SliderViewHolder viewHolder, int position) {
        viewHolder.onbind(imageList.get(position));
    }

    public class SliderViewHolder extends ViewHolder {
        private ImageView img_slide_show;
        private Context context;
        public SliderViewHolder(View itemView) {
            super(itemView);
            img_slide_show = itemView.findViewById(R.id.img_slide_show);
            context = itemView.getContext();
        }

        public void onbind(String img) {
            Glide.with(context).load(img).into(img_slide_show);
        }
    }
}
