package com.example.rfid_app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class AndroidJSInterface {
    private Context mContext;
    private OnRFIDReceivedListener mListener;
    private String rfidData;
    public AndroidJSInterface(Context context) {
        mContext = context;
    }

    public void setOnRFIDReceivedListener(OnRFIDReceivedListener listener) {
        mListener = listener;
    }

    @JavascriptInterface
    public void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void setRFID(String rfid) {
        Toast.makeText(mContext, rfid, Toast.LENGTH_SHORT).show();
        rfidData = rfid;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onRFIDReceived(rfidData);
                    Log.e("Received", rfidData);
                }
            }
        });
    }

    @JavascriptInterface
    public String getDataFromAndroid() {
        return "Data from Android？？？？？？？？";
    }

    public interface OnRFIDReceivedListener {
        void onRFIDReceived(String rfid);
    }
}
