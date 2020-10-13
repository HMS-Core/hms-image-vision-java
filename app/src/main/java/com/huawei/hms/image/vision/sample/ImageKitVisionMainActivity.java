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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;

import com.huawei.secure.android.common.intent.SafeIntent;
import com.huawei.secure.android.common.util.LogsUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The  ImageKitVision activity.
 *
 * @author huawei
 * @since 1.0.3.300
 */
public class ImageKitVisionMainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private Button btn_filter;
    private Button btn_themetag;
    private Button btn_smartlayout;
    private Button btn_sticker;
    private Button btn_crop;
    private Context context;
    private static final int GET_BY_CROP = 804;
    private static final int GET_BY_ALBUM1 = 801;
    private static final int GET_BY_CAMERA = 805;
    List<String> mPermissionList = new ArrayList<>();
    String[] permissions = new String[] {Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private final int mRequestCode = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_filter = findViewById(R.id.btn_filter);
        btn_themetag = findViewById(R.id.btn_themetag);
        btn_smartlayout = findViewById(R.id.btn_smartlayout);
        btn_sticker = findViewById(R.id.btn_sticker);
        btn_crop = findViewById(R.id.btn_crop);

        btn_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ImageKitVisionMainActivity.this, FilterActivity.class);
                startActivity(intent1);
            }
        });

        btn_smartlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ImageKitVisionMainActivity.this,
                    SmartLayoutActivity.class);
                startActivity(intent1);
            }
        });

        btn_themetag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ImageKitVisionMainActivity.this,
                    ThemeTagActivity.class);
                startActivity(intent1);
            }
        });

        btn_sticker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ImageKitVisionMainActivity.this, StickerActivity.class);
                startActivity(intent1);
            }
        });

        btn_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getByAlbum(ImageKitVisionMainActivity.this, GET_BY_CROP);
            }
        });
        if (Build.VERSION.SDK_INT >= 23) {
            initPermission();
        }
    }

    @SuppressLint("WrongConstant")
    private void initPermission() {
        // Clear the permissions that fail the verification.
        mPermissionList.clear();
        //Check whether the required permissions are granted.
        for (int i = 0; i < permissions.length; i++) {
            if (PermissionChecker.checkSelfPermission(this, permissions[i])
                != PackageManager.PERMISSION_GRANTED) {
                // Add permissions that have not been granted.
                mPermissionList.add(permissions[i]);
            }
        }
        //Apply for permissions.
        if (mPermissionList.size() > 0) {//The permission has not been granted. Please apply for the permission.
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        }
    }

    /**
     * Obtain pictures from the album
     */
    public static void getByAlbum(Activity act, int type) {
        Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
        getAlbum.setType("image/*");
        act.startActivityForResult(getAlbum, type);
    }

    /**
     * Process the obtained image.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != data) {
            super.onActivityResult(requestCode, resultCode, data);
            try {
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri;
                    switch (requestCode) {
                        case GET_BY_CROP:
                            Intent intent = new SafeIntent(data);
                            uri = intent.getData();
                            Intent intent4 = new Intent(ImageKitVisionMainActivity.this,
                                CropImageActivity.class);
                            intent4.putExtra("uri", uri.toString());
                            startActivity(intent4);
                            break;
                    }
                }
            } catch (Exception e) {
                LogsUtil.i("onActivityResult", "Exception");
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
        int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri photoURI = FileProvider.getUriForFile(ImageKitVisionMainActivity.this,
                        ImageKitVisionMainActivity.this.getApplicationContext().getPackageName()
                            + ".fileprovider", new File(context.getFilesDir(), "temp.jpg"));
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(cameraIntent, GET_BY_CAMERA);

                } else {
                    Toast.makeText(ImageKitVisionMainActivity.this, "No permission.", Toast.LENGTH_LONG)
                        .show();
                }
                return;
            }
        }
    }

}
