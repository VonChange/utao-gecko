package com.vonchange.utao.gecko.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.vonchange.utao.gecko.MyApplication;


public class AppVersionUtils {

    private static PackageInfo mPackageInfo;

    /**
     * get app version name.
     *
     * @return version name.
     */
    public static String getVersionName() {
        getPackageInfo();
        return mPackageInfo.versionName;
    }

    private static void getPackageInfo() {
        if (mPackageInfo == null) {
            try {
                mPackageInfo = MyApplication.getContext().getPackageManager()
                        .getPackageInfo(MyApplication.getContext().getPackageName(), PackageManager.GET_CONFIGURATIONS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * get app version code.
     *
     * @return version code.
     */
    public static int getVersionCode() {
        getPackageInfo();
        return mPackageInfo.versionCode;
    }
}
