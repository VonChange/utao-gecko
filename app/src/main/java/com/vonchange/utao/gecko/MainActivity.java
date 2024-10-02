package com.vonchange.utao.gecko;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Instrumentation;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;


public class MainActivity extends Activity {
    private static GeckoRuntime sRuntime;
    private static  GeckoView view;

    private static final String EXTENSION_LOCATION = "resource://android/assets/tv-web/";
    private static final String EXTENSION_ID = "utao@163.com";
    // If you make changes to the extension you need to update this
    private static final String EXTENSION_VERSION = "1.0";
    private WebExtension.Port mPort;
    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.geckoview);
        GeckoSession session = getGeckoSession();
        GeckoSessionSettings settings= session.getSettings();
        settings.setAllowJavascript(true);
        //USER_AGENT_MODE_MOBILE
        settings.setUserAgentMode(GeckoSessionSettings.USER_AGENT_MODE_DESKTOP);//?
        String userAgent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.0.0";
                //"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";
        settings.setUserAgentOverride(userAgent);

//.allowInsecureConnections(ALLOW_ALL)
        if (sRuntime == null) {
            // GeckoRuntime can only be initialized once per process
            sRuntime = GeckoRuntime.create(this);
        }
        sRuntime.getSettings().setConsoleOutputEnabled(true)
                      //  .setAboutConfigEnabled(true)
                .setFontInflationEnabled(true)
                .setWebFontsEnabled(true);
                //.setExtensionsProcessEnabled(true)
                //.setExtensionsWebAPIEnabled(true);
        // Let's make sure the extension is installed
        WebExtension.MessageDelegate messageDelegate= initMessage();
        sRuntime
                .getWebExtensionController()
                .ensureBuiltIn(EXTENSION_LOCATION, EXTENSION_ID)
                .accept(
                        extension -> extension.setMessageDelegate(messageDelegate, "browser"),
                        e -> Log.e("MessageDelegate", "Error registering WebExtension", e)
                );
        session.open(sRuntime);
        view.setSession(session);
       /* view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return keyDown(v,keyCode,event);
            }
        });*/
        //https://tv.cctv.com/live/cctv1/ about:buildconfig
        //https://www.ixigua.com/7405917477189714469?logTag=2701e1cad4c007fe299
        //https://www.yangshipin.cn/tv/home?pid=600002475
        //file://android_asset/index.html resource://android/assets/tv-web/index.html
        //https://v.qq.com/biu/u/history
        //https://v.qq.com/x/cover/mzc00200q4aajw9/a4100yfzbi5.html
        session.loadUri("resource://android/assets/tv-web/index.html"); // Or any other URL...
    }
    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            Log.i("keyDown", "keyDown"+keyCode);
            // 使用switch语句来检查按下的键码
            switch (keyCode){
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    onKeyEvent(KeyEvent.KEYCODE_D);
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    onKeyEvent(KeyEvent.KEYCODE_A);
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    onKeyEvent(KeyEvent.KEYCODE_S);
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    onKeyEvent(KeyEvent.KEYCODE_W);
                    break;
                case KeyEvent.KEYCODE_MENU:
                    onKeyEvent(KeyEvent.KEYCODE_R);
                    break;
                default:
                    //return true;
                    super.dispatchKeyEvent(event);
                    break;
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /*private boolean keyDown(View v, int keyCode, KeyEvent event){
        //按下
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            Log.i("keyDown", "keyDown"+keyCode);
            //onKeyEvent(KeyEvent.KEYCODE_D);
            switch (keyCode){
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    onKeyEvent(KeyEvent.KEYCODE_D);
                    break;
                default:
                    super.dispatchKeyEvent(event);
                    break;
            }
            return true;
          *//*  if(null!=mPort){
               // mPort.postMessage(msg(event));
                return true;
            }*//*
     *//*           switch(keyCode){
                case KeyEvent.KEYCODE_BACK:
                    mPort.postMessage(msg(event));
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    mPort.postMessage(msg(event));
                    break;
                default:
                    return super.dispatchKeyEvent(event);
            }*//*
        }
        return true;
    }*/
    private JSONObject msg(KeyEvent event){

        JSONObject message = new JSONObject();
        try {
            message.put("service", "keyDown");
            message.put("keyCode", KeyEvent.keyCodeToString(event.getKeyCode()));
            //message.put("event", KeyEvent.keyCodeToString(event.getKeyCode()));
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
        return message;
    }
    public static void onKeyEvent(final int keyCode) {
        new Thread() {
            public void run() {
                try {
                    Log.i("onKeyEvent", "onKeyEvent"+keyCode);
                    Instrumentation inst = new Instrumentation();
                    //inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                    inst.sendKeyDownUpSync(keyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private  WebExtension.MessageDelegate  initMessage(){
        WebExtension.PortDelegate portDelegate = new WebExtension.PortDelegate() {
            public WebExtension.Port port = null;

            public void onPortMessage(final @NonNull Object message,
                                      final @NonNull WebExtension.Port port) {
                // This method will be called every time a message is sent from the
                // extension through this port. For now, let's just log a
                // message.
                Log.d("PortDelegate", "Received message from WebExtension: "
                        + message);
            }

            public void onDisconnect(final @NonNull WebExtension.Port port) {
                // After this method is called, this port is not usable anymore.
                if (port == mPort) {
                    mPort = null;
                }
            }
        };

        // This delegate will handle requests to open a port coming from the
        // extension
        WebExtension.MessageDelegate messageDelegate = new WebExtension.MessageDelegate() {
            @Nullable
            public void onConnect(final @NonNull WebExtension.Port port) {
                // Let's store the Port object in a member variable so it can be
                // used later to exchange messages with the WebExtension.
                mPort = port;

                // Registering the delegate will allow us to receive messages sent
                // through this port.
                mPort.setDelegate(portDelegate);
            }
        };
        return messageDelegate;
    }

    private static  GeckoSession getGeckoSession() {
        GeckoSession session = new GeckoSession();
        session.setPermissionDelegate(new GeckoSession.PermissionDelegate() {
            @Override
            public void onMediaPermissionRequest( GeckoSession session,  String uri,  MediaSource[] video,  MediaSource[] audio,  MediaCallback callback) {
                Log.i("gecko", "Media Permission Needed"+uri);
                GeckoSession.PermissionDelegate.super.onMediaPermissionRequest(session, uri, video, audio, callback);
                //callback.grant(null, audio[MediaSource.TYPE_VIDEO]);
            }

        /*    @Override
            public GeckoResult<Integer> onContentPermissionRequest(GeckoSession session, ContentPermission contentPermission ) {//String uri, int type,  Callback callback
                Log.i("gecko", "nContentPermission Permission Needed "+contentPermission.uri+" "+contentPermission.permission);
               // callback.grant();
               // GeckoSession.PermissionDelegate.super.onContentPermissionRequest(session, uri, type, callback);
               return GeckoResult.fromValue(ContentPermission.VALUE_ALLOW);
            }*/
            @Override
            public void onContentPermissionRequest(GeckoSession session, String uri, int type,  Callback callback ) {
              Log.i("gecko", "nContentPermission Permission Needed "+uri+" "+type);
                callback.grant();
            }
            @Override
            public void onAndroidPermissionsRequest( GeckoSession session,  String[] permissions,  Callback callback) {
                Log.i("gecko", "AndroidPermission Permission Needed"+permissions.toString());
                callback.grant();
                //GeckoSession.PermissionDelegate.super.onAndroidPermissionsRequest(session, permissions, callback);
            }
        });

        return session;
    }
}
