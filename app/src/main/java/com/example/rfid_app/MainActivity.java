package com.example.rfid_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.xlzn.hcpda.uhf_test.UHFReader;
import com.xlzn.hcpda.uhf_test.entity.UHFReaderResult;
import com.xlzn.hcpda.uhf_test.enums.InventoryModeForPower;
import com.xlzn.hcpda.uhf_test.module.UHFReaderSLR;
import com.example.rfid_app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Fragment> datas = new ArrayList<>();
    private List<String> titles = new ArrayList<>();
    FragmentPagerAdapter fragmentPagerAdapter;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.loadSoundPool(this);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(fragmentPagerAdapter);
        datas.add(new FirstFragment());
        datas.add(new SecondFragment());
        titles.add("first");
        titles.add("second");
        fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), datas,titles);
        viewPager.setAdapter(fragmentPagerAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.releaseSoundPool();
        close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectUHFReader();
    }

    @SuppressLint("StaticFieldLeak")
    private void connectUHFReader() {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.start_connect));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        new AsyncTask<String, Integer, UHFReaderResult>() {

            @Override
            protected UHFReaderResult doInBackground(String... strings) {
                return UHFReader.getInstance().connect(MainActivity.this);
            }

            @Override
            protected void onPostExecute(UHFReaderResult result) {
                super.onPostExecute(result);
                progressDialog.dismiss();
                if (result.getResultCode() == UHFReaderResult.ResultCode.CODE_SUCCESS) {
                    if (UHFReaderSLR.getInstance().is5300) {
                        UHFReader.getInstance().setInventoryModeForPower(InventoryModeForPower.POWER_SAVING_MODE);
                    }
                    UHFReader.getInstance().setPower(HcPreferences.getInstance().getInt(getApplicationContext(), "pda", "power"));
                    Toast.makeText(MainActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.fail, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

//    原demo未知代码
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0 && keyCode == 293 || keyCode == 290 || keyCode == 287|| keyCode == 286) {
            MyFragment myFragment = (MyFragment) fragmentPagerAdapter.getItem(viewPager.getCurrentItem());
            myFragment.onKeyDownTo(keyCode);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void close() {
        UHFReader.getInstance().disConnect();
    }
}
