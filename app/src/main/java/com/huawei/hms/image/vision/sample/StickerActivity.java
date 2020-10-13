/*
 * Copyright 2020. Huawei TechnoLogies Co., Ltd. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.image.vision.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.huawei.hms.image.vision.sticker.StickerLayout;
import com.huawei.hms.image.vision.sticker.item.TextEditInfo;
import com.huawei.secure.android.common.util.LogsUtil;

import java.io.File;
import java.io.InputStream;

public class StickerActivity extends AppCompatActivity implements View.OnClickListener {
    private Context context;
    private static final String TAG = "StickerActivity";
    private Bitmap inputBm;
    private Button btn_removeSticks;
    private EditText fonts;
    private Button btn_picture;
    private StickerLayout mStickerLayout;
    private TextEditInfo textEditInfo;
    private ImageView iv;
    private TextView tv;
    String rootPath = "";
    Button mButton12;
    Button mButton13;
    Button mButton14;
    Button mButton15;
    Button mButton16;
    Button mButton17;
    Button mButton18;
    public static final int PERMISSION_REQUEST_CODE = 0x01;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker);
        try {
            rootPath = getBaseContext().getFilesDir().getPath() + "/vgmap/";
            initData();
        } catch (Exception e) {
            LogsUtil.e(TAG, "Exception: " + e.getMessage());
        }
        mStickerLayout = findViewById(R.id.sticker_container);
        iv = findViewById(R.id.imageView);

        tv = findViewById(R.id.tv);
        btn_picture = findViewById(R.id.btn_picture);
        btn_picture.setOnClickListener(this);
        mButton12 = findViewById(R.id.button12);
        mButton12.setOnClickListener(this);
        mButton13 = findViewById(R.id.button13);
        mButton13.setOnClickListener(this);
        mButton14 = findViewById(R.id.button14);
        mButton14.setOnClickListener(this);
        mButton15 = findViewById(R.id.button15);
        mButton15.setOnClickListener(this);
        mButton16 = findViewById(R.id.button16);
        mButton16.setOnClickListener(this);
        mButton17 = findViewById(R.id.button17);
        mButton17.setOnClickListener(this);
        mButton18 = findViewById(R.id.button18);
        mButton18.setOnClickListener(this);
        btn_removeSticks = findViewById(R.id.btn_removeSticks);
        btn_removeSticks.setOnClickListener(this);
        fonts = findViewById(R.id.fonts);
        fonts.setVisibility(View.GONE);
        fonts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textEditInfo.setText(s.toString());
                mStickerLayout.updateStickerText(textEditInfo);
                mStickerLayout.postInvalidate();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mStickerLayout.setStickerLayoutListener(new StickerLayout.StickerLayoutListener() {

            @Override
            public void onStickerLayoutClick() {
                fonts.setVisibility(View.GONE);
            }

            @Override
            public void onStickerTouch(int index) {

            }

            @Override
            public void onTextEdit(TextEditInfo textEditInfo) {
                StickerActivity.this.textEditInfo = textEditInfo;
                fonts.setVisibility(View.VISIBLE);
                fonts.setText(textEditInfo.getText());
            }

            @Override
            public void needDisallowInterceptTouchEvent(boolean isNeed) {

            }
        });

    }

    private void initData() {
        int permissionCheck = ContextCompat.checkSelfPermission(StickerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Utility.copyAssetsFileToDirs(getBaseContext(), "vgmap" , rootPath);
        } else {
            ActivityCompat.requestPermissions(StickerActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_OK && requestCode == 801) {
                try {
                    inputBm = Utility.getBitmapFromUri(data, this);
                    iv.setImageBitmap(inputBm);
                } catch (Exception e) {
                    LogsUtil.e(TAG, "Exception: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button12:
                addSticker(rootPath + "textArt1", "");
                break;
            case R.id.button13:
                addSticker(rootPath + "textArt2", "");
                break;
            case R.id.button14:
                addSticker(rootPath + "textArt3", "");
                break;
            case R.id.button15:
                addSticker(rootPath + "textArt4", "");
                break;
            case R.id.button16:
                addSticker(rootPath + "sticker1", "sticker_10_editable.png");
                break;
            case R.id.button17:
                addSticker(rootPath + "sticker2", "sticker_6_editable.png");
                break;
            case R.id.button18:
                addSticker(rootPath + "sticker3", "sticker_6_editable.png");
                break;
            case R.id.btn_picture:
                Utility.getByAlbum(this);
                break;
            case R.id.btn_removeSticks:
                removeStickers();
                break;
            default:
                break;
        }
    }

    private void addSticker(String rootPath, String fileName) {
        int resultCode = mStickerLayout.addSticker(rootPath, fileName);
        tv.setText("resultCode：" + resultCode);
    }

    private void removeStickers() {
        mStickerLayout.removeAllSticker();
    }

}
