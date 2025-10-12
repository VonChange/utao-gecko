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
import com.vonchange.utao.gecko.util.HttpUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;



public class UpdateService {

    private static final  String TAG="UpdateService";
    public static String baseFolder;
    private static final String PREF_TV_LIST = "tvList";
    private static final String PREF_TV_LIST_TIME = "tvListTime";
    private static final long ONE_WEEK_MS = 7L * 24 * 60 * 60 * 1000;
    private static final String REMOTE_TV_URL = "http://qn.vonchange.com/utao/res/tv2.json";
    protected static Map<String, Vod> indexVodMap = new HashMap<>();
    protected static Map<Integer,Integer> tagMaxMap = new HashMap<>();
    protected static Map<String,String> urlKeyMap= new HashMap<>();
    protected static List<Live> newLives= new ArrayList<>();
    public static  void initTvData(){
        Context ctx = MyApplication.getContext();
        String json = ValueUtil.getString(ctx, PREF_TV_LIST, "");
        if (json == null || json.trim().isEmpty()) {
            json = FileUtil.readAssert(ctx,"tv-web/js/cctv/tv2.json");
            if(json == null || json.trim().isEmpty()){
                return;
            }
            // 首次写入本地数据到SP
            ValueUtil.putString(ctx, PREF_TV_LIST, json);
            ValueUtil.putLong(ctx, PREF_TV_LIST_TIME, System.currentTimeMillis());
        }
        applyTvJson(json);
    }

    private static void applyTvJson(String json){
        DataWrapper<List<Live>> data = JsonUtil.fromJson(json,new TypeToken<DataWrapper<List<Live>>>(){}.getType());
        if (data == null || data.getData() == null || data.getData().isEmpty()) { return; }
        List<Live> lives = data.getData();
        indexVodMap = new HashMap<>();
        tagMaxMap=new HashMap<>();
        urlKeyMap=new HashMap<>();
        int i=0,j;
        for (Live life : lives) {
            j=0;
            if (life.getVods()==null) { tagMaxMap.put(i,-1); i++; continue; }
            for (Vod vod : life.getVods()) {
                vod.setTagIndex(i);
                vod.setDetailIndex(j);
                String key= i+"_"+j;
                vod.setKey(key);
                indexVodMap.put(key,vod);
                if (vod.getUrl()!=null) { urlKeyMap.put(vod.getUrl(),key); }
                j++;
            }
            tagMaxMap.put(i,j-1);
            i++;
        }
        newLives=lives;
    }

    // 启动时异步检查一周更新
    public static boolean refreshIfNeeded(Context ctx){
        try{
            long last = ValueUtil.getLong(ctx, PREF_TV_LIST_TIME, 0);
            long now = System.currentTimeMillis();
            if (now - last < ONE_WEEK_MS){
                android.util.Log.i(TAG, "refreshIfNeeded: within one week, skip");
                return false;
            }
            android.util.Log.i(TAG, "refreshIfNeeded: last="+last+" now="+now+" diff="+(now-last));
            return refreshNow(ctx);
        }catch (Throwable ignore){ return false; }
    }

    // 立即拉取更新（测试按钮）
    public static boolean refreshNow(Context ctx){
        try{
            android.util.Log.i(TAG, "refreshNow: requesting "+REMOTE_TV_URL+" (http)");
            String json = HttpUtil.getJson(REMOTE_TV_URL, null);
            android.util.Log.i(TAG, "refreshNow: rawLen="+(json==null? -1 : json.length()));
            if (json == null || json.length()<10 || (!json.trim().startsWith("{"))) { return false; }
            DataWrapper<List<Live>> data = JsonUtil.fromJson(json,new TypeToken<DataWrapper<List<Live>>>(){}.getType());
            if (data == null || data.getData() == null || data.getData().isEmpty()) {
                android.util.Log.e(TAG, "refreshNow: parsed data empty");
                return false; }
            ValueUtil.putString(ctx, PREF_TV_LIST, json);
            ValueUtil.putLong(ctx, PREF_TV_LIST_TIME, System.currentTimeMillis());
            applyTvJson(json);
            android.util.Log.i(TAG, "refreshNow: success, saved to SP and applied. size="+data.getData().size());
            return true;
        }catch (Throwable t){
            android.util.Log.e(TAG, "refreshNow: exception "+t.getClass()+" "+t.getMessage());
            return false; }
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
