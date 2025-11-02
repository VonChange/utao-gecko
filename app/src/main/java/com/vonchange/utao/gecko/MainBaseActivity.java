package com.vonchange.utao.gecko;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
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

import com.google.gson.reflect.TypeToken;
import com.vonchange.utao.gecko.api.ConfigApi;
import com.vonchange.utao.gecko.databinding.ActivityMainBinding;
import com.vonchange.utao.gecko.databinding.DialogExitBinding;
import com.vonchange.utao.gecko.domain.HzItem;
import com.vonchange.utao.gecko.domain.live.Live;
import com.vonchange.utao.gecko.domain.live.Vod;
import com.vonchange.utao.gecko.service.UpdateService;
import com.vonchange.utao.gecko.util.JsonUtil;
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
    // Exit dialog
    private DialogExitBinding exitDialogBinding;
    private boolean isExitDialogShowing = false;
    // video quality data from extension
    private String videoQualityData = null;
    // 数字键输入缓冲：用于多位数字跳转收藏台
    private final StringBuilder digitBuffer = new StringBuilder();
    private final Handler digitHandler = new Handler(Looper.getMainLooper());
    private final Runnable digitCommitRunnable = new Runnable() {
        @Override
        public void run() { commitDigitInput(); }
    };
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
        // 预先构建含收藏的索引，保证历史 usave=1 可定位到收藏栏
        try { UpdateService.getByLivesWithFavorites(this); } catch (Throwable ignore) {}
        // 异步检查一周更新
        new Thread(() -> {
            try {
                UpdateService.refreshIfNeeded(this);
                // 刷新后索引可能被基础数据覆盖，再次构建收藏映射
                UpdateService.getByLivesWithFavorites(this);
            } catch (Throwable ignore) {}
        }).start();
        thisContext=this;
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setupAndroidMediaSession();
        if(null==currentLive){
            Log.i(TAG, "onCreate: reading history tvUrl from SP=" + ValueUtil.getString(this,"tvUrl"));
            currentLive = currentChannel(this);
            Log.i(TAG, "onCreate: currentChannel -> " + (currentLive==null ? "null" : (currentLive.getName()+" url="+currentLive.getUrl()+" key="+currentLive.getKey())));
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
    protected void onResume() {
        super.onResume();
        // 回到前台时尝试请求一次画质列表
        requestVideoQualityIfNeeded(true);
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
        // 停止网页播放并释放会话
        stopWebPlayback();
        try {
            if (view != null) {
                view.setSession(null);
            }
            if (session != null) {
                session.close();
            }
        } catch (Throwable ignore) {}
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

    @SuppressLint("NewApi")
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

    @SuppressLint("NewApi")
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
        Log.i(TAG, "currentChannel: read tvUrl=" + tvUrl);
        if(null==tvUrl){
            Log.i(TAG, "currentChannel: tvUrl is null, fallback key=0_0");
            return UpdateService.getByKey("0_0");
        }
        Vod vod= UpdateService.getByUrl(tvUrl);
        if(null==vod){
            Log.i(TAG, "currentChannel: not found by url, fallback key=0_0");
            return UpdateService.getByKey("0_0");
        }
        Log.i(TAG, "currentChannel: resolved -> name=" + vod.getName() + " key=" + vod.getKey() + " url=" + vod.getUrl());
        return vod;
    }
    public static void updateChannel(Context context, String url){
        Log.i(TAG, "updateChannel: request save url=" + url);
        Vod vod = UpdateService.getByUrl(url);
        if(null==vod||null==vod.getUrl()){
            Log.e(TAG, "updateChannel: vod null or url null, skip save");
            return;
        }
        Log.i(TAG, "updateChannel: save -> name=" + vod.getName() + " key=" + vod.getKey() + " url=" + vod.getUrl());
        ValueUtil.putString(context,"tvUrl",vod.getUrl());
    }

    private void initData() {
        // 使用异步任务加载数据
        new Thread(() -> {
            // 在后台线程执行耗时操作（包含收藏栏目）
            List<Live> result = UpdateService.getByLivesWithFavorites(this);

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
		// 渲染完成后，将焦点与选中项指向当前频道（当退出对话框未显示时）
		if (!isExitDialogShowing) {
			try {
				if (currentLive != null && currentLive.getTagIndex() == currentProvinceIndex) {
					int idx = Math.max(0, currentLive.getDetailIndex());
					if (binding.channelList.getAdapter() != null && binding.channelList.getCount() > 0) {
						if (idx >= binding.channelList.getCount()) { idx = binding.channelList.getCount() - 1; }
						final int finalIdx = idx;
						binding.channelList.post(() -> {
							binding.channelList.setSelection(finalIdx);
							binding.channelList.requestFocus();
						});
					}
				}
			} catch (Throwable ignore) {}
		}
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
                    // 收藏栏前缀数字索引（1-based）
                    boolean isFavorites = false;
                    try {
                        Live currentProvince = provinces.get(currentProvinceIndex);
                        isFavorites = currentProvince != null && "favorite".equals(currentProvince.getTag());
                    } catch (Throwable ignore) {}
                    String name = channel.getName();
                    if (isFavorites) {
                        name = (position + 1) + ". " + (name == null ? "" : name);
                    }
                    btn.setText(name);
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
        // 将省份定位到当前播放的省份
        try {
            if (currentLive != null) {
                currentProvinceIndex = currentLive.getTagIndex();
            }
        } catch (Throwable ignore) {}
        showCurrentProvince();
        setupProvinceButtons();
        // 将列表焦点与选中项定位到当前频道
        try {
            int sel = 0;
            if (currentLive != null) { sel = Math.max(0, currentLive.getDetailIndex()); }
            if (binding.channelList.getAdapter() != null && binding.channelList.getCount() > 0) {
                if (sel >= binding.channelList.getCount()) { sel = binding.channelList.getCount() - 1; }
                binding.channelList.setSelection(sel);
                binding.channelList.requestFocus();
            }
        } catch (Throwable ignore) {}

        // 点击空白处关闭菜单
        binding.menuContainer.setOnClickListener(v -> hideMenu());
    }
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            return super.dispatchKeyEvent(event);
        }
        int keyCode = event.getKeyCode();
        LogUtil.i("keyDown keyCode ", keyCode+" event" + event);
        // 优先处理退出对话框，避免与上下左右快捷切台/菜单冲突
        if (isExitDialogShowing) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // 退出前停止播放
                stopWebPlayback();
                finish();
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_TAB) {
                hideExitDialog();
                return true;
            }
            // 让系统处理上下左右与确认键的焦点切换
            return super.dispatchKeyEvent(event);
        }
        // 数字键：收集多位数字，延时执行跳转到收藏台
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                handleDigitKey(keyCode);
                return true;
            }
        }
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
            // Show exit dialog instead of immediate exit
            showExitDialog();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    // 数字键处理：收集输入并启动定时提交
    private void handleDigitKey(int keyCode) {
        int d = keyCode - KeyEvent.KEYCODE_0; // 0..9
        digitBuffer.append(d);
        // 重置计时器，600ms后提交
        digitHandler.removeCallbacks(digitCommitRunnable);
        digitHandler.postDelayed(digitCommitRunnable, 600);
    }

    // 提交数字输入：解析并跳转到收藏第 N 台
    private void commitDigitInput() {
        try {
            if (digitBuffer.length() == 0) return;
            int oneBased = Integer.parseInt(digitBuffer.toString());
            digitBuffer.setLength(0);
            jumpToFavoriteIndex(oneBased);
        } catch (Throwable ignore) {
            digitBuffer.setLength(0);
        }
    }

    // 跳转到收藏栏第 oneBased 台并直接播放
    private void jumpToFavoriteIndex(int oneBased) {
        if (provinces == null || provinces.isEmpty()) return;
        int favTagIdx = findFavoritesTagIndex();
        if (favTagIdx < 0 || favTagIdx >= provinces.size()) return;
        Live fav = provinces.get(favTagIdx);
        if (fav == null || fav.getVods() == null || fav.getVods().isEmpty()) return;
        int zeroBased = oneBased - 1;
        if (zeroBased < 0 || zeroBased >= fav.getVods().size()) return;

        Vod channel = fav.getVods().get(zeroBased);
        if (channel == null || channel.getUrl() == null) return;

        // 切到收藏栏并播放该台
        currentProvinceIndex = favTagIdx;
        currentLive = channel;
        showCurrentProvince();

        // 播放并更新历史
        runOnUiThread(() -> {
            try {
                LogUtil.i(TAG, "Loading URL in WebView: " + channel.getUrl());
                loadUrl(channel.getUrl());
                LogUtil.i(TAG, "URL loaded successfully");
            } catch (Exception e) {
                LogUtil.e(TAG, "Error loading URL in WebView: " + e.getMessage());
            }
        });
        updateChannel(thisContext, channel.getUrl());
        showToast(channel.getName(), this);
        hideMenu();
    }

    // 查找收藏栏所在的栏目索引（优先 tag=favorite，否则兜底 0）
    private int findFavoritesTagIndex() {
        if (provinces == null) return -1;
        for (int i = 0; i < provinces.size(); i++) {
            Live l = provinces.get(i);
            if (l != null && "favorite".equals(l.getTag())) {
                return i;
            }
        }
        // 如果没有设置 tag，则默认第一栏为收藏
        return 0;
    }
    private void showExitDialog() {
        if (exitDialogBinding == null) {
            View dialogView = findViewById(R.id.exitDialog);
            exitDialogBinding = DataBindingUtil.bind(dialogView);
        }
        if (exitDialogBinding == null) {
            return;
        }
        isExitDialogShowing = true;
        exitDialogBinding.exitDialogContainer.setVisibility(View.VISIBLE);
        // render quality buttons if data present
        try { setupHzListInExit(); } catch (Throwable ignore) {}
        // 若暂无数据，主动请求一次
        if (isVideoQualityEmpty()) {
            requestVideoQualityIfNeeded(false);
            // 轻微延迟后刷新渲染
            exitDialogBinding.hzListInExit.postDelayed(() -> {
                try { setupHzListInExit(); } catch (Throwable ignore) {}
            }, 600);
        }
        // 更新收藏按钮文案
        try { updateFavoriteButtonText(); } catch (Throwable ignore) {}
        // default focus
        exitDialogBinding.btnFavorite.setFocusable(true);
        exitDialogBinding.btnCancel.setFocusable(true);
        exitDialogBinding.btnFavorite.post(() -> exitDialogBinding.btnFavorite.requestFocus());
        exitDialogBinding.btnCancel.setOnClickListener(v -> hideExitDialog());
		exitDialogBinding.btnFavorite.setOnClickListener(v -> {
			toggleFavoriteCurrent();
			// 点击后恢复焦点到收藏按钮，避免被列表抢走
			exitDialogBinding.btnFavorite.post(() -> exitDialogBinding.btnFavorite.requestFocus());
		});
        exitDialogBinding.btnRefreshNow.setOnClickListener(v -> {
            new Thread(() -> {
                boolean ok = UpdateService.refreshNow(this);
                runOnUiThread(() -> {
                    try {
                        ToastUtils.show(this, ok? "更新成功" : "更新失败", Toast.LENGTH_SHORT);
                        if (ok) { initData(); }
                    } catch (Throwable ignore) {}
                });
            }).start();
        });
        exitDialogBinding.dialogBackdrop.setOnClickListener(v -> hideExitDialog());
        // 设置左侧二维码图片
        try {
            android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(getAssets().open("tv-web/img/myzsm.jpg"));
            exitDialogBinding.qrDonate.setImageBitmap(bmp);
        } catch (Throwable ignore) {}
    }

    private void hideExitDialog() {
        if (exitDialogBinding != null) {
            isExitDialogShowing = false;
            exitDialogBinding.exitDialogContainer.setVisibility(View.GONE);
        }
    }

    private void setupHzListInExit(){
        if (exitDialogBinding == null) { return; }
        exitDialogBinding.hzListInExit.removeAllViews();
        exitDialogBinding.hzListInExit.setVisibility(View.GONE);
        if (videoQualityData == null || videoQualityData.trim().length() == 0) { return; }
        List<HzItem> hzItems = null;
        try {
            hzItems = JsonUtil.fromJson(videoQualityData, new TypeToken<List<HzItem>>(){}.getType());
        } catch (Throwable ignore) { }
        if (hzItems == null || hzItems.isEmpty()) { return; }
        exitDialogBinding.hzListInExit.setVisibility(View.VISIBLE);
        int previousBtnId = View.NO_ID;
        int firstBtnId = View.NO_ID;
        for (int i = 0; i < hzItems.size(); i++) {
            HzItem item = hzItems.get(i);
            Button btn = new Button(this);
            btn.setText(item.getName());
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(16);
            btn.setPadding(24, 16, 24, 16);
            btn.setBackgroundResource(R.drawable.menu_button_background);
			btn.setFocusable(true);
            int thisId = View.generateViewId();
            btn.setId(thisId);
            btn.setOnClickListener(v -> {
                String action = item.getAction();
                if (action != null && action.trim().length() > 0) {
                    postMessage("js", action);
                }
                try {
                    ToastUtils.show(this, "切换到 " + item.getName(), Toast.LENGTH_SHORT);
                } catch (Throwable ignore) {}
				// 点击后保持焦点在当前按钮
				v.post(() -> v.requestFocus());
            });
            // 焦点关系：左右在画质项之间切换；下到取消
            if (previousBtnId != View.NO_ID) {
                btn.setNextFocusLeftId(previousBtnId);
            }
            if (previousBtnId != View.NO_ID) {
                View prev = exitDialogBinding.hzListInExit.findViewById(previousBtnId);
                if (prev != null) {
                    prev.setNextFocusRightId(thisId);
                }
            }
            btn.setNextFocusDownId(exitDialogBinding.btnFavorite.getId());
            if (firstBtnId == View.NO_ID) { firstBtnId = thisId; }
            previousBtnId = thisId;
            exitDialogBinding.hzListInExit.addView(btn);
        }
        // 焦点链：画质 -> 收藏 -> 取消；收藏上移到第一项
        if (firstBtnId != View.NO_ID) {
            exitDialogBinding.btnFavorite.setNextFocusUpId(firstBtnId);
        }
        exitDialogBinding.btnFavorite.setNextFocusDownId(exitDialogBinding.btnCancel.getId());
        exitDialogBinding.btnCancel.setNextFocusUpId(exitDialogBinding.btnFavorite.getId());
    }

    private void stopWebPlayback(){
        try {
            if (session != null) {
                session.loadUri("about:blank");
            }
        } catch (Throwable ignore) {}
    }

    private boolean isVideoQualityEmpty(){
        if (videoQualityData == null) return true;
        String s = videoQualityData.trim();
        if (s.length()==0) return true;
        if ("[]".equals(s) || "null".equalsIgnoreCase(s)) return true;
        return false;
    }

    private void requestVideoQualityIfNeeded(boolean soft){
        try {
            // soft=true 仅在已为空时请求；soft=false 强制请求
            if (!soft || isVideoQualityEmpty()) {
                String js = "(function(){try{if(window._data&&_data.hzList){_data.hzList(window._tvFunc? _tvFunc.getVideo(): null);}else if(window._apiX){_apiX.msg('videoQuality',[]);} }catch(e){}})();";
                postMessage("js", js);
            }
        } catch (Throwable ignore) {}
    }

    // ================= 收藏（SharedPreferences: key=favorites, json list of urls） =================
    private List<Vod> loadFavorites(){
        try {
            String json = ValueUtil.getString(this, "favorites", "[]");
            List<Vod> list = JsonUtil.fromJson(json, new TypeToken<List<Vod>>(){}.getType());
            return list == null ? new ArrayList<>() : list;
        } catch (Throwable t){
            return new ArrayList<>();
        }
    }

    private void saveFavorites(List<Vod> list){
        try {
            String json = JsonUtil.toJson(list);
            ValueUtil.putString(this, "favorites", json);
        } catch (Throwable ignore) {}
    }
    private String favUrl(String url){
        if(url.contains("?")){
            return  url +"&usave=1";
        }
        return url+"?usave=1";
    }
    private String checkFavUrl(String url){
        if(url.contains("usave=1")){
            return url;
        }
        return favUrl(url);
    }
    private int isFavoriteUrl(String url){
        if (url == null || url.trim().isEmpty()) return -1;
        url=checkFavUrl(url);
        List<Vod> list = loadFavorites();
        for (int i = 0; i < list.size(); i++) {
            if (url.equals(list.get(i).getUrl())) return i;
        }
        return -1;
    }

    private Vod  favVod(Vod vod){
        Vod vodNew = new Vod();
        vodNew.setName("♥"+vod.getName());
        vodNew.setUrl(favUrl(vod.getUrl()));
        return vodNew;
    }
    private void toggleFavoriteCurrent(){
        if (currentLive == null || currentLive.getUrl() == null) return;
        String url = currentLive.getUrl();
        List<Vod> list = loadFavorites();
        int favIndex = isFavoriteUrl(url);
        if (favIndex==-1){ list.add(favVod(currentLive)); } else{
            list.remove(favIndex);
        }
        saveFavorites(list);
        try {
            updateFavoriteButtonText();
            ToastUtils.show(this, favIndex!=-1? "已取消收藏" : "已收藏", Toast.LENGTH_SHORT);
            // 重新加载栏目含收藏
            initData();
        } catch (Throwable ignore) {}
    }

    private void updateFavoriteButtonText(){
        if (exitDialogBinding == null) return;
        boolean isFav = currentLive != null && (isFavoriteUrl(currentLive.getUrl())!=-1);
        exitDialogBinding.btnFavorite.setText(isFav ? "取消收藏" : "收藏当前频道");
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
            Log.i(TAG, "goNext: currentLive was null, set default 0_0");
        }
        String key= UpdateService.liveNext(currentLive.getTagIndex(),currentLive.getDetailIndex(),nextType);
        Log.i(TAG, "goNext: nextType=" + nextType + " -> key=" + key);
        currentLive = UpdateService.getByKey(key);
        if(null!=currentLive){
            Log.i(TAG, "goNext: switched -> name=" + currentLive.getName() + " url=" + currentLive.getUrl() + " key=" + currentLive.getKey());
            // 记录历史，确保退出重进能定位到最近频道
            updateChannel(thisContext, currentLive.getUrl());
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
        if("videoQuality".equals(service)){
            videoQualityData = data;
            if(isExitDialogShowing){
                runOnUiThread(() -> {
                    try { setupHzListInExit(); } catch (Throwable ignore) {}
                });
            }
        }
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
