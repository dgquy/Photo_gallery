package com.example.photo_gallery;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.photo_gallery.Image;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;

public class GetAllimagefromGallery {
    public static ArrayList<Image> allImages;
    private static boolean allImagesPresent=false;
    private static boolean addNewestImagesOnly = false;
    public List<Image> getAllImages() {
        return allImages;
    }
    public static void refreshAllImages(){
        allImagesPresent = false;
    }
    public static void updateNewImages(){
        addNewestImagesOnly = true;
    }
    public static void removeImageFromAllImages(String path) {  // remove deleted photo from "database"
        Log.d("Photo_gallery","GetAllimagefromGallery -> Trying to remove "+path);
        for(int i=0;i<allImages.size();i++) {
            if(allImages.get(i).getPath().equals(path)) {
                Log.d("Photo_gallery","GetAllimagefromGallery -> Image removed from allImages. Breaking");
                allImages.remove(i);
                break;
            }
        }
    }

    public GetAllimagefromGallery(Context context){

    }
    public static boolean hasSlash(String a, String b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return false;
        }
        int indexOfB = a.indexOf(b);
        if (indexOfB != -1 && indexOfB + b.length() < a.length()) {
            String remainingString = a.substring(indexOfB + b.length());
            return remainingString.contains("/");
        } else {
            return false;
        }
    }
    public  static  ArrayList<Image> getAllImageFromGallery(Context context){
        if(!allImagesPresent){
            Uri uri;
            Cursor cursor;
            int columnIndexData,thumb,dateIndex;
            ArrayList<Image> listImage=new ArrayList<>();
            String absolutePathImage = null;
            String thumbnail = null;
            Long dateTaken = null;
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {
                    MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_TAKEN
            };

            final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
            cursor = context.getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
            columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            thumb = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA);
            dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
            Calendar myCal = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MM-yyyy");
            while (cursor.moveToNext()) {
                try {
                    absolutePathImage = cursor.getString(columnIndexData);
                    File file = new File(absolutePathImage);
                    if (!file.canRead()) {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }
                thumbnail = cursor.getString(thumb);
                dateTaken = cursor.getLong(dateIndex);
                myCal.setTimeInMillis(dateTaken);
                String dateText = formatter.format(myCal.getTime());
                Image image = new Image();
                image.setPath(absolutePathImage);
                image.setThump(thumbnail);
                image.setDateTaken(dateText);
                if (image.getPath() == "") {
                    continue;
                }
                Log.d("Path", image.getPath());
                Log.d("Path", listImage.size() + "");
                if(addNewestImagesOnly){
                    boolean iscontained = false; // in the "database"
                    for(Image i : allImages){
                        if(i.getPath().equals(image.getPath())){
                            iscontained = true;
                            break;
                        }
                    }
                    if(iscontained){
                        Log.d("Photo_gallery","GetAllimagefromGallery -> Image already in allImages. Breaking");
                        addNewestImagesOnly = false;
                        allImagesPresent = true;
                        cursor.close(); // Android Studio suggestion
                        return allImages;
                    } else{
                        Log.d("Photo_gallery", allImages.size() + "");
                        if(allImages.size()>1200){
                            addNewestImagesOnly = false;
                            allImagesPresent = true;
                            cursor.close(); // Android Studio suggestion
                            return allImages;
                        }
                        allImages.add(0, image);
                    }
                } else {
                    listImage.add(image);
                }

                if(listImage.size()>1000) { // Just for testing.
                    break;                  // I don't want to load 10 000 photos at once.
                }
            }
            cursor.close(); // Android Studio suggestion
            ArrayList<Image> listImage1= new ArrayList<>();
            String cut="Pictures/";
            for (int i = 0; i < listImage.size(); i++) {
                if(!hasSlash(listImage.get(i).getPath(),cut)){
                    listImage1.add(listImage.get(i));
                }
            }
            allImages = listImage1;
            addNewestImagesOnly = false;
            allImagesPresent = true;
            return listImage1;
        }
        else{
            return allImages;
        }
    }
}
