package com.lcodecore.twinklingrefreshlayout;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.github.moduth.blockcanary.BlockCanary;
import com.github.moduth.blockcanary.BlockCanaryContext;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;

/**
 * Created by lcodecore on 2016/12/4.
 */

public class TkApplication extends Application {

    public static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();

        appContext = this;

        enabledStrictMode();

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDeath().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());

        BlockCanary.install(this, new AppBlockCanaryContext()).start();

//        TwinklingRefreshLayout.setDefaultHeader(SinaRefreshView.class.getName());
//        TwinklingRefreshLayout.setDefaultFooter(BallPulseView.class.getName());
    }

    private void enabledStrictMode() {
        if (SDK_INT >= GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
                    .detectAll() //
                    .penaltyLog() //
                    .penaltyDeath() //
                    .build());
        }
    }

    private class AppBlockCanaryContext extends BlockCanaryContext {
    }
}
