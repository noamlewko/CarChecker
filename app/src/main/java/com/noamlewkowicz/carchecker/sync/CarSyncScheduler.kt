package com.noamlewkowicz.carchecker.sync

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules the daily background refresh of previously searched vehicles.
 */
object CarSyncScheduler {

    private const val UNIQUE_WORK_NAME = "car_sync_daily"

    /**
     * Enqueues the daily sync if it is not already scheduled. Safe to call
     * every time the app starts.
     */
    fun scheduleDaily(context: Context) {
        val dailyRequest =
            PeriodicWorkRequestBuilder<CarSyncWorker>(1, TimeUnit.DAYS)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyRequest
            )
    }
}
