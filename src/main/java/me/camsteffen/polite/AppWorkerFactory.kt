package me.camsteffen.polite

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.Lazy
import me.camsteffen.polite.state.PoliteStateManager
import javax.inject.Inject

class AppWorkerFactory
@Inject constructor(
    private val politeStateManager: Lazy<PoliteStateManager>
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            RefreshWorker::class.java.name -> {
                RefreshWorker(appContext, workerParameters, politeStateManager.get())
            }
            else -> null
        }
    }
}
