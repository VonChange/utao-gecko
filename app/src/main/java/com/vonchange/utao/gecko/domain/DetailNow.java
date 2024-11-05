package com.vonchange.utao.gecko.domain;

import androidx.databinding.BaseObservable;

public class DetailNow extends BaseObservable {
    private RateItem rate;
    private XjItem xj;
    private HzItem hz;

    public RateItem getRate() {
        return rate;
    }

    public void setRate(RateItem rate) {
        this.rate = rate;
    }

    public XjItem getXj() {
        return xj;
    }

    public void setXj(XjItem xj) {
        this.xj = xj;
       // notifyPropertyChanged(BR.menu);
       // notifyChange();
    }

    public HzItem getHz() {
        return hz;
    }

    public void setHz(HzItem hz) {
        this.hz = hz;
    }
}
