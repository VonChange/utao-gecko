package com.vonchange.utao.gecko.dao;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class History {
    @PrimaryKey(autoGenerate = true)
    public Integer id;
    @ColumnInfo(name = "site")
    public  String site;
    @ColumnInfo(name = "vod_id",index = true)
    public String vodId;
    @ColumnInfo(name = "cid")
    public String cid;
    @ColumnInfo(name = "core")
    public String core;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "remark")
    public String remark;
    @ColumnInfo(name = "url")
    public String url;
    @ColumnInfo(name = "pic")
    public  String pic;

}
