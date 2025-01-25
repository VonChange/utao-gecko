package com.vonchange.utao.gecko;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.vonchange.utao.gecko.dao.HistoryDaoX;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends MainBaseActivity  {


    private static String TAG="MainActivity";
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
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            float x= event.getX();
            float y= event.getY();
            Log.i("dispatchTouchEvent", "x" + x+"y "+y);
            if(x<100f&&y<100f) {
                menuCtrl();
            }
        }
        return super.dispatchTouchEvent(event);
    }
    private static long mClickBackTime = 0;
    private static long mClickOkTime=0;
    private void menuCtrl(){
        boolean  isMenuShow=isMenuShow();
        if(isMenuShow){
            hideMenu();
        }else{
            ctrl("menu");
        }
    }
/*    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            Log.i("keyDown c ", keyCode+" keyDown" + event);
            //4 返回  62空格(32)' 双enter 也是
            if((keyCode==KeyEvent.KEYCODE_MENU)||keyCode==KeyEvent.KEYCODE_TAB){
                menuCtrl();
                return true;
            }
         *//*   if((keyCode == KeyEvent.KEYCODE_DPAD_CENTER||keyCode==KeyEvent.KEYCODE_ENTER)){
                long currentTime = System.currentTimeMillis();
                if (currentTime - mClickOkTime < 2000) {
                    menuCtrl();
                    return true;
                }
                mClickOkTime = currentTime;
            }*//*
        }
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if (event.getAction() == KeyEvent.ACTION_UP) {
                return true;
            }
            boolean isMenuShow=isMenuShow();
            if(isMenuShow){
                hideMenu();
                return true;
            }
            String url = NextPlusNavigationDelegate.backUrl();
            if(null==url){
                long currentTime = System.currentTimeMillis();
                if (currentTime - mClickBackTime < 3000) {
//                android.os.Process.killProcess(android.os.Process.myPid());
                    super.onBackPressed();
                    System.exit(0);
                } else {
                    Toast.makeText(getApplicationContext(), "再按一次返回键退出", Toast.LENGTH_SHORT).show();
                    mClickBackTime = currentTime;
                }
            }else{
                session.loadUri(url);
            }
            //detail-> home-> index
            postMessage("back","");
        }
        return super.dispatchKeyEvent(event);
    }*/
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
        if(event.getAction() == KeyEvent.ACTION_UP){
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN ) {
            int keyCode = event.getKeyCode();
            Log.i("keyDown", "keyDown" + keyCode);
            // 使用switch语句来检查按下的键码
            int type=0;
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    ctrl("right");
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    ctrl("left");
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    ctrl("down");
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    ctrl("up");
                    break;
                case KeyEvent.KEYCODE_BACK:
                    ctrl("back");
                    break;
                case KeyEvent.KEYCODE_MENU:
                    ctrl("menu");
                    break;
                case KeyEvent.KEYCODE_ENTER:
                    ctrl("ok");
                    break;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    ctrl("ok");
                    break;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    type=1;
                    break;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    type=1;
                    break;
                default:
                    Log.i("default", "default" + keyCode);
                    //return true;
                    //super.dispatchKeyEvent(event);
                    break;
            }
            if(type==1){
                return super.dispatchKeyEvent(event);
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
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
    private void ctrl(String  ctrlEvent) {
        postMessage("ctrl",ctrlEvent);
    }
}
