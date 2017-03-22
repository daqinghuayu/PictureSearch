package com.z.dragimageviewapplication.ui;


import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lx.picturesearch.R;
import com.lx.picturesearch.util.Utils;
import com.z.dragimageviewapplication.util.UiUtils;
import com.z.dragimageviewapplication.view.DragImageView;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class ImgDetailFragment extends Fragment {

    private static final String ARG_IMG_ID = "param1";

    private DragImageView imageView;

    private String imgResourceId;//图片资源ID
    private int screenWidth;//屏幕宽度
    private int screenHeight;//屏幕高度
    private int stateHeight =0;//状态栏高度


    /**
     * 获取ImgDetailFragment 实例
     * @param imgResourceId 图片资源ID
     * @return
     */
    public static ImgDetailFragment newInstance(String imgResourceId) {
        ImgDetailFragment fragment = new ImgDetailFragment();
        Bundle arg = new Bundle();// 传参
        arg.putString(ARG_IMG_ID, imgResourceId);
        fragment.setArguments(arg);
        return fragment;
    }


    public ImgDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);


        if (getArguments() != null) {
            imgResourceId = getArguments().getString(ARG_IMG_ID);// 取参
        }

         //获取屏幕宽度
        screenWidth = UiUtils.getWindowWidth(this.getActivity());
        //获取屏幕高度
        screenHeight = UiUtils.getWindowHeight(this.getActivity());
        //获取状态栏高度
        if (null==saveInstanceState) {
            Rect frame = new Rect();
            ImgDetailFragment.this.getActivity().getWindow().getDecorView()
                    .getWindowVisibleDisplayFrame(frame);
            stateHeight = frame.top;
        }else {
            stateHeight =saveInstanceState.getInt("stateHeight");
        }

        screenHeight = screenHeight - stateHeight;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = (View) inflater.inflate(R.layout.fragment_img_detail, container, false);

        imageView = (DragImageView) view.findViewById(R.id.iv_big_img);

        imageView.setScreen_H(screenHeight);
        imageView.setScreen_W(screenWidth);

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        //根据图片资源ID获取bitmap
//        final Bitmap bitmap=BitmapUtil.ReadBitmapById(getActivity(),imgResourceId);

        String uri = imgResourceId;
        //改造bitmapUtuils
        imageView.setVisibility(View.VISIBLE);
        Utils.getBitmapUtils().display(imageView,uri);

//        setBitmap(bitmap);// 显示图片

    }


    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putInt("stateHeight", stateHeight);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach(){
        if (imageView != null) {
            // Cancel any pending image work
            imageView.setImageDrawable(null);
        }
        super.onDetach();
    }

    public void setBitmap( Bitmap bitmap) {

        if (null==bitmap){
            return;
        }

        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(bitmap);

    }

}
