package me.camsteffen.polite

import android.os.Build
import android.provider.CalendarContract
import androidx.annotation.RequiresApi
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppWorkManager
@Inject constructor(
    private val workManager: WorkManager
) {
    @RequiresApi(Build.VERSION_CODES.N)
    fun cancelRefreshOnCalendarChange() {
        workManager.cancelUniqueWork(CALENDAR_CHANGE_WORK_NAME)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun refreshOnCalendarChange() {
        workManager.enqueueUniqueWork(
            CALENDAR_CHANGE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<RefreshWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .addContentUriTrigger(CalendarContract.Events.CONTENT_URI, true)
                        .setTriggerContentMaxDelay(0, TimeUnit.SECONDS)
                        .build()
                )
                .build()
        )
    }
}

private const val CALENDAR_CHANGE_WORK_NAME: String = "calendar-change"
