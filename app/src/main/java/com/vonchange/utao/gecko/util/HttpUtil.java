package com.vonchange.utao.gecko.util;

import android.util.Log;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HttpUtil {
    private static String TAG = "HttpUtil";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    private static final int DEFAULT_TIMEOUT = 5;
    private static OkHttpClient defaultClient = null;
    private static OkHttpClient noRedirectClient = null;
    private static final Object lockO = new Object();
    static {
        defaultClient();
        noRedirectClient();
    }
/*    public static OkHttpClient defaultClient() {
        synchronized (lockO) {
            if (defaultClient == null) {
                OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                        .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                        .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .sslSocketFactory(new SSLSocketFactoryCompat(SSLSocketFactoryCompat.trustAllCert), SSLSocketFactoryCompat.trustAllCert);
                defaultClient = builder.build();
            }
            return defaultClient;
        }
    }*/
    public static void defaultClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .sslSocketFactory(new SSLSocketFactoryCompat(SSLSocketFactoryCompat.trustAllCert), SSLSocketFactoryCompat.trustAllCert);
          defaultClient = builder
                  //.dns(new HttpDns())
                  .build();
    }
    public static boolean isErrorResponse(String json){
        if(!json.startsWith("{")||json.equals("400")||json.equals("500")){
            return true;
        }
        return false;
    }
    public static void noRedirectClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(true)
                .sslSocketFactory(new SSLSocketFactoryCompat(SSLSocketFactoryCompat.trustAllCert), SSLSocketFactoryCompat.trustAllCert);
        noRedirectClient = builder
               // .dns(new HttpDns())
                .build();
    }
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static String postJson(String url,
                            Map<String, String> headerMap, String requestBody) {
        RequestBody body = RequestBody.create(JSON, requestBody);
        Request.Builder builder =new Request.Builder()
                .url(url);
        if(null!=headerMap&&!headerMap.isEmpty()){
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                Log.i(TAG,entry.getKey()+" "+entry.getValue());
                if(entry.getKey().equals("tv-ref")){
                    builder.addHeader("Referer",entry.getValue());
                    continue;
                }
                builder.addHeader(entry.getKey(),entry.getValue());
            }
        }
        Request request = builder
                .post(body)
                .build();
       return responseDo(request);
    }

    public static   String getJson(String url,
                           Map<String, String> headerMap){
        Request.Builder builder =new Request.Builder()
                .url(url);
        if(null!=headerMap&&!headerMap.isEmpty()){
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                Log.i(TAG,entry.getKey()+" "+entry.getValue());
                if(entry.getKey().equals("tv-ref")){
                    builder.addHeader("Referer",entry.getValue());
                    continue;
                }
                builder.addHeader(entry.getKey(),entry.getValue());
            }
        }
        Request request = builder
                .get()
                .build();
        return responseDo(request);
    }

    private static String responseDo(Request request){
        Response response =null;
        try{
            response = defaultClient.newCall(request).execute();
        }catch (IOException e){
            e.printStackTrace();
            Log.e(TAG,"500 error: "+e.getMessage());
            return "500";
        }
        if(!response.isSuccessful()){
            Log.e(TAG,"400 code: "+response.code());
            return "400";
        }
        try{
            return  response.body().string();
        }catch (IOException e){
            e.printStackTrace();
            Log.e(TAG," 500 error response: "+e.getMessage());
            return "500";
        }
    }

}
