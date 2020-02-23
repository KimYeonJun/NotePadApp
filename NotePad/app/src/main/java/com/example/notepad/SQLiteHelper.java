package com.example.notepad;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper {
    private static final String dbName="myMemotest2";
    private static final String table1 = "MemoTable";
    private static final String table2 = "ImageTable";
    private static final int dbVersion = 1;

    private OpenHelper opener;
    private SQLiteDatabase db;
    private Context context;

    public SQLiteHelper(Context context) {
        this.context = context;
        this.opener = new OpenHelper(context,dbName,null,dbVersion);
        db = opener.getWritableDatabase();

    }

    private class OpenHelper extends SQLiteOpenHelper{

        public OpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String create = "CREATE TABLE "+table1+" ("+
                    "seq integer PRIMARY KEY AUTOINCREMENT,"+
                    "titletext text,"+
                    "maintext text)";

            String create2 = "CREATE TABLE "+table2+" ("+
                    "imagenum integer PRIMARY KEY AUTOINCREMENT,"+
                    "seq integer,"+
                    "uri text)";
            db.execSQL(create);
            db.execSQL(create2);


        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+table1);
            onCreate(db);

        }
    }

    public void insertMemo(Memo memo){ //데이터 추가
        String sql = "INSERT INTO "+table1+" VALUES(NULL, '"+memo.titleText+"','"+memo.mainText+"');";
        db.execSQL(sql);
    }

    public void insertImage(int seq, String uri){
        String sql = "INSERT INTO "+table2+" VALUES(NULL, "+seq+","+"'"+uri+"');";
        db.execSQL(sql);
    }
    public int getSeq(){ //데이터 추가시 seq넘버 구하는 sql문.
        String sql = "SELECT seq FROM "+table1+";";
        Cursor result = db.rawQuery(sql,null);
        result.moveToLast();
        return result.getInt(0);
    }
    public void deleteMemo(int position){ //데이터 삭제
        String sqlMemo = "DELETE FROM "+table1+" WHERE seq = "+position+";";
        db.execSQL(sqlMemo);
    }
    public void deleteImage(int position){
        String sql = "DELETE FROM "+table2+" WHERE seq = "+position+";";
        db.execSQL(sql);
    }
    public void updateMemo(Memo memo,int seq){
        String sql = "UPDATE "+table1+" SET titletext = '"+memo.titleText+"', maintext = '"+memo.mainText+"' WHERE seq = "+seq+";";
        db.execSQL(sql);
    }

    public Memo selectMemo(int position){
        String getImageSql = "SELECT uri FROM "+table2+" WHERE seq="+position+";";
        Cursor images = db.rawQuery(getImageSql,null);
        images.moveToFirst();
        List<String> uri_list = new ArrayList<String>();
        while(!images.isAfterLast()){
            uri_list.add(images.getString(0));
            images.moveToNext();
        }
        images.close();

        String sql = "SELECT * FROM "+table1+" WHERE seq = "+position+";";
        Cursor result =  db.rawQuery(sql,null);
        Memo memo =null;
        if(result != null){
            if(result.moveToFirst()){
                memo = new Memo(result.getInt(0),result.getString(1),result.getString(2),uri_list);
            }
        }

        result.close();
        return memo;


    }
    public ArrayList<Memo> selectAll(){ //데이터 조회
        String sql = "SELECT * FROM "+ table1;
        String getImageSql;

        ArrayList<Memo> list = new ArrayList<>();
        Cursor results = db.rawQuery(sql,null);
        results.moveToFirst();
        while(!results.isAfterLast()){ //전체 메모를 조회
            getImageSql = "SELECT uri FROM "+table2+" WHERE seq="+results.getInt(0)+";";
            Cursor images = db.rawQuery(getImageSql,null);
            images.moveToFirst();
            List<String> uri_list = new ArrayList<String>();
            while(!images.isAfterLast()){ // 각 메모에 대한 이미지 조회
                uri_list.add(images.getString(0));
                images.moveToNext();
            }
            images.close();
            Memo memo = new Memo(results.getInt(0),results.getString(1),results.getString(2),uri_list);
            list.add(memo);
            results.moveToNext();

        }
        results.close();
        return list;

    }
}
