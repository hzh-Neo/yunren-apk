package com.example.yunren;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.Manifest;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.yunren.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class convert extends Activity  {
    int PERMISSION_REQUEST_CODE = 1;
    FileUpload fileUpload;
    UUID uuid=UUID.randomUUID();
    Button select_file_button,download_btn;
    String selectExt;
    String downUrl;
    Boolean isDownload=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img_convert);
        fileUpload=new FileUpload(convert.this);
        setPinner();
        select_file_button=findViewById(R.id.select_file_button);

        select_file_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{
                                    Manifest.permission.READ_MEDIA_IMAGES,},
                            1);

                } else {
                    fileUpload.selectImage(convert.this);
                }

            }
        });
        download_btn=findViewById(R.id.download);
        download_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(downUrl!=null&&!isDownload){
                    downloadImage(downUrl);
                }

            }
        });
    }
    private void downloadImage(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Download failed", e);
                runOnUiThread(() -> Toast.makeText(convert.this, "Download failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    InputStream inputStream = response.body().byteStream();
                    saveImageToDisk(inputStream, "downloaded_convert_image.jpg");
                    isDownload=true;
                    try {
                        deleteImages();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(convert.this, "Failed to download image", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void deleteImages() throws JSONException {
        OkHttpClient client = new OkHttpClient();

        // 构造 JSON 请求体
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("taskID", uuid);
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));

        // 创建请求
        Request request = new Request.Builder()
                .url(config.serverUrl+"v1/image/deleteImages")
                .post(requestBody)
                .build();

        // 发送请求
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Post request failed", e);
                runOnUiThread(() -> Toast.makeText(convert.this, "Post request failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            int code = jsonResponse.getInt("code");
                            if (code == 0) {
                                reloadCurrentActivity();
                            } else {
                                Toast.makeText(convert.this, "Failed to delete images", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(convert.this, "Response JSON Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(convert.this, "Server error", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void saveImageToDisk(InputStream inputStream, String fileName) {
        try {
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyAppImages");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, fileName);
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            runOnUiThread(() -> Toast.makeText(convert.this, "Image saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show());
        } catch (IOException e) {
            Log.e(TAG, "Failed to save image", e);
            runOnUiThread(() -> Toast.makeText(convert.this, "Failed to save image", Toast.LENGTH_SHORT).show());
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fileUpload.onActivityResult(requestCode, resultCode, data, new FileUpload.ImageSelectionCallback() {
            @Override
            public void onImageSelected(Uri imageUri) {
                // 可以在这里调用 uploadFile 方法
                new UploadFileTask().execute(imageUri);
            }
        });
    }

    private class UploadFileTask extends AsyncTask<Uri, Void, String> {
        @Override
        protected String doInBackground(Uri... uris) {
            if (uris.length == 0) {
                return null;
            }
            Uri imageUri = uris[0];
            uploadFile(imageUri);
            return "1";
        }

        @Override
        protected void onPostExecute(String result) {
            // Handle the upload result
            if (result != null) {
                Log.d("FileUpload", "Upload successful: " + result);
            } else {
                Log.d("FileUpload", "Upload failed");
            }
        }
    }
    private void uploadFile(Uri imageUri) {
        // 在此处处理上传文件逻辑
        if (imageUri != null) {
            File file = new File(FileUtils.getPath(convert.this, imageUri));
            if (!file.exists()) {
                Log.e("FileUpload", "File does not exist: " + file.getPath());
                return;
            }
            String boundary = "*****"; // 定义边界字符串
            String twoHyphens = "--"; // 两个连字符
            String lineEnd = "\r\n"; // 换行字符串
            int maxBufferSize = 1024 * 1024; // 最大缓冲区大小，这里设置为 1MB
            String result = null;

            try {
                URL url = new URL(config.serverUrl+"v1/image/convert_android?taskID=" + uuid + "&filename="+file.getName()+"&ext="+selectExt);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + "*****");

                FileInputStream fileInputStream = new FileInputStream(file);
                OutputStream outputStream = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
                writer.append("--*****").append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"").append("\r\n");
                writer.append("Content-Type: application/octet-stream").append("\r\n");
                writer.append("\r\n");
                writer.flush();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                fileInputStream.close();

                writer.append("\r\n");
                writer.append("--*****--").append("\r\n");
                writer.close();

                // 获取响应
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 文件上传成功
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String code=jsonResponse.get("code").toString();
                    if(code.equals("0")){
                        JSONArray dataArray = jsonResponse.getJSONArray("data");
                        String resImgUrl= dataArray.get(0).toString();
                        resImgUrl= resImgUrl.replace("http://localhost:3000/",config.serverUrl);
                        ImageView imageView = findViewById(R.id.imageView);
                        downUrl = resImgUrl;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 在这里调用Picasso加载图片的方法
                                Picasso.get().load(downUrl).fit().centerInside().into(imageView,new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        // 图片加载成功
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        // 图片加载失败
                                        e.printStackTrace();
                                    }
                                });
                                LinearLayout lly=findViewById(R.id.resultBox);
                                lly.setVisibility(LinearLayout.VISIBLE);
                            }
                        });

                    }else{
                        //Toast.makeText(convert.this, "转换失败", Toast.LENGTH_SHORT).show();
                    }
                    System.out.println("File uploaded successfully. Response: " + response.toString());
                } else {
                    // 文件上传失败
                    System.out.println("File upload failed with response code: " + responseCode);
                }
                connection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            Log.d("yunren", "No image selected");
        }
    }

    private void setPinner(){

        Spinner spinner = findViewById(R.id.spinner);

        // 设置 Spinner 的数据源
        String[] items = {"JPEG", "PNG", "WEBP"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // 处理 Spinner 的选择事件
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                selectExt=selectedItem;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 当没有选中任何项时
            }
        });
    }
    private void reloadCurrentActivity() {
        Intent intent = getIntent();
        finish(); // 结束当前Activity
        startActivity(intent); // 重新启动当前Activity
    }

}
