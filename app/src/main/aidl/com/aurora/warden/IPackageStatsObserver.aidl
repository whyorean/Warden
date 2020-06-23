
package com.aurora.warden;

import android.content.pm.PackageStats;

interface IPackageStatsObserver {

    void onGetStatsCompleted(in PackageStats pStats, boolean succeeded);
}
