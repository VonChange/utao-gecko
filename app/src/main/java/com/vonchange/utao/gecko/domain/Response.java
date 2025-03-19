package com.vonchange.utao.gecko.domain;


public class Response {
    public  String data;
    public Response(){
    }
    public Response(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
