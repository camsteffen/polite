package me.camsteffen.polite

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import me.camsteffen.polite.state.PoliteStateManager

class RefreshWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val politeStateManager: PoliteStateManager
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        politeStateManager.refresh()
        return Result.success()
    }
}
