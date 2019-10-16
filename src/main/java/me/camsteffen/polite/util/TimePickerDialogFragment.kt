package me.camsteffen.polite.util

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import org.threeten.bp.LocalTime

class TimePickerDialogFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    companion object {
        const val FRAGMENT_TAG = "TimePickerDialogFragment"
        private const val KEY_TIME = "time"
        private const val KEY_REQUEST_CODE = "request code"

        fun newInstance(target: Fragment, requestCode: Int, localTime: LocalTime):
            TimePickerDialogFragment {
            return TimePickerDialogFragment().apply {
                setTargetFragment(target, requestCode)
                arguments = bundleOf(
                    KEY_TIME to localTime.toSecondOfDay(),
                    KEY_REQUEST_CODE to requestCode
                )
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val time = LocalTime.ofSecondOfDay(arguments!!.getInt(KEY_TIME).toLong())
        return TimePickerDialog(
            activity, this, time.hour, time.minute, DateFormat.is24HourFormat(activity)
        )
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val requestCode = arguments!!.getInt(KEY_REQUEST_CODE)
        (targetFragment as OnTimeSetListener).onTimeSet(hourOfDay, minute, requestCode)
    }

    interface OnTimeSetListener {
        fun onTimeSet(hourOfDay: Int, minute: Int, requestCode: Int)
    }
}
