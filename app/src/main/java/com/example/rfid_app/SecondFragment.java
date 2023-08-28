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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        androidJSInterface = new AndroidJSInterface(mainActivity);
        androidJSInterface.setOnRFIDReceivedListener(this);
//        storeRFID("");
//        Log.d("Received", "good");
    }

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

    private void loadWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebChromeClient(new WebChromeClient());

        webView.addJavascriptInterface(androidJSInterface, "AndroidJSInterface");
//        Log.e("Received", "AndroidJSInterface被正确添加");
        androidJSInterface = new AndroidJSInterface(mainActivity);
        androidJSInterface.setOnRFIDReceivedListener(this);
        webView.addJavascriptInterface(androidJSInterface, "AndroidJSInterface");
        webView.setFocusableInTouchMode(true);
//        webView.setFocusable(true);
        webView.loadUrl("http://10.0.0.226/cad/terminal_rfid.php");
    }

    @Override
    public void onRFIDReceived(String rfid) {
        Log.d("Received", rfid);
        storeRFID(rfid);
//        getView().setFocusableInTouchMode(true);
//        getView().requestFocus();
//        webView.reload();
//        跳转到本fragment中
        goToAnotherFragment();
    }

    private void storeRFID(String rfid) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("rfid_key", rfid);
        editor.apply();
    }

    private void RFIDWrite(String rfid) {
        getActivity().runOnUiThread(() -> {
            UHFReaderResult<Boolean> readerResult = null;
            readerResult = UHFReader.getInstance().write("00000000", 1, 2,6,rfid, null);
            if (readerResult.getResultCode() != UHFReaderResult.ResultCode.CODE_SUCCESS) {
                Toast.makeText(mainActivity, "数据写入失败, 请再次写入", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mainActivity, "数据写入成功", Toast.LENGTH_SHORT).show();
                storeRFID("");
            }
        });
    }

    private void goToAnotherFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SecondFragment secondFragment = new SecondFragment();
        fragmentTransaction.replace(R.id.fragment_container, secondFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
