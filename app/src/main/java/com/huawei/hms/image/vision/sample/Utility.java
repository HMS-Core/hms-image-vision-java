/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2018-2019. All rights reserved.
 */

package com.huawei.hms.image.vision.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.huawei.secure.android.common.ssl.SSFCompatiableSystemCA;
import com.huawei.secure.android.common.ssl.hostname.StrictHostnameVerifier;
import com.huawei.secure.android.common.util.IOUtil;
import com.huawei.secure.android.common.util.LogsUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

/**
 * The type Smart layout utility.
 *
 * @author huawei
 * @since 1.0.3.300
 */
public class Utility {

    private static final String TAG = "Utility";

    /**
     * Gets token.
     *
     * @param context the context
     * @param client_id the client id
     * @param client_secret the client secret
     * @return the token
     */
    public static String getToken(Context context, String client_id, String client_secret) {
        String token = "";
        try {
            String body = "grant_type=client_credentials&client_id=" + client_id + "&client_secret=" + client_secret;
            token = commonHttpsRequest(context, body, context.getResources().getString(R.string.urlToken));
            if (token != null && token.contains(" ")) {
                token = token.replaceAll(" ", "+");
                token = URLEncoder.encode(token, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            LogsUtil.e(TAG, e.getMessage());
        }
        return token;
    }

    /**
     * Common https request string.
     *
     * @param context the context
     * @param body the body
     * @param urlStr the url str
     * @return the string
     */
    public static String commonHttpsRequest(Context context, String body, String urlStr) {
        final int connectTimeoutValue = 5000;
        final int readTimeoutValue = 5000;
        try {
            URL url = new URL(urlStr);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            SSFCompatiableSystemCA sf = SSFCompatiableSystemCA.getInstance(context);
            if (sf != null && sf instanceof javax.net.ssl.SSLSocketFactory) {
                connection.setSSLSocketFactory(sf);
                connection.setHostnameVerifier(new StrictHostnameVerifier());
            }
            connection.setConnectTimeout(connectTimeoutValue);
            connection.setReadTimeout(readTimeoutValue);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Charsert", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.connect();
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                writer.write(body);
            } catch (IOException e) {
                LogsUtil.e(TAG, "IOException1: " + e.getMessage());
            } finally {
                IOUtil.closeSecure(writer);
            }
            StringBuffer sb = new StringBuffer("");
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                String lines;
                while ((lines = reader.readLine()) != null) {
                    lines = URLDecoder.decode(lines, "utf-8");
                    sb.append(lines);
                }
            } catch (IOException e) {
                LogsUtil.e(TAG, "IOException2: " + e.getMessage());
            } finally {
                IOUtil.closeSecure(reader);
                if (connection != null) {
                    connection.disconnect();
                }
            }
            String accessToken = "";
            if (sb.toString().length() > 0) {
                JSONObject jsonObject = new JSONObject(sb.toString());
                accessToken = jsonObject.getString("access_token");
            }
            return accessToken;
        } catch (MalformedURLException e) {
            LogsUtil.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            LogsUtil.e(TAG, "UnsupportedEncodingException: " + e.getMessage());
        } catch (IOException e) {
            LogsUtil.e(TAG, "IOException3: " + e.getMessage());
        } catch (CertificateException e) {
            LogsUtil.e(TAG, "CertificateException: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            LogsUtil.e(TAG, "NoSuchAlgorithmException: " + e.getMessage());
        } catch (KeyStoreException e) {
            LogsUtil.e(TAG, "KeyStoreException: " + e.getMessage());
        } catch (KeyManagementException e) {
            LogsUtil.e(TAG, "KeyManagementException: " + e.getMessage());
        } catch (JSONException e) {
            LogsUtil.e(TAG, "JSONException: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets by album.
     *
     * @param act the act
     */
    public static void getByAlbum(Activity act) {
        Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
        String[] mimeTypes = {"image/jpeg", "image/png", "image/webp"};
        getAlbum.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        getAlbum.setType("image/*");
        getAlbum.addCategory(Intent.CATEGORY_OPENABLE);
        act.startActivityForResult(getAlbum, 801);
    }

    /**
     * Gets bitmap from uri.
     *
     * @param intent the intent
     * @param context the context
     * @return the bitmap from uri
     */
    public static Bitmap getBitmapFromUri(Intent intent, Context context) {
        Uri uri = intent.getData();
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            return bitmap;
        } catch (Exception e) {
            LogsUtil.e(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * Gets bitmap from uri str.
     *
     * @param intent the intent
     * @param context the context
     * @return the bitmap from uri str
     */
    public static Bitmap getBitmapFromUriStr(Intent intent, Context context) {
        String picPath = "";
        Uri uri = null;
        if (null != intent) {
            picPath = intent.getStringExtra("uri");
        }
        if (picPath != null) {
            uri = Uri.parse(picPath);
        }

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            return bitmap;
        } catch (Exception e) {
            LogsUtil.e(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * Cut smart layout image bitmap.
     *
     * @param bitmap the bitmap
     * @return the bitmap
     */
    public static Bitmap cutSmartLayoutImage(Bitmap bitmap) {
        Bitmap cutBitmap;
        if ((float) bitmap.getHeight() / (float) bitmap.getWidth() == 16f / 9f) {
            return bitmap;
        }
        if (bitmap.getWidth() / 9 < bitmap.getHeight() / 16) {
            cutBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth() / 9 * 9, bitmap.getWidth() / 9 * 16);
        } else {
            cutBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getHeight() / 16 * 9, bitmap.getHeight() / 16 * 16);
        }
        return cutBitmap;
    }

    public static boolean copyAssetsFileToDirs(Context context, String oldPath, String newPath) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            String fileNames[] = context.getAssets().list(oldPath);
            if (fileNames.length > 0) {
                File file = new File(newPath);
                file.mkdirs();
                for (String fileName : fileNames) {
                    copyAssetsFileToDirs(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }
        return true;
    }

}
