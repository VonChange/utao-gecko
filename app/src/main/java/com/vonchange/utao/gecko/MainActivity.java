package com.vonchange.utao.gecko;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.res.Configuration;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import com.vonchange.utao.gecko.dao.HistoryDaoX;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends MainBaseActivity  {

    private static String TAG="MainActivity";
    private long mClickBackTime = 0;
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG,"onConfigurationChanged...."+newConfig.orientation);
        super.onConfigurationChanged(newConfig);
        // 检查屏幕方向是否改变
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 在这里处理横屏模式下的布局调整
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 在这里处理竖屏模式下的布局调整
        }
    }
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            float x= event.getX();
            float y= event.getY();
            //Log.i("dispatchTouchEvent", "x" + x+"y "+y);
            if(x<100f&&y<100f) {
                ctrl("menu");
            }
        }
        return super.dispatchTouchEvent(event);
    }
    @Override
    protected  void message(String service,String data){
        Log.i(TAG, "messageservice "+service);
        if("history.save".equals(service)){
            //final AppDatabase db = AppDatabase.getInstance(this);
            HistoryDaoX.save(this,data);
            return;
        }
        if("history.update".equals(service)){
            //final AppDatabase db = AppDatabase.getInstance(this);
            HistoryDaoX.update(this,data);
            return;
        }
        if("history.all".equals(service)){
            HistoryDaoX.all(this,data);
        }
        if("app".equals(service)){
            postMessage(service,data);
            return;
        }
        if("menu".equals(service)){
            boolean isMenuShow=isMenuShow();
            if(isMenuShow){
                hideMenu();
                return;
            }
            showMenu(data);
           return;
        }
        if("key".equals(service)){
            keyCodeAllByCode(data);
        }
        if("exit".equals(service)){
            Log.i("service exit",service);
            super.onBackPressed();
            System.exit(0);
           // finish();
            return;
        }
    }


    private void menuCtrl(){
        boolean  isMenuShow=isMenuShow();
        if(isMenuShow){
            hideMenu();
        }else{
            ctrl("menu");
        }
    }
    private boolean normalKey(int keyCode){
        int[] keys=new int[]{KeyEvent.KEYCODE_DPAD_RIGHT,KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_DOWN,KeyEvent.KEYCODE_DPAD_UP,KeyEvent.KEYCODE_VOLUME_UP,KeyEvent.KEYCODE_VOLUME_DOWN,
                KeyEvent.KEYCODE_DPAD_CENTER,KeyEvent.KEYCODE_ENTER,KeyEvent.KEYCODE_BACK};
        for (int key : keys) {
           if(key==keyCode){
               return true;
           }
        }
        return false;
    }
    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            return super.dispatchKeyEvent(event);
        }
        int keyCode = event.getKeyCode();
        Log.i("keyDown keyCode ", keyCode+" event" + event);
        boolean isMenuShow=isMenuShow();
        if(isMenuShow){
            if(keyCode==KeyEvent.KEYCODE_BACK||keyCode==KeyEvent.KEYCODE_MENU||keyCode==KeyEvent.KEYCODE_TAB){
                hideMenu();
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
        if(keyCode==KeyEvent.KEYCODE_BACK){
            return keyBack();
        }
        if(keyCode==KeyEvent.KEYCODE_DPAD_CENTER||keyCode==KeyEvent.KEYCODE_ENTER){
            return ctrl("ok");
        }
        if(keyCode==KeyEvent.KEYCODE_MENU||keyCode==KeyEvent.KEYCODE_TAB){
            return ctrl("menu");
        }
        if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
            return ctrl("right");
        }
        if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
            return ctrl("left");
        }
        if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN){
            return ctrl("down");
        }
        if(keyCode==KeyEvent.KEYCODE_DPAD_UP){
            return ctrl("up");
        }
        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP||keyCode==KeyEvent.KEYCODE_VOLUME_DOWN
                ||keyCode==KeyEvent.KEYCODE_VOLUME_MUTE){
            return super.dispatchKeyEvent(event);
        }
        return ctrl("menu");
        //return super.dispatchKeyEvent(event);
    }
    private boolean keyBack(){
        String url = NextPlusNavigationDelegate.backUrl();
        Log.i("keyBack","keyBack "+url);
        //NextPlusNavigationDelegate.backUrl();
        if(null==url){
            long currentTime = System.currentTimeMillis();
            if (currentTime - mClickBackTime < 3000) {
                //killAppProcess();
                finish();
                //super.onBackPressed();
                //System.exit(0);
            } else {
                Toast.makeText(this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
                mClickBackTime = currentTime;
            }
        }else{
            session.loadUri(url);
        }
        //detail-> home-> index
        return true;
    }

    private static Instrumentation inst = new Instrumentation();

    private static Map<String,Integer> keyCodeMap=new HashMap<>();
    static {
        keyCodeMap.put("SPACE",62);
        keyCodeMap.put("F",34);
    }
    protected void keyCodeAllByCode(String keyCode){
       Integer keyCodeNum=  keyCodeMap.get(keyCode);
       if(null==keyCodeNum){return;}
        Log.i("onKeyEvent", "keyCodeStr "+keyCode);
        keyEventAll(keyCodeNum);
    }
    protected void keyEventAll(final int keyCode){
            new Thread() {
                public void run() {
                    try {
                        Log.i("onKeyEvent", "onKeyEvent"+keyCode);
                        inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                        inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
    }

    public  void onKeyEvent(final int keyCode, int action) {
        new Thread() {
            public void run() {
                try {
                    Log.i("onKeyEvent", "onKeyEvent"+keyCode);
                    //inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                    //inst.sendKeyDownUpSync(keyCode);
                 /*   if(null==lastFocus){
                        inst.sendKeySync(new KeyEvent(action,keyCode));
                    }*/
                    inst.sendKeySync(new KeyEvent(action,keyCode));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
       // super.dispatchKeyEvent(new KeyEvent(action, keyCode));
    }
    private boolean ctrl(String  ctrlEvent) {
        postMessage("ctrl",ctrlEvent);
        return true;
    }
}
