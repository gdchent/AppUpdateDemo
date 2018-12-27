package com.example.admin.rxjava.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.blankj.utilcode.util.PermissionUtils;
import com.example.admin.rxjava.R;
import com.example.admin.rxjava.ui.net.ApiService;
import com.example.admin.rxjava.ui.net.HttpRetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class PublishMainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvDownload;
    private TextView tvSinglePermission = null;
    private TextView tvMuticePermission = null;

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    private static final int MY_PERMISSIONS_REQUEST_CALL_CAMERA = 2;


    //运行时候需要的所有权限
    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE
    };
    //声明一个集合 拒绝的权限
    List<String> permissionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_main);
        tvDownload = findViewById(R.id.tv_download_file);
        tvSinglePermission = this.findViewById(R.id.tv_single_permission);
        tvMuticePermission = this.findViewById(R.id.tv_multice_permission);

        tvSinglePermission.setOnClickListener(this);
        tvMuticePermission.setOnClickListener(this);
        tvDownload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_single_permission: //单个授权
                int sdk_int = Build.VERSION.SDK_INT;
                int android_m = Build.VERSION_CODES.M;
                Toast.makeText(PublishMainActivity.this, "sdk_int" + sdk_int + "," + android_m, Toast.LENGTH_LONG).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //如果当前系统是大于23的话
                    if (PermissionUtils.isGranted(Manifest.permission.CALL_PHONE)) {
                        //检查用户是否授权
                        Toast.makeText(PublishMainActivity.this, "用户已经授权", Toast.LENGTH_LONG).show();
                    } else {
                        //请求授权 系统会打开弹窗
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);

                    }
                }
                break;
            case R.id.tv_multice_permission:

                break;
            case R.id.tv_download_file:


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (PermissionUtils.isGranted(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.REQUEST_INSTALL_PACKAGES
                    )) {
                       installApk(PublishMainActivity.this);
                    } else {
                        //请求授权 系统会打开弹窗
                        ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.REQUEST_INSTALL_PACKAGES
                        }, MY_PERMISSIONS_REQUEST_CALL_PHONE);

                    }
                } else {
                    //android 小于 6.0
                }
                break;
        }
    }


    private void downLoadFile() {
        ApiService apiService = HttpRetrofitClient.getInstance().create(ApiService.class);
        Observable<ResponseBody> observable = apiService.downLoadFile("data/wisegame/55dc62995fe9ba82/jinritoutiao_448.apk");
        observable.map(new Function<ResponseBody, InputStream>() {
                    @Override
                    public InputStream apply(ResponseBody responseBody) throws Exception {

                        return responseBody.byteStream();
                    }
                })
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream) throws Exception {

                        writeToFile(inputStream);
                    }
                });
    }


    private void writeToFile(InputStream inputStream) {

        String path = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            path = Environment.getExternalStorageDirectory().getPath();
//            path = getExternalFilesDir("apk").getAbsolutePath();
        } else {
            path = getFilesDir().getAbsolutePath();
        }
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        FileOutputStream fos = null;
        try {
            File file = new File(dir, "gdchent.apk");
            if(!file.exists()){
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            byte[] b = new byte[1024];
            int len;
            while ((len = inputStream.read(b)) != -1) {
                fos.write(b, 0, len);
            }
            fos.flush();
            fos.close();
            if(!file.exists()){
                Log.i("gdchent","文件不存在");
                return ;
            }else{
                Log.i("gdchent","文件存在");
            }

        } catch (FileNotFoundException e) {
            Log.i("gdchent", "e_file:" + e.getMessage());
        } catch (IOException e) {
            Log.i("gdchent", "e:" + e.getMessage());
            e.printStackTrace();
        }
    }


    public  void installApk(Activity activity) {

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            String path = Environment.getExternalStorageDirectory().getPath();
            File dir = new File(path);
            File file = new File(dir, "gdchent.apk");
            if(!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                Log.i("gdchent","文件存在");
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                //小于7.0
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                //com.example.admin.rxjava
                //大于7.0
                // 声明需要的临时权限
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // 第二个参数，即第一步中配置的authorities
                String packageName = activity.getApplication().getPackageName();
                Log.i("gdchent","package:"+packageName);
                Log.i("gdchent","ab_path:"+file.getAbsolutePath());
                Uri contentUri = FileProvider.getUriForFile(activity, packageName + ".fileProvider", file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            }
            activity.startActivity(intent);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE) {

            installApk(PublishMainActivity.this);
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //打电话权限已经申请===
                Log.i("gdchent", "result");
            } else {
                Log.i("gdchent", "用户没有授权");
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}
