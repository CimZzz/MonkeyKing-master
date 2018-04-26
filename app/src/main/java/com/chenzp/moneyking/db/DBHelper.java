package com.chenzp.moneyking.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.chenzp.moneyking.model.User;

/**
 * Created by CimZzz on 2018/4/26.<br>
 * Description:<br>
 */
public class DBHelper extends SQLiteOpenHelper {


    public DBHelper(Context context) {
        super(context,"db_monkeyKing",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        StringBuilder createTableBuilder = new StringBuilder();
        createTableBuilder.append("create table tb_user(")
                .append("userName text primary key,")
                .append("userPwd text not null,")
                .append("score decimal default 0)");
        sqLiteDatabase.execSQL(createTableBuilder.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    public User findUserBy(String userName,String userPwd) {
        SQLiteDatabase db = getWritableDatabase();

        User user = null;
        Cursor cursor = db.rawQuery("select * from tb_user where userName='"+userName+"' and userPwd='"+userPwd+"'",null);
        if(cursor.moveToNext()) {
            user = new User();
            user.setUserName(cursor.getString(cursor.getColumnIndex("userName")));
            user.setUserPwd(cursor.getString(cursor.getColumnIndex("userPwd")));
            user.setScore(cursor.getFloat(cursor.getColumnIndex("score")));
        }

        cursor.close();
        return user;
    }

    public boolean insertUser(User user) {
        SQLiteDatabase db = getWritableDatabase();


        Cursor cursor = db.rawQuery("select * from tb_user where userName='"+user.getUserName()+"'",null);
        if(cursor.moveToNext()) {
            cursor.close();
            return false;
        }

        db.execSQL("insert into tb_user values('"+user.getUserName()+"','"+user.getUserPwd()+"',"+user.getScore()+")");
        return true;
    }

    public void updateUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("update tb_user set score="+user.getScore()+" where userName='"+user.getUserName()+"'");
    }
}
