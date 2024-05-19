package com.example.yunren;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.provider.MediaStore;
public class FileUpload {
    private static FileUpload instance;

    private static final int PICK_IMAGE_REQUEST = 1;
    public Uri imageUri;
    private Context context;

    // 私有构造函数，防止外部直接实例化
    public FileUpload(Context context) {
        this.context = context.getApplicationContext();
    }

    // 获取单例实例的方法
    public static synchronized FileUpload getInstance(Context context) {
        if (instance == null) {
            instance = new FileUpload(context);
        }
        return instance;
    }
    public interface ImageSelectionCallback {
        void onImageSelected(Uri imageUri);
    }
    public void selectImage(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data,ImageSelectionCallback callback) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            // 调用回调并传递选中的图片 URI
            if (callback != null) {
                callback.onImageSelected(imageUri);
            }
        }
    }

    // 其他方法和变量
}
