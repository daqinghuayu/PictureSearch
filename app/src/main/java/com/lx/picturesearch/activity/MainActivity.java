package com.lx.picturesearch.activity;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lx.picturesearch.Constants;
import com.lx.picturesearch.ISelect;
import com.lx.picturesearch.R;
import com.lx.picturesearch.adapter.ImageAdapter;
import com.lx.picturesearch.util.Utils;
import com.z.dragimageviewapplication.DragImageActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener, ISelect, AdapterView.OnItemClickListener {

    //网格
    GridView gv_main;
    //信息条
    TextView tv_info;
    //进度条
    ProgressBar pb_hor;

    Button btn_stop;
    //适配器
    ImageAdapter adapter;
    //回退 下载 全选
    ImageView iv_main_back, iv_btn, iv_select;
    //设置 深度搜索
    RelativeLayout menu_main_corner, rl_deep_search,rl_main_check_picture;
    //主页侧滑菜单
    DrawerLayout drawer_main;

    //获得自定义的旋转进度框
    Dialog grabingDialog;//正在抓取自定义对话框

    String html;
    List<String> list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
        setContentView(R.layout.activity_main);

        Utils.setContext(this);

        // 接收从Home传来的参数
        String url = getIntent().getStringExtra("url");
        int state = getIntent().getIntExtra("state",0);


        //初始化控件
        initView();

        //抓取进度框
        grabingDialog = Utils.createLoadingDialog(this, "");

        updateIV_btn();

        //初始化监听
        initListener();


        if(state==Constants.S_SDCARD){
            // 获取文件信息
            menu_main_corner.setVisibility(View.GONE);
            list = getDownloadImages(Constants.SAVE_DIR);
            adapter.setList(list);
            adapter.notifyDataSetChanged();
            Constants.state = Constants.S_SDCARD;// 更新状态
            update_infobar("");
        }else{
            getHttpImage(url);
        }



    }


    private void initView() {
        menu_main_corner = (RelativeLayout) findViewById(R.id.menu_main_corner);
        rl_deep_search = (RelativeLayout) findViewById(R.id.rl_deep_search);
        rl_main_check_picture = (RelativeLayout) findViewById(R.id.rl_main_check_picture);
        iv_main_back = (ImageView) findViewById(R.id.iv_main_back);
        gv_main = (GridView) findViewById(R.id.gv_main);
        tv_info = (TextView) findViewById(R.id.tv_info);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        iv_btn = (ImageView) findViewById(R.id.iv_btn);
        iv_select = (ImageView) findViewById(R.id.iv_select);
        pb_hor = (ProgressBar) findViewById(R.id.pb_hor);

        drawer_main = (DrawerLayout) findViewById(R.id.drawer_main);


        adapter = new ImageAdapter(this);
        adapter.setiSelect(this);
        list = new ArrayList<String>();
        gv_main.setAdapter(adapter);
        gv_main.setOnItemClickListener(this);


    }

    //初始化右上角的隐藏图标
    private void updateIV_btn() {
        if (Constants.state == Constants.S_WEB) {
            //下载
            iv_btn.setImageResource(R.drawable.icon_s_download_press);
        } else {
            //删除
            iv_btn.setImageResource(R.drawable.op_del_press);
        }
    }

    private void initListener() {
        menu_main_corner.setOnClickListener(this);
        iv_main_back.setOnClickListener(this);
        rl_deep_search.setOnClickListener(this);
        rl_main_check_picture.setOnClickListener(this);
        btn_stop.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        //右边侧滑
        if (v.getId() == R.id.menu_main_corner) {
            if (drawer_main.isDrawerOpen(Gravity.RIGHT)) {
                drawer_main.closeDrawer(Gravity.RIGHT);
            } else {
                drawer_main.openDrawer(Gravity.RIGHT);
            }
        }

        //深度搜索
        if (v.getId() == R.id.rl_deep_search) {

            deepSearch(html);
            drawer_main.closeDrawer(Gravity.RIGHT);
        }

        //查看下载
        if (v.getId() == R.id.rl_main_check_picture) {

            Intent intent = new Intent(this,MainActivity.class);
            intent.putExtra("state", Constants.S_SDCARD);
            startActivity(intent);
        }

        //后退
        if (v.getId() == R.id.iv_main_back) {
            onBackPressed();
        }
        //停止搜索
        if (v.getId() == R.id.btn_stop) {
            stopGrab();
        }

    }

