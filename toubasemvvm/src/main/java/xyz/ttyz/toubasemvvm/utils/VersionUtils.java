package xyz.ttyz.toubasemvvm.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xyz.ttyz.tou_example.ActivityManager;
import xyz.ttyz.tou_example.init.ApplicationUtils;
import xyz.ttyz.tou_example.init.TouDelegate;
import xyz.ttyz.toubasemvvm.R;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class VersionUtils {
    public static void check(final Context context, final boolean isManual) {//检查更新
        XXPermissions.with(context)
                .permission(WRITE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        //SD卡已装入
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && context.getExternalFilesDir("") != null) {
                            ApplicationUtils.touDelegate.checkVersion(new TouDelegate.VersionDelegate() {
                                @Override
                                public void installVersion(String mUpdateUrl, String updateMSG, int versionCode) {
                                    installNewVersion(context, mUpdateUrl, updateMSG, versionCode + "");
                                }
                            });
                        }
                    }

                    @Override
                    public void onDenied(final List<String> permissions, final boolean never) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (String str : permissions) {
                            stringBuilder.append("\n").append(str);
                        }
                        DialogUtils.showDialog("检查更新功能需要授权：" + stringBuilder + "\n否则将无法使用", new DialogUtils.DialogButtonModule("前往授权", new DialogUtils.DialogClickDelegate() {
                            @Override
                            public void click(DialogUtils.DialogButtonModule dialogButtonModule) {
                                if (never) {
                                    XXPermissions.startPermissionActivity(ActivityManager.getInstance(), permissions);
                                } else {
                                    check(context, isManual);
                                }
                            }
                        }), new DialogUtils.DialogButtonModule("暂不授权", new DialogUtils.DialogClickDelegate() {
                            @Override
                            public void click(DialogUtils.DialogButtonModule dialogButtonModule) {
                                ToastUtil.showToast("无权限操作，请重新尝试");
                            }
                        }));
                    }
                });


    }

    private static void installNewVersion(final Context context, String mUpdateUrl, String updateMSG, final String newVersion) {
        if (context == null) {
            return;
        }
        if (mUpdateUrl == null || mUpdateUrl.isEmpty()) {
            ToastUtil.showToast("下载地址为空");
            return;
        }

        ProgressUtil.showProgress(context, context.getString(R.string.is_install_new_version) + newVersion + "\n" + updateMSG, 0, 100);
        OkHttpClient client = new OkHttpClient();
        Request request;
        try {
            request = new Request.Builder().url(mUpdateUrl).build();
        } catch (Exception e) {
            ProgressUtil.missProgress();
            ToastUtil.showToast(e.getMessage());
            e.printStackTrace();
            return;
        }
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                ProgressUtil.missProgress();
                ToastUtil.showToast(e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[1024 * 20];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = FileUtil.getNewFile(TouUtils.scanForActivity(context).getApplication(), "landun_" + newVersion + ".apk");
                    if (file == null) {
                        ToastUtil.showToast("文件创建失败");
                        ProgressUtil.missProgress();
                    } else {
                        fos = new FileOutputStream(file);
                        long sum = 0;
                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                            sum += len;
                            int progress = (int) (sum * 1.0f / total * 100);
                            ProgressUtil.updateProgress(progress);
                        }
                        installApk(context, file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    ProgressUtil.missProgress();
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                        ProgressUtil.missProgress();
                        ToastUtil.showToast(e.getMessage());
                        e.printStackTrace();
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        ProgressUtil.missProgress();
                        ToastUtil.showToast(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void installApk(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            Uri contentUri = FileProvider.getUriForFile(TouUtils.scanForActivity(context),
                    ApplicationUtils.touDelegate.applicationId() + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
