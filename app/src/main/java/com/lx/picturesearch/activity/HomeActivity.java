package com.lx.picturesearch.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.lx.picturesearch.Constants;
import com.lx.picturesearch.R;
import com.lx.picturesearch.adapter.HomeAdapter;
import com.lx.picturesearch.entity.HomeLink;
import com.lx.picturesearch.entity.HomeLinkList;
import com.lx.picturesearch.util.SPUtil;
import com.lx.picturesearch.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomeActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    GridView gv_home;

    HomeAdapter adapter;

    private long mExitTime;//双击退出

    private List<HomeLink> list;//当前列表
    private List<HomeLink> bakList = new ArrayList<HomeLink>();//备用列表

    RelativeLayout menu_corner, menu_search;//右上角菜单按钮
    //侧滑菜单按钮
    RelativeLayout rl_check_picture,rl_update_list,rl_recover_list;

    DrawerLayout drawer_home;//主页侧滑菜单

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
        setContentView(R.layout.activity_home);

        //设置全局上下文
        Utils.setContext(this);

        //初始化控件
        initView();

        //初始化数据
        initData();

        //初始化监听
        initListener();

    }


    private void initView() {
        menu_corner = (RelativeLayout) findViewById(R.id.menu_corner);
        menu_search = (RelativeLayout) findViewById(R.id.menu_search);
        drawer_home = (DrawerLayout) findViewById(R.id.drawer_home);

        rl_check_picture = (RelativeLayout) findViewById(R.id.rl_check_picture);
        rl_update_list = (RelativeLayout) findViewById(R.id.rl_update_list);
        rl_recover_list = (RelativeLayout) findViewById(R.id.rl_recover_list);


        gv_home = (GridView) findViewById(R.id.gv_home);
        adapter = new HomeAdapter(this);
        gv_home.setAdapter(adapter);

    }

    private void initData() {
        list = new ArrayList<HomeLink>();

        if (list.size() == 0){
            addList();//准备默认列表
            adapter.setList(list);
            adapter.notifyDataSetChanged();
        }

        //获得首选项
        getSP();


        addBakList();//准备备份列表
    }

    private void initListener() {
        menu_corner.setOnClickListener(this);
        menu_search.setOnClickListener(this);
        gv_home.setOnItemClickListener(this);


        rl_check_picture.setOnClickListener(this);
        rl_update_list.setOnClickListener(this);
        rl_recover_list.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        //右边侧滑
        if(v.getId() == R.id.menu_corner){
            if (drawer_home.isDrawerOpen(Gravity.RIGHT)) {
                drawer_home.closeDrawer(Gravity.RIGHT);
            } else {
                drawer_home.openDrawer(Gravity.RIGHT);
            }
        }

        //搜索
        if(v.getId() == R.id.menu_search){

            Intent intent = new Intent(HomeActivity.this,SearchActivity.class);
            startActivity(intent);


        }

        //查看下载图片
        if(v.getId() == R.id.rl_check_picture){

            Intent intent = new Intent(this,MainActivity.class);
            intent.putExtra("state", Constants.S_SDCARD);
            startActivity(intent);
        }


        //更新推荐列表
        if(v.getId() == R.id.rl_update_list){
            //先关闭抽屉
            drawer_home.closeDrawers();
            list = createRandomList(bakList, 15);//把默认列表替换为随机生成的列表
            //放入首选项
            setSP();
            adapter.setList(list);
            adapter.notifyDataSetChanged();
        }

        //恢复默认
        if(v.getId() == R.id.rl_recover_list){
            //移除所有添加的网址
            list.removeAll(list);
            list.clear();//清空当前集合
            //添加默认网址
            addList();
            //把默认网址添加到首选项中
            adapter.notifyDataSetChanged();
            setSP();
            drawer_home.closeDrawers();
        }




    }




    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HomeLink link = (HomeLink) parent.getItemAtPosition(position);
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("url",link.url);
        startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        //关闭抽屉
        if (drawer_home.isDrawerOpen(Gravity.RIGHT)) {
            drawer_home.closeDrawer(Gravity.RIGHT);
        }

        //双击退出
        if (!drawer_home.isDrawerOpen(Gravity.RIGHT) //抽屉关闭状态
               ){
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出",Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
        }

    }

    /**
     * 从list中随机抽取元素
     * @param list  要抽取的集合
     * @param count 要抽取几个元素
     * @return void
     * @throws
     * @Title: createRandomList
     */
    private static List createRandomList(List<HomeLink> list, int count) {
        Map map = new HashMap();
        List<HomeLink> listNew = new ArrayList<HomeLink>();
        if (list.size() <= count) {
            return list;
        } else {
            while (map.size() < count) {
                int random = (int) (Math.random() * list.size());
                if (!map.containsKey(random)) {
                    map.put(random, "");
                    listNew.add(list.get(random));
                }
            }
            return listNew;
        }
    }


    //填充默认列表
    private void addList() {







        list.add(new HomeLink("海洋世界", "assets/haiyang.jpg", "http://www.ivsky.com/tupian/haiyangshijie/"));
        list.add(new HomeLink("动物世界", "assets/dongwu.jpg", "http://www.ivsky.com/bizhi/dongwu/"));
        list.add(new HomeLink("建筑环境", "assets/jianzhu.jpg", "http://www.ivsky.com/tupian/jianzhuhuanjing/"));




        //素材
        list.add(new HomeLink("懒人图库", "assets/lanrentuku1.jpg", "lanrentuku.com"));
        list.add(new HomeLink("天堂图库", "assets/tttp.jpg", "http://www.ivsky.com/"));
        list.add(new HomeLink("素材公社", "assets/scgs1.jpg", "http://www.tooopen.com/"));




        //美食
        list.add(new HomeLink("美食世界", "assets/meishi1.jpg", "http://www.ivsky.com/tupian/meishishijie/"));
        list.add(new HomeLink("美食天下", "assets/meishi2.jpg", "http://www.meishichina.com/"));
        list.add(new HomeLink("菜谱图片", "assets/caipu.jpg", "http://www.chinacaipu.com/"));

        //军事
        list.add(new HomeLink("坦克图片", "assets/tk.jpg", "http://wot.duowan.com/"));
        list.add(new HomeLink("飞机图片", "assets/fj.jpg", "http://www.ivsky.com/bizhi/feiji/"));


        list.add(new HomeLink("交通运输", "assets/jiaotong.jpg", "http://www.ivsky.com/tupian/jiaotongyunshu/"));


        list.add(new HomeLink("体育之家", "assets/ttty.jpg", "http://sports.qq.com/photo/"));
        list.add(new HomeLink("NBA", "assets/nba.jpg", "http://china.nba.com/"));
        list.add(new HomeLink("综合体育", "assets/fhty.jpg", "http://sports.ifeng.com/"));





    }


    /**
     * 填充备用列表
     */
    private void addBakList() {
        //美女
//        bakList.add(new HomeLink("瑞丽网", "assets/1.jpg", "http://www.rayli.com.cn/"));
        bakList.add(new HomeLink("美空网", "assets/2.jpg", "moko.cc"));
//        bakList.add(new HomeLink("靓丽女人", "assets/3.jpg", "http://www.27270.com/ent/meinvtupian/"));
//        bakList.add(new HomeLink("宅男女神", "assets/zhainan.jpg", "http://www.zngirls.com/"));

        //动漫
        bakList.add(new HomeLink("动漫之家", "assets/dongmanzhijia.jpg", "donghua.dmzj.com"));
        bakList.add(new HomeLink("酷酷动漫", "assets/kukudongman.jpg", "comic.kukudm.com"));
        bakList.add(new HomeLink("动漫屋", "assets/dajian.jpg", "http://www.dm5.com/"));
        bakList.add(new HomeLink("腾讯动漫", "assets/txdm.jpg", "http://comic.qq.com/"));

        //游戏
        bakList.add(new HomeLink("7k7k", "assets/7k7k1.jpg", "7k7k.com"));
        bakList.add(new HomeLink("3dm论坛", "assets/3dm1.jpg", "http://www.3dmgame.com/"));
        bakList.add(new HomeLink("a9vg", "assets/a9vg1.jpg", "http://www.a9vg.com/"));
        bakList.add(new HomeLink("178游戏", "assets/178.jpg", "http://www.178.com/"));

        //素材
        bakList.add(new HomeLink("懒人图库", "assets/lanrentuku1.jpg", "lanrentuku.com"));
        bakList.add(new HomeLink("天堂图库", "assets/tttp.jpg", "http://www.ivsky.com/"));
        bakList.add(new HomeLink("素材公社", "assets/scgs1.jpg", "http://www.tooopen.com/"));
        bakList.add(new HomeLink("昵图网", "assets/ntw.jpg", "http://www.nipic.com/index.html"));

        //电影
        bakList.add(new HomeLink("豆瓣电影", "assets/douban.jpg", "https://movie.douban.com/"));
        bakList.add(new HomeLink("电影海报", "assets/dyhb.jpg", "http://poster.yugaopian.com/"));
        bakList.add(new HomeLink("电影明星", "assets/dymx.jpg", "http://dianying.2345.com/mingxing/"));
        bakList.add(new HomeLink("IMDB", "assets/imdb.jpg", "http://www.imdb.cn/"));

        //体育
        bakList.add(new HomeLink("腾讯体育", "assets/ttty.jpg", "http://sports.qq.com/photo/"));
        bakList.add(new HomeLink("NBA", "assets/nba.jpg", "http://china.nba.com/"));
        bakList.add(new HomeLink("综合体育", "assets/fhty.jpg", "http://sports.ifeng.com/"));
        bakList.add(new HomeLink("国际足球", "assets/gjzq.jpg", "http://sports.163.com/world/"));

        //新闻
        bakList.add(new HomeLink("人民网", "assets/rmw.jpg", "http://www.people.com.cn/"));
        bakList.add(new HomeLink("参考消息", "assets/ckxx.jpg", "http://www.cankaoxiaoxi.com/"));
        bakList.add(new HomeLink("环球网", "assets/hqw1.jpg", "http://www.huanqiu.com/"));
        bakList.add(new HomeLink("联合早报", "assets/lhzb.jpg", "http://www.zaobao.com/"));

        //军事
        bakList.add(new HomeLink("军事酷图", "assets/js.jpg", "http://mil.news.sina.com.cn/"));
        bakList.add(new HomeLink("坦克图片", "assets/tk.jpg", "http://wot.duowan.com/"));
        bakList.add(new HomeLink("飞机图片", "assets/fj.jpg", "http://www.ivsky.com/bizhi/feiji/"));

        //小说
        bakList.add(new HomeLink("起点小说", "assets/qd.jpg", "http://www.qidian.com/"));
        bakList.add(new HomeLink("纵横小说", "assets/zh.jpg", "http://www.zongheng.com/"));
        bakList.add(new HomeLink("红袖添香", "assets/hxtx.jpg", "http://www.hongxiu.com/"));
        bakList.add(new HomeLink("创世中文", "assets/cszw.jpg", "http://chuangshi.qq.com/"));

        //科学探索
        bakList.add(new HomeLink("环球科学", "assets/hqkx.jpg", "http://www.stdaily.com/"));
        bakList.add(new HomeLink("国家地理", "assets/gjdl.jpg", "http://www.nationalgeographic.com.cn/"));
        bakList.add(new HomeLink("中国地理", "assets/zgdl.jpg", "http://www.dili360.com/"));

        //手机
        bakList.add(new HomeLink("ZEALER", "assets/zealer.jpg", "http://www.zealer.com/"));
        bakList.add(new HomeLink("手机图片", "assets/shouji.jpg", "http://www.cnmo.com/"));

        //社交

        //金融

        //美食
        bakList.add(new HomeLink("美食世界", "assets/meishi1.jpg", "http://www.ivsky.com/tupian/meishishijie/"));
        bakList.add(new HomeLink("美食天下", "assets/meishi2.jpg", "http://www.meishichina.com/"));
        bakList.add(new HomeLink("菜谱图片", "assets/caipu.jpg", "http://www.chinacaipu.com/"));

        //汽车
        bakList.add(new HomeLink("汽车图片", "assets/qiche.jpg", "http://www.autohome.com.cn/beijing/"));

        //综合
        bakList.add(new HomeLink("海洋世界", "assets/haiyang.jpg", "http://www.ivsky.com/tupian/haiyangshijie/"));
        bakList.add(new HomeLink("动物世界", "assets/dongwu.jpg", "http://www.ivsky.com/bizhi/dongwu/"));
        bakList.add(new HomeLink("建筑环境", "assets/jianzhu.jpg", "http://www.ivsky.com/tupian/jianzhuhuanjing/"));
        bakList.add(new HomeLink("旅游旅行", "assets/lvyou.jpg", "http://www.ivsky.com/tupian/chengshilvyou/"));
        bakList.add(new HomeLink("节日庆祝", "assets/jieri.jpg", "http://www.ivsky.com/tupian/jieritupian/"));
        bakList.add(new HomeLink("交通运输", "assets/jiaotong.jpg", "http://www.ivsky.com/tupian/jiaotongyunshu/"));
        bakList.add(new HomeLink("艺术绘画", "assets/yishu.jpg", "http://www.ivsky.com/tupian/yishu/"));
        bakList.add(new HomeLink("植物花卉", "assets/zhiwu.jpg", "http://www.ivsky.com/tupian/zhiwuhuahui/"));
        bakList.add(new HomeLink("自然风光", "assets/zr.jpg", "http://www.ivsky.com/tupian/ziranfengguang/"));
        bakList.add(new HomeLink("物品物件", "assets/wp.jpg", "http://www.ivsky.com/tupian/wupin/"));
        bakList.add(new HomeLink("人物图片", "assets/rw.jpg", "http://www.ivsky.com/tupian/renwutupian/"));
        bakList.add(new HomeLink("广告设计", "assets/gg.jpg", "http://www.ivsky.com/tupian/guanggaosheji/"));
        bakList.add(new HomeLink("装修装饰", "assets/zx.jpg", "http://www.ivsky.com/tupian/jiaju/"));
        bakList.add(new HomeLink("卡通图片", "assets/kt.jpg", "http://www.ivsky.com/tupian/katongtupian/"));
        bakList.add(new HomeLink("运动之美", "assets/ydzm.jpg", "http://www.ivsky.com/tupian/yundongtiyu/"));
        bakList.add(new HomeLink("其他图片", "assets/qita.jpg", "http://www.ivsky.com/tupian/qita/"));

        //其他
        bakList.add(new HomeLink("钟表图片", "assets/zb.jpg", "http://www.chinawatchnet.com/index.html"));
    }


    /**
     * 放入分享首选项
     */
    private void setSP() {
        HomeLinkList homeLinkList = new HomeLinkList();
        homeLinkList.setList(list);
        String strJson = JSON.toJSONString(homeLinkList);
        SPUtil.put(this, "list", strJson);
    }

    /**
     * 从缓存中取出分享首选项
     */
    private void getSP() {
        //取出分享首选项,一进入当前页面就显示结果(也算是初始化视图的一部分)
        if (SPUtil.contains(this, "list")) {
            String str = SPUtil.getString(this, "list", "");
            HomeLinkList homeLinkList = JSON.parseObject(str, HomeLinkList.class);
            list = homeLinkList.getList();
            adapter.setList(list);
            adapter.notifyDataSetChanged();
        }
    }
}
