package com.yongyida.yydrobotcv.useralbum;

import java.io.Serializable;

/**
 * @author Brandon on 2018/3/13
 * update 18/4/10
 **/
public class User implements Serializable{


    public static final String TAG = User.class.getSimpleName();

    private String userId;
    private String personId;
    private String userName;
    private String birthDay;
    private String age;
    private String gender;
    private String phoneNum;
    private String vipRate;
    private String  identifyCount;
    private String headPortrait;
    private String tag;

    public static String getTAG() {
        return TAG;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String uaerName) {
        this.userName = uaerName;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getVipRate() {
        return vipRate;
    }

    public void setVipRate(String vipRate) {
        this.vipRate = vipRate;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIdentifyCount() {
        return identifyCount;
    }

    public void setIdentifyCount(String identifyCount) {
        this.identifyCount = identifyCount;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getHeadPortrait() {
        return headPortrait;
    }

    public void setHeadPortrait(String headPortrait) {
        this.headPortrait = headPortrait;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
