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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.image.vision.crop.CropLayoutView;
import com.huawei.secure.android.common.intent.SafeIntent;

/**
 * The type Crop activity.
 *
 * @author huawei
 * @since 1.0.3.300
 */
public class CropImageActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CropImageActivity";
    private String picPath;
    private Bitmap inputBm;
    private Button cropImage;
    private Button flipH;
    private Button flipV;
    private Button rotate;
    private BitmapFactory.Options options;
    private CropLayoutView cropLayoutView;
    private RadioGroup rgCrop;
    private RadioButton rbCircular;
    private RadioButton rbRectangle;
    private Spinner spinner;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        cropLayoutView = findViewById(R.id.cropImageView);
        cropImage = findViewById(R.id.btn_crop_image);
        rotate = findViewById(R.id.btn_rotate);
        flipH = findViewById(R.id.btn_flip_horizontally);
        flipV = findViewById(R.id.btn_flip_vertically);
        cropLayoutView.setAutoZoomEnabled(true);
        cropLayoutView.setCropShape(CropLayoutView.CropShape.RECTANGLE);
        cropImage.setOnClickListener(this);
        rotate.setOnClickListener(this);
        flipH.setOnClickListener(this);
        flipV.setOnClickListener(this);
        rbCircular = findViewById(R.id.rb_circular);
        rgCrop = findViewById(R.id.rb_crop);
        rgCrop.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButton = radioGroup.findViewById(i);
                if (radioButton.equals(rbCircular)) {
                    cropLayoutView.setCropShape(CropLayoutView.CropShape.OVAL);
                } else {
                    cropLayoutView.setCropShape(CropLayoutView.CropShape.RECTANGLE);
                }
            }
        });
        spinner = (Spinner) findViewById(R.id.spinner1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String[] ratios = getResources().getStringArray(R.array.ratios);
                try {
                    int ratioX = Integer.parseInt(ratios[pos].split(":")[0]);
                    int ratioY = Integer.parseInt(ratios[pos].split(":")[1]);
                    cropLayoutView.setAspectRatio(ratioX, ratioY);
                } catch (Exception e) {
                    cropLayoutView.setFixedAspectRatio(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
        rbRectangle = findViewById(R.id.rb_rectangle);
        Intent intent = new SafeIntent(getIntent());
        inputBm = Utility.getBitmapFromUriStr(intent, this);
        cropLayoutView.setImageBitmap(inputBm);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_crop_image:
                Bitmap croppedImage = cropLayoutView.getCroppedImage();
                cropLayoutView.setImageBitmap(croppedImage);
                break;
            case R.id.btn_rotate:
                cropLayoutView.rotateClockwise();
                break;
            case R.id.btn_flip_horizontally:
                cropLayoutView.flipImageHorizontally();
                break;
            case R.id.btn_flip_vertically:
                cropLayoutView.flipImageVertically();
                break;
        }
    }

}
