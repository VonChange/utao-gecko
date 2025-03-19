package com.vonchange.utao.gecko.api;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.vonchange.utao.gecko.MyApplication;
import com.vonchange.utao.gecko.util.AppVersionUtils;
import com.vonchange.utao.gecko.util.HttpUtil;
import com.vonchange.utao.gecko.util.Util;

import java.text.MessageFormat;
import java.util.HashMap;



public class ConfigApi {
    public static final String  apiHost="http://api.vonchange.com";
    private static final  String updateUrl=apiHost+"/utao/config/update.json";
    private static String firstUpX5Ok ="";
    public static void  syncLogData(Context context){
       if("".equals(firstUpX5Ok)){
            new Thread(()->{
                getConfig();
                firstUpX5Ok="1";
               //ValueUtil.putString(context,"firstUpX5Ok","1");
            }).start();
       }
    }


    public static void getConfig(){
        String json;
        try {
             String androidId= MyApplication.androidId;
             String num="32";
             if(Util.is64()){
                 num="64";
             }
             int api = Build.VERSION.SDK_INT;
            String remark=  "Fox_"+Build.MANUFACTURER+"_"+Build.MODEL+"_"+Build.VERSION.RELEASE+"_"+
                    AppVersionUtils.getVersionCode();
            String paramStr=   MessageFormat.format("?id={0}&num={1}&api={2}&remark={3}&type=1",androidId,num,api,remark);
            String reqUrl =updateUrl+paramStr;
            Log.i("getConfig","reqUrl "+reqUrl);
             json = HttpUtil.getJson(reqUrl,new HashMap<>());
        }catch (Exception e){
            return  ;
        }
        Log.i("getConfig","getConfig "+json);
    }

}
