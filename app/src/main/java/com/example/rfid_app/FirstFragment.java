package com.example.rfid_app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xlzn.hcpda.uhf_test.UHFReader;
import com.xlzn.hcpda.uhf_test.entity.UHFReaderResult;
import com.xlzn.hcpda.uhf_test.entity.UHFTagEntity;
import com.xlzn.hcpda.uhf_test.interfaces.OnInventoryDataListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends MyFragment {
    private MainActivity mainActivity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_read, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        监听按键键值
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == 290) {
                Log.e("Received", "first");
//                read();
                singleInventory();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().requestFocus();
    }

    @Override
    public void onKeyDownTo(int keycode) {
        super.onKeyDownTo(keycode);
        getView().requestFocus();
        if (keycode == 290) {
            Utils.play();
            Log.e("Received", "read");
//            read();
//            singleInventory();
        }
    }

    public void read() {
        UHFReaderResult<String> readerResult = null;
        readerResult = UHFReader.getInstance().read("00000000", 1, 2, 6, null);
        if (readerResult.getResultCode() != UHFReaderResult.ResultCode.CODE_SUCCESS) {
            Toast.makeText(mainActivity, "成功", Toast.LENGTH_SHORT).show();
            return;
        }
        String inputString = readerResult.getData();
        Log.e("Received", inputString);
        Toast.makeText(mainActivity, inputString, Toast.LENGTH_SHORT).show();
    }
    public void singleInventory() {
        UHFReaderResult<UHFTagEntity> uhfTagEntityUHFReaderResult = null;
        uhfTagEntityUHFReaderResult = UHFReader.getInstance().singleTagInventory();
        if (uhfTagEntityUHFReaderResult.getResultCode() != UHFReaderResult.ResultCode.CODE_SUCCESS) {
            Toast.makeText(getActivity(), "成功", Toast.LENGTH_SHORT).show();
            return;
        }
        UHFTagEntity tagEntity = uhfTagEntityUHFReaderResult.getData();
        if (tagEntity != null) {
//            String epcHex = tagEntity.getPcHex();
            String epcHex = tagEntity.getEcpHex();
            int rssi = tagEntity.getRssi();
            Log.d("TAG", "EPC: " + epcHex + ", RSSI: " + rssi + ", tagEntity: " + tagEntity);
            Toast.makeText(mainActivity, epcHex, Toast.LENGTH_SHORT).show();
        } else {
            Log.d("TAG", "No tag detected.");
        }
    }
    private static String hexString = "0123456789ABCDEFabcdef";
    public static String decode(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                bytes.length() / 2);
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
                    .indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }
}
