package com.example.admin.rxjava.ui.net;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by chentao
 * Date:2018/12/25
 */
public interface ApiService {

    @Streaming
    @GET
    Observable<ResponseBody> downLoadFile(@Url String fileUrl);
}
