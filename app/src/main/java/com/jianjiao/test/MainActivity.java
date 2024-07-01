package com.jianjiao.test;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.accessibility.AccessibilityManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chrisplus.rootmanager.RootManager;

import java.io.IOException;
import java.util.List;

import com.jianjiao.test.PermissionRequest;

public class MainActivity extends AppCompatActivity {
    private WebView webView = null;
    private static final int REQUEST_ENABLE_ACCESSIBILITY = 1;
    private BootCompleteReceiver mBroadcastReceiver;
    private RootManager mRootManager;
    Wechat fridaWechat = null;

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        setContentView(R.layout.activity_main);
        WebView webView = (WebView) findViewById(R.id.webHome);
        this.webView = webView;
//        webView.getSettings().setAppCacheEnabled(true);
        this.webView.getSettings().setBlockNetworkImage(false);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.loadUrl("file:///android_asset/list.html");
        this.webView.addJavascriptInterface(this, "android");
        PermissionRequest permissionRequest = new PermissionRequest(this);
        if (permissionRequest.checkPermissionForREADPHONESTATE()) {
            permissionRequest.requestPermissionForCamera();
        }
        if (!permissionRequest.checkPermissionForREADPHONESTATE()) {
            permissionRequest.requestCameraPermission();
        }
        this.mBroadcastReceiver = new BootCompleteReceiver();

        /*try {
            // build an instance of FridaInjector providing binaries for arm/arm64/x86/x86_64 as needed
            // assets/frida-inject-12.8.2-android-arm64
            FridaInjector fridaInjector = new FridaInjector.Builder(this)
                    .withArm64Injector("frida-inject-12.8.2-android-arm64")
                    .build();

            // build an instance of FridaAgent
            FridaAgent fridaAgent = new FridaAgent.Builder(this)
                    .withAgentFromAssets("agent.js")
                    .withOnMessage(this)
                    .build();

            // register a custom interface
            fridaAgent.registerInterface("activityInterface", Interfaces.ActivityInterface.class);

            // inject whatsapp
            fridaInjector.inject(fridaAgent, "mark.via", true);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public static void sendJavaScriptEvent(Context context, String func, String data) {
        Intent intent = new Intent("com.pinduoduo2.web");
        intent.putExtra("act", "JavaScriptEvent");
        intent.putExtra("func", func);
        intent.putExtra("data", data);
        context.sendBroadcast(intent);
    }

    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> accessibilityServiceClass) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(16);
        for (AccessibilityServiceInfo enabledService : enabledServices) {
            if (enabledService.getId().contains(accessibilityServiceClass.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    @JavascriptInterface
    public void JS_analt_wechat(String userId) {
        if (this.fridaWechat == null) {
            this.fridaWechat = new Wechat(getApplicationContext(), "frida-wechat.js", userId);
        }
        Toast.makeText(getApplicationContext(), "正在注入，稍等3-5s打开微信", Toast.LENGTH_SHORT).show();
        this.fridaWechat.start("com.tencent.mm");
    }

    @JavascriptInterface
    public void JS_analt_miniprogram(String analyToken) {
        /*if (this.fridaWechat == null) {
            this.fridaWechat = new Wechat(getApplicationContext(), "frida-miniprogram.js", analyToken);
        }
        Toast.makeText(getApplicationContext(), "正在注入2", Toast.LENGTH_SHORT).show();
        this.fridaWechat.start("com.tencent.mm:appbrand0");*/
    }

    @JavascriptInterface
    public boolean JS_getIsRoot() {
        RootManager rootManager = RootManager.getInstance();
        this.mRootManager = rootManager;
        return rootManager.hasRooted();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
    }


    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onPause() {
        super.onPause();
    }

    /*@JavascriptInterface
    public void JS_openQrCodeScanner() {
        Intent intent = new Intent(this, QRCodeScannerActivity.class);
        startActivity(intent);
    }*/
    class BootCompleteReceiver extends BroadcastReceiver {
        BootCompleteReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String act = intent.getStringExtra("act");
            if (((act.hashCode() == -1204034323 && act.equals("JavaScriptEvent")) ? (char) 0 : (char) 65535) == 0) {
                String func = intent.getStringExtra("func");
                String data = intent.getStringExtra("data");
                WebView webView = MainActivity.this.webView;
                webView.loadUrl("javascript:callbackAndroid('" + func + "','" + data + "')");
            }
        }
    }

}
