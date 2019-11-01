package me.camsteffen.polite.service.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import me.camsteffen.polite.service.PoliteStateManager
import timber.log.Timber

class RefreshWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val politeStateManager: PoliteStateManager
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Timber.d("Running RefreshWorker")
        politeStateManager.refresh()
        return Result.success()
    }
}
