package me.camsteffen.polite.service.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import me.camsteffen.polite.service.PoliteModeManager
import timber.log.Timber

class RefreshWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val politeModeManager: PoliteModeManager
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Timber.d("Running RefreshWorker")
        politeModeManager.refresh()
        return Result.success()
    }
}
