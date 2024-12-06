package com.vonchange.utao.gecko;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vonchange.utao.gecko.databinding.ActivityMainBinding;
import com.vonchange.utao.gecko.databinding.ItemHzBinding;
import com.vonchange.utao.gecko.databinding.ItemRateBinding;
import com.vonchange.utao.gecko.databinding.ItemXjBinding;
import com.vonchange.utao.gecko.domain.DetailMenu;
import com.vonchange.utao.gecko.domain.HzItem;
import com.vonchange.utao.gecko.domain.RateItem;
import com.vonchange.utao.gecko.domain.XjItem;
import com.vonchange.utao.gecko.impl.BaseBindingAdapter;
import com.vonchange.utao.gecko.impl.BaseViewHolder;
import com.vonchange.utao.gecko.impl.IBaseBindingPresenter;
import com.vonchange.utao.gecko.util.ConstantMy;
import com.vonchange.utao.gecko.util.FileUtil;
import com.vonchange.utao.gecko.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

public class MainBaseActivity extends Activity {
    private static  final String json="{\"now\":{\"play\":{\"enabled\":false,\"play\":true,\"btn\":\"暂停\"},\"rate\":{\"id\":\"3\",\"name\":\"正常\",\"isCurrent\":true},\"hz\":{\"id\":\"0\",\"name\":\"超清\",\"isVip\":false,\"level\":1080},\"dm\":{\"enabled\":false,\"name\":\"弹幕开\"},\"xj\":{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEFPawmPLPSzUQPK1EmpW4241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEFPawmPLPSzUQPK1EmpW4241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第1集\",\"index\":0,\"site\":\"cctv\"}},\"video\":true,\"hzs\":[{\"id\":\"0\",\"name\":\"超清\",\"isVip\":false,\"level\":1080},{\"id\":\"1\",\"name\":\"高清\",\"isVip\":false,\"level\":720},{\"id\":\"2\",\"name\":\"标清\",\"isVip\":false,\"level\":480},{\"id\":\"3\",\"name\":\"流畅\",\"isVip\":false,\"level\":480}],\"xjs\":[{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEFPawmPLPSzUQPK1EmpW4241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEFPawmPLPSzUQPK1EmpW4241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第1集\",\"index\":0,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDENlVbFmFvGpcJZjM5ie9b241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDENlVbFmFvGpcJZjM5ie9b241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第2集\",\"index\":1,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEf0UNIWk0LDNRJKSXrKvv241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEf0UNIWk0LDNRJKSXrKvv241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第3集\",\"index\":2,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEfOvUN4ByrzXCUnnCxJn4241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEfOvUN4ByrzXCUnnCxJn4241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第4集\",\"index\":3,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEky737CJjkECt0QFypznF241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEky737CJjkECt0QFypznF241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第5集\",\"index\":4,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEKjStsWu7GMjP0dXWlrIV241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEKjStsWu7GMjP0dXWlrIV241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第6集\",\"index\":5,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDErauRBBfbiFhCB5v8tKya241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDErauRBBfbiFhCB5v8tKya241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第7集\",\"index\":6,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEk5QNBZCrR6c3tUKQ5G0v241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEk5QNBZCrR6c3tUKQ5G0v241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第8集\",\"index\":7,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDE7Ax2EJBHIIv6NLkcLeQ1241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDE7Ax2EJBHIIv6NLkcLeQ1241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第9集\",\"index\":8,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDE4tjHWxAz9hrJy1vvQIum241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDE4tjHWxAz9hrJy1vvQIum241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第10集\",\"index\":9,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDECC5cQljnq3VJiNDVCIh2241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDECC5cQljnq3VJiNDVCIh2241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第11集\",\"index\":10,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDErEGIBT80OQka4D4zzqyR241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDErEGIBT80OQka4D4zzqyR241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第12集\",\"index\":11,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEoxYPNHkw8H7tpaJbQ8Ez241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEoxYPNHkw8H7tpaJbQ8Ez241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第13集\",\"index\":12,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEfNaEXnhDLGlHKnbSyIiA241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEfNaEXnhDLGlHKnbSyIiA241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第14集\",\"index\":13,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEyszGt3kQcMqF0Ler3aqN241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEyszGt3kQcMqF0Ler3aqN241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第15集\",\"index\":14,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEGAj71EmjkfJCE3HJrLT0241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEGAj71EmjkfJCE3HJrLT0241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第16集\",\"index\":15,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEWrwLZ4ecCNheHxKkGagB241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEWrwLZ4ecCNheHxKkGagB241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第17集\",\"index\":16,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEPnIaxsZfUwQrNtsGZPu1241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEPnIaxsZfUwQrNtsGZPu1241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第18集\",\"index\":17,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDE5yVCNlsqB66wGZeXQnXd241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDE5yVCNlsqB66wGZeXQnXd241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第19集\",\"index\":18,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEt9IO3Ef2XAUk9kJj5ckI241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEt9IO3Ef2XAUk9kJj5ckI241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第20集\",\"index\":19,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDE0teKgS3YSkLXdsMbU5e7241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDE0teKgS3YSkLXdsMbU5e7241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第21集\",\"index\":20,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEaeBa6l7jvzA3FT9wi7QD240909\",\"url\":\"https://tv.cctv.com/2024/09/09/VIDEaeBa6l7jvzA3FT9wi7QD240909.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第22集\",\"index\":21,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDET6ITKgxlsT90wnDHRphg241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDET6ITKgxlsT90wnDHRphg241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第23集\",\"index\":22,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEdSIeI5j8tboauPoHuxQ3241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEdSIeI5j8tboauPoHuxQ3241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第24集\",\"index\":23,\"site\":\"cctv\"}],\"rates\":[{\"id\":\"0\",\"name\":\"2x\",\"isCurrent\":false},{\"id\":\"1\",\"name\":\"1.5x\",\"isCurrent\":false},{\"id\":\"2\",\"name\":\"1.25x\",\"isCurrent\":false},{\"id\":\"3\",\"name\":\"正常\",\"isCurrent\":true},{\"id\":\"4\",\"name\":\"0.5x\",\"isCurrent\":false}],\"tab\":\"hz\",\"focusId\":\"tv\",\"isVip\":true}";
    private static String TAG="MainBaseActivity";
    private static GeckoView view;
    private static GeckoRuntime sRuntime;
    public static GeckoSession session;
    private static  String EXTENSION_LOCATION ="android/assets/tv-web/";
            //"resource://android/assets/tv-web/";
    private static String updateUrl;
    private static final String EXTENSION_ID = "utao@163.com";
    // If you make changes to the extension you need to update this
    private static final String EXTENSION_VERSION = "1.0";
    private  static WebExtension.Port webPort;
    protected ActivityMainBinding binding;
    //@SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //setContentView(R.layout.activity_main);
        //binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        bind();
        view = binding.geckoview;
        preInitWebView();
        session.setNavigationDelegate(new NextPlusNavigationDelegate(this));
        view.setSession(session);
        //extension.setMessageDelegate(messageDelegate, "browser"),
        //file://android_asset/index.html resource://android/assets/tv-web/index.html
       // session.loadUri(webExtension.metaData.baseUrl+"index.html"); // Or any other URL...
    }

    private AssetManager createAssetManager(String skinFilePath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, skinFilePath);
            return assetManager;
        } catch (Exception e) {
           throw  new RuntimeException(e);
           // return null;
        }
    }
    //https://github.com/VonChange/res/archive/refs/heads/master.zip
    private void updateRes(){
           //copyAssets 到应用目录
         String tvWebZip="tv-web";
         String  baseFolder= this.getExternalFilesDir(ConstantMy.UTAO).getPath();
         String toZipFilePath=baseFolder+"/"+tvWebZip+".xpi";
         File toZipFile =new File(toZipFilePath);
         if(!toZipFile.exists()){
             //不存在 copy assert 下文件
            // FileUtil.copyFileFromAssert(this,
                     //tvWebZip+".zip",toZipFilePath);
         }else{
             //校验线上版本 不一致 更新
         }
        FileUtil.copyFileFromAssert(this,
                "b7aad81f44464e2cae86-1.0.3.xpi",toZipFilePath);
         //解压
     /*   try {
            FileUtil.unzipFile(toZipFilePath,baseFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        updateUrl= new File(toZipFilePath).toURI()+"";
                //this.getExternalFilesDir(ConstantMy.UTAO).toURI()+"tv-web/";

                //"resource://android"+baseFolder+"/tv-web/";
                //baseFolder+"/tv-web";

    }
    private void bind(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMenuTitleHandler(new MenuTitleHandler());
        //webViewFocusChange();
        focusChange();
    }
    private  void webViewFocusChange(){
        binding.geckoview.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.i("webViewFocusChange","v "+v+" hasFocus "+hasFocus);
            }
        });
    }


    protected  boolean isMenuShow(){
        int visible=  binding.tvMenu.getVisibility();
        if(visible== View.VISIBLE){
           return true;
        }
        return false;
    }
    protected void showMenu(String data){
        binding.geckoview.setFocusable(false);
        //binding.tvMenu.setFocusable(true);
        Log.i(TAG,"data:: "+data);
        DetailMenu detailMenu = JsonUtil.fromJson(data,DetailMenu.class);
        binding.setMenu(detailMenu);
        xjBlind(detailMenu.getXjs());
        hzBind(detailMenu.getHzs());
        rateBind(detailMenu.getRates());
        //binding.nextBtn.setBackgroundResource(R.drawable.btnsel);
        binding.xjBtn.requestFocus();
        binding.tvMenu.setVisibility(View.VISIBLE);
        //binding.tvMenu.setFocusable(false);
    }

    protected void hideMenu(){
        binding.tvMenu.setVisibility(View.GONE);
        binding.geckoview.setFocusable(true);
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
                postMessage("app", "geckoview");
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
                //.installBuiltIn(EXTENSION_LOCATION)
                //.ens()
                .ensureBuiltIn(EXTENSION_LOCATION, EXTENSION_ID)
                .accept(
                        extension -> {
                            Log.i(TAG, "Extension installed: " + extension);
                            runOnUiThread(() -> {
                                assert extension != null;
                                extension.setMessageDelegate(messageDelegate, "browser");
                                session.loadUri(extension.metaData.baseUrl+"index.html");
                                binding.loading.setVisibility(View.GONE);
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
       // session.getWebExtensionController()
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

    //菜单
    private Button defaultFocusBtn(View oldFocus, View newFocus){
        if(!(newFocus instanceof Button)){
            return null;
        }
        if(null!=oldFocus){  oldFocus.setScaleX(1.0f); oldFocus.setScaleY(1.0f);}
        newFocus.setScaleX(1.1f);
        newFocus.setScaleY(1.1f);
        return (Button) newFocus;

    }
    private String  oldBtnTag(View oldFocus){
        if(!(oldFocus instanceof Button)){
            return null;
        }
        Object tagObj=oldFocus.getTag();
        if(null==tagObj){return null;}
        return  tagObj.toString();
    }

    private void focusChange() {
        View view = binding.tvMenu;
        view.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                Log.d(TAG, "onGlobalFocusChanged: oldFocus=" + oldFocus);
                Log.d(TAG, "onGlobalFocusChanged: newFocus=" + newFocus);
                //lastFocus=oldFocus;nowFocus=newFocus;
                Button focusBtn=defaultFocusBtn(oldFocus,newFocus);
                if(null==focusBtn){return;}
                Object tagObj=focusBtn.getTag();
                if(null==tagObj){return;}
                String tag= tagObj.toString();
                Log.d(TAG, "onGlobalFocusChanged: newFocus=" + tag);
                if(tag.startsWith("menu_")){
                    tag=tag.substring(5);
                    binding.getMenu().setTab(tag);
                }
                String oldTag=null;
                switch (tag){
                    case "hzItem":
                        focusBtn.setNextFocusUpId(R.id.hzBtn);
                        oldTag=oldBtnTag(oldFocus);
                        if(null!=oldTag){
                            if(oldTag.equals("menu_hz")){
                                RecyclerView.ViewHolder viewHolder = binding.hzsView.findViewHolderForLayoutPosition(0);
                                if(null!=viewHolder){
                                    viewHolder.itemView.requestFocus();
                                }
                            }
                        }
                        break;
                    case "rateItem":
                        focusBtn.setNextFocusUpId(R.id.rateBtn);
                        oldTag=oldBtnTag(oldFocus);
                        if(null!=oldTag){
                            if(oldTag.equals("menu_rate")){
                                RecyclerView.ViewHolder viewHolder = binding.ratesView.findViewHolderForLayoutPosition(0);
                                if(null!=viewHolder){
                                    viewHolder.itemView.requestFocus();
                                }
                            }
                        }
                        break;
                    case "xjItem":
                        LinearLayout layout = (LinearLayout) focusBtn.getParent();
                        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) layout.getLayoutParams();
                        int itemPosition = params.getViewLayoutPosition();
                        if(itemPosition<6){
                            focusBtn.setNextFocusUpId(R.id.xjBtn);
                        }
                        Log.d(TAG, "xjItem: index=" + itemPosition);
                        oldTag=oldBtnTag(oldFocus);
                        if(null!=oldTag){
                            //old上一个是选集btn 下一个是item 自动选择
                            if(oldTag.equals("menu_xj")){
                                int id =  binding.xjsView.getLayoutManager().getItemCount();
                                Log.i(TAG,"count "+ id+" "+ binding.xjsView.getChildCount()+" "+binding.xjsView.getAdapter().getItemCount());
                                int viewCount= binding.xjsView.getChildCount();
                                int num=binding.getMenu().getNow().getXj().getIndex();
                                if(num>viewCount){
                                    num=viewCount-1;
                                }
                                RecyclerView.ViewHolder viewHolder = binding.xjsView.findViewHolderForLayoutPosition(num);
                                if(null!=viewHolder){
                                    viewHolder.itemView.requestFocus();
                                }
                            }
                        }
                        break;
                    default:
                        Log.i(TAG,"setTab"+tag);
                        break;
                }
            }
        });
    }

    private void xjBlind(List<XjItem> xjItems){
        BaseBindingAdapter xjAdapter = new BaseBindingAdapter<XjItem, ItemXjBinding>(xjItems,R.layout.item_xj) {
            @Override
            public void doBindViewHolder(BaseViewHolder<ItemXjBinding> holder, XjItem item) {
                holder.getBinding().setVariable(BR.item, item);
                holder.getBinding().setVariable(BR.itemPresenter, ItemPresenter);
            }
        };
        xjAdapter.setItemPresenter(new XjBindPresenter());
        binding.xjsView
                .setLayoutManager(new GridLayoutManager(this, 6));
        binding.xjsView
                .setAdapter(xjAdapter);
    }
    private void hzBind(List<HzItem> hzItems){
        BaseBindingAdapter hzAdapter = new BaseBindingAdapter<HzItem, ItemHzBinding>(hzItems,R.layout.item_hz) {
            @Override
            public void doBindViewHolder(BaseViewHolder<ItemHzBinding> holder, HzItem item) {
                holder.getBinding().setVariable(BR.item, item);
                holder.getBinding().setVariable(BR.itemPresenter, ItemPresenter);
            }
        };
        hzAdapter.setItemPresenter(new HzBindPresenter());
        binding.hzsView
                .setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        binding.hzsView
                .setAdapter(hzAdapter);
    }

    private void rateBind(List<RateItem> rateItems){
        BaseBindingAdapter rateAdapter = new BaseBindingAdapter<RateItem, ItemRateBinding>(rateItems,R.layout.item_rate) {
            @Override
            public void doBindViewHolder(BaseViewHolder<ItemRateBinding> holder, RateItem item) {
                holder.getBinding().setVariable(BR.item, item);
                holder.getBinding().setVariable(BR.itemPresenter, ItemPresenter);
            }
        };
        rateAdapter.setItemPresenter(new RateBindPresenter());
        binding.ratesView
                .setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        binding.ratesView
                .setAdapter(rateAdapter);

    }

    public  class XjBindPresenter implements IBaseBindingPresenter {

        public void onClick(XjItem item) {
            Log.i(TAG,item.getTitle());
            //TestActivity.binding.getMenu().getNow().setXj(item);
            hideMenu();
            postMessage("click","xj-"+item.getId());

        }
    }

    public    class HzBindPresenter implements IBaseBindingPresenter {

        public void onClick(HzItem item) {
            Log.i(TAG,item.getName());
            hideMenu();
            postMessage("click","hz-"+item.getId());

        }
    }
    public  class RateBindPresenter implements IBaseBindingPresenter {

        public void onClick(RateItem item) {
            Log.i(TAG,item.getName());
            hideMenu();
            postMessage("click","rate-"+item.getId());

        }
    }
    public  class MenuTitleHandler {

        public void nextBtn() {
            hideMenu();
            postMessage("click","tv-next");
        }
        public void reloadBtn() {
             hideMenu();
             session.reload();
        }
    }

}
