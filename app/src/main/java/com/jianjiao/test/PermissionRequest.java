package com.jianjiao.test;

import android.app.Activity;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionRequest {
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 3;
    Activity activity;
    String[] permissions = {"android.permission.READ_PHONE_STATE", "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.CAMERA"};
    List<String> mPermissionList = new ArrayList();

    public PermissionRequest(Activity activity) {
        this.activity = activity;
    }

    public void requestPermissionForCamera() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this.activity, "android.permission.WRITE_EXTERNAL_STORAGE")) {
            Toast.makeText(this.activity, "Camera permission needed. Please allow in App Settings for additional functionality.", 1).show();
        } else {
            ActivityCompat.requestPermissions(this.activity, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 3);
        }
    }

    private boolean checkCameraPermission() {
        return Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(this.activity.getApplicationContext(), "android.permission.CAMERA") == 0;
    }

    public void requestCameraPermission() {
        ActivityCompat.requestPermissions(this.activity, new String[]{"android.permission.CAMERA"}, 3);
    }

    public boolean checkPermissionForREADPHONESTATE() {
        int result = ContextCompat.checkSelfPermission(this.activity, "android.permission.WRITE_EXTERNAL_STORAGE");
        if (result == 0) {
            return true;
        }
        return false;
    }

}