//    ProgressDialog pd;  //对话框对象

   /* //信息
    public void showProgressDialog(String msg) {
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setTitle("提示信息");
        pd.setMessage(msg);
        pd.setCancelable(true);
        pd.show();

    }*/




    //系统后退
    @Override
    public void onBackPressed() {
        //关闭抽屉
        if (drawer_main.isDrawerOpen(Gravity.RIGHT)) {
            drawer_main.closeDrawer(Gravity.RIGHT);
        }else if(inDeepSearch) {//停止搜索
            stopGrab();
        }else{
            super.onBackPressed();
        }


    }



    /**
     * 根据当前状态更新信息栏
     */
    private void update_infobar(String url) {
        if (Constants.state == Constants.S_WEB) {
            tv_info.setText("总共搜索到" + list.size() + "张图片!");
        }
        if (Constants.state == Constants.S_SDCARD) {
            tv_info.setText("在下载文件夹中共有" + list.size() + "张图片!");
        }
        updateIV_btn();
        Constants.selectedAll = false;
        iv_select.setImageResource(R.drawable.op_select_nothing_press);

    }


    /**
     * 获取文件中的所有图片的绝对路径
     *
     * @param dir
     * @return
     */
    private List<String> getDownloadImages(String dir) {
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



    String currURL = "";
    String currquery = "";

    //获取图片
    private void getHttpImage(final String query) {
        currquery = query;
        currURL = query;
        //提交网址
        if (!query.startsWith("http")) {
            currURL = "http://" + query;
        }
//        showProgressDialog("正在搜索" + currURL + "网页上的图片");
        grabingDialog.show();
        new HttpUtils().send(
                HttpRequest.HttpMethod.GET,
                currURL,
                new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> stringResponseInfo) {
                        // 首页内容
                        html = stringResponseInfo.result;
                        //解析HTML页面
                        Document doc = Jsoup.parse(html);

                        //获取所有连接
                        Elements links = doc.select("a[href]");
                        Log.i("lx", "总数" + links.size());
                        List<String> useLinks = getUseableLinks(links);// 过滤
                        Log.i("lx", "过滤后" + useLinks.size());

                        // 获取图片
                        List<Element> imgs = doc.getElementsByTag("img");
                        list.clear();

                        mapImages.clear();// 清空
                        for (Element img : imgs) {
                            String src = img.attr("abs:src");
                            // 保持顺序不变,同时去重
                            if (!mapImages.containsKey(src)) {
                                list.add(src);
                                mapImages.put(src, src);// 去重src
                            }
                        }

                        //UI
                        adapter.setList(list);
                        adapter.notifyDataSetChanged();

                        Constants.state = Constants.S_WEB;
                        update_infobar(query);

                        grabingDialog.dismiss();
//                        pd.dismiss();
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {

                    }
                });


    }


    int numTotalLinks = 0;// 总连接数
    int numCurrLinks = 0; // 抓取完的当前链接
    Map<String, String> mapImages = new HashMap<String, String>();


    // 在深度搜索中
    boolean inDeepSearch = false;
    // 停止抓取
    boolean stopGrab = false;

    /**
     * 深度搜索
     *
     * @param
     */
    private void deepSearch(String html) {

        inDeepSearch = true;
        stopGrab = false;


        Document doc = Jsoup.parse(html);// 解析HTML页面
        // 获取页面中的所有连接
        Elements links = doc.select("a[href]");
        Log.i("lx", "链接总数: " + links.size());
        List<String> useLinks = getUseableLinks(links);// 过滤
        Log.i("lx", "过滤后,链接总数: " + useLinks.size());

        numTotalLinks = useLinks.size();
        numCurrLinks = 0;

        pb_hor.setVisibility(View.VISIBLE);
        btn_stop.setVisibility(View.VISIBLE);
        pb_hor.setMax(numTotalLinks);
        pb_hor.setProgress(numCurrLinks);


        // 遍历哈希表
        for (final String href : useLinks) {

            // 新的抓取线程
            new HttpUtils().send(HttpRequest.HttpMethod.GET, href, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> objectResponseInfo) {
                    if (!stopGrab) {
                        // 拿到2级页面源码
                        String html = objectResponseInfo.result;
                        /***********************解析<img>图片标签**************************/
                        // 框架JSoup:GitHub
                        Document doc = Jsoup.parse(html);// 解析HTML页面
                        // 获取图片
                        List<Element> imgs = doc.getElementsByTag("img");
                        // 存入集合=哈希表
                        // 所有图片地址
                        for (Element img : imgs) {
                            String src = img.attr("abs:src");
                            // 保持顺序不变,同时去重
                            if (!mapImages.containsKey(src)) {
                                list.add(src);
                                mapImages.put(src, src);// 去重src
                            }
                            //}
                        }

                        /***********************解析<img>图片标签**************************/
                        update_final(href);
                    }


                }

                @Override
                public void onFailure(HttpException e, String s) {

                    if (!stopGrab) {
                        update_final(href);
                    }

                }
            });

        }

    }

    /**
     * 在最后一个链接抓取结束后,更新列表
     *
     * @param href
     */
    private void update_final(String href) {
        // 处理进度条
        numCurrLinks++;
        pb_hor.setProgress(numCurrLinks);
        tv_info.setText("深度搜索中("+numCurrLinks+"/"+numTotalLinks+")," + href );
        adapter.setList(list);
        adapter.notifyDataSetChanged();


        if (numCurrLinks >= numTotalLinks) {// 最后一名
            pb_hor.setProgress(numTotalLinks);
            pb_hor.setVisibility(View.GONE);
            btn_stop.setVisibility(View.GONE);

            update_infobar(currquery);
            inDeepSearch = false;

        }
    }

    @Override
    public void handleSelected(int position) {
        boolean temp = adapter.getCheckList().get(position);//选中
        adapter.getCheckList().put(position, !temp);//取反赋值
        adapter.notifyDataSetChanged();//跟新
        //判斷当前选中个数:1选中/ 0个选中
        if (hasSelected()) {
            iv_btn.setVisibility(View.VISIBLE);
            iv_select.setVisibility(View.VISIBLE);
        } else {
            iv_btn.setVisibility(View.GONE);
            iv_select.setVisibility(View.GONE);
        }
    }

    /**
     * 判断是否有一个选中
     *
     * @return
     */
    private boolean hasSelected() {
        for (Boolean value : adapter.getCheckList().values()) {
            if (value) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回"true"的个数
     *
     * @return
     */
    private int getSelectedNumber() {
        int number = 0;
        for (Boolean value : adapter.getCheckList().values()) {
            if (value) {
                number++;
            }
        }
        return number;
    }

    // 点击"批量"图片按钮
    public void iv_btnClick(View v) {

        if (v.getId() == R.id.iv_select) {
            // 全选/全不选
            // 改变哈希表

            Constants.selectedAll = !Constants.selectedAll;
            adapter.setCheckList(Constants.selectedAll);
            adapter.notifyDataSetChanged();

            if (Constants.selectedAll) {
                iv_select.setImageResource(R.drawable.op_select_all_press);
            } else {
                iv_select.setImageResource(R.drawable.op_select_nothing_press);
            }

            // 判断当前选中的个数: 1个选中/ 0 个选中
            if (hasSelected()) {
                // 图片显示
                iv_btn.setVisibility(View.VISIBLE);
                iv_select.setVisibility(View.VISIBLE);
            } else {
                iv_btn.setVisibility(View.GONE);
                iv_select.setVisibility(View.GONE);
            }

        }

        if (v.getId() == R.id.iv_btn) {

            if (Constants.state == Constants.S_WEB) {
                // 批量下载
                Constants.num_max = getSelectedNumber();
                Constants.num_curr = 0;
                // 添加下载进度(水平)
//                showProgressDialog("开始批量下载");
                grabingDialog.show();
//                pd.setTitle("批量下载");
//                pd.setMax(Constants.num_max);
//                pd.setProgress(Constants.num_curr);
                // 遍历哈希表, 哪些选中的item是下载的图片
                for (Integer position : adapter.getCheckList().keySet()) {
                    if (adapter.getCheckList().get(position)) {
                        // 当前图片需要下载
                        downloadImage(list.get(position));// 子线程

                    }
                }

            } else {
                // 批量删除
                for (Integer position : adapter.getCheckList().keySet()) {
                    if (adapter.getCheckList().get(position)) {
                        // 当前图片需要删除
                        String path = list.get(position);
                        File file = new File(path);
                        if (file.exists()) {
                            file.delete();// 删除
                        }
                    }
                }
                Utils.showToast("所有图片删除完毕!");
                // 列表更新
                list = getDownloadImages(Constants.SAVE_DIR);
                adapter.setList(list);
                adapter.notifyDataSetChanged();
                Constants.state = Constants.S_SDCARD;// 更新状态
                update_infobar("");
                iv_btn.setVisibility(View.GONE);
                iv_select.setVisibility(View.GONE);

            }
        }

    }

    //看大图
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //item点击事件
        Intent intent = new Intent(this, DragImageActivity.class);
        intent.putExtra(Constants.P_URL, listToArray());//图片地址
        intent.putExtra(Constants.P_POS, position);//图片位置
        startActivity(intent);
    }

    private String[] listToArray() {
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    /**
     * 过滤出有效链接
     *
     * @param links
     * @return
     */
    private List<String> getUseableLinks(Elements links) {
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

            Log.i("lx", "有效链接:" + href);
        }

        return lstLinks;
    }


    // 停止抓取
    private void stopGrab() {
        stopGrab = true;
        pb_hor.setVisibility(View.GONE);
        btn_stop.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
        update_infobar(currquery);
        inDeepSearch = false;
        Utils.showToast("深度抓取已经终止");
    }

    public void downloadImage(String url) {
        File fdir = new File(Constants.SAVE_DIR);
        //创建文件夹
        if (!fdir.exists()) {
            fdir.mkdirs();
        }
        String target = Constants.SAVE_DIR + "/" + System.currentTimeMillis() + Utils.cutImagePath(url);//避免重名

        new HttpUtils().download(url, target, new RequestCallBack<File>() {
            @Override
            public void onSuccess(ResponseInfo<File> fileResponseInfo) {
                Constants.num_curr++;
//                pd.setProgress(Constants.num_curr);
                if (Constants.num_curr >= Constants.num_max) {
                    //全部下载完
                    Utils.showToast("所有图片下载成功");
//                    pd.dismiss();
                    grabingDialog.dismiss();
                    //去掉勾选
                    adapter.initCheckList();//初始化
                    adapter.notifyDataSetChanged();
                    iv_btn.setVisibility(View.GONE);
                    iv_select.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Utils.showToast("图片" + list.get(Constants.num_curr) + "下载失败");
                Constants.num_curr++;
//                pd.setProgress(Constants.num_curr);
                if (Constants.num_curr >= Constants.num_max) {
                    //全部下载完
                    Utils.showToast("所有图片下载成功");
//                    pd.dismiss();
                    //去掉勾选
                    adapter.initCheckList();//初始化
                    adapter.notifyDataSetChanged();

                    iv_btn.setVisibility(View.GONE);
                    iv_select.setVisibility(View.GONE);
                }
            }
        });


    }
}

