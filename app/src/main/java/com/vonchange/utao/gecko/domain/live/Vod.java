package com.vonchange.utao.gecko.domain.live;

public class Vod {
   private String name;
   private String url;

   private Integer tagIndex;
   private Integer detailIndex;
   private String key;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getTagIndex() {
        return tagIndex;
    }

    public void setTagIndex(Integer tagIndex) {
        this.tagIndex = tagIndex;
    }

    public Integer getDetailIndex() {
        return detailIndex;
    }

    public void setDetailIndex(Integer detailIndex) {
        this.detailIndex = detailIndex;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
