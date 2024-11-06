package com.vonchange.utao.gecko;

import android.app.Instrumentation;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.vonchange.utao.gecko.dao.HistoryDaoX;

public class MainActivity extends MainBaseActivity {
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
        if("runEnv".equals(service)){
            postMessage(service,data);
            return;
        }
        if("exit".equals(service)){
            Log.i("service exit",service);
            super.onBackPressed();
            System.exit(0);
           // finish();
            return;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN ){
            float x= event.getX();
            float y= event.getY();
            Log.i("dispatchTouchEvent", "x" + x+"y "+y);
            if(x<300f){
                if(y<300f){
                    ctrl("back");
                    return true;
                }
                ctrl("left");
                return true;
            }
            if(x>1600f){
                if(y<300f){
                    ctrl("menu");
                    return true;
                }
                ctrl("right");
                return true;
            }
            if(y<300f){
                ctrl("up");
                return true;
            }
            if(y>800f){
                ctrl("down");
                return true;
            }
            ctrl("ok");
            return true;
        }
        return true;
    }

/*    @SuppressLint("RestrictedApi")
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
    }*/

    private static Instrumentation inst = new Instrumentation();

    public static void onKeyEvent(final int keyCode, int action) {
        new Thread() {
            public void run() {
                try {
                    //Log.i("onKeyEvent", "onKeyEvent"+keyCode);
                    //inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                    //inst.sendKeyDownUpSync(keyCode);
                    inst.sendKeySync(new KeyEvent(action, keyCode));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    private void ctrl(String  ctrlEvent) {
        postMessage("ctrl",ctrlEvent);
    }
}
