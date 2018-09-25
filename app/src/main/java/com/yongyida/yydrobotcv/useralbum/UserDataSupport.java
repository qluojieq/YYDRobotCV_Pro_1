package com.yongyida.yydrobotcv.useralbum;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.yongyida.yydrobotcv.utils.ChineseCharacterUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mobile.ReadFace.YMFaceTrack;

/**
 * @author Brandon on 2018/3/13
 * update 18/4/10
 **/
public class UserDataSupport extends ContentProvider {

    static final String AUTHORITY = "com.yyd.yydrobotcv";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/images");

    public static final String TAG = UserDataSupport.class.getSimpleName();
    UserDataHelper userHelper;
    SQLiteDatabase database;
    private static UserDataSupport userDataSupport;
    private String[] allColumns = {
            UserDataHelper.C_ID,
            UserDataHelper.C_ID_PERSON,
            UserDataHelper.C_UN,
            UserDataHelper.C_UBD,
            UserDataHelper.C_UGD,
            UserDataHelper.C_UPN,
            UserDataHelper.C_UPR,
            UserDataHelper.C_HEAD,
            UserDataHelper.C_UIC,
            UserDataHelper.C_TAG
    };



    public UserDataSupport() {
    }

    public static UserDataSupport getInstance(Context context){
        if (userDataSupport ==null){
            userDataSupport = new UserDataSupport(context);
        }
        return userDataSupport;
    }

    private UserDataSupport(Context context) {
        userHelper = new UserDataHelper(context);
    }

    private void open() {
        database = userHelper.getWritableDatabase();
    }
    private void close() {
        userHelper.close();
    }




    //获取全部用户
    public List<User> getAllUsers(String type) {
        open();
        List<User> allUsers = new ArrayList<>();
        if (type.equals("list")){//展示列表中添加了“添加”按钮

            User user1 = new User();
            user1.setUserId("");
            user1.setPersonId("");
            user1.setUserName("添加");
            user1.setBirthDay("");
            user1.setGender("");
            user1.setPhoneNum("");
            user1.setVipRate("");
            user1.setHeadPortrait("");
            user1.setIdentifyCount("");
            user1.setTag("");
            allUsers.add(user1);
        }
        Cursor cursor = database.query(UserDataHelper.DATABASE_TABLE, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            User user = new User();
            user.setUserId(cursor.getString(0));
            user.setPersonId(cursor.getString(1));
            user.setUserName(cursor.getString(2));
            user.setBirthDay(cursor.getString(3));
            user.setGender(cursor.getString(4));
            user.setPhoneNum(cursor.getString(5));
            user.setVipRate(cursor.getString(6));
            user.setHeadPortrait(cursor.getString(7));
            user.setIdentifyCount(cursor.getString(8));
            user.setTag(cursor.getString(9));
            allUsers.add(user);
            cursor.moveToNext();
        }
        Log.e(TAG,allUsers.size() + " get all user success " + cursor.getCount());
        cursor.close();
        close();
        final ArrayList<String> indexLetterTemp = new ArrayList();
        Collections.sort(allUsers, new Comparator<User>() {//对输出的结果提前排好序
            @Override
            public int compare(User o1, User o2) {
                int ret = 0;
                String name1 = o1.getUserName();
                String name2 = o2.getUserName();

                if (!indexLetter.contains(ChineseCharacterUtil.getFirstChar(o1.getUserName()).charAt(0)+"")){
                    indexLetter.add(ChineseCharacterUtil.getFirstChar(o1.getUserName()).charAt(0)+"");
                }
                if (!indexLetterTemp.contains(ChineseCharacterUtil.getFirstChar(o1.getUserName()).charAt(0)+"")){
                    indexLetterTemp.add(ChineseCharacterUtil.getFirstChar(o1.getUserName()).charAt(0)+"");
                }
                String first1 = ChineseCharacterUtil.getFirstChar(o1.getUserName());
                String second2 = ChineseCharacterUtil.getFirstChar(o2.getUserName());
                if (TextUtils.isEmpty(name1)&&TextUtils.isEmpty(name2)){

                }else if (o1.getUserName().equals("添加")){
                    ret = 1;
                }else if (o2.getUserName().equals("添加")){
                    ret = 1;
                }else {
                    ret = first1.charAt(0) - second2.charAt(0);
                }
                return ret;
            }
        });
        Collections.sort(indexLetter);
        Collections.sort(indexLetterTemp);
        for (int i = 0;i<indexLetter.size();i++){
            Log.e(TAG,"work " + indexLetter.get(i));
        }
        for (int i = 0;i<indexLetterTemp.size();i++){
            Log.e(TAG,"workTemp " + indexLetterTemp.get(i));
        }

        // 删除多余的
//        if (indexLetterTemp.size()!= indexLetterTemp.size()){
//            for (int i = 0;i<indexLetter.size();i++){
//                Log.e(TAG,"work " + indexLetter.get(i) + " temp " + indexLetterTemp.get(i));
//                if (indexLetter.get(i)!= indexLetterTemp.get(i)){
//                    indexLetter.remove(i);
//                    break;
//                }
//            }
//        }
        indexLetter = indexLetterTemp;
        for (int i = 0;i<indexLetter.size();i++){
            Log.e(TAG,"workAfter " + indexLetter.get(i));
        }
        for (int i = 0;i<indexLetterTemp.size();i++){
            Log.e(TAG,"workTempAfter " + indexLetterTemp.get(i));
        }
        return allUsers;
    }

    ArrayList<String> indexLetter = new ArrayList();

    public ArrayList<String> getIndexLetter() {

        return indexLetter;
    }

    //判断是否重名
    public  boolean checkNameUsed(String name){
        open();
        Cursor cursor = database.query(UserDataHelper.DATABASE_TABLE, allColumns, UserDataHelper.C_UN + "= ?", new String[] {name}, null, null, null);
        int ret = cursor.getCount();
        close();
        if (ret>0){
            Log.e(TAG,"名字查重 " + ret);
            return true;
        }
        return false;
    }

    //  判断

    //判断是否重名
    public  boolean checkPhoneNum(String name){
        open();
        Cursor cursor = database.query(UserDataHelper.DATABASE_TABLE, allColumns, UserDataHelper.C_UPN + "= ?", new String[] {name}, null, null, null);
        int ret = cursor.getCount();
        close();
        if (ret>0){
            Log.e(TAG,"电话号码重复 " + ret);
            return true;
        }
        return false;
    }
    //插入一个用户
    public long insertUser(User user) {
        open();
        ContentValues values = new ContentValues();
        values.put(allColumns[0], user.getUserId());
        values.put(allColumns[1], user.getPersonId());
        values.put(allColumns[2], user.getUserName());
        values.put(allColumns[3], user.getBirthDay());
        values.put(allColumns[4], user.getGender());
        values.put(allColumns[5], user.getPhoneNum());
        values.put(allColumns[6], user.getVipRate());
        values.put(allColumns[7], user.getHeadPortrait());
        values.put(allColumns[8], user.getIdentifyCount());
        values.put(allColumns[9], user.getTag());
        Long insertCount = database.insert(UserDataHelper.DATABASE_TABLE, null, values);
        close();
        return insertCount;
    }

    //更新访问次数

    public long updateIdentifyCount(String personId) {
        Log.e(TAG,"更新访问次数 开始");
        open();
        Cursor lastCursor = database.query(UserDataHelper.DATABASE_TABLE, new String[]{UserDataHelper.C_UIC}, UserDataHelper.C_ID_PERSON + "= ?", new String[] {personId}, null, null, null);
        lastCursor.moveToFirst();
        String temp = lastCursor.getString(0);
        Log.e(TAG,"更新访问次数 返回cursor" + temp);
        int lastCount = 0;
        if (!TextUtils.isEmpty(temp)){
            lastCount = Integer.parseInt(temp);
            Log.e(TAG,"更新访问次数 返回cursor" + null);
        }else {
            Log.e(TAG,"更新访问次数 返回cursor 0 +" + null);
        }

        Log.e(TAG,"更新访问次数 获取lastCount " + lastCount);
        lastCount++;
        ContentValues contentValues = new ContentValues();
        contentValues.put(allColumns[8], lastCount);
        long ret = database.update(UserDataHelper.DATABASE_TABLE, contentValues, UserDataHelper.C_ID_PERSON + " = ?" , new String[]{personId});
        if (ret>0){
           Log.e(TAG,"访问次数更新成功 " + lastCount);
        }else {
            Log.e(TAG,"访问次数更新失败 " + lastCount);
        }
        close();
        return ret;
    }

    //删除用户
    public long deleteUser(String personId) {
        long ret = -1;
        open();
        // 同时删除阅面中的信息
        YMFaceTrack ymFaceTrack = new YMFaceTrack();
        ymFaceTrack.deletePerson(Integer.parseInt(personId));
        ret = database.delete(UserDataHelper.DATABASE_TABLE, UserDataHelper.C_ID_PERSON + "= ?" , new String [] { personId});
        close();
        return ret;
    }

    //获取单个用户，不易频繁启动
    public User getUser(String personId){
        open();
        User user = new User();
        Cursor cursor = database.query(UserDataHelper.DATABASE_TABLE, allColumns, UserDataHelper.C_ID_PERSON + "= ?", new String[] {personId}, null, null, null);
       if (cursor.getCount()==1){
           cursor.moveToFirst();
           user.setUserId(cursor.getString(0));
           user.setPersonId(cursor.getString(1));
           user.setUserName(cursor.getString(2));
           user.setBirthDay(cursor.getString(3));
           user.setGender(cursor.getString(4));
           user.setPhoneNum(cursor.getString(5));
           user.setVipRate(cursor.getString(6));
           user.setHeadPortrait(cursor.getString(7));
           user.setIdentifyCount(cursor.getString(8));
           user.setTag(cursor.getString(9));
       }
       close();
        return  user;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        userHelper = new UserDataHelper(this.getContext());
        database = userHelper.getWritableDatabase();
        Cursor cursor = database.query(UserDataHelper.DATABASE_TABLE, projection, selection, selectionArgs, null,null, sortOrder);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
