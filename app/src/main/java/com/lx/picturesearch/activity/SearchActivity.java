package com.lx.picturesearch.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.lx.picturesearch.MyListView;
import com.lx.picturesearch.R;
import com.lx.picturesearch.dao.SearchDao;
import com.lx.picturesearch.util.Utils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class SearchActivity extends Activity implements View.OnClickListener, TextView.OnEditorActionListener {

    //后退
    private ImageView iv_back;
    //输入框
    private EditText et_search;
    String inputUrl = "";//用户输入的网址
    //搜索
    private TextView tv_search;


    private MyListView listView;
    private TextView tv_clear;

    private BaseAdapter adapter;
    private SearchDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dao = new SearchDao(this);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
        setContentView(R.layout.activity_search);
        //设置全局上下文
        Utils.setContext(this);
        // 初始化控件
        initView();

        initListener();


        et_search.setFocusable(true);
        et_search.setFocusableInTouchMode(true);
        et_search.requestFocus();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() { //让软键盘延时弹出，以更好的加载Activity

            public void run() {
                InputMethodManager inputManager =
                        (InputMethodManager) et_search.getContext().
                                getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.showSoftInput(et_search, 0);
            }

        }, 200);



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                String name = textView.getText().toString();
                et_search.setText(name);

//                Toast.makeText(SearchActivity.this, name, Toast.LENGTH_SHORT).show();
            }
        });

        // 第一次进入查询所有的历史记录
        queryData("");

    }


    private void initView() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        et_search = (EditText) findViewById(R.id.et_search);
        tv_search = (TextView) findViewById(R.id.tv_search);

        // 动态调整EditText左边的搜索按钮的大小
        Drawable drawable = getResources().getDrawable(R.drawable.search_icon);
        drawable.setBounds(0, 0, 48, 48);// 第一0是距左边距离，第二0是距上边距离，48分别是长宽
        et_search.setCompoundDrawables(drawable, null, null, null);// 只放左边


        listView = (MyListView) findViewById(R.id.listView);
        tv_clear = (TextView) findViewById(R.id.tv_clear);


    }

    private void initListener() {
        iv_back.setOnClickListener(this);
        tv_search.setOnClickListener(this);

        et_search.addTextChangedListener(mTextWatcher);
        et_search.setOnEditorActionListener(this);

        // 清空搜索历史
        tv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dao.deleteData();// 清空数据on
                queryData("");// 全查询
            }
        });





    }

    /**
     * 模糊查询数据
     */
    private void queryData(String tempName) {
        Cursor cursor = dao.queryData(tempName);
        adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                cursor,
                new String[]{"name"},
                new int[]{android.R.id.text1},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        // 设置适配器
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        //后退
        if (v.getId() == R.id.iv_back) {
            onBackPressed();
        }
        //搜索
        if (v.getId() == R.id.tv_search) {
            doSearchUrl(inputUrl);
        }
    }

    /**
     * //判断url网址是否合法,如果合法,就跳转到MainActivity进行抓取,否则提示
     *
     * @param url 要判断的网址
     */
    private void doSearchUrl(String url) {

//        Log.i("lixiang","###"+url+"###");

        //判断网址是否合法
        Pattern pattern = Pattern
                .compile("(http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*");
        boolean isUrl = pattern.matcher(url).matches();

        if (isUrl) {

            //关闭软键盘
            closeKeyboard();

            //携带参数跳转到MainActivity
//            Intent intent = new Intent(this, ManagerActivity.class);
//            intent.putExtra("url", url);//携带用户输入的url跳转到ManagerActivity
//            startActivity(intent);

            //添加历史记录
            String kw = et_search.getText().toString().trim();
//            Log.i("kw",kw);
            boolean isKey = dao.hasData(kw);
            if (!isKey) {
                dao.insertData(kw);// 添加这个key到数据库
                queryData("");// 全查询
            }

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("url", url);
            startActivity(intent);


        } else {
            Utils.showToast("您输入的网址不合法,请重新输入!");

        }
    }

    //关闭软键盘
    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 监听搜索框输入变化
     */
    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            inputUrl = "http://"+s;
            String tempName = et_search.getText().toString();
            // 根据tempName去模糊查询数据库中有没有数据
            queryData(tempName);


        }
    };

    //监听软键盘的搜索按钮
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        //判断网址是否合法,如果合法,就跳转到进行抓取,否则提示
        doSearchUrl(inputUrl);
        return true;
    }


    @Override
    public void onBackPressed() {
        et_search.setText("");
        closeKeyboard();
        super.onBackPressed();
    }




}
