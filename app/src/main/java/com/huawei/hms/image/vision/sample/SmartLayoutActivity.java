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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.image.vision.ImageVision;
import com.huawei.hms.image.vision.ImageVisionImpl;
import com.huawei.hms.image.vision.bean.ImageLayoutInfo;
import com.huawei.hms.image.vision.bean.ResultCode;
import com.huawei.secure.android.common.util.LogsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

/**
 * The type SmartLayout activity.
 *
 * @author huawei
 * @since 1.0.3.300
 */
public class SmartLayoutActivity extends AppCompatActivity implements View.OnClickListener {
    private Context context;

    private RelativeLayout show_image_view;

    public static final String TAG = "SmartLayoutActivity";

    private Button btn_submit;

    private EditText et_title;

    private EditText et_copyRight;

    private EditText et_description;

    private EditText et_anchor;

    private EditText et_info;

    private Button btn_image;

    ImageVisionImpl imageVisionLayoutAPI = null;

    private ImageView iv;

    private Bitmap imageBitmap;

    private TextView tv;

    private TextView tv2;

    private String token;

    public ImageView img_btn;

    private Button btn_init;

    private int initCodeState = -2;

    private int stopCodeState = -2;

    private int width = 2222;

    private int height = 3333;

    private Button btn_stop;

    private String client_id = "102216043";

    private String client_secret = "e2cd4510c9f52b17d0e23ac0e2e46b84ac0732eb59a7a235a1e0f7a8e4feb80f";

    private String string
        = "{\"projectId\":\"projectIdTest\",\"appId\":\"appIdTest\",\"authApiKey\":\"authApiKeyTest\",\"clientSecret\":\"clientSecretTest\",\"clientId\":\"clientIdTest\",\"token\":\"tokenTest\"}";

    private JSONObject authJson;

    private JSONObject requestJson = new JSONObject();

