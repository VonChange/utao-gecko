package com.vonchange.utao.gecko;


import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.vonchange.utao.gecko.service.CrashHandler;

import java.util.UUID;

public class MyApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    public static  String androidId=null;
    private static final String TAG = "MyApplication";
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
        androidId = Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);
        if(null==androidId){
            Log.i(TAG, "androidId: getUUID");
            androidId=getUUID();
        }
        androidId="0"+androidId;
    }
    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        allErrorCatch();
        CrashHandler.getInstance().init(this);
        CrashHandler.uploadExceptionToServer(this);
        //preInitWebView();
    }

    private void allErrorCatch(){
        final Thread.UncaughtExceptionHandler systemDefault = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                // 检查是否为 SurfaceTexture 相关异常
                if (throwable != null && throwable.getMessage() != null &&
                        (throwable instanceof NullPointerException) &&
                        (throwable.getStackTrace() != null && throwable.getStackTrace().length > 0 &&
                                containsSurfaceTextureInStackTrace(throwable.getStackTrace()))) {

                    Log.e("Application", "捕获到 SurfaceTexture 相关异常: " + throwable.getMessage());

                    // 记录非致命异常但不终止应用
                    CrashHandler.recordNonFatal(getApplicationContext(), throwable);
                    return;
                }

                // 其他异常，交给系统默认处理器，避免递归
                if (systemDefault != null) {
                    systemDefault.uncaughtException(thread, throwable);
                } else {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }

            // 检查堆栈跟踪是否包含 SurfaceTexture 相关内容
            private boolean containsSurfaceTextureInStackTrace(StackTraceElement[] stackTrace) {
                for (StackTraceElement element : stackTrace) {
                    if (element.getClassName().contains("SurfaceTexture") ||
                            element.getMethodName().contains("SurfaceTexture")) {
                        return true;
                    }
                }
                return false;
            }
        });
    }
    public static String getUUID() {
        String serial = null;
        String m_szDevIDShort = "随机两位数" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 位
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                serial = "默认值";
            } else {
                serial = Build.SERIAL;
            }
            //API>=9 使用serial号
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            //serial需要一个初始化
            serial = "默认值"; // 随便一个初始化
        }
        //使用硬件信息拼凑出来的15位号码
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

}
