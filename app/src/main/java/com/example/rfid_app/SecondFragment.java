package com.example.rfid_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.xlzn.hcpda.uhf_test.UHFReader;
import com.xlzn.hcpda.uhf_test.entity.UHFReaderResult;

import java.util.Objects;

/**
 * 这是第二个Fragment界面: 写入标签, 包含web页面, 下拉刷新web页面, 存储rfid信息, 加载JS接口, rfid写入.
 */
public class SecondFragment extends MyFragment implements AndroidJSInterface.OnRFIDReceivedListener {
    private WebView webView = null;
    private MainActivity mainActivity;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AndroidJSInterface androidJSInterface;
    private String rfidData;
    private SharedPreferences sharedPreferences;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sharedPreferences = getActivity().getSharedPreferences("rfid_data", Context.MODE_PRIVATE);
        return inflater.inflate(R.layout.fragment_write, container, false);
    }

    /**
     * 初始化视图和变量
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        androidJSInterface = new AndroidJSInterface(mainActivity);
        androidJSInterface.setOnRFIDReceivedListener(this);
//        storeRFID("");
//        Log.d("Received", "good");
    }

    /**
     * 设置按键监听
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        webView = view.findViewById(R.id.webView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
//        loadWebView();
        loadWebView();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            webView.reload();
            swipeRefreshLayout.setRefreshing(false);
        });
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == 290) {
                webView.clearFocus();
                Log.d("Received", "second" + rfidData);
                rfidData = sharedPreferences.getString("rfid_key", "");
                if (rfidData != "") {
                    Log.d("Received", "write" + rfidData);
                    RFIDWrite(rfidData);
                } else {
                    Toast.makeText(mainActivity, "还没有获取到rfid数据", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    /**
     * 加载web页面
     * 设置setJavaScriptEnabled
     * 设置setJavaScriptCanOpenWindowsAutomatically
     * 设置WebView内核, 否则会启动设备自带浏览器打开页面
     * 设置JS接口
     * 设置js数据监听
     */
    private void loadWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebChromeClient(new WebChromeClient());

//        Log.e("Received", "AndroidJSInterface被正确添加");
        androidJSInterface = new AndroidJSInterface(mainActivity);
//        数据监听
        androidJSInterface.setOnRFIDReceivedListener(this);
//        JS接口
        webView.addJavascriptInterface(androidJSInterface, "AndroidJSInterface");
        webView.setFocusableInTouchMode(true);
//        webView.setFocusable(true);
//        加载页面
        webView.loadUrl("http://10.0.0.226/cad/terminal_rfid.php");
    }

    /**
     * 接收js接口传递的数据
     * @param rfid 这是接收到的RFID数据
     */
    @Override
    public void onRFIDReceived(String rfid) {
        Log.d("Received", rfid);
//        将接收到的数据存储起来
        storeRFID(rfid);
//        getView().setFocusableInTouchMode(true);
//        getView().requestFocus();
//        webView.reload();
//        跳转到本fragment中, 目的是使按键监听生效
        goToAnotherFragment();
    }

    /**
     * 存储rfid数据
     * @param rfid 这是接收到的RFID数据
     */
    private void storeRFID(String rfid) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("rfid_key", rfid);
        editor.apply();
    }

    /**
     * 将接收到RFID数据进行数据写入
     * @param rfid 这是接收到的数据
     */
    private void RFIDWrite(String rfid) {
//        这是创建的新线程
        getActivity().runOnUiThread(() -> {
            UHFReaderResult<Boolean> readerResult = null;
//            RFID数据写入语句, 带密码验证
            readerResult = UHFReader.getInstance().write("00000000", 1, 2,6,rfid, null);
//            获取结果码, 判断是否成功写入
            if (readerResult.getResultCode() != UHFReaderResult.ResultCode.CODE_SUCCESS) {
                Toast.makeText(mainActivity, "数据写入失败, 请再次写入", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mainActivity, "数据写入成功", Toast.LENGTH_SHORT).show();
                Utils.play();
//                如果数据写入成功则清空RFID数据
                storeRFID("");
                webView.evaluateJavascript("javascript:clearData()", null);
            }
        });
    }

    /**
     * fragment原地跳转, 重置fragment
     */
    private void goToAnotherFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SecondFragment secondFragment = new SecondFragment();
        fragmentTransaction.replace(R.id.fragment_container, secondFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
