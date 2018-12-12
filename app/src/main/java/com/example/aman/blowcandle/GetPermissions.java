package com.example.aman.blowcandle;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

public class GetPermissions {

    //要申请的权限
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO   //录音权限
    };

    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
     *
     * @param activity
     * @param requestCode
     * @return
     */
    public static boolean isGrantExternalRW(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int writeStoragePermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readStoragePermission = activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int recorderPermission = activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            //检测是否有权限，如果没有权限，就需要申请
            if (writeStoragePermission != PackageManager.PERMISSION_GRANTED ||
                    readStoragePermission != PackageManager.PERMISSION_GRANTED ||
                    recorderPermission != PackageManager.PERMISSION_GRANTED) {
                //申请权限
                activity.requestPermissions(PERMISSIONS, requestCode);
                //返回false。说明没有授权
                return false;
            }
        }
        //说明已经授权
        return true;
    }
}