package com.vonchange.utao.gecko.service;

import com.google.gson.reflect.TypeToken;
import com.vonchange.utao.gecko.MyApplication;
import com.vonchange.utao.gecko.domain.live.DataWrapper;
import com.vonchange.utao.gecko.domain.live.Live;
import com.vonchange.utao.gecko.domain.live.Vod;
import com.vonchange.utao.gecko.util.FileUtil;
import com.vonchange.utao.gecko.util.JsonUtil;

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
