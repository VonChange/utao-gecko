package com.vonchange.utao.gecko.controller;

import android.os.Build;

import com.vonchange.utao.gecko.MyApplication;
import com.vonchange.utao.gecko.dao.History;
import com.vonchange.utao.gecko.dao.HistoryDaoX;
import com.vonchange.utao.gecko.domain.Response;
import com.vonchange.utao.gecko.domain.SysInfo;
import com.vonchange.utao.gecko.util.AppVersionUtils;
import com.vonchange.utao.gecko.util.JsonUtil;
import com.vonchange.utao.gecko.util.Util;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/service")
public class ServiceController {
    //String pre="tv-web";
    private static String TAG="ServiceController";
    //,consumes= "application/json"
    @GetMapping(path = "/queryHistory")
    public String queryHistory() {
        List<History> histories = HistoryDaoX.queryHistory(MyApplication.context);
        return toData(histories);
        //return FileUtil.readAssert(MyApplication.context,pre+"/css/my.css");
    }
    @GetMapping(path = "/querySysInfo")
    public String querySysInfo() {

        SysInfo sysInfo = new SysInfo();
        sysInfo.setHaveNew(false);
        boolean is64= Util.is64();
        sysInfo.setIs64(is64);
        sysInfo.setVersionCode(Build.VERSION.SDK_INT);
        //Build.VERSION.SDK_INT
        sysInfo.setVersionName(AppVersionUtils.getVersionName());
        return toData(sysInfo);
    }

    private String toData(Object data){
        return JsonUtil.toJson(new Response(JsonUtil.toJson(data)));
    }
}