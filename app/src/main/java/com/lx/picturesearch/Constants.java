package com.lx.picturesearch;


import com.lx.picturesearch.util.Utils;

/**
 * 常量类
 */
public class Constants {

    //传递图片网址的关键字
    public static final String P_URL = "url";
    public static final String P_POS = "pos";

    public static final String SAVE_DIR = Utils.getSDCardPath() + "/mypics";

    public static final int S_WEB = 0;//网络图片
    public static final int S_SDCARD = 1;//本地图片
    public static int state = S_WEB;//程序装填
    public static int num_max = 0;//下载总量
    public static int num_curr = 0;//下载当前量


    public static boolean selectedAll = false;
}

