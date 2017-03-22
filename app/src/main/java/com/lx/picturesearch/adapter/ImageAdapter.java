package com.lx.picturesearch.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lidroid.xutils.BitmapUtils;
import com.lx.picturesearch.ISelect;
import com.lx.picturesearch.R;
import com.lx.picturesearch.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/18.
 */
public class ImageAdapter extends BaseAdapter {

    //声明接口
    ISelect iSelect;

    public void setiSelect(ISelect iSelect) {
        this.iSelect = iSelect;
    }

    //哈希表
    Map<Integer,Boolean> checkList = new HashMap<Integer, Boolean>();

    public Map<Integer, Boolean> getCheckList() {
        return checkList;
    }

    public void setCheckList(boolean value) {
        for (int i = 0; i < list.size(); i++) {
            checkList.put(i,value);
        }
    }

    public void initCheckList(){
        checkList.clear();
        for(int i = 0; i<list.size() ;i++){
            checkList.put(i,false);
        }
    }


    BitmapUtils bitmapUtils;

    /**数据集合*/
    List<String> list;

    /**反射器*/
    LayoutInflater inflater;

    public ImageAdapter() {}

    /**构造器*/
    public ImageAdapter(Context context) {
        inflater =LayoutInflater.from(context);
        bitmapUtils = Utils.getBitmapUtils();
        bitmapUtils.configDefaultLoadingImage(R.drawable.default_image);
        bitmapUtils.configDefaultLoadFailedImage(R.drawable.default_image);
        bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);

        bitmapUtils.configMemoryCacheEnabled(true);//内存缓存
        bitmapUtils.configDiskCacheEnabled(true);//磁盘缓存
    }

    /**
     * 传入数据集合
     * @param list
     */
    public void setList(List<String> list) {
        this.list = list;
        initCheckList();
    }


    @Override
    public int getCount() {
        return (list==null)?0:list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder  holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item, null);
            holder = new ViewHolder();
            holder.img = (ImageView) convertView.findViewById(R.id.iv_img);
            //holder.pb = (ProgressBar) convertView.findViewById(R.id.pb);
            holder.iv_select = (ImageView) convertView.findViewById(R.id.iv_select);
            holder.box = (RelativeLayout) convertView.findViewById(R.id.right_box);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        String url = list.get(position);
        bitmapUtils.display(holder.img,url);// 显示图片

        // 根据哈希表决定item选中状态
        if (checkList.get(position)){// 选中
            holder.iv_select.setImageResource(R.drawable.blue_selected);
        }else{// 不选中
            holder.iv_select.setImageResource(R.drawable.blue_unselected);
        }

        //编写选中的点击事件
        holder.box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 把业务逻辑转移到外面
                iSelect.handleSelected(position);
            }
        });



        return convertView;
    }

  /*  public class MyCallBack extends DefaultBitmapLoadCallBack<ImageView>{
        ViewHolder holder;

        public MyCallBack(ViewHolder holder){
            this.holder = holder;
        }

        @Override
        public void onLoadStarted(ImageView container, String uri, BitmapDisplayConfig config) {
//            this.holder.pb.setProgress(0);
            super.onLoadStarted(container, uri, config);
        }

        @Override
        public void onLoading(ImageView container, String uri, BitmapDisplayConfig config, long total, long current) {
//           this.holder.pb.setProgress((int) (current*100/total));
        }

        @Override
        public void onLoadCompleted(ImageView container, String uri, Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
//            this.holder.pb.setProgress(100);
            super.onLoadCompleted(container, uri, bitmap, config, from);
        }

    }*/


    public  class ViewHolder{
        ImageView img;
//        ProgressBar pb;
        RelativeLayout box;
        ImageView iv_select;
    }

}