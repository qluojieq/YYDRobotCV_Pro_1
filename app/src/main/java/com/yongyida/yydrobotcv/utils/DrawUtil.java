package com.yongyida.yydrobotcv.utils;

import android.content.Context;
import android.util.Log;

import com.yongyida.yydrobotcv.useralbum.User;
import com.yongyida.yydrobotcv.useralbum.UserDataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dou.utils.DLog;


/**
 * Created by Brandon on 18/4/19.
 */
public class DrawUtil {

     private static final String TAG = DrawUtil.class.getSimpleName();

    private static Map<Integer, User> userMap = new HashMap<>();
    private static List<User> userList = new ArrayList<>();


    public static List<User> updateDataSource(Context context) {

        long time = System.currentTimeMillis();
        UserDataSupport dataSource =  UserDataSupport.getInstance(context);
        userMap.clear();
        userList.clear();
        userList = dataSource.getAllUsers("");
        for (int i = 0; i < userList.size(); i++) {
            String imgPath = context.getCacheDir()
                    + "/" + userList.get(i).getPersonId() + ".jpg";
            File imgFile = new File(imgPath);
            if (imgFile.exists()) {
                userList.get(i).setHeadPortrait(imgPath);
            }
            Log.e(TAG,"长度 " + userList.size() + " 获取的personId " + userList.get(i).getPersonId());
            userMap.put(Integer.valueOf(userList.get(i).getPersonId()), userList.get(i));
        }
        DLog.d(" update sql cost: " + (System.currentTimeMillis() - time));
        return userList;
    }

    public static String getNameFromPersonId(int personId) {
        if (personId > 0 && userMap.containsKey(personId)) {
            User user = userMap.get(personId);
            return user.getUserName();
        }
        return "";
    }

    // 获取列表有人
    public static User getUserFromPersonId(int personId) {
        if (personId > 0 && userMap.containsKey(personId)) {
            User user = userMap.get(personId);
           return user;
        }
        return null;
    }
}
