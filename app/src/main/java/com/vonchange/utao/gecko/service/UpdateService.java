package com.vonchange.utao.gecko.service;

import android.content.Context;
import com.google.gson.reflect.TypeToken;
import com.vonchange.utao.gecko.MyApplication;
import com.vonchange.utao.gecko.domain.live.DataWrapper;
import com.vonchange.utao.gecko.domain.live.Live;
import com.vonchange.utao.gecko.domain.live.Vod;
import com.vonchange.utao.gecko.util.FileUtil;
import com.vonchange.utao.gecko.util.JsonUtil;
import com.vonchange.utao.gecko.util.ValueUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;



public class UpdateService {

    private static final  String TAG="UpdateService";
    public static String baseFolder;
    protected static Map<String, Vod> indexVodMap = new HashMap<>();
    protected static Map<Integer,Integer> tagMaxMap = new HashMap<>();
    protected static Map<String,String> urlKeyMap= new HashMap<>();
    protected static List<Live> newLives= new ArrayList<>();
    public static  void initTvData(){
        String json= FileUtil.readAssert(MyApplication.getContext(),"tv-web/js/cctv/tv2.json");
        if(json.trim().isEmpty()){
            return;
        }
        DataWrapper<List<Live>> data = JsonUtil.fromJson(json,new TypeToken<DataWrapper<List<Live>>>(){}.getType());
        List<Live> lives = data.getData();
        indexVodMap = new HashMap<>();
        tagMaxMap=new HashMap<>();
        urlKeyMap=new HashMap<>();
        int i=0,j;
        for (Live life : lives) {
            j=0;
            for (Vod vod : life.getVods()) {
                vod.setTagIndex(i);
                vod.setDetailIndex(j);
                String key= i+"_"+j;
                vod.setKey(key);
                indexVodMap.put(key,vod);
                urlKeyMap.put(vod.getUrl(),key);
                j++;
            }
            tagMaxMap.put(i,j-1);
            i++;
        }
        newLives=lives;
    }
    public static List<Live> getByLives(){
        return newLives;
    }
    public static List<Live> getByLivesWithFavorites(Context context){
        List<Live> combined = new ArrayList<>(newLives);
        try {
            String favJson = ValueUtil.getString(context, "favorites", "[]");
            List<String> favUrls = JsonUtil.fromJson(favJson, new TypeToken<List<String>>(){}.getType());
            if (favUrls != null && !favUrls.isEmpty()) {
                Live favoriteLive = new Live();
                favoriteLive.setName("我的收藏");
                favoriteLive.setTag("favorite");
                int tagIndex = combined.size();
                favoriteLive.setIndex(tagIndex);
                List<Vod> favoriteVods = new ArrayList<>();
                int j = 0;
                for (String url : favUrls) {
                    if (url == null || url.trim().isEmpty()) { continue; }
                    Vod base = getByUrl(url);
                    Vod vod = new Vod();
                    if (base != null) {
                        vod.setName("❤" + base.getName());
                    } else {
                        vod.setName("❤收藏");
                    }
                    String useUrl = url;
                    if (useUrl.contains("?")) {
                        if (!useUrl.contains("usave=")) { useUrl = useUrl + "&usave=1"; }
                    } else {
                        useUrl = useUrl + "?usave=1";
                    }
                    vod.setUrl(useUrl);
                    vod.setTagIndex(tagIndex);
                    vod.setDetailIndex(j);
                    String key = tagIndex + "_" + j;
                    vod.setKey(key);
                    favoriteVods.add(vod);
                    j++;
                }
                if (!favoriteVods.isEmpty()) {
                    favoriteLive.setVods(favoriteVods);
                    combined.add(favoriteLive);
                }
            }
        } catch (Throwable ignore) {}

        // rebuild maps to include favorites for navigation
        rebuildMaps(combined);
        return combined;
    }

    private static void rebuildMaps(List<Live> lives){
        indexVodMap = new HashMap<>();
        tagMaxMap = new HashMap<>();
        urlKeyMap = new HashMap<>();
        int i = 0;
        for (Live live : lives) {
            int j = 0;
            if (live.getVods() != null) {
                for (Vod vod : live.getVods()) {
                    vod.setTagIndex(i);
                    vod.setDetailIndex(j);
                    String key = i + "_" + j;
                    vod.setKey(key);
                    indexVodMap.put(key, vod);
                    if (vod.getUrl() != null) {
                        urlKeyMap.put(vod.getUrl(), key);
                    }
                    j++;
                }
            }
            tagMaxMap.put(i, j - 1);
            i++;
        }
        newLives = lives;
    }
    public static Vod getByKey(String key){
        return indexVodMap.get(key);
    }
    /*public static String getKeyByUrl(String url){
        return  urlKeyMap.get(url);
    }*/
    public static Vod getByUrl(String url){
        String key= urlKeyMap.get(url);
        if(null==key){
            return null;
        }
        return indexVodMap.get(key);
    }


    public static  String liveNext(Integer tagIndexNow,Integer detailIndexNow,String nextType){
        if(nextType.equals("up")){
            if(detailIndexNow==0){
                return tagIndexNow+"_"+tagMaxMap.get(tagIndexNow);
            }
            return tagIndexNow+"_"+(detailIndexNow-1);
        }
        if(nextType.equals("down")){
            if(Objects.equals(detailIndexNow, tagMaxMap.get(tagIndexNow))){
                return tagIndexNow+"_0";
            }
            return tagIndexNow+"_"+(detailIndexNow+1);
        }
        if(nextType.equals("left")){
            if(tagIndexNow==0){
                return (tagMaxMap.size()-1)+"_0";
            }
            return (tagIndexNow-1)+"_0";
        }
        if(nextType.equals("right")){
            if(tagIndexNow.equals(tagMaxMap.size()-1)){
                return "0_0";
            }
            return (tagIndexNow+1)+"_0";
        }
        return "0_0";
    }

}
