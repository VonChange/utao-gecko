package com.vonchange.utao.gecko;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;

public class MainBaseActivity extends Activity {

    private static String TAG="MainBaseActivity";
    private static GeckoView view;

    private static GeckoRuntime sRuntime;
    public static GeckoSession session;
   // private WebExtension webExtension;
    private static final String EXTENSION_LOCATION = "resource://android/assets/tv-web/";
    private static final String EXTENSION_ID = "utao@163.com";
    // If you make changes to the extension you need to update this
    private static final String EXTENSION_VERSION = "1.0";
    private  static WebExtension.Port webPort;

    //@SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.geckoview);
        preInitWebView();
        session.setNavigationDelegate(new NextPlusNavigationDelegate(this));

        //NextPlusNavigationDelegate navigation = new NextPlusNavigationDelegate(this);
        view.setSession(session);
        //extension.setMessageDelegate(messageDelegate, "browser"),
        //file://android_asset/index.html resource://android/assets/tv-web/index.html
        //resource://android/assets/tv-web/
        Log.i(TAG,"loadUriloadUriloadUri");
       // session.loadUri(webExtension.metaData.baseUrl+"index.html"); // Or any other URL...
    }



    public   static void postMessage(String service, String data) {
        try {
            JSONObject message = new JSONObject();
            message.put("service", service);
            message.put("data", data);
            Log.i(TAG,"mPort.postMessage：" + webPort+message);
            if(null!=webPort) {
                webPort.postMessage(message);
            }
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }
    protected  void message(String service,String data){

    }

    private WebExtension.MessageDelegate initMessage() {
        WebExtension.PortDelegate portDelegate = new WebExtension.PortDelegate() {
            public WebExtension.Port port = null;

            public void onPortMessage(final Object message,
                                      final WebExtension.Port port) {
                // This method will be called every time a message is sent from the
                // extension through this port. For now, let's just log a
                // message.
                JSONObject jsonObject = (JSONObject) message;
                Log.d("PortDelegate", "Received message from WebExtension: "
                        + jsonObject);
                try {
                    message(jsonObject.getString("service"),jsonObject.getString("data"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            public void onDisconnect(final WebExtension.Port port) {
                // After this method is called, this port is not usable anymore.
               // if (port == mPort) {
                  //  mPort = null;
               // }
            }
        };

        // This delegate will handle requests to open a port coming from the
        // extension
        WebExtension.MessageDelegate messageDelegate = new WebExtension.MessageDelegate() {

            public void onConnect(final WebExtension.Port port) {
                // Let's store the Port object in a member variable so it can be
                // used later to exchange messages with the WebExtension.
                webPort = port;
                Log.i(TAG, "onConnect: "+webPort);
                // Registering the delegate will allow us to receive messages sent
                // through this port.
                webPort.setDelegate(portDelegate);
            }
        };
        return messageDelegate;
    }

    private void preInitWebView() {
        session = getGeckoSession();
        GeckoSessionSettings settings = session.getSettings();
        settings.setAllowJavascript(true);
        settings.setDisplayMode(GeckoSessionSettings.DISPLAY_MODE_FULLSCREEN);
        //USER_AGENT_MODE_MOBILE
        settings.setUserAgentMode(GeckoSessionSettings.USER_AGENT_MODE_DESKTOP);//?
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.0.0";
        //"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";
        settings.setUserAgentOverride(userAgent);
//.allowInsecureConnections(ALLOW_ALL)
        if (sRuntime == null) {
            // GeckoRuntime can only be initialized once per process
            sRuntime = GeckoRuntime.create(this);
        }
        sRuntime.getSettings().setConsoleOutputEnabled(true)
                //.setAboutConfigEnabled(true)
                .setAllowInsecureConnections(GeckoRuntimeSettings.ALLOW_ALL)
                //.setAutomaticFontSizeAdjustment(true)
                //.setRemoteDebuggingEnabled(true)
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
                        extension -> {
                            Log.i(TAG, "Extension installed: " + extension);
                            runOnUiThread(() -> {
                                assert extension != null;
                                extension.setMessageDelegate(messageDelegate, "browser");
                                session.loadUri(extension.metaData.baseUrl+"index.html");
                            });
                        },
                        e -> Log.e(TAG, "Error registering WebExtension", e)
                );
            /*    .accept(
                        extension -> Log.i("MessageDelegate", "Extension installed: " + extension),
                        e -> Log.e("MessageDelegate", "Error registering WebExtension", e)
                );*/
        session.open(sRuntime);
    }

    private  GeckoSession getGeckoSession() {
        session = new GeckoSession();
    /*    session.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override
            public void onFullScreen(GeckoSession session, boolean fullScreen) {
                Log.i(TAG,"fullScreen:::"+fullScreen);
                if (fullScreen) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
                else
                {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    session.exitFullScreen();
                }
            }
        });*/
        //GeckoSession.PermissionDelegate.PERMISSION_MEDIA_KEY_SYSTEM_ACCESS
        session.setPermissionDelegate(new GeckoSession.PermissionDelegate() {
            @Override
            public void onMediaPermissionRequest(GeckoSession session, String uri, MediaSource[] video, MediaSource[] audio, MediaCallback callback) {
                Log.i("gecko", "Media Permission Needed" + uri);
                GeckoSession.PermissionDelegate.super.onMediaPermissionRequest(session, uri, video, audio, callback);
                //callback.grant(null, audio[MediaSource.TYPE_VIDEO]);
            }

            @Override
            public GeckoResult<Integer> onContentPermissionRequest(GeckoSession session, ContentPermission contentPermission) {//String uri, int type,  Callback callback
                Log.i("gecko", "nContentPermission Permission Needed " + contentPermission.uri + " " + contentPermission.permission);
                // callback.grant();
                // GeckoSession.PermissionDelegate.super.onContentPermissionRequest(session, uri, type, callback);
                return GeckoResult.fromValue(ContentPermission.VALUE_ALLOW);
            }

            /*  @Override
              public void onContentPermissionRequest(GeckoSession session, String uri, int type,  Callback callback ) {
                  Log.i("gecko", "nContentPermission Permission Needed "+uri+" "+type);
                  callback.grant();
              }*/
            @Override
            public void onAndroidPermissionsRequest(GeckoSession session, String[] permissions, Callback callback) {
                Log.i("gecko", "AndroidPermission Permission Needed" + permissions.toString());
                callback.grant();
                //GeckoSession.PermissionDelegate.super.onAndroidPermissionsRequest(session, permissions, callback);
            }
        });

        return session;
    }
}
