package com.yongyida.yydrobotcv.useralbum;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Brandon on 2018/3/13
 * update 2018/4/10
 **/
public class UserDataHelper extends SQLiteOpenHelper {

    public  static final String C_ID = "_id";
    public static final String C_ID_PERSON = "person_id";
    public static final String C_UN =  "u_name";
    public static final String C_UBD = "u_birthday";
    public static final String C_UGD = "u_gender";
    public static final String C_UPN = "u_phone_num";
    public static final String C_UPR = "u_vip_rate";
    public static final String C_UIC = "u_identify_count";
    public static final String C_HEAD = "u_head_path";
    public static final String C_TAG = "u_tag";

    public static final String DATABASE_TABLE = "users";
    public static final String DATABASE_NAME = "user.db";
    public static final int DATABASE_VERSION = 1;//必须大于等于1


    private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE +
            " (" + C_ID + " integer primary key autoincrement,"
            + C_UN + " text not null,"
            + C_ID_PERSON + " text not null,"
            + C_UBD + " ,"
            + C_UGD + " ,"
            + C_UPN + " ,"
            + C_HEAD + " ,"
            + C_UIC + " ,"
            + C_UPR + " ,"
            + C_TAG +" );";


    public UserDataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE );
        onCreate(db);
    }
}
