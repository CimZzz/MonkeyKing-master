package com.chenzp.moneyking.model;

import java.io.Serializable;

/**
 * Created by CimZzz on 2018/4/26.<br>
 * Description:<br>
 */
public class User implements Serializable{
    private String userName;
    private String userPwd;
    private float score;

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
