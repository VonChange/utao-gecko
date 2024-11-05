package com.vonchange.utao.gecko.impl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
 
/**
 * Created by $wu on 2017-09-09 上午 10:40.
 */
 
public abstract class BaseAdapter<T> extends RecyclerView.Adapter {
 
    private List<T> datas;
    private Context context;
 
    public BaseAdapter(List<T> datas, Context context) {
         if(datas == null){
           datas = new ArrayList<>();
        }
        this.datas = datas;
        this.context = context;
    }

    public abstract   int getLayoutId();
    public abstract   void setData(ViewDataBinding binding);
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewDataBinding binding = DataBindingUtil.inflate(inflater,getLayoutId(), parent, false);
        return new MyViewHolder(binding.getRoot());
    }
 
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewDataBinding binding = DataBindingUtil.getBinding(holder.itemView);
        setData(binding);
       // binding.setUser(users.get(position));
        binding.executePendingBindings();
    }
 
 
    @Override
    public int getItemCount() {
        return datas.size();
    }
 
 
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(View itemView) {
            super(itemView);
        }
    }
 
}
 