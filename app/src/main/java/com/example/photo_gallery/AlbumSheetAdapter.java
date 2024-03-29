package com.example.photo_gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AlbumSheetAdapter extends RecyclerView.Adapter<AlbumSheetAdapter.AlbumViewHolder> {
    private List<Album> mListAlbums;
    private Context context;
    private SubInterface subInterface;
    public AlbumSheetAdapter(List<Album> mListAlbums, Context context) {
        this.mListAlbums = mListAlbums;
        this.context = context;
    }

    public void setSubInterface(SubInterface subInterface) {
        this.subInterface = subInterface;
    }

    public void setData(List<Album> mListAlbums) {
        this.mListAlbums = mListAlbums;
        notifyDataSetChanged();
    }
    public void notifyData() {
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);

        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        holder.onBind(mListAlbums.get(position), position);
    }

    @Override
    public int getItemCount() {
        if (mListAlbums != null) {
            return mListAlbums.size();
        }
        return 0;
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder {
        private final ImageView img_album;
        private final TextView txtName_album;
        private final TextView txtCount_item_album;
        private Context context;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            img_album = itemView.findViewById(R.id.img_album);
            txtName_album = itemView.findViewById(R.id.txtName_album);
            txtCount_item_album = itemView.findViewById(R.id.txtCount_item_album);
            context = itemView.getContext();
        }

        public void onBind(Album ref, int pos) {
            bindData(ref);

            img_album.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    subInterface.add(ref);
                }
            });

        }

        private void bindData(Album ref) {
            txtName_album.setText(ref.getName());
            txtCount_item_album.setText(String.valueOf(ref.getList().size()) + " items");
            Glide.with(context).load(ref.getImg().getThump()).into(img_album);
        }
    }
}
