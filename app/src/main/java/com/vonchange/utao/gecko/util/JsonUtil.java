package com.vonchange.utao.gecko.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class JsonUtil {

    public static <T> T fromJson(String json,Class<T> tClass){
        Gson gson = new Gson();
        return gson.fromJson(json,tClass);
    }
    public static <T> T fromJson(String json, Type typeOfT){
        Gson gson = new Gson();
        //Type type = new TypeToken<T>() {}.getType();
        return gson.fromJson(json,typeOfT);
    }

    public  static <T> String toJson(T t){
        Gson gson = new Gson();
        return gson.toJson(t);
    }

}
