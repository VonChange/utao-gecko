package com.vonchange.utao.gecko;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Build;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.vonchange.utao.gecko.api.ConfigApi;
import com.vonchange.utao.gecko.databinding.ActivityMainBinding;
import com.vonchange.utao.gecko.domain.live.Live;
import com.vonchange.utao.gecko.domain.live.Vod;
import com.vonchange.utao.gecko.service.UpdateService;
import com.vonchange.utao.gecko.util.LogUtil;
import com.vonchange.utao.gecko.util.ToastUtils;
import com.vonchange.utao.gecko.util.ValueUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class MainBaseActivity extends Activity {
    private static String TAG="MainBaseActivity";
    private static GeckoView view;
    private static GeckoRuntime sRuntime;
    public static GeckoSession session;
    private static  String EXTENSION_LOCATION ="resource://android/assets/tv-web/";
            //"resource://android/assets/tv-web/";
    private static String updateUrl;
    private static final String EXTENSION_ID = "utao@163.com";
    // If you make changes to the extension you need to update this
    private static final String EXTENSION_VERSION = "1.0";
    private  static WebExtension.Port webPort;
    protected ActivityMainBinding binding;
    private Context thisContext;
    private static Vod currentLive = null;
    private List<Live> provinces = new ArrayList<>();
    private int currentProvinceIndex = 0;
    private static boolean isMenuShow=false;
    private static String  baseUrl;
    private AudioManager audioManager;
    private AudioManager.AudioPlaybackCallback audioPlaybackCallback;
    private MediaSession mediaSession;
    //@SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        // 强制横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        bind();
        ConfigApi.syncLogData(this);
        UpdateService.initTvData();
        thisContext=this;
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setupAndroidMediaSession();
        if(null==currentLive){
            currentLive = currentChannel(this);
            //UpdateService.getByKey("0_0");
        }
        if(null==currentLive){
            ToastUtils.show(this,"获取数据错误 请重启", Toast.LENGTH_SHORT);
            //finish();
            return;
        }
        initData();
        view = binding.geckoview;
        preInitWebView();
        session.setNavigationDelegate(new NextPlusNavigationDelegate(this));
        progress();
        view.setSession(session);
        ToastUtils.show(this,"遥控器上下左右可快速切台",Toast.LENGTH_SHORT);
        // 或者如果使用旧的 ActionBar
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        //extension.setMessageDelegate(messageDelegate, "browser"),
        //file://android_asset/index.html resource://android/assets/tv-web/index.html
       // session.loadUri(webExtension.metaData.baseUrl+"index.html"); // Or any other URL...
    }
    @Override
    protected void onStart() {
        super.onStart();
        registerAudioPlaybackMonitor();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterAudioPlaybackMonitor();
        updatePlaybackState(false, PlaybackState.PLAYBACK_POSITION_UNKNOWN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterAudioPlaybackMonitor();
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
            mediaSession = null;
        }
    }
    private void progress(){
        session.setProgressDelegate(new GeckoSession.ProgressDelegate() {
            @Override
            public void onProgressChange(GeckoSession session, int progress) {
                // progress 为当前加载进度（0-100）
                Log.d("GeckoProgress", "加载进度：" + progress + "%");
                // 可在此更新 UI（如进度条）
                isMenuShow=false;
                String url= NextPlusNavigationDelegate.currentUrl;
                try {
                    url=  URLDecoder.decode(url, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                }
                if(url.startsWith("moz-extension")){
                    url=url.replace("moz-extension://","");
                    url=url.substring(url.indexOf("/")+1);
                }
                LogUtil.i(TAG,"onProgressChangedX"+url);
                Vod vod = UpdateService.getByUrl(url);
                if(null!=vod){
                    currentLive=vod;
                    binding.liveName.setText(currentLive.getName()+" "+progress+"%");
                }
                if(progress==100){
                    updateChannel(thisContext,url);
                    handler.sendMessageDelayed (handler.obtainMessage(2, "noText"),1000);
                }
            }

            @Override
            public void onPageStart(GeckoSession session, String url) {
                // 页面开始加载时触发（进度开始）
            }

            @Override
            public void onPageStop(GeckoSession session, boolean success) {
                // 页面加载结束时触发（成功/失败）
                if (success) {
                    Log.d("GeckoProgress", "加载完成");
                }
            }
        });
    }
    private void setupAndroidMediaSession() {
        mediaSession = new MediaSession(this, "GeckoWebPlayer");
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(false);
    }

    private void updatePlaybackState(boolean isPlaying, long positionMs) {
        int state = isPlaying ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED;
        PlaybackState.Builder builder = new PlaybackState.Builder()
                .setActions(
                        PlaybackState.ACTION_PLAY |
                        PlaybackState.ACTION_PAUSE |
                        PlaybackState.ACTION_PLAY_PAUSE |
                        PlaybackState.ACTION_STOP |
                        PlaybackState.ACTION_SEEK_TO
                )
                .setState(state, positionMs, isPlaying ? 1f : 0f);
        if (mediaSession != null) {
            mediaSession.setPlaybackState(builder.build());
            mediaSession.setActive(isPlaying);
        }
        if (isPlaying) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void registerAudioPlaybackMonitor() {
        if (audioManager == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        if (audioPlaybackCallback != null) {
            // already registered
            return;
        }
        audioPlaybackCallback = new AudioManager.AudioPlaybackCallback() {
            @Override
            public void onPlaybackConfigChanged(java.util.List<AudioPlaybackConfiguration> configs) {
                boolean active = false;
                try { active = audioManager != null && audioManager.isMusicActive(); } catch (Throwable ignore) {}
                updatePlaybackState(active, PlaybackState.PLAYBACK_POSITION_UNKNOWN);
            }
        };
        try {
            audioManager.registerAudioPlaybackCallback(audioPlaybackCallback, new android.os.Handler(android.os.Looper.getMainLooper()));
        } catch (Throwable t) {
            Log.w(TAG, "registerAudioPlaybackCallback failed", t);
        }
    }

    private void unregisterAudioPlaybackMonitor() {
        if (audioManager == null || audioPlaybackCallback == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            audioPlaybackCallback = null;
            return;
        }
        try {
            audioManager.unregisterAudioPlaybackCallback(audioPlaybackCallback);
        } catch (Throwable t) {
            Log.w(TAG, "unregisterAudioPlaybackCallback failed", t);
        } finally {
            audioPlaybackCallback = null;
        }
    }
    public static Vod currentChannel(Context context){
        String tvUrl=  ValueUtil.getString(context,"tvUrl");
        if(null==tvUrl){
            return UpdateService.getByKey("0_0");
        }
        Vod vod= UpdateService.getByUrl(tvUrl);
        if(null==vod){
            return UpdateService.getByKey("0_0");
        }
        return vod;
    }
    public static void updateChannel(Context context, String url){
        Vod vod = UpdateService.getByUrl(url);
        if(null==vod||null==vod.getUrl()){
            return;
        }
        LogUtil.i(TAG,vod.getName()+vod.getUrl());
        ValueUtil.putString(context,"tvUrl",vod.getUrl());
    }

    private void initData() {
        // 使用异步任务加载数据
        new Thread(() -> {
            // 在后台线程执行耗时操作
            List<Live> result = UpdateService.getByLives();

            // 在UI线程更新界面
            runOnUiThread(() -> {
                provinces = result;
                currentProvinceIndex = currentLive.getTagIndex();
                showCurrentProvince();
            });
        }).start();
    }
    private void showCurrentProvince() {
        if (provinces == null || provinces.isEmpty()) {
            // 处理空数据情况
            binding.provinceName.setText("无数据");
            setupChannelList(new ArrayList<>());
            return;
        }

        // 确保索引在有效范围内
        if (currentProvinceIndex < 0) {
            currentProvinceIndex = 0;
        } else if (currentProvinceIndex >= provinces.size()) {
            currentProvinceIndex = provinces.size() - 1;
        }

        Live currentProvince = provinces.get(currentProvinceIndex);
        binding.provinceName.setText(currentProvince.getName() + "(" + currentProvince.getVods().size() + ")");
        setupChannelList(currentProvince.getVods());
    }
    private void setupChannelList(List<Vod> channels) {
        ArrayAdapter<Vod> adapter = new ArrayAdapter<Vod>(this, android.R.layout.simple_list_item_1, channels) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                Button btn;
                if (convertView == null) {
                    btn = new Button(getContext());
                    btn.setLayoutParams(new AbsListView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    btn.setTextColor(Color.WHITE);
                    btn.setTextSize(16);
                    btn.setPadding(24, 16, 24, 16);
                    btn.setBackgroundResource(R.drawable.menu_button_background);
                    btn.setClickable(false);
                    btn.setFocusable(false);
                } else {
                    btn = (Button) convertView;

                    if (!(btn.getLayoutParams() instanceof AbsListView.LayoutParams)) {
                        btn.setLayoutParams(new AbsListView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                }

                Vod channel = getItem(position);
                if (channel != null) {
                    btn.setText(channel.getName());
                }

                return btn;
            }
        };
        binding.channelList.setAdapter(adapter);
        binding.channelList.setOnItemClickListener((parent, view, position, id) -> {
            try {
                Vod channel = channels.get(position);
                if (channel.getUrl() != null) {
                    currentLive = channel;
                    // 在主线程中执行WebView操作
                    runOnUiThread(() -> {
                        try {
                            LogUtil.i(TAG, "Loading URL in WebView: " + channel.getUrl());
                            loadUrl(channel.getUrl());
                            LogUtil.i(TAG, "URL loaded successfully");
                        } catch (Exception e) {
                            LogUtil.e(TAG, "Error loading URL in WebView: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });

                    // 更新历史记录
                    updateChannel(thisContext, channel.getUrl());

                    // 显示提示
                    showToast(channel.getName(), this);

                    // 隐藏菜单
                    hideMenu();
                } else {
                    LogUtil.e(TAG, "Channel or URL is null");
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "Error handling channel click: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    protected void showMenu() {
        binding.menuContainer.setVisibility(View.VISIBLE);
        isMenuShow = true;
        showCurrentProvince();
        setupProvinceButtons();
        // 默认选中第一个频道
        if (binding.channelList.getAdapter() != null && binding.channelList.getCount() > 0) {
            binding.channelList.setSelection(0);
            binding.channelList.requestFocus();
        }

        // 点击空白处关闭菜单
        binding.menuContainer.setOnClickListener(v -> hideMenu());
    }
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            return super.dispatchKeyEvent(event);
        }
        int keyCode = event.getKeyCode();
        LogUtil.i("keyDown keyCode ", keyCode+" event" + event);
        boolean isMenuShow=isMenuShow();
        if(isMenuShow){
            if(keyCode==KeyEvent.KEYCODE_BACK||keyCode==KeyEvent.KEYCODE_MENU||keyCode==KeyEvent.KEYCODE_TAB){
                hideMenu();
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    currentProvinceIndex--;
                    if (currentProvinceIndex < 0) {
                        currentProvinceIndex = provinces.size() - 1;
                    }
                } else {
                    currentProvinceIndex++;
                    if (currentProvinceIndex >= provinces.size()) {
                        currentProvinceIndex = 0;
                    }
                }
                showCurrentProvince();
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
        if(keyCode==KeyEvent.KEYCODE_MENU|| keyCode == KeyEvent.KEYCODE_TAB||keyCode==KeyEvent.KEYCODE_DPAD_CENTER||keyCode==KeyEvent.KEYCODE_ENTER){
            showMenu();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            return goNext("right");
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            return goNext("left");
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            return goNext("down");
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            return goNext("up");
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            keyBack();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    private long mClickBackTime = 0;
    private boolean keyBack(){
        long currentTime = System.currentTimeMillis();
        if (currentTime - mClickBackTime < 3000) {
            //killAppProcess();
            finish();
            //super.onBackPressed();
            System.exit(0);
        } else {
            Toast.makeText(this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
            mClickBackTime = currentTime;
        }
        return true;
    }
    // 在 Handler 对象中处理消息
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String messageContent = (String) msg.obj;
                    // 处理接收到的消息（例如，显示 Toast）
                    if(currentLive.getKey().equals(messageContent)){
                        if (session != null) {
                            loadUrl(currentLive.getUrl());
                        }
                        //记录到db
                    }
                    break;
                case 2:
                    binding.liveName.setText("");
                    break;
            }
        }
    };
    private void loadUrl(String url){
        if(!url.startsWith("http")){
            url=baseUrl+url;
        }
        session.loadUri(url);
    }
    private boolean goNext(String nextType){
        if(null==currentLive){
            currentLive = UpdateService.getByKey("0_0");
        }
        String key= UpdateService.liveNext(currentLive.getTagIndex(),currentLive.getDetailIndex(),nextType);
        currentLive = UpdateService.getByKey(key);
        if(null!=currentLive){
            //延迟1s
            showToast(currentLive.getName(),this);
            String liveKey=currentLive.getKey();
            handler.sendMessageDelayed (handler.obtainMessage(1, liveKey),1000);

        }
        return true;
    }
    protected   void showToast(String text, Context context){
        binding.liveName.setText(text);
        //showToastOrg(text,context);
    }
    private void setupProvinceButtons() {
        binding.prevProvinceArea.setOnClickListener(v -> {
            currentProvinceIndex--;
            if (currentProvinceIndex < 0) {
                currentProvinceIndex = provinces.size() - 1;
            }
            showCurrentProvince();
        });

        binding.nextProvinceArea.setOnClickListener(v -> {
            currentProvinceIndex++;
            if (currentProvinceIndex >= provinces.size()) {
                currentProvinceIndex = 0;
            }
            showCurrentProvince();
        });
    }

    protected void hideMenu() {
        binding.menuContainer.setVisibility(View.GONE);
        isMenuShow = false;
        binding.menuContainer.setOnClickListener(null);
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
    private void bind(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        //webViewFocusChange();
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
        int visible=  binding.menuContainer.getVisibility();
        if(visible== View.VISIBLE){
            return true;
        }
        return false;
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
                .setRemoteDebuggingEnabled(true)
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
                                //"https://tv.cctv.com/live/cctv13/" "https://www.gdtv.cn/tvChannelDetail/43"
                                baseUrl= extension.metaData.baseUrl;
                                loadUrl(currentLive.getUrl());
                                //extension.metaData.baseUrl+"index.html"
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
           /*   @Override
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


}
