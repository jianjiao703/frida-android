package com.jianjiao.test;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Wechat implements OnMessage {
    private FridaAgent fridaAgent;
    private FridaInjector fridaInjector;
    private Context mContext;

    public Wechat(Context context, String hookFileName,String userId) {
        this.mContext = context;
        init(hookFileName, userId);
    }

    public void init(String filename, String userId) {
        try {
            Log.e("TAG", "Build.VERSION.SDK_IN:" + Build.VERSION.SDK_INT);
            Log.e("TAG", "Build.VERSION.SDK_IN:" + Build.VERSION.SDK_INT);
            if (Build.HARDWARE.indexOf("x86") > 0) {
                this.fridaInjector = new FridaInjector.Builder(this.mContext).withX86Injector("frida-inject-15.2.0-android-x86").build();
            } else {
                this.fridaInjector = new FridaInjector.Builder(this.mContext).withArm64Injector("frida-inject-15.2.0-android-arm64").build();
            }
            Log.e("TAG", "Model:" + Build.BRAND);
            FridaAgent build = new FridaAgent.Builder(this.mContext).withAgentFromAssets(filename,userId).withOnMessage(this).build();
            this.fridaAgent = build;
            build.registerInterface("activityInterface", Interfaces.ActivityInterface.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(String packagename) {
        this.fridaInjector.inject(this.fridaAgent, packagename, false);
    }

    @Override // com.analy.fridainjector.OnMessage
    public void onMessage(String data) {
        try {
            JSONObject object = new JSONObject(data);
            String act = object.optString("act");
            if (act.equals("Frida_Log")) {
                Log.e("TAG", "[Frida_log]" + object.getString(NotificationCompat.CATEGORY_MESSAGE));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}