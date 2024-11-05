package com.vonchange.utao.gecko.event;

import android.util.Log;

import com.vonchange.utao.gecko.domain.HzItem;
import com.vonchange.utao.gecko.domain.RateItem;
import com.vonchange.utao.gecko.domain.XjItem;
import com.vonchange.utao.gecko.impl.IBaseBindingPresenter;

public class EventBlind {

    private static String TAG="EventBlind";
    public static class XjBindPresenter implements IBaseBindingPresenter {

        public void onClick(XjItem item) {
            Log.i(TAG,item.getTitle());
            //TestActivity.binding.getMenu().getNow().setXj(item);
        }
    }

    public static class HzBindPresenter implements IBaseBindingPresenter {

        public void onClick(HzItem item) {
            Log.i(TAG,item.getName());
        }
    }
    public static class RateBindPresenter implements IBaseBindingPresenter {

        public void onClick(RateItem item) {
            Log.i(TAG,item.getName());
        }
    }

}
