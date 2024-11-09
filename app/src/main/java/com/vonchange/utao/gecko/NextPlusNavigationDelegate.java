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
    private static  String lastUrl=null;
    private static  String rootUrl=null;
    private static  String currentUrl=null;
    public NextPlusNavigationDelegate(Context context){
        this.context = context;
    }


    @Override
    public GeckoResult<AllowOrDeny> onLoadRequest( GeckoSession session,LoadRequest request) {
        Log.d(LOGTAG, "onLoadRequest=" + request.uri +
                " triggerUri=" + request.triggerUri +
                " where=" + request.target +
                " isRedirect=" + request.isRedirect);
        String url=request.uri;
        currentUrl=url;
        if(url.startsWith("moz-extension")){
            if(url.endsWith("index.html")){
                rootUrl=url;
            }
            lastUrl=url;
        }
        return GeckoResult.fromValue(AllowOrDeny.ALLOW);
    }


    @Override
    public GeckoResult<String> onLoadError(GeckoSession session,  String uri,  WebRequestError error) {
        Log.d(LOGTAG, "onLoadError=" + uri +
                " error category=" + error.category +
                " error=" + error.code);
        currentUrl="moz-extension://error.html";
        return GeckoResult.fromValue("");
    }
    public static String  backUrl(){
        if(currentUrl.startsWith("moz-extension")){
            if(currentUrl.endsWith("index.html")){
                return null;
            }
            return rootUrl;
        }
        return lastUrl;
    }
}