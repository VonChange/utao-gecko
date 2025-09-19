package com.vonchange.utao.gecko;

import android.content.res.Configuration;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class MainActivity extends MainBaseActivity  {

    private static String TAG="MainActivity";

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
        if(!isMenuShow()&&event.getAction() == KeyEvent.ACTION_DOWN){
            showMenu();
            return true;
        }
        return super.dispatchTouchEvent(event);
    }





}
