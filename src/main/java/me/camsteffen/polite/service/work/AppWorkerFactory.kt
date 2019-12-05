package me.camsteffen.polite.service.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.Lazy
import me.camsteffen.polite.service.PoliteModeManager
import javax.inject.Inject

class AppWorkerFactory
@Inject constructor(
    private val politeModeManager: Lazy<PoliteModeManager>
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            RefreshWorker::class.java.name -> {
                RefreshWorker(appContext, workerParameters, politeModeManager.get())
            }
            else -> null
        }
    }
}
