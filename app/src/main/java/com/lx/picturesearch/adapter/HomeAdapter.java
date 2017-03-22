package com.lx.picturesearch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.BitmapUtils;
import com.lx.picturesearch.R;
import com.lx.picturesearch.entity.HomeLink;
import com.lx.picturesearch.util.Utils;

import java.util.List;


/**
 * 首页推荐适配器
 */
public class HomeAdapter extends BaseAdapter {
    BitmapUtils bitmapUtils;
    /**
     * 数据集合
     */
    List<HomeLink> list;


    /**
     * 反射器
     */
    LayoutInflater inflater;

    public HomeAdapter() {
    }

    /**
     * 构造器
     *
     * @param context 上下文
     */
    public HomeAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        bitmapUtils = Utils.getBitmapUtils();
    }

    /**
     * 传入数据集合
     *
     * @param list
     */
    public void setList(List<HomeLink> list) {
        this.list = list;
    }

    public void addList(List<HomeLink> list) {
        this.list.addAll(list);// 尾部追加
    }

    @Override
    public int getCount() {
        return (list == null) ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_home, null);
            holder = new ViewHolder();
            holder.img = (ImageView) convertView.findViewById(R.id.iv_img);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        HomeLink link = list.get(position);
        holder.tv_name.setText(link.name);
        bitmapUtils.display(holder.img, link.image);// 显示图片
        return convertView;
    }


    public static class ViewHolder {
        ImageView img;
        TextView tv_name;
    }

}
