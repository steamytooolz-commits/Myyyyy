// =========================================
// File: presentation/viewmodel/GameStateManager.kt
// =========================================
package com.example.lifesim.presentation.viewmodel

import android.content.Context
import androidx.work.*
import com.example.lifesim.data.local.AppDatabase
import java.util.concurrent.TimeUnit

class GameStateManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleAutoSave() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val saveRequest = PeriodicWorkRequestBuilder<SaveGameWorker>(5, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "auto_save",
            ExistingPeriodicWorkPolicy.REPLACE,
            saveRequest
        )
    }

    fun cancelAutoSave() {
        workManager.cancelUniqueWork("auto_save")
    }

    fun hasSavedGame(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork("auto_save")
        return try {
            val infos = java.util.concurrent.CompletableFuture.supplyAsync { workInfos.get() }.get(2, TimeUnit.SECONDS)
            infos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
        } catch (e: Exception) {
            false
        }
    }
}

class SaveGameWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            applicationContext.getDatabasePath("aeterna_life_db")?.let {
                if (it.exists()) Result.success() else Result.retry()
            } ?: Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
