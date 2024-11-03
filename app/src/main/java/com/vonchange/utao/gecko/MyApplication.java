package com.vonchange.utao.gecko;



import android.app.Application;
import android.util.Log;

import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //preInitWebView();
    }

}
