package com.vonchange.utao.gecko;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vonchange.utao.gecko.databinding.ActivityTestBinding;
import com.vonchange.utao.gecko.databinding.ItemHzBinding;
import com.vonchange.utao.gecko.databinding.ItemRateBinding;
import com.vonchange.utao.gecko.databinding.ItemXjBinding;
import com.vonchange.utao.gecko.domain.DetailMenu;
import com.vonchange.utao.gecko.domain.HzItem;
import com.vonchange.utao.gecko.domain.RateItem;
import com.vonchange.utao.gecko.domain.XjItem;
import com.vonchange.utao.gecko.event.EventBlind;
import com.vonchange.utao.gecko.impl.BaseBindingAdapter;
import com.vonchange.utao.gecko.impl.BaseViewHolder;
import com.vonchange.utao.gecko.util.JsonUtil;

import java.util.List;

public class TestActivity extends Activity {

    private static String TAG = "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind();
    }
    //public  ActivityTestBinding binding;
    private void bind(){
        DetailMenu detailMenu = JsonUtil.fromJson(json,DetailMenu.class);
        Log.i(TAG,JsonUtil.toJson(detailMenu));
        ActivityTestBinding  binding = DataBindingUtil.setContentView(this, R.layout.activity_test);
        binding.setMenu(detailMenu);
        xjBlind(binding,detailMenu.getXjs());
        hzBind(binding,detailMenu.getHzs());
        rateBind(binding,detailMenu.getRates());
        binding.hzBtn.requestFocus();
        DetailMenu detailMenu2 = JsonUtil.fromJson(json,DetailMenu.class);
        detailMenu2.getNow().getXj().setIndex(23);
        binding.setMenu(detailMenu2);
        focusChange(binding);
       // binding.setVariable(BR.menu)
    }

    private  Button defaultFocusBtn(View oldFocus, View newFocus){
        if(!(newFocus instanceof Button)){
            return null;
        }
        if(null!=oldFocus){  oldFocus.setScaleX(1.0f); oldFocus.setScaleY(1.0f);}
        newFocus.setScaleX(1.1f);
        newFocus.setScaleY(1.1f);
        return (Button) newFocus;

    }
    private String  oldBtnTag(View oldFocus){
        if(!(oldFocus instanceof Button)){
            return null;
        }
        Object tagObj=oldFocus.getTag();
        if(null==tagObj){return null;}
        return  tagObj.toString();
    }
    private void focusChange(ActivityTestBinding binding) {
        View view = binding.tvMenu;
        view.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                Log.d(TAG, "onGlobalFocusChanged: oldFocus=" + oldFocus);
                Log.d(TAG, "onGlobalFocusChanged: newFocus=" + newFocus);
                Button focusBtn=defaultFocusBtn(oldFocus,newFocus);
                if(null==focusBtn){return;}
                Object tagObj=focusBtn.getTag();
                if(null==tagObj){return;}
                String tag= tagObj.toString();
                Log.d(TAG, "onGlobalFocusChanged: newFocus=" + tag);
                if(tag.startsWith("menu_")){
                    tag=tag.substring(5);
                    binding.getMenu().setTab(tag);
                }
                String oldTag=null;
                switch (tag){
                    case "hzItem":
                        focusBtn.setNextFocusUpId(R.id.hzBtn);
                        oldTag=oldBtnTag(oldFocus);
                        if(null!=oldTag){
                            if(oldTag.equals("menu_hz")){
                                RecyclerView.ViewHolder viewHolder = binding.hzsView.findViewHolderForLayoutPosition(0);
                                if(null!=viewHolder){
                                    viewHolder.itemView.requestFocus();
                                }
                            }
                        }
                        break;
                    case "rateItem":
                        focusBtn.setNextFocusUpId(R.id.rateBtn);
                        oldTag=oldBtnTag(oldFocus);
                        if(null!=oldTag){
                            if(oldTag.equals("menu_rate")){
                                RecyclerView.ViewHolder viewHolder = binding.ratesView.findViewHolderForLayoutPosition(0);
                                if(null!=viewHolder){
                                    viewHolder.itemView.requestFocus();
                                }
                            }
                        }
                        break;
                    case "xjItem":
                        LinearLayout layout = (LinearLayout) focusBtn.getParent();
                        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) layout.getLayoutParams();
                        int itemPosition = params.getViewLayoutPosition();
                        if(itemPosition<6){
                            focusBtn.setNextFocusUpId(R.id.xjBtn);
                        }
                        Log.d(TAG, "xjItem: index=" + itemPosition);
                        oldTag=oldBtnTag(oldFocus);
                        if(null!=oldTag){
                            //old上一个是选集btn 下一个是item 自动选择
                            if(oldTag.equals("menu_xj")){
                               int id =  binding.xjsView.getLayoutManager().getItemCount();
                                Log.i(TAG,"count "+ id+" "+ binding.xjsView.getChildCount()+" "+binding.xjsView.getAdapter().getItemCount());
                                int viewCount= binding.xjsView.getChildCount();
                                int num=binding.getMenu().getNow().getXj().getIndex();
                                if(num>viewCount){
                                    num=viewCount-1;
                                }
                               RecyclerView.ViewHolder viewHolder = binding.xjsView.findViewHolderForLayoutPosition(num);
                                if(null!=viewHolder){
                                    viewHolder.itemView.requestFocus();
                                }
                            }
                        }
                        break;
                    default:
                        Log.i(TAG,"setTab"+tag);
                        break;
                }
            }
        });
    }

    private void xjBlind(ActivityTestBinding binding,List<XjItem> xjItems){
        BaseBindingAdapter xjAdapter = new BaseBindingAdapter<XjItem, ItemXjBinding>(xjItems,R.layout.item_xj) {
            @Override
            public void doBindViewHolder(BaseViewHolder<ItemXjBinding> holder, XjItem item) {
                holder.getBinding().setVariable(BR.item, item);
                holder.getBinding().setVariable(BR.itemPresenter, ItemPresenter);
            }
        };
        xjAdapter.setItemPresenter(new EventBlind.XjBindPresenter());
        binding.xjsView
                .setLayoutManager(new GridLayoutManager(this, 6));
        binding.xjsView
                .setAdapter(xjAdapter);
    }
    private void hzBind(ActivityTestBinding binding, List<HzItem> hzItems){
        BaseBindingAdapter hzAdapter = new BaseBindingAdapter<HzItem, ItemHzBinding>(hzItems,R.layout.item_hz) {
            @Override
            public void doBindViewHolder(BaseViewHolder<ItemHzBinding> holder, HzItem item) {
                holder.getBinding().setVariable(BR.item, item);
                holder.getBinding().setVariable(BR.itemPresenter, ItemPresenter);
            }
        };
        hzAdapter.setItemPresenter(new EventBlind.HzBindPresenter());
        binding.hzsView
                .setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        binding.hzsView
                .setAdapter(hzAdapter);
    }

    private void rateBind(ActivityTestBinding binding, List<RateItem> rateItems){
        BaseBindingAdapter rateAdapter = new BaseBindingAdapter<RateItem, ItemRateBinding>(rateItems,R.layout.item_rate) {
            @Override
            public void doBindViewHolder(BaseViewHolder<ItemRateBinding> holder, RateItem item) {
                holder.getBinding().setVariable(BR.item, item);
                holder.getBinding().setVariable(BR.itemPresenter, ItemPresenter);
            }
        };
        rateAdapter.setItemPresenter(new EventBlind.RateBindPresenter());
        binding.ratesView
                .setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        binding.ratesView
                .setAdapter(rateAdapter);

    }

    public class MenuHandler {

        public void tabFocus() {

        }
    }




    public static  final String json="{\"now\":{\"play\":{\"enabled\":false,\"play\":true,\"btn\":\"暂停\"},\"rate\":{\"id\":\"3\",\"name\":\"正常\",\"isCurrent\":true},\"hz\":{\"id\":\"0\",\"name\":\"超清\",\"isVip\":false,\"level\":1080},\"dm\":{\"enabled\":false,\"name\":\"弹幕开\"},\"xj\":{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEFPawmPLPSzUQPK1EmpW4241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEFPawmPLPSzUQPK1EmpW4241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第1集\",\"index\":0,\"site\":\"cctv\"}},\"video\":true,\"hzs\":[{\"id\":\"0\",\"name\":\"超清\",\"isVip\":false,\"level\":1080},{\"id\":\"1\",\"name\":\"高清\",\"isVip\":false,\"level\":720},{\"id\":\"2\",\"name\":\"标清\",\"isVip\":false,\"level\":480},{\"id\":\"3\",\"name\":\"流畅\",\"isVip\":false,\"level\":480}],\"xjs\":[{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEFPawmPLPSzUQPK1EmpW4241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEFPawmPLPSzUQPK1EmpW4241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第1集\",\"index\":0,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDENlVbFmFvGpcJZjM5ie9b241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDENlVbFmFvGpcJZjM5ie9b241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第2集\",\"index\":1,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEf0UNIWk0LDNRJKSXrKvv241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEf0UNIWk0LDNRJKSXrKvv241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第3集\",\"index\":2,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEfOvUN4ByrzXCUnnCxJn4241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEfOvUN4ByrzXCUnnCxJn4241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第4集\",\"index\":3,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEky737CJjkECt0QFypznF241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEky737CJjkECt0QFypznF241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第5集\",\"index\":4,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEKjStsWu7GMjP0dXWlrIV241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEKjStsWu7GMjP0dXWlrIV241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第6集\",\"index\":5,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDErauRBBfbiFhCB5v8tKya241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDErauRBBfbiFhCB5v8tKya241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第7集\",\"index\":6,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEk5QNBZCrR6c3tUKQ5G0v241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEk5QNBZCrR6c3tUKQ5G0v241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第8集\",\"index\":7,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDE7Ax2EJBHIIv6NLkcLeQ1241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDE7Ax2EJBHIIv6NLkcLeQ1241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第9集\",\"index\":8,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDE4tjHWxAz9hrJy1vvQIum241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDE4tjHWxAz9hrJy1vvQIum241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第10集\",\"index\":9,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDECC5cQljnq3VJiNDVCIh2241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDECC5cQljnq3VJiNDVCIh2241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第11集\",\"index\":10,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDErEGIBT80OQka4D4zzqyR241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDErEGIBT80OQka4D4zzqyR241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第12集\",\"index\":11,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEoxYPNHkw8H7tpaJbQ8Ez241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEoxYPNHkw8H7tpaJbQ8Ez241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第13集\",\"index\":12,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEfNaEXnhDLGlHKnbSyIiA241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEfNaEXnhDLGlHKnbSyIiA241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第14集\",\"index\":13,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEyszGt3kQcMqF0Ler3aqN241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEyszGt3kQcMqF0Ler3aqN241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第15集\",\"index\":14,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEGAj71EmjkfJCE3HJrLT0241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEGAj71EmjkfJCE3HJrLT0241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第16集\",\"index\":15,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEWrwLZ4ecCNheHxKkGagB241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEWrwLZ4ecCNheHxKkGagB241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第17集\",\"index\":16,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEPnIaxsZfUwQrNtsGZPu1241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEPnIaxsZfUwQrNtsGZPu1241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第18集\",\"index\":17,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDE5yVCNlsqB66wGZeXQnXd241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDE5yVCNlsqB66wGZeXQnXd241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第19集\",\"index\":18,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEt9IO3Ef2XAUk9kJj5ckI241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEt9IO3Ef2XAUk9kJj5ckI241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第20集\",\"index\":19,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDE0teKgS3YSkLXdsMbU5e7241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDE0teKgS3YSkLXdsMbU5e7241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第21集\",\"index\":20,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEaeBa6l7jvzA3FT9wi7QD240909\",\"url\":\"https://tv.cctv.com/2024/09/09/VIDEaeBa6l7jvzA3FT9wi7QD240909.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第22集\",\"index\":21,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDET6ITKgxlsT90wnDHRphg241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDET6ITKgxlsT90wnDHRphg241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第23集\",\"index\":22,\"site\":\"cctv\"},{\"vodId\":\"VIDAbX80MYkBm26iUGxFbZiS240823\",\"id\":\"VIDEdSIeI5j8tboauPoHuxQ3241004\",\"url\":\"https://tv.cctv.com/2024/10/04/VIDEdSIeI5j8tboauPoHuxQ3241004.shtml\",\"isVip\":false,\"remark\":\"\",\"title\":\"第24集\",\"index\":23,\"site\":\"cctv\"}],\"rates\":[{\"id\":\"0\",\"name\":\"2x\",\"isCurrent\":false},{\"id\":\"1\",\"name\":\"1.5x\",\"isCurrent\":false},{\"id\":\"2\",\"name\":\"1.25x\",\"isCurrent\":false},{\"id\":\"3\",\"name\":\"正常\",\"isCurrent\":true},{\"id\":\"4\",\"name\":\"0.5x\",\"isCurrent\":false}],\"tab\":\"hz\",\"focusId\":\"tv\",\"isVip\":true}";
}