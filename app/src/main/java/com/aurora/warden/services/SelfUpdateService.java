/*
 *  Aurora Wallpapers
 *  Copyright (C) 2020, Rahul Kumar Patel <auroraoss.dev@gmail.com>
 *
 *  Aurora Wallpapers is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Aurora Wallpapers is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Aurora Wallpapers.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aurora.warden.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.aurora.warden.BuildConfig;
import com.aurora.warden.Constants;
import com.aurora.warden.R;
import com.aurora.warden.retro.RetroClient;
import com.aurora.warden.retro.UpdateService;
import com.aurora.warden.utils.FileUtil;
import com.aurora.warden.utils.Log;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelfUpdateService extends Service {

    private String url;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            url = intent.getStringExtra(Constants.STRING_EXTRA);
            startForeground(1, getNotification());
            startUpdate();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void startUpdate() {
        if (url != null) {

            final String fileName = FileUtil.getFileName(url);
            final File file = new File(getCacheDir(), fileName);

            UpdateService downloadService = RetroClient.getInstance().create(UpdateService.class);
            Call<ResponseBody> call = downloadService.getUpdate(url);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            InputStream inputStream = response.body().byteStream();
                            FileUtils.copyInputStreamToFile(inputStream, file);
                            installUpdate(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(getString(R.string.update_failed));
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable throwable) {
                    Toast.makeText(SelfUpdateService.this, getString(R.string.update_failed),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            stopSelf();
        }
    }

    private Notification getNotification() {
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(this, Constants.NOTIFICATION_CHANNEL_ALERT);
        return builder
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setContentTitle("Self update")
                .setContentText("Updating in background")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .build();
    }

    private void installUpdate(File file) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file));
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
