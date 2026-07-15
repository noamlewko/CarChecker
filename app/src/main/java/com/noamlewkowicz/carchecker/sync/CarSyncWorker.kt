package com.noamlewkowicz.carchecker.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.noamlewkowicz.carchecker.R
import com.noamlewkowicz.carchecker.data.repository.CarRepository
import kotlinx.coroutines.CancellationException

/**
 * Refreshes every previously searched vehicle from the network once a day.
 *
 * Runs as a foreground service, promoted via [setForeground], so the
 * operating system does not kill the refresh partway through.
 */
class CarSyncWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())

        return try {
            val repository =
                CarRepository.createOfflineFirst(applicationContext)

            repository.refreshCachedVehicles()

            Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            // Network hiccups are common for a daily background job;
            // WorkManager will retry with backoff.
            Result.retry()
        }
    }

    /**
     * Builds the notification required to run as a foreground service.
     */
    private fun createForegroundInfo(): ForegroundInfo {
        val notificationManager =
            applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    applicationContext.getString(R.string.sync_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        val notification =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(
                    applicationContext.getString(R.string.sync_notification_title)
                )
                .setContentText(
                    applicationContext.getString(R.string.sync_notification_text)
                )
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private companion object {
        const val CHANNEL_ID = "car_sync_channel"
        const val NOTIFICATION_ID = 1
    }
}
