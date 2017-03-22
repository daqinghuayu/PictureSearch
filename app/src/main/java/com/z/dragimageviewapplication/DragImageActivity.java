package com.z.dragimageviewapplication;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lx.picturesearch.Constants;
import com.lx.picturesearch.R;
import com.lx.picturesearch.util.Utils;
import com.z.dragimageviewapplication.ui.ImgDetailFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class DragImageActivity extends FragmentActivity implements View.OnClickListener {

    private ViewPager mViewPager;
    private int iIndex;
    private TextView tv_name, tv_pageno;
    private ImageView iv_dl;

    private ImageView[] dots;
    private ArrayList<String> imgLists;// 图片集合
    private PictureSlidePagerAdapter viewPagerAdapter;

    // 图片的来源
    private String imgIds[] = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getActionBar().hide();
        setContentView(R.layout.activity_drag_image);

        imgIds = getIntent().getStringArrayExtra(Constants.P_URL);
        iIndex = getIntent().getIntExtra(Constants.P_POS, 0);

        initialize();

        mViewPager.setCurrentItem(iIndex);// 设当前页
        setCurrentDot(iIndex);
    }

    private void initialize() {
        initData();
        initView();
        initListener();
    }

    int total = 0;


    private void initData() {

        imgLists = new ArrayList<String>();
        // 数组 --> List集合
        for (String id : imgIds) {
            imgLists.add(id);
        }
        total = imgLists.size();// 求总数

    }

    private void initView() {

        mViewPager = (ViewPager) findViewById(R.id.vp_img_prev_container);
        viewPagerAdapter = new PictureSlidePagerAdapter(getSupportFragmentManager());
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_pageno = (TextView) findViewById(R.id.tv_pageno);

        mViewPager.setAdapter(viewPagerAdapter);

        iv_dl = (ImageView) findViewById(R.id.iv_dl);

        iv_dl.setOnClickListener(this);

        if (Constants.state == Constants.S_WEB) {
            iv_dl.setImageResource(R.drawable.icon_s_download_press);
        } else {
            iv_dl.setImageResource(R.drawable.toolbar_pop_send_press);
        }

        initDots();//页面标记
    }

    private void initListener() {
        //viewPager的页面切换监听
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int position) {
                setCurrentDot(position);
            }

            @Override
            public void onPageScrollStateChanged(int position) {

            }
        });
    }

    //点击事件
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.iv_dl) {
            if (Constants.state == Constants.S_WEB) {
                //下载
               Utils.downloadImage(imgIds[iIndex]);
            } else {
                //设置壁纸
                openFile(this,imgIds[iIndex]);

            }
        }


    }

    /**
     * 分享本地图片
     * @param context
     * @param path
     */
    public static void openFile(Context context,String path){
        File file = new File(path);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        if(file.exists()){
            intent.setDataAndType(Uri.fromFile(file),"image/*");
            context.startActivity(intent);
        }
    }




    /**
     * 设置壁纸
     *
     * @param bitmap
     */
    private void putWallpaper(Bitmap bitmap) {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            wallpaperManager.setBitmap(bitmap);
            Utils.showToast("设置成功");
        } catch (IOException e) {
            Utils.showToast("设置失败");
            e.printStackTrace();
        }

    }


    class PictureSlidePagerAdapter extends FragmentStatePagerAdapter {

        public PictureSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return ImgDetailFragment.newInstance(imgLists.get(i));
        }

        @Override
        public int getCount() {
            return imgLists.size();
        }
    }

    /**
     * 初始化指示点，根据图片数组数量创建指示点
     */
    private void initDots() {


//        //只有一张图片，或者没有图片，不创建指示点
//        if (imgLists.size() <= 1) {
//            return;
//        }
//        //指示点的容器，横向linearLayout
//        LinearLayout ll = (LinearLayout) findViewById(R.id.indicator);
//
//        dots = new ImageView[imgLists.size()];
//
//        //两个指示点之间间隙
//        int dip5 = UiUtils.dip2px(this, 5);
//
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dip5 * 2, dip5, 1);
//        layoutParams.setMargins(dip5 / 2, 0, dip5 / 2, 0);
//
//
//        // 循环取得小点图片
//        for (int i = 0; i < imgLists.size(); i++) {
//
//            ImageView imageView = new ImageView(this);
//            dots[i] = imageView;
//            dots[i].setBackgroundColor(Color.WHITE);// 都设为白色
//            ll.addView(dots[i], layoutParams);
//        }
//
//        dots[iIndex].setBackgroundColor(Color.RED);// 设置为白色，即选中状态
    }

    /**
     * 设置指示点的选中位置
     *
     * @param position
     */
    private void setCurrentDot(int position) {

        int curr = position + 1;
        tv_pageno.setText(curr + "/" + total);
        tv_name.setText(Utils.cutImagePath(imgLists.get(position)));


//        if (position < 0 || position > imgLists.size() - 1 || imgLists.size() <= 1
//                || iIndex == position) {
//            return;
//        }
//
//        dots[position].setBackgroundColor(Color.RED);
//        dots[iIndex].setBackgroundColor(Color.WHITE);

        iIndex = position;
    }
}
