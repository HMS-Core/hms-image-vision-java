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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.image.vision.ImageVision;
import com.huawei.hms.image.vision.ImageVisionImpl;
import com.huawei.hms.image.vision.bean.ImageVisionResult;
import com.huawei.hms.image.vision.bean.ResultCode;
import com.huawei.secure.android.common.util.LogsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The type ThemeTag activity.
 *
 * @author huawei
 * @since 1.0.3.300
 */
public class ThemeTagActivity extends AppCompatActivity implements View.OnClickListener {
    private Context context;
    public static final String TAG = "ThemeTagActivity";
    private Button btn_result;
    private Button btn_image;
    private Bitmap imageBitmap;
    private String token;
    ImageVisionImpl imageVisionTagAPI = null;
    private ImageView iv;
    private TextView tv;
    private TextView tv2;
    private RadioButton rbEn;
    private RadioButton rbCn;
    private int initCodeState = -2;
    private int stopCodeState = -2;
    private RadioGroup rgTag;
    private Button btn_init;
    private Button btn_stop;
    private String client_id = "102216043";
    private String client_secret
            = "e2cd4510c9f52b17d0e23ac0e2e46b84ac0732eb59a7a235a1e0f7a8e4feb80f";
    private String string
            = "{\"projectId\":\"projectIdTest\",\"appId\":\"appIdTest\",\"authApiKey\":\"authApiKeyTest\",\"clientSecret\":\"clientSecretTest\",\"clientId\":\"clientIdTest\",\"token\":\"tokenTest\"}";
    private JSONObject authJson;
    private String language = "cn";

    {
        try {
            authJson = new JSONObject(string);
        } catch (JSONException e) {
            LogsUtil.e(TAG, "tag exp" + e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_tag);
        iv = findViewById(R.id.iv);
        tv = findViewById(R.id.tv);
        context = this;
        tv2 = findViewById(R.id.tv2);
        btn_result = findViewById(R.id.btn_result);
        btn_image = findViewById(R.id.btn_image);
        rbEn = findViewById(R.id.rb_en);
        rbCn = findViewById(R.id.rb_cn);
        btn_stop = findViewById(R.id.btn_stop);
        btn_init = findViewById(R.id.btn_init);
        rgTag = findViewById(R.id.rg_tag);
        rgTag.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButton = radioGroup.findViewById(i);
                language = radioButton.getText().toString();
                Toast.makeText(ThemeTagActivity.this, radioButton.getText(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
        btn_init.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        btn_result.setOnClickListener(this);
        btn_image.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == 801) {
                    try {
                        imageBitmap = Utility.getBitmapFromUri(data, this);
                        iv.setImageBitmap(imageBitmap);
                    } catch (Exception e) {
                        LogsUtil.e(TAG, "Exception: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void stopFilter() {
        if (null != imageVisionTagAPI) {
            int stopCode = imageVisionTagAPI.stop();
            tv2.setText("stopCode:" + stopCode);
            iv.setImageBitmap(null);
            tv.setText("");
            imageBitmap = null;
            stopCodeState = stopCode;
            imageVisionTagAPI = null;
        } else {
            tv2.setText("The service has not been enabled.");
            stopCodeState = 0;
        }
    }

    private void initTag(final Context context) {
        imageVisionTagAPI = ImageVision.getInstance(this);
        imageVisionTagAPI.setVisionCallBack(new ImageVision.VisionCallBack() {
            @Override
            public void onSuccess(int successCode) {
                int initCode = imageVisionTagAPI.init(context, authJson);
                tv2.setText("initCode:" + initCode);
                initCodeState = initCode;
                stopCodeState = -2;
            }

            @Override
            public void onFailure(int errorCode) {
                LogsUtil.e(TAG, "getImageVisionAPI failure, errorCode = " + errorCode);
                tv2.setText("initFailed");
            }
        });
    }

    private void getThemeTag(String language) {
        JSONObject requestJson = new JSONObject();
        JSONObject taskJson = new JSONObject();
        final Bitmap tagBitmap = this.imageBitmap;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    token = Utility.getToken(context, client_id, client_secret);
                    authJson.put("token", token);
                    taskJson.put("language", language);
                    requestJson.put("requestId", "requestId");
                    requestJson.put("taskJson", taskJson);
                    requestJson.put("authJson", authJson);
                    final ImageVisionResult result = imageVisionTagAPI.analyzeImageThemeTag(
                            requestJson, tagBitmap);
                    if (taskJson.has("needObjectList") && taskJson.getBoolean("needObjectList")) {
                        if (result.getResultCode() == ResultCode.SUCCESS && result.getResponse().getJSONArray("objectList").length() > 0) {
                            JSONArray array = result.getResponse().getJSONArray("objectList");
                            List<Rect> rectList = new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                JSONObject borderObject = object.getJSONObject("box");
                                rectList.add(getRect(borderObject, tagBitmap));
                            }
                            drawRectangles(tagBitmap, rectList);
                        } else {
                            tv2.setText("resultCode:" + result.getResultCode());
                        }
                    } else {
                        iv.post(new Runnable() {
                            @Override
                            public void run() {
                                tv.setText("response:" + result.getResponse().toString());
                                tv2.setText("resultCode:" + result.getResultCode());
                            }
                        });
                    }

                } catch (JSONException e) {
                    LogsUtil.e(TAG, "JSONException" + e.getMessage());
                }
            }
        }).start();

    }

    private Rect getRect(JSONObject object, Bitmap bitmap) {
        double width = 0;
        double height = 0;
        double center_x = 0;
        double center_y = 0;
        try {
            width = object.getDouble("width");
            height = object.getDouble("height");
            center_x = object.getDouble("center_x");
            center_y = object.getDouble("center_y");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int left = (int) (center_x * bitmap.getWidth() - 0.5 * width * bitmap.getWidth());
        int top = (int) (center_y * bitmap.getHeight() - 0.5 * height * bitmap.getHeight());
        int right = (int) (center_x * bitmap.getWidth() + 0.5 * width * bitmap.getWidth());
        int bottom = (int) (center_y * bitmap.getHeight() + 0.5 * height * bitmap.getHeight());
        return new Rect(left, top, right, bottom);
    }

    private void drawRectangles(Bitmap imageBitmap, List<Rect> rectList) {
        Bitmap mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        for (int i = 0; i < rectList.size(); i++) {
            Rect rect = rectList.get(i);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);//不填充
            paint.setStrokeWidth(5); //线的宽度
            canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, paint);
            LogsUtil.e(TAG, "left: " + rect.left + "; right: " + rect.right + "; top: " + rect.top + "; bottom: " + rect.bottom);
        }
        iv.post(new Runnable() {
            @Override
            public void run() {
                iv.setImageBitmap(mutableBitmap);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_result:
                if (initCodeState != 0 | stopCodeState == 0) {
                    tv2.setText(
                            "The service has not been initialized. Please initialize the service before calling it.");
                    return;
                }
                getThemeTag(language);
                break;
            case R.id.btn_image:
                Utility.getByAlbum(this);
                break;
            case R.id.btn_init:
                initTag(context);
                break;
            case R.id.btn_stop:
                stopFilter();
                break;
        }
    }

}
