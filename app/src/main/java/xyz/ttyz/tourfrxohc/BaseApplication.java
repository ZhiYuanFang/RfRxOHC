package xyz.ttyz.tourfrxohc;

import android.app.Application;

import com.ihsanbal.logging.LoggingInterceptor;

import java.io.IOException;

import io.rong.imlib.RongIMClient;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import xyz.ttyz.mylibrary.RfRxOHCUtil;
import xyz.ttyz.tou_example.init.ApplicationUtils;
import xyz.ttyz.tou_example.init.TouDelegate;

/**
 * Created by tou on 2019/5/20.
 */

public class BaseApplication extends Application {
    public static ApiService apiService;

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationUtils.init(this, 750, 1334, new TouDelegate() {
            @Override
            public boolean isLogin() {
                return DefaultUtils.token != null;//当前用户是否登录
            }

            @Override
            public void gotoLoginActivity() {
                //前往登录界面
            }

            @Override
            public void checkVersion(VersionDelegate versionDelegate) {
                //请求接口判断是否需要更新
                if (true) {
                    versionDelegate.installVersion("", "更新了一些功能", BuildConfig.VERSION_CODE);
                }
            }

            @Override
            public String applicationId() {
                return BuildConfig.APPLICATION_ID;
            }

            @Override
            public void cacheMainThrowable(Throwable e) {
                //捕获主线程异常，上传bugly
            }
        });
        RfRxOHCUtil.initApiService(this, "http://api.x16.com/", getPackageName() + "-cache",
                2 * 1024 * 1024, 30, BuildConfig.BUILD_TYPE.equals("release"), BuildConfig.DEBUG, BuildConfig.VERSION_NAME,
                "huawei", "android", 0, new RfRxOHCUtil.TouRRCDelegate() {
                    @Override
                    public void addMoreForOkHttpClient(OkHttpClient.Builder httpBuilder) {
                        //动态值
                        httpBuilder.addInterceptor(new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                Request originalRequest = chain.request();
                                if (DefaultUtils.token == null) {
                                    return chain.proceed(originalRequest);
                                }
                                Request authorised = originalRequest.newBuilder()
                                        .header("Authorization", "Bearer " + DefaultUtils.token)
                                        .header("Role", "32")
                                        .build();
                                return chain.proceed(authorised);
                            }
                        });
                    }

                    @Override
                    public void addMoreForInterceptor(LoggingInterceptor.Builder logBuilder) {
                        //静态值
                        logBuilder.addHeader("Data-Type", "json")
                                .addHeader("Accept", "*/*")
                                .addHeader("Cache-Control", "no-cache")
                                .addHeader("x-app-version", "1.0");
                    }

                    @Override
                    public void initApiService(Retrofit retrofit) {
                        apiService = retrofit.create(ApiService.class);
                    }
                });
        //初始化融云
        RongIMClient.init(this, Constants.rmAppKey, false);//是否使用 VoIP 推送功能。false 代表不使用推送相关功能, SDK 里将不会携带推送相关文件

    }
}
