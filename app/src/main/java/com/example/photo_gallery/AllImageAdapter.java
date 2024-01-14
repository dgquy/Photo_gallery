package com.example.photo_gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AllImageAdapter extends RecyclerView.Adapter<AllImageAdapter.ImageViewHolder> {
    private List<Image> listImg;
    private ShowImageToChoose thisActivity;
    private static final String KEY_THUMBNAIL = "KEY_THUMBNAIL";


    public AllImageAdapter(List<Image> listImg, ShowImageToChoose _thisActivity) {
        this.listImg = listImg;
        this.thisActivity = _thisActivity;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picture, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.onBind(listImg.get(position), position);
    }

    @Override
    public int getItemCount() {
        return listImg.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;
        private Context context;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgPhoto);
            context = itemView.getContext();
        }

        public void onBind(Image image, int position) {
            Glide.with(context).load(image.getPath()).into(img);

            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(KEY_THUMBNAIL, image);
                    thisActivity.setResult(Activity.RESULT_OK, resultIntent);
                    thisActivity.finish();
                }
            });
        }
    }

}
