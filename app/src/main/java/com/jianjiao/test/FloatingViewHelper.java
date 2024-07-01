package com.jianjiao.test;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

public class FloatingViewHelper {

    private WindowManager windowManager;
    private View floatingView;

    public void createFloatingView(Context context, int layoutResId) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        floatingView = LayoutInflater.from(context).inflate(layoutResId, null);

        // 设置视图属性，如大小、位置和透明度
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // 设置位置，例如在屏幕中心
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        layoutParams.x = 0;
        layoutParams.y = 0;

        // 添加视图到WindowManager
        windowManager.addView(floatingView, layoutParams);
    }

    public void removeFloatingView() {
        if (floatingView != null && windowManager != null) {
            windowManager.removeView(floatingView);
            floatingView = null;
        }
    }
}
