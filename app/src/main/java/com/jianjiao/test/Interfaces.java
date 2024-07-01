package com.jianjiao.test;

import android.util.Log;

import java.util.Arrays;

public class Interfaces {
    static final class ActivityInterface implements FridaInterface {
        @Override
        public Object call(Object[] args) {
            Log.e("FridaAndroidInject", Arrays.toString(args));
            return null;
        }
    }
}
