package com.lx.picturesearch.util;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lx.picturesearch.Constants;
import com.lx.picturesearch.adapter.ImageAdapter;
import com.lx.picturesearch.R;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分享首选项的工具类
 */
public class Utils {

    /**
     * 上下文
     */
    public static Context context;


    // 单例模式
    private static BitmapUtils bitmapUtils;

    /**
     * 获取bitmapUtils实例的方法
     *
     * @return
     */
    public static BitmapUtils getBitmapUtils() {
        if (bitmapUtils == null) {
            bitmapUtils = new BitmapUtils(context);

            bitmapUtils.configDefaultLoadingImage(R.drawable.default_image);// 加载图片
            bitmapUtils.configDefaultLoadFailedImage(R.drawable.default_image);// 失败图片
            bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);// 编码格式

            bitmapUtils.configMemoryCacheEnabled(true);// 内存缓存
            bitmapUtils.configDiskCacheEnabled(true);// 磁盘缓存

        }
        return bitmapUtils;
    }


    public static void setContext(Context context) {
        Utils.context = context;
    }

    /**
     * 写入首选项文件(.xml)
     *
     * @param filename
     * @param key
     * @param value
     */
    public static void writeData(String filename, String key, String value) {
        //实例化SharedPreferences对象,参数1是存储文件的名称，参数2是文件的打开方式，当文件不存在时，直接创建，如果存在，则直接使用
        SharedPreferences mySharePreferences =
                context.getSharedPreferences(filename, Context.MODE_PRIVATE);

        //实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = mySharePreferences.edit();

        //用putString的方法保存数据
        editor.putString(key, value);

        //提交数据
        editor.commit();
    }


    /**
     * 从首选项中读取值
     *
     * @param filename
     * @param key
     */
    public static String readData(String filename, String key) {
        //实例化SharedPreferences对象
        SharedPreferences mySharePerferences =
                context.getSharedPreferences(filename, Context.MODE_PRIVATE);

        //用getString获取值
        String name = mySharePerferences.getString(key, "");
        return name;
    }


    /**
     * 获取全部的键值对
     *
     * @param filename
     * @return
     */
    public static Map<String, ?> getAll(String filename) {
        SharedPreferences sp =
                context.getSharedPreferences(filename, Context.MODE_PRIVATE);

        return sp.getAll();
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param filename
     * @param key
     * @return
     */
    public static boolean contains(String filename, String key) {
        SharedPreferences sp =
                context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    /**
     * 移除某个值
     *
     * @param filename
     * @param key
     */
    public static void remove(String filename, String key) {
        SharedPreferences sp = context.getSharedPreferences(filename,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.commit();
    }

    /**
     * 移除首选项全部值
     *
     * @param filename
     */
    public static void removeAll(String filename) {
        SharedPreferences sp = context.getSharedPreferences(filename,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
        editor.commit();
    }

    /**
     * 获取网址中的图片名称
     *
     * @param url
     * @return
     */
    public static String cutImagePath(String url) {
        String res = "";
        int start = url.lastIndexOf("/") + 1;
        res = url.substring(start);
        return res;
    }

    /**
     * 获取SDcard根路径
     *
     * @return
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    /**
     * 返回"true"的个数
     *
     * @return
     */
    public static int getSelectedNumber(ImageAdapter adapter) {
        int number = 0;
        for (Boolean value : adapter.getCheckList().values()) {
            if (value) {
                number++;
            }
        }
        return number;
    }

    /**
     * 判断是否有1个选中
     *
     * @return
     */
    public static boolean hasSelected(ImageAdapter adapter) {
        for (Boolean value : adapter.getCheckList().values()) {
            if (value) {
                return true;
            }
        }
        return false;
    }


    /**
     * 集合到数组
     *
     * @return
     */
    public static String[] listToArray(List<String> list) {
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }


    /**
     * 获取文件中的所有图片的绝对路径
     *
     * @param dir
     * @return
     */
    public static List<String> getDownloadImages(String dir) {
        List<String> res = new ArrayList<String>();
        File fdir = new File(dir);
        File[] files = fdir.listFiles();
        if (files != null) {
            // 遍历
            for (int i = 0; i < files.length; i++) {
                res.add(files[i].getAbsolutePath());// 绝对路径
            }
        }
        return res;
    }


    /**
     * 过滤出有效链接
     *
     * @param links
     * @return
     */
    public static List<String> getUseableLinks(Elements links, String currURL) {
        Map<String, String> mapLinks = new HashMap<String, String>();
        List<String> lstLinks = new ArrayList<String>();

        String home = currURL;// 本站的域名

        //遍历所有links,过滤,保存有效链接
        for (Element link : links) {
            String href = link.attr("href");// abs:href, "http://"
            //Log.i("spl","过滤前,链接:"+href);
            // 设置过滤条件
            if (href.equals("")) {
                continue;// 跳过
            }
            if (href.equals(home)) {
                continue;// 跳过
            }
            if (href.startsWith("javascript")) {
                continue;// 跳过
            }

            if (href.startsWith("/")) {
                href = home + href;
            }
            if (!mapLinks.containsKey(href)) {
                mapLinks.put(href, href);// 将有效链接保存至哈希表中
                lstLinks.add(href);
            }

            Log.i("wxs", "有效链接:" + href);
        }

        return lstLinks;
    }


    /**
     * 判断当前应用程序处于前台还是后台
     * return false 在前台
     * return true 在后台
     */
    public static boolean isAppToBackground(final Context context) {
        if (context != null){
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (!tasks.isEmpty()) {
                ComponentName topActivity = tasks.get(0).topActivity;
                if (!topActivity.getPackageName().equals(context.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }




    /**
     * 分割字符串
     *
     * @param all   要分割的字符串
     * @param start 开始
     * @param end   结束
     * 如"("  ")"
     * @return
     */
    public static String splitStr(String all, String start, String end) {
        int s = all.indexOf(start);
        int l = all.indexOf(end);
        end = all.substring(s + 1, l);
        return end;
    }

    /**
     * 更新历史记录集合
     * @param list 要全部添加的集合
     * @param filePath 历史记录文件路径
     */
    public static List<String> updateHistory(List<String> list, String filePath){
        Map<String, ?> spUrls = Utils.getAll(filePath);//从首选项中拿值

        Log.i("wxs", "--------");
        String[] arr = new String[spUrls.size()];// 定义一个数组

        int index = 0;
        for (String key : spUrls.keySet()) {
            //String url = surl.get(key).toString();
            arr[index] = key;
            index++;
        }

        final String[] historyUrls = arr;//历史记录列表

        //数组转化成集合
        list = Arrays.asList(historyUrls);
        return list;
    }





    public static void downloadImage(String url){
        File fdir = new File(Constants.SAVE_DIR);
        //创建文件夹
        if(!fdir.exists()){
            fdir.mkdirs();
        }
        String target = Constants.SAVE_DIR + "/" + System.currentTimeMillis() + Utils.cutImagePath(url);//避免重名

        new HttpUtils().download(url,target,new RequestCallBack<File>() {
            @Override
            public void onSuccess(ResponseInfo<File> fileResponseInfo) {
                Utils.showToast("图片下载成功");
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Utils.showToast("图片下载失败");
            }
        });
    }




    /**
     * 得到自定义的progressDialog
     *
     * @param context
     * @param msg
     * @return
     */
    public static Dialog createLoadingDialog(Context context, String msg) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.loading_animation);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);// 设置加载信息

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

        //loadingDialog.setCancelable(false);// 不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));// 设置布局
        return loadingDialog;
    }
}
