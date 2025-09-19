package com.vonchange.utao.gecko.domain.live;

import java.util.List;

public class Live {
    private String tag;
    private String name;
    private Integer index;
    private List<Vod> vods;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public List<Vod> getVods() {
        return vods;
    }

    public void setVods(List<Vod> vods) {
        this.vods = vods;
    }
}
