package com.example.admin.rxjava.ui.net;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by chentao
 * Date:2018/12/25
 */
public class HttpRetrofitClient {

    public static Retrofit INSTANCE = null;
    public static String BASE_URL = "http://gdown.baidu.com/";

    public static Retrofit getInstance() {
        if (INSTANCE == null) {
            INSTANCE = HttpRetrofitClient.createRetrofit();
        }
        return INSTANCE;
    }

    public static Retrofit createRetrofit() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.i("gdchent", "retrofit网络请求拦截信息:" + message);
            }
        });
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //初始化OkhttpClient
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        okHttpBuilder.addInterceptor(httpLoggingInterceptor);
        OkHttpClient okHttpClient = okHttpBuilder.build();

        INSTANCE = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .build();
        return INSTANCE;
    }

}
