package com.vonchange.utao.gecko;

import android.content.Context;
import android.util.Log;

import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.WebRequestError;

public class NextPlusNavigationDelegate implements GeckoSession.NavigationDelegate {
    final String LOGTAG = "NextPlus";
    private Context context;
    public NextPlusNavigationDelegate(Context context){
        this.context = context;
    }


    @Override
    public GeckoResult<AllowOrDeny> onLoadRequest( GeckoSession session,LoadRequest request) {
        return GeckoResult.fromValue(AllowOrDeny.ALLOW);
    }


    @Override
    public GeckoResult<String> onLoadError(GeckoSession session,  String uri,  WebRequestError error) {
        Log.d(LOGTAG, "onLoadError=" + uri +
                " error category=" + error.category +
                " error=" + error.code);
        return GeckoResult.fromValue("");
    }
}