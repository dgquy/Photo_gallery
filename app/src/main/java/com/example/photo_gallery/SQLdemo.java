package com.example.photo_gallery;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SQLdemo extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASENAME="NHOM24.db";
    public  static  final int version=1;

    public SQLdemo(@Nullable Context context) {
        super(context, DATABASENAME, null, version);
        this.context=context;
        if (!isDatabaseExists() || !isTableExists()) {
            createDatabase();
        }
    }
    private boolean isDatabaseExists() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(context.getDatabasePath(DATABASENAME).getPath(), null, SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        } catch (SQLiteException e) {
            // database doesn't exist yet
            return false;
        }
        // database exists
        return true;
    }

    public List<Hashtag> getListHashtag() {
        String query = "SELECT *FROM NHOM24";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }

        ArrayList<Hashtag> mylist = new ArrayList<>();
        while (cursor.moveToNext()) {
            Image image = new Image(cursor.getString(0), cursor.getString(1), cursor.getString(2));
            Hashtag hashtag = new Hashtag(cursor.getString(3), image);
            mylist.add(hashtag);
        }
        return mylist;
    }

    private boolean isTableExists() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table' AND name='NHOM24'", null);
        boolean tableExists = cursor.getCount() > 0;
        cursor.close();
        return tableExists;
    }
    public void drophashtag(String hash) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            db.execSQL("DELETE FROM NHOM24 WHERE HASHTAG = '" + hash + "';");
            db.setTransactionSuccessful();
            Log.d("SQLdemo", "Record deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("SQLdemo", "Error deleting record: " + e.getMessage());
        } finally {
            db.endTransaction();
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
    public void dropimage(String path){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            db.execSQL("DELETE FROM NHOM24 WHERE PATH = '" + path + "';");
            db.setTransactionSuccessful();
            Log.d("SQLdemo", "Record deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("SQLdemo", "Error deleting record: " + e.getMessage());
        } finally {
            db.endTransaction();
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
    public boolean checkExist(String hashtag, String imagePath) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            if (db != null) {
                // Sử dụng tham số để tránh SQL injection
                String query = "SELECT * FROM NHOM24 WHERE HASHTAG=? AND PATH=?";
                cursor = db.rawQuery(query, new String[]{hashtag, imagePath});

                // Kiểm tra xem có dữ liệu trong Cursor hay không
                return cursor != null && cursor.moveToFirst();
            }
        } finally {
            // Đảm bảo đóng Cursor để tránh rò rỉ tài nguyên
            if (cursor != null) {
                cursor.close();
            }
        }

        return false;
    }

    private void createDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Log.d("Database", "Creating new tables...");
            String createTable1 = "CREATE TABLE NHOM24 (PATH TEXT,THUMP TEXT,DATETOKEN TEXT, HASHTAG TEXT);";
            db.execSQL(createTable1);
            Log.d("Database", "Tables created successfully.");
        } catch (Exception e) {
            Log.e("Database", "Error creating tables: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public void deleteDatabase() {
        try {
            context.deleteDatabase(DATABASENAME);
            Log.d("SQLdemo", "Database deleted successfully.");
        } catch (Exception e) {
            Log.e("SQLdemo", "Error deleting database: " + e.getMessage());
        }
    }
    private void openDatabase(){
        try {
            //deleteDatabase(); // Xóa database cũ trước khi mở mới
            SQLdemo db=new SQLdemo(context);
            SQLiteDatabase db1 = db.getWritableDatabase();
            if (db1 == null) {
                Log.e("SQLdemo", "Database is null after opening");
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
            Log.e("SQLdemo", "Error opening database");
        }
    }
    Cursor readAllData(String Path){
        String query = "SELECT *FROM NHOM24 WHERE PATH='"+Path+"'";
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=null;
        if(db!=null){
            cursor=db.rawQuery(query,null);
        }
        return cursor;
    }

    public ArrayList<Hashtag> readDatatoList(String path){
        ArrayList<Hashtag> mylist=new ArrayList<>();
        Cursor cursor=readAllData(path);
        while(cursor.moveToNext()){
            Image image=new Image(cursor.getString(0),cursor.getString(1),cursor.getString(2));
            Hashtag hashtag=new Hashtag(cursor.getString(3),image);
            mylist.add(hashtag);
        }
        return mylist;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d("Database", "Creating new tables...");
            String createTable1 = "CREATE TABLE NHOM24 (PATH TEXT,THUMP TEXT,DATETOKEN TEXT, HASHTAG TEXT);";
            db.execSQL(createTable1);
            Log.d("Database", "Tables created successfully.");
        } catch (Exception e) {
            Log.e("Database", "Error creating tables: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS NHOM24");
    }
    public void insertdata(Image image, String hashtag){
        SQLiteDatabase db=this.getWritableDatabase();
        try{
            db.beginTransaction();
            db.execSQL("INSERT INTO NHOM24(PATH, THUMP, DATETOKEN, HASHTAG) VALUES (?, ?, ?, ?);", new Object[]{image.getPath(), image.getThump(), image.getDateTaken(), hashtag});
            db.setTransactionSuccessful();
        }catch (SQLException e2) {
            e2.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

}

