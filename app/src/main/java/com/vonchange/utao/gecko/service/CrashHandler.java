package com.vonchange.utao.gecko.service;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import com.vonchange.utao.gecko.MyApplication;
import com.vonchange.utao.gecko.util.FileUtil;
import com.vonchange.utao.gecko.util.HttpUtil;
import com.vonchange.utao.gecko.util.JsonUtil;
import com.vonchange.utao.gecko.util.Util;
import com.vonchange.utao.gecko.util.ValueUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Create by ChenHao on 2018/6/299:30
 * use : 应用异常处理类
 * 使用方式： 在Application 中初始化  CrashHandler.getInstance().init(this);
 */
public class CrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    public static final boolean DEBUG = true;
    /**
     * 文件名
     */
    public static final String FILE_NAME = "crash";
    /**
     * 异常日志 存储位置为根目录下的 Crash文件夹
     */
   // private static final String PATH = Environment.getExternalStorageDirectory().getPath() +
        //    "/Crash/log/";
    /**
     * 文件名后缀
     */
    private static final String FILE_NAME_SUFFIX = ".trace";

    private static CrashHandler sInstance = new CrashHandler();
    private UncaughtExceptionHandler mDefaultCrashHandler;
    private Context mContext;


    private CrashHandler() {

    }

    public static CrashHandler getInstance() {
        return sInstance;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        //得到系统的应用异常处理器
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        //将当前应用异常处理器改为默认的
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context.getApplicationContext();

    }


    /**
     * 这个是最关键的函数，当系统中有未被捕获的异常，系统将会自动调用 uncaughtException 方法
     *
     * @param thread 为出现未捕获异常的线程
     * @param ex     为未捕获的异常 ，可以通过e 拿到异常信息
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        //导入异常信息到SD卡中
        try {
            String path= dumpExceptionToSDCard(ex);
            Log.e("PATH",path);
            ValueUtil.putString(mContext,"errorLog",path);
            ValueUtil.putString(mContext,"errorLogRead","0");
           //String error=  dumpExceptionToStr(ex);
            //这里可以上传异常信息到服务器，便于开发人员分析日志从而解决Bug
           // uploadExceptionToServer(error);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ex.printStackTrace();
        //如果系统提供了默认的异常处理器，则交给系统去结束程序，否则就由自己结束自己
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
            Process.killProcess(Process.myPid());
        }

    }

    private String dumpExceptionToStr(Throwable e){
        //得到当前年月日时分秒
        long current = System.currentTimeMillis();
        StringWriter stringWriter = new StringWriter();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
        //在定义的Crash文件夹下创建文件
        PrintWriter pw = new PrintWriter(new BufferedWriter(stringWriter));
        try{
            //写入时间
            pw.println(time);
            //写入手机信息
            dumpPhoneInfo(pw);
            pw.println();//换行
            e.printStackTrace(pw);
            pw.close();//关闭输入流
        } catch (Exception e1) {
            Log.e(TAG,"dump crash info failed");
        }finally {
            pw.close();//关闭输入流
        }
        StringBuffer buffer = stringWriter.getBuffer();
        return buffer.toString();
    }
    /**
     * 将异常信息写入SD卡
     *
     * @param e
     */
    private String dumpExceptionToSDCard(Throwable e) throws IOException{
        //如果SD卡不存在或无法使用，则无法将异常信息写入SD卡
      /*  if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (DEBUG) {
                Log.w(TAG, "sdcard unmounted,skip dump exception");
                return;
            }
        }*/
        File dir = mContext.getFilesDir();
        //如果目录下没有文件夹，就创建文件夹
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //得到当前年月日时分秒
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date(current));
        //在定义的Crash文件夹下创建文件
        String path= dir.getPath()+"/" + FILE_NAME + time + FILE_NAME_SUFFIX;
        File file = new File(path);

        try{
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            //写入时间
            pw.println(time);
            //写入手机信息
            dumpPhoneInfo(pw);
            pw.println();//换行
            e.printStackTrace(pw);
            pw.close();//关闭输入流
        } catch (Exception e1) {
           Log.e(TAG,"dump crash info failed");
        }
        return path;
    }

    /**
     * 获取手机各项信息
     * @param pw
     */
    private void dumpPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
       //得到包管理器
        PackageManager pm = mContext.getPackageManager();
        //得到包对象
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),PackageManager.GET_ACTIVITIES);
        //写入APP版本号
        String androidId= MyApplication.androidId;
        String num="32";
        if(Util.is64()){
            num="64";
        }
        int api = Build.VERSION.SDK_INT;
        pw.print("SYS API: ");
        pw.println(androidId+" "+num+ " "+ api);
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print("_");
        pw.println(pi.versionCode);
        //写入 Android 版本号
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print("_");
        pw.println(Build.VERSION.SDK_INT);
        //手机制造商
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);
        //手机型号
        pw.print("Model: ");
        pw.println(Build.MODEL);
        //CPU架构
        pw.print("CPU ABI: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pw.println(JsonUtil.toJson(Build.SUPPORTED_ABIS));
        }else {
            pw.println(Build.CPU_ABI);
        }

    }

    /**
     * 将错误信息上传至服务器
     */
    public static void uploadExceptionToServer(Context context)  {
        //Log.e("ERORRR",content);
        //HttpUtil.postJson("http://api.vonchange.com/utao/error",null,content);
        //String body=  HttpRequest.post("http://api.vonchange.com/utao/error").send(content).body();
       // Log.e("body",body);
       String errorRead=  ValueUtil.getString(context,"errorLogRead");
       if("0".equals(errorRead)){
          String path=  ValueUtil.getString(context,"errorLog");
           String errLog=null;
          try{
               errLog=  FileUtil.getStringFromInputStream(new FileInputStream(new File(path)));
          }catch (Exception e){
               errLog=null;
          }
          if(null==errLog){
              return;
          }
           Log.e("errLog",errLog);
           String finalErrLog = errLog;
           new Thread(()->{
               //HttpRequest.post("http://api.vonchange.com/utao/error").send(finalErrLog).body();

               HttpUtil.postJson("http://api.vonchange.com/utao/error",null, finalErrLog);
               Log.i("POST","http://api.vonchange.com/utao/error");
               ValueUtil.putString(context,"errorLogRead","1");
           }).start();
       }
    }
}