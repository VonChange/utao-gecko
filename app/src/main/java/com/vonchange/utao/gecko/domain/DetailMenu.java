package com.vonchange.utao.gecko.domain;

import androidx.databinding.BaseObservable;

import java.util.List;

public class DetailMenu extends BaseObservable  {
    private DetailNow now;
    private List<XjItem> xjs;
    private List<HzItem> hzs;
    private List<RateItem> rates;
    private Boolean video;
    private String tab;
    private Boolean isVip;

    private String test;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public DetailNow getNow() {
        return now;
    }

    public void setNow(DetailNow now) {
        this.now = now;
    }

    public List<XjItem> getXjs() {
        return xjs;
    }

    public void setXjs(List<XjItem> xjs) {
        this.xjs = xjs;
    }

    public List<HzItem> getHzs() {
        return hzs;
    }

    public void setHzs(List<HzItem> hzs) {
        this.hzs = hzs;
    }

    public List<RateItem> getRates() {
        return rates;
    }

    public void setRates(List<RateItem> rates) {
        this.rates = rates;
    }

    public Boolean getVideo() {
        return video;
    }

    public void setVideo(Boolean video) {
        this.video = video;
    }

    public String getTab() {
        return tab;
    }

    public void setTab(String tab) {
        this.tab = tab;
        notifyChange();
    }



    public Boolean getVip() {
        return isVip;
    }

    public void setVip(Boolean vip) {
        isVip = vip;
    }
}
