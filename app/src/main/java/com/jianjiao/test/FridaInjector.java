package com.jianjiao.test;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.chrisplus.rootmanager.RootManager;
import com.chrisplus.rootmanager.container.Result;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class FridaInjector {
    private final Context mContext;

    private final File mInjector;

    private FridaInjector(Builder builder) {
        mContext = builder.mContext;
        mInjector = builder.getInjector();
    }

    public void inject(FridaAgent fridaAgent, String packageName, boolean spawn) {
        if (mInjector == null) {
            throw new RuntimeException("did you forget to call init()?");
        }

        if (!RootManager.getInstance().isProcessRunning(packageName)) {
            spawn = true;
        }

        StringBuilder agent = new StringBuilder(fridaAgent.getWrappedAgent());

        if (!fridaAgent.getInterfaces().isEmpty()) {
            try {
                ApplicationInfo ownAi = fridaAgent.getPackageManager().getApplicationInfo(
                        fridaAgent.getPackageName(), 0);
                String ownApk = ownAi.publicSourceDir;
                ApplicationInfo targetAi = fridaAgent.getPackageManager().getApplicationInfo(packageName, 0);
                String targetPath = new File(targetAi.publicSourceDir).getPath().substring(0,
                        targetAi.publicSourceDir.lastIndexOf("/"));
                if (targetPath.startsWith("/system/")) {
                    RootManager.getInstance().remount("/system", "rw");
                }
                RootManager.getInstance().runCommand("cp " + ownApk + " " + targetPath + "/xd.apk");
                RootManager.getInstance().runCommand("chmod 644 " + targetPath + "/xd.apk");
                if (targetPath.startsWith("/system/")) {
                    RootManager.getInstance().runCommand("chown root:root " + targetPath + "/xd.apk");
                    RootManager.getInstance().remount("/system", "ro");
                } else {
                    RootManager.getInstance().runCommand("chown system:system " + targetPath + "/xd.apk");
                }

                agent.append(FridaAgent.sRegisterClassLoaderAgent);

                for (LinkedHashMap.Entry<String, Class<? extends FridaInterface>> entry :
                        fridaAgent.getInterfaces().entrySet()) {
                    agent.append("Java['")
                            .append(entry.getKey())
                            .append("'] = function() {")
                            .append("var defaultClassLoader = Java.classFactory.loader;")
                            .append("Java.classFactory.loader = Java.classFactory['xd_loader'];")
                            .append("var clazz = Java.use('")
                            .append(entry.getValue().getName())
                            .append("').$new();")
                            .append("var args = [];")
                            .append("for (var i=0;i<arguments.length;i++) {")
                            .append("args[i] = arguments[i]")
                            .append("}")
                            .append("clazz.call(Java.array('java.lang.Object', args));")
                            .append("Java.classFactory.loader = defaultClassLoader;")
                            .append("};")
                            .append("\n");
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        File fridaAgentFile = new File(fridaAgent.getFilesDir(), "wrapped_agent.js");
        Utils.writeToFile(fridaAgentFile, agent.toString());
        RootManager.getInstance().runCommand("chmod 777 " + fridaAgentFile.getPath());

        if (spawn) {
            Log.e("TAG", "[Frida]打开目标应用 --> " + packageName);
            Intent launchIntent = this.mContext.getPackageManager().getLaunchIntentForPackage(packageName);
            new Thread(new Runnable() { // from class: com.analy.fridainjector.-$$Lambda$FridaInjector$PdsqUDd1bNkD1pUCzGmHhsiIV4s
                @Override // java.lang.Runnable
                public final void run() {
                    FridaInjector.this.inject(packageName, fridaAgentFile);
                }
            }).start();
            if (launchIntent == null) {
                Log.e("TAG", "[Frida]找不到该应用包");
                return;
            }
            this.mContext.startActivity(launchIntent);
            Log.e("TAG", "[Frida]运行微信");
            return;
        }
        RootManager rootManager7 = RootManager.getInstance();
        String ProcessID = rootManager7.runCommand("ps -A | grep -E '" + packageName + "$' | head -n 1 | awk '{ print $2 }'").getMessage().replaceAll("\n", "");
        StringBuilder sb2 = new StringBuilder();
        sb2.append("[Frida]通过shell获取进程（tail=最后一行，head=第一行）PID：");
        sb2.append(ProcessID);
        Log.e("TAG", sb2.toString());
        inject(packageName, ProcessID, fridaAgentFile.getPath());

    }
    public  void inject(String packageName, File fridaAgentFile) {
        long start = System.currentTimeMillis();
        Log.e("TAG", "[Frida]等待目标应用运行...");
        while (!RootManager.getInstance().isProcessRunning(packageName)) {
            try {
                Thread.sleep(250L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (System.currentTimeMillis() - start > TimeUnit.SECONDS.toMillis(5L)) {
                Log.e("TAG", "[Frida]等待目标应用运行超时...");
                throw new RuntimeException("wait timeout for process spawn");
            }
        }
        RootManager rootManager = RootManager.getInstance();
        String ProcessID = rootManager.runCommand("ps | grep -E '" + packageName + "$' | tail -n 1 | awk '{ print $2 }'").getMessage().replaceAll("\n", "");
        StringBuilder sb = new StringBuilder();
        sb.append("微信PID：");
        sb.append(ProcessID);
        Log.e("TAG", sb.toString());
        inject(packageName, ProcessID, fridaAgentFile.getPath());
    }

    private void inject(String packageName,final String ProcessID, String agentPath) {
        Log.e("TAG", "[Frida]正在注入目标应用.PID:" + ProcessID + "..." + packageName);
        new Thread(new Runnable() { // from class: com.analy.fridainjector.FridaInjector.1
            @Override // java.lang.Runnable
            public void run() {
                Log.e("TAG", "[frida] agentPath:" + agentPath);
                RootManager rootManager = RootManager.getInstance();
                Result result = rootManager.runCommand(FridaInjector.this.mInjector.getPath() + " -p " + ProcessID + " -s " + agentPath);
                StringBuilder sb = new StringBuilder();
                sb.append("注入结果返回:");
                sb.append(result.getMessage());
                Log.e("TAG", sb.toString());
            }
        }).start();
    }

    public static class Builder {
        private final Context mContext;
        private String mArmBinaryPath;
        private String mArm64BinaryPath;
        private String mX86BinaryPath;
        private String mX86_64BinaryPath;

        private File mInjector;

        public Builder(Context context) {
            if (!RootManager.getInstance().hasRooted()) {
                throw new RuntimeException("must run on a rooted device");
            }
            if (!RootManager.getInstance().obtainPermission()) {
                throw new RuntimeException("failed to obtain root permissions");
            }

            mContext = context;
        }

        public Builder withArmInjector(String armInjectorBinaryAssetName) {
            mArmBinaryPath = armInjectorBinaryAssetName;
            return this;
        }

        public Builder withArm64Injector(String arm64InjectorBinaryAssetName) {
            mArm64BinaryPath = arm64InjectorBinaryAssetName;
            return this;
        }

        public Builder withX86Injector(String x86InjectorBinaryAssetName) {
            mX86BinaryPath = x86InjectorBinaryAssetName;
            return this;
        }

        public Builder withX86_64Injector(String x86_64InjectorBinaryAssetName) {
            mX86_64BinaryPath = x86_64InjectorBinaryAssetName;
            return this;
        }

        public FridaInjector build() throws IOException {
            if (mArmBinaryPath == null && mArm64BinaryPath == null &&
                    mX86BinaryPath == null && mX86_64BinaryPath == null) {
                throw new RuntimeException("injector asset file name not provided");
            }

            String arch = getArch();
            String injectorName = null;
            switch (arch) {
                case "arm":
                    injectorName = mArmBinaryPath;
                    break;
                case "arm64":
                    injectorName = mArm64BinaryPath;
                    break;
                case "x86":
                    injectorName = mX86BinaryPath;
                    break;
                case "x86_64":
                    injectorName = mX86_64BinaryPath;
                    break;
            }

            if (injectorName == null) {
                throw new RuntimeException("injector binary not provided for arch: " + arch);
            }

            mInjector = extractInjectorIfNeeded(mContext, injectorName);
            return new FridaInjector(this);
        }

        private File getInjector() {
            return mInjector;
        }
    }

    private static File extractInjectorIfNeeded(Context context, String name) throws IOException {
        File injectorPath = new File(context.getFilesDir(), "injector");
        File injector = new File(injectorPath, name);

        if (!injectorPath.exists()) {
            injectorPath.mkdir();
        } else {
            File[] files = injectorPath.listFiles();
            if (files != null && files.length > 0) {
                if (files[0].getName().equals(name)) {
                    return injector;
                }
                files[0].delete();
            }
        }

        Utils.extractAsset(context, name, injector);
        RootManager.getInstance().runCommand("chmod 777 " + injector.getPath());
        return injector;
    }

    private static String getArch() {
        for (String androidArch : Build.SUPPORTED_ABIS) {
            switch (androidArch) {
                case "arm64-v8a": return "arm64";
                case "armeabi-v7a": return "arm";
                case "x86_64": return "x86_64";
                case "x86": return "x86";
            }
        }

        throw new RuntimeException("Unable to determine arch from Build.SUPPORTED_ABIS =  " +
                Arrays.toString(Build.SUPPORTED_ABIS));
    }
}