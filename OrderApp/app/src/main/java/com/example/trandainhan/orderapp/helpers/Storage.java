package com.example.trandainhan.orderapp.helpers;

import android.text.TextUtils;

import com.example.trandainhan.orderapp.models.QuanLy;

public class Storage {
    public static String Username = null;
    public static String Password = null;

    public static QuanLy getQuanLy() {
        return TextUtils.isEmpty(Username) ? null : (TextUtils.isEmpty(Password) ? null : new QuanLy(Username, Password));
    }

}
