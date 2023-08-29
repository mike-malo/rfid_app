package com.example.rfid_app;

import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.xlzn.hcpda.uhf_test.UHFReader;
import com.xlzn.hcpda.uhf_test.entity.UHFReaderResult;
import com.xlzn.hcpda.uhf_test.entity.UHFTagEntity;
import com.xlzn.hcpda.uhf_test.interfaces.OnInventoryDataListener;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 这是第一个Fragment界面: 读取标签, 包含:
 * 下拉清空rfid信息,
 * 生成rfid二维码,
 * 显示rfid,
 * 显示读取时间.
 */

public class FirstFragment extends MyFragment {
    private MainActivity mainActivity;
    private TextView textView;
    private TextView timeText;
    private ImageView qrcodeImageView;
    private SwipeRefreshLayout readRefresh;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        设置布局文件
        return inflater.inflate(R.layout.fragment_read, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        查找并设置UI元素
        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        textView = mainActivity.findViewById(R.id.textView);
        timeText = mainActivity.findViewById(R.id.textView2);
        qrcodeImageView = mainActivity.findViewById(R.id.qrcode_image);
    }

    /**
     * 初始化视图和变量
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        下拉刷新清空rfid信息
        readRefresh = view.findViewById(R.id.readRefresh);
        readRefresh.setOnRefreshListener(() -> {
            textView.setText("");
            timeText.setText("");
            qrcodeImageView.setImageBitmap(null);
            readRefresh.setRefreshing(false);
        });

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

//    public void read() {
//        UHFReaderResult<String> readerResult = null;
//        readerResult = UHFReader.getInstance().read("00000000", 1, 2, 6, null);
//        if (readerResult.getResultCode() != UHFReaderResult.ResultCode.CODE_SUCCESS) {
//            Toast.makeText(mainActivity, "成功", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        String inputString = readerResult.getData();
//        Log.e("Received", inputString);
//        Toast.makeText(mainActivity, inputString, Toast.LENGTH_SHORT).show();
//    }

    /**
     * 单次盘点
     */
    public void singleInventory() {
        UHFReaderResult<UHFTagEntity> uhfTagEntityUHFReaderResult = null;
        uhfTagEntityUHFReaderResult = UHFReader.getInstance().singleTagInventory();
        if (uhfTagEntityUHFReaderResult.getResultCode() != UHFReaderResult.ResultCode.CODE_SUCCESS) {
            Toast.makeText(getActivity(), "没有扫描到数据", Toast.LENGTH_SHORT).show();
            return;
        }
        UHFTagEntity tagEntity = uhfTagEntityUHFReaderResult.getData();
        if (tagEntity != null) {
            Utils.play();
//            String epcHex = tagEntity.getPcHex();
            String epcHex = tagEntity.getEcpHex();
            int rssi = tagEntity.getRssi();
            Log.d("TAG", "EPC: " + epcHex + ", RSSI: " + rssi + ", tagEntity: " + tagEntity);
//            Toast.makeText(mainActivity, epcHex, Toast.LENGTH_SHORT).show();
//            获取到rfid后, 传递参数epcHex到RFID_Thread方法中
            RFID_Thread(epcHex);
        } else {
            Log.d("TAG", "No tag detected.");
        }
    }

    /**
     * 获取到RFID后的逻辑, 需要生成rfid二维码
     * @param epcHex 接收传递过来的参数
     */
    private void RFID_Thread(String epcHex) {
        textView.setText(epcHex);
        QRCodeWriter writer = new QRCodeWriter();
        try {
//            生成二维码图像
            BitMatrix bitMatrix = writer.encode(epcHex, BarcodeFormat.QR_CODE,200, 200);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK: Color.WHITE);
                }
            }
//            获取当前时间
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日-HH时mm分ss秒 E");
            String sim = dateFormat.format(date);
            getActivity().runOnUiThread(() -> {
//                新线程中与UI交互需要使用getActivity().runOnUIThread(() -> {})
                qrcodeImageView.setImageBitmap(bitmap);
                timeText.setText(sim);
            });
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

//    private static String hexString = "0123456789ABCDEFabcdef";
//    public static String decode(String bytes) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(
//                bytes.length() / 2);
//        for (int i = 0; i < bytes.length(); i += 2)
//            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
//                    .indexOf(bytes.charAt(i + 1))));
//        return new String(baos.toByteArray());
//    }
}