    {
        try {
            authJson = new JSONObject(string);
        } catch (JSONException e) {
            LogsUtil.e(TAG, "layout exp" + e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_layout);
        iv = findViewById(R.id.iv);
        tv = findViewById(R.id.tv);
        tv2 = findViewById(R.id.tv2);
        WindowManager manager = getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        context = this;
        btn_submit = findViewById(R.id.btn_submit);
        btn_image = findViewById(R.id.btn_image);
        btn_stop = findViewById(R.id.btn_stop);
        btn_init = findViewById(R.id.btn_init);
        img_btn = findViewById(R.id.cloud_img_btn);
        show_image_view = findViewById(R.id.cloud_show_image_view);

        et_title = findViewById(R.id.et_title);
        et_info = findViewById(R.id.et_info);
        et_description = findViewById(R.id.et_description);
        et_copyRight = findViewById(R.id.et_copyRight);
        et_anchor = findViewById(R.id.et_anchor);

        btn_submit.setOnClickListener(this);
        btn_image.setOnClickListener(this);
        btn_init.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
    }

    /**
     * Process the obtained image.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (resultCode == Activity.RESULT_OK) {
                switch (requestCode) {
                    case 801:
                        try {
                            imageBitmap = Utility.getBitmapFromUri(data, this);
                            iv.setImageBitmap(imageBitmap);
                            break;
                        } catch (Exception e) {
                            LogsUtil.e(TAG, "Exception: " + e.getMessage());
                        }
                }
            }
        }
    }

    private void layoutAdd(String title, String info, String description, String copyRight, String anchor) {
        if (imageBitmap == null) {
            tv2.setText("resultCode:" + ResultCode.FILTER_INTERFACE_REQUEST_PARAMETER_ERROR);
            return;
        }
        final Bitmap reBitmap = this.imageBitmap;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    token = Utility.getToken(context, client_id, client_secret);
                    authJson.put("token", token);
                    createRequestJson(title, description, copyRight, anchor, info);
                    final ImageLayoutInfo imageLayoutInfo = imageVisionLayoutAPI.analyzeImageLayout(requestJson,
                        reBitmap);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv.setImageBitmap(null);
                            Bitmap resizebitmap = Bitmap.createScaledBitmap(Utility.cutSmartLayoutImage(reBitmap),
                                width, height, false);
                            img_btn.setBackground(new BitmapDrawable(getResources(), resizebitmap));
                            setView(imageLayoutInfo);
                            viewSaveToImage(show_image_view);
                            tv.setText("response:" + imageLayoutInfo.getResponse().toString());
                            tv2.setText("resultCode:" + imageLayoutInfo.getResultCode());
                        }
                    });
                } catch (JSONException e) {
                    LogsUtil.e(TAG, "JSONException" + e.getMessage());
                }
            }
        }).start();

    }

    private void createRequestJson(String title, String description, String copyRight, String anchor, String info) {
        try {
            JSONObject taskJson = new JSONObject();
            taskJson.put("title", title);
            taskJson.put("imageUrl", "imageUrl");
            taskJson.put("description", description);
            taskJson.put("copyRight", copyRight);
            taskJson.put("isNeedMask", false);
            taskJson.put("anchor", anchor);
            JSONArray jsonArray = new JSONArray();
            if (info != null && info.length() > 0) {
                String[] split = info.split(",");
                for (int i = 0; i < split.length; i++) {
                    jsonArray.put(split[i]);
                }
            } else {
                jsonArray.put("info8");
            }
            taskJson.put("styleList", jsonArray);
            requestJson.put("requestId", "");
            requestJson.put("taskJson", taskJson);
            requestJson.put("authJson", authJson);
        } catch (JSONException e) {
            LogsUtil.e(TAG, e.getMessage());
        }
    }

    private void setView(ImageLayoutInfo imageLayoutInfo) {
        try {
            if (imageLayoutInfo.getViewGroup() != null) {
                if (imageLayoutInfo.getMaskView() != null) {
                    show_image_view.addView(imageLayoutInfo.getMaskView());
                }
                imageLayoutInfo.getViewGroup().setX(imageLayoutInfo.getResponse().getInt("locationX"));
                imageLayoutInfo.getViewGroup().setY(imageLayoutInfo.getResponse().getInt("locationY"));
                show_image_view.addView(imageLayoutInfo.getViewGroup());
                imageLayoutInfo.getViewGroup()
                    .measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                imageLayoutInfo.getViewGroup()
                    .layout(0, 0, imageLayoutInfo.getViewGroup().getMeasuredWidth(),
                        imageLayoutInfo.getViewGroup().getMeasuredHeight());
            }
        } catch (JSONException e) {
            LogsUtil.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                if (initCodeState != 0 | stopCodeState == 0) {
                    tv2.setText(
                        "The service has not been initialized. Please initialize the service before calling it.");
                    return;
                }
                String title = et_title.getText().toString();
                String description = et_description.getText().toString();
                String copyRight = et_copyRight.getText().toString();
                String anchor = et_anchor.getText().toString();
                String info = et_info.getText().toString();
                layoutAdd(title, info, description, copyRight, anchor);
                break;
            case R.id.btn_image:
                Utility.getByAlbum(this);
                break;
            case R.id.btn_init:
                initApi(context);
                break;
            case R.id.btn_stop:
                stopFilter();
                break;
        }
    }

    private void stopFilter() {
        if (null != imageVisionLayoutAPI) {
            int stopCode = imageVisionLayoutAPI.stop();
            tv2.setText("stopCode:" + stopCode);
            iv.setImageBitmap(null);
            imageBitmap = null;
            stopCodeState = stopCode;
            tv.setText("");
            imageVisionLayoutAPI = null;
        } else {
            tv2.setText("The service has not been enabled.");
            stopCodeState = 0;
        }
    }

    private void initApi(final Context context) {
        imageVisionLayoutAPI = ImageVision.getInstance(this);
        imageVisionLayoutAPI.setVisionCallBack(new ImageVision.VisionCallBack() {
            @Override
            public void onSuccess(int successCode) {
                int initCode = imageVisionLayoutAPI.init(context, authJson);
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

    private void viewSaveToImage(View view) {
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        view.setDrawingCacheBackgroundColor(Color.WHITE);
        Bitmap cachebmp = loadBitmapFromView(view);
        FileOutputStream fos;
        String imagePath = "";
        try {
            boolean isHasSDCard = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
            if (isHasSDCard) {
                File sdRoot = Environment.getExternalStorageDirectory();
                File file = new File(sdRoot, Calendar.getInstance().getTimeInMillis() + ".png");
                fos = new FileOutputStream(file);
                imagePath = file.getAbsolutePath();
            } else {
                throw new Exception("create failed!");
            }

            cachebmp.compress(Bitmap.CompressFormat.PNG, 90, fos);

            fos.flush();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        view.destroyDrawingCache();
    }

    private Bitmap loadBitmapFromView(View v) {
        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        int w = v.getWidth();
        int h = v.getHeight();

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        c.drawColor(Color.WHITE);
        v.layout(0, 0, w, h);
        v.draw(c);

        return bmp;
    }

}
