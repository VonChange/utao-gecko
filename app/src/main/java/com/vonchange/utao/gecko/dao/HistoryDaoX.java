package com.vonchange.utao.gecko.dao;

import android.content.Context;
import android.util.Log;

import com.vonchange.utao.gecko.MainActivity;
import com.vonchange.utao.gecko.domain.BaseMsg;
import com.vonchange.utao.gecko.util.JsonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryDaoX {
    private static String TAG="MainActivity";

    public  static  void save(Context context,String data){
        HistoryDao historyDao = AppDatabase.getInstance(context).historyDao();
        new Thread(new Runnable() {
            @Override
            public void run() {
                History history =  JsonUtil.fromJson(data, History.class);
                //vodId
                history.id=null;
                String vodId = history.vodId;
                Log.i(TAG,history.name);
                List<History> historyListOrg =   historyDao.queryByVodId(vodId,history.site);
                String url=null;
                if(historyListOrg.isEmpty()){
                    historyDao.insertAll(history);
                    url=history.url;
                }else{
                    url=historyListOrg.get(0).url;
                }
                Log.i(TAG,"loadUrl"+url);
                MainActivity.session.loadUri(url);
            }
        }).start();
    }
    public  static  void all(Context context,String data){
        HistoryDao historyDao = AppDatabase.getInstance(context).historyDao();
        new Thread(new Runnable() {
            @Override
            public void run() {
                BaseMsg baseMsg = JsonUtil.fromJson(data, BaseMsg.class);
                String msgId=baseMsg.getMsgId();
                List<History> historyList =   historyDao.queryHistory();
                Map<String,String> dataMap = new HashMap<>();
                dataMap.put("key",msgId);
                dataMap.put("value",JsonUtil.toJson(historyList));
                MainActivity.postMessage("sessionStorage",JsonUtil.toJson(dataMap));
            }
        }).start();
    }
    public  static  List<History> queryHistory(Context context){
        HistoryDao historyDao = AppDatabase.getInstance(context).historyDao();
        return    historyDao.queryHistory();
    }
    public  static  void update(Context context,String data){
        HistoryDao historyDao = AppDatabase.getInstance(context).historyDao();
        new Thread(new Runnable() {
            @Override
            public void run() {
                History history =  JsonUtil.fromJson(data, History.class);
                //vodId
                historyDao.updateUrl(history.vodId,history.site,history.remark,history.url);
            }
        }).start();
    }
}
