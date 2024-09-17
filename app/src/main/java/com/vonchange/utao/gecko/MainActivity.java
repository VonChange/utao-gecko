package com.vonchange.utao.gecko;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.gecko.util.ThreadUtils;
import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;

import java.util.List;

public class MainActivity extends Activity {
    private static GeckoRuntime sRuntime;
    private static  GeckoView view;

    private static final String EXTENSION_LOCATION = "resource://android/assets/messaging/";
    private static final String EXTENSION_ID = "utao@163.com";
    // If you make changes to the extension you need to update this
    private static final String EXTENSION_VERSION = "1.0";
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
        settings.setUserAgentMode(GeckoSessionSettings.USER_AGENT_MODE_MOBILE);//?
        String userAgent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";
        settings.setUserAgentOverride(userAgent);
//.allowInsecureConnections(ALLOW_ALL)
        if (sRuntime == null) {
            // GeckoRuntime can only be initialized once per process
            sRuntime = GeckoRuntime.create(this);
        }
        // Let's make sure the extension is installed
        sRuntime
                .getWebExtensionController()
                .ensureBuiltIn(EXTENSION_LOCATION, EXTENSION_ID)
                .accept(
                        extension -> Log.i("MessageDelegate", "Extension installed: " + extension),
                        e -> Log.e("MessageDelegate", "Error registering WebExtension", e)
                );
        session.open(sRuntime);
        view.setSession(session);
        //https://tv.cctv.com/live/cctv1/ about:buildconfig
        //https://www.ixigua.com/7405917477189714469?logTag=2701e1cad4c007fe299
        //https://www.yangshipin.cn/tv/home?pid=600002475
        //file://android_asset/index.html resource://android/assets/tv-web/index.html
        //https://v.qq.com/biu/u/history
        session.loadUri("https://v.qq.com/biu/u/history"); // Or any other URL...
    }

    private static @NonNull GeckoSession getGeckoSession() {
        GeckoSession session = new GeckoSession();
        session.setPermissionDelegate(new GeckoSession.PermissionDelegate() {
            @Override
            public void onMediaPermissionRequest(@NonNull GeckoSession session, @NonNull String uri, @Nullable MediaSource[] video, @Nullable MediaSource[] audio, @NonNull MediaCallback callback) {
                Log.i("gecko", "Media Permission Needed"+uri);
                GeckoSession.PermissionDelegate.super.onMediaPermissionRequest(session, uri, video, audio, callback);
                //callback.grant(null, audio[MediaSource.TYPE_VIDEO]);
            }

            @Override
            public void onContentPermissionRequest(@NonNull GeckoSession session, @Nullable String uri, int type, @NonNull Callback callback) {
                Log.i("gecko", "nContentPermission Permission Needed "+type+uri);
                callback.grant();
               // GeckoSession.PermissionDelegate.super.onContentPermissionRequest(session, uri, type, callback);
            }

            @Override
            public void onAndroidPermissionsRequest(@NonNull GeckoSession session, @Nullable String[] permissions, @NonNull Callback callback) {
                Log.i("gecko", "AndroidPermission Permission Needed"+permissions.toString());
                callback.grant();
                //
                //GeckoSession.PermissionDelegate.super.onAndroidPermissionsRequest(session, permissions, callback);
            }
        });
// Workaround for Bug 1758212 Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36
       // session.setContentDelegate(new GeckoSession.ContentDelegate() {});
       /* session.setNavigationDelegate(new GeckoSession.NavigationDelegate(){
            @Override
            public GeckoResult<GeckoSession> onNewSession(GeckoSession session, String uri) {
                Log.i("gecko", "onNewSession"+uri);
                GeckoSession newSession = new GeckoSession();
                //[your GeckoView instance].setSession(newSession);
                view.setSession(newSession);
                return GeckoResult.fromValue(newSession);
            }
            @Override
            public  GeckoResult<AllowOrDeny> onLoadRequest(@NonNull final GeckoSession session,
                                                   @NonNull final LoadRequest request) {
                Log.i("gecko", "TARGET_WINDOW_NEW"+request.uri);
                if (request.target == GeckoSession.NavigationDelegate.TARGET_WINDOW_NEW) {
                    session.loadUri(request.uri);
                }
                return GeckoResult.fromValue(AllowOrDeny.ALLOW);
            }
            public void onLocationChange(@NonNull GeckoSession session, @Nullable String url, final @NonNull List<GeckoSession.PermissionDelegate.ContentPermission> perms) {
                Log.i("gecko", "onLocationChange "+url);
                session.getNavigationDelegate().onLocationChange(session, url);
            }
            public void onCanGoForward(@NonNull final GeckoSession session, final boolean canGoForward) {
                Log.i("gecko", "canGoForward "+canGoForward);
            }
        });*/
        return session;
    }
}
