/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.image.vision.ImageVision;
import com.huawei.hms.image.vision.ImageVisionImpl;
import com.huawei.hms.image.vision.bean.ImageVisionResult;
import com.huawei.secure.android.common.util.LogsUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The type Filter activity.
 *
 * @author huawei
 * @since 1.0.3.300
 */
public class FilterActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "FilterActivity";
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    private Button btn_submit;
    private String picPath;
    private Button btn_init;
    private Button btn_picture;
    private Button btn_stop;
    private EditText btn_filter;
    private EditText btn_compress;
    private EditText btn_intensity;
    private ImageView iv;
    private TextView tv;
    private TextView tv2;
    private Context context;
    private Bitmap bitmap;
    private int initCodeState = -2;
    private int stopCodeState = -2;
    ImageVisionImpl imageVisionFilterAPI;
    String string
            = "{\"projectId\":\"projectIdTest\",\"appId\":\"appIdTest\",\"authApiKey\":\"authApiKeyTest\",\"clientSecret\":\"clientSecretTest\",\"clientId\":\"clientIdTest\",\"token\":\"tokenTest\"}";
    private JSONObject authJson;

    {
        try {
            authJson = new JSONObject(string);
        } catch (JSONException e) {
            LogsUtil.e(TAG, "filter exp" + e.getMessage());
        }
    }

    /**
     * The Image vision api.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        iv = findViewById(R.id.iv);
        tv = findViewById(R.id.tv);
        tv2 = findViewById(R.id.tv2);
        btn_filter = findViewById(R.id.btn_filter);
        btn_init = findViewById(R.id.btn_init);
        btn_picture = findViewById(R.id.btn_picture);
        btn_intensity = findViewById(R.id.btn_intensity);
        btn_compress = findViewById(R.id.btn_compress);
        btn_submit = findViewById(R.id.btn_submit);
        btn_stop = findViewById(R.id.btn_stop);

        btn_submit.setOnClickListener(this);
        btn_init.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        btn_picture.setOnClickListener(this);

    }

    /**
     * Process the obtained image.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != data) {
            if (resultCode == Activity.RESULT_OK) {
                switch (requestCode) {
                    case 801:
                        try {
                            bitmap = Utility.getBitmapFromUri(data, this);
                            iv.setImageBitmap(bitmap);
                            break;
                        } catch (Exception e) {
                            LogsUtil.e(TAG, "Exception: " + e.getMessage());
                        }

                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                String filterType = btn_filter.getText().toString();
                String intensity = btn_intensity.getText().toString();
                String compress = btn_compress.getText().toString();
                if (initCodeState != 0 | stopCodeState == 0) {
                    tv2.setText(
                            "The service has not been initialized. Please initialize the service before calling it.");
                    return;
                }
                startFilter(filterType, intensity, compress, authJson);
                break;
            case R.id.btn_init:
                initFilter(context);
                break;
            case R.id.btn_picture:
                Utility.getByAlbum(this);
                break;
            case R.id.btn_stop:
                stopFilter();
                break;
        }

    }

    private void stopFilter() {
        if (null != imageVisionFilterAPI) {
            int stopCode = imageVisionFilterAPI.stop();
            tv2.setText("stopCode:" + stopCode);
            iv.setImageBitmap(null);
            bitmap = null;
            tv.setText("");
            imageVisionFilterAPI = null;
            stopCodeState = stopCode;
        } else {
            tv2.setText("The service has not been enabled.");
            stopCodeState = 0;
        }
    }

    private void initFilter(final Context context) {
        imageVisionFilterAPI = ImageVision.getInstance(this);
        imageVisionFilterAPI.setVisionCallBack(new ImageVision.VisionCallBack() {
            @Override
            public void onSuccess(int successCode) {
                int initCode = imageVisionFilterAPI.init(context, authJson);
                initCodeState = initCode;
                stopCodeState = -2;
                tv2.setText("initCode = " + initCode);
            }

            @Override
            public void onFailure(int errorCode) {
                tv2.setText("Failed");
                LogsUtil.e(TAG, "ImageVisionAPI fail, errorCode: " + errorCode);
            }
        });
    }

    private void startFilter(final String filterType, final String intensity, final String compress,
                             final JSONObject authJson) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject = new JSONObject();
                JSONObject taskJson = new JSONObject();
                try {
                    taskJson.put("intensity", intensity);
                    taskJson.put("filterType", filterType);
                    taskJson.put("compressRate", compress);
                    jsonObject.put("requestId", "1");
                    jsonObject.put("taskJson", taskJson);
                    jsonObject.put("authJson", authJson);
                    final ImageVisionResult visionResult = imageVisionFilterAPI.getColorFilter(jsonObject,
                            bitmap);
                    iv.post(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap image = visionResult.getImage();
                            iv.setImageBitmap(image);
                            tv.setText(
                                    visionResult.getResponse().toString() + "resultCode:" + visionResult
                                            .getResultCode());
                        }
                    });
                } catch (JSONException e) {
                    LogsUtil.e(TAG, "JSONException: " + e.getMessage());
                }
            }
        };
        executorService.execute(runnable);
    }


}
