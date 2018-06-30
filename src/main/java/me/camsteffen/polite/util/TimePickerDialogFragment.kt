package me.camsteffen.polite.util

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.text.format.DateFormat
import android.widget.TimePicker

class TimePickerDialogFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    companion object {
        const val FRAGMENT_TAG = "TimePickerDialogFragment"
        const val KEY_TIME = "time"
        const val KEY_REQUEST_CODE = "request code"

        fun newInstance(target: Fragment, requestCode: Int, time: TimeOfDay): TimePickerDialogFragment {
            val fragment = TimePickerDialogFragment()
            fragment.setTargetFragment(target, requestCode)
            val bundle = Bundle()
            bundle.putInt(KEY_TIME, time.toInt())
            bundle.putInt(KEY_REQUEST_CODE, requestCode)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val time = TimeOfDay(arguments!!.getInt(KEY_TIME))
        return TimePickerDialog(activity, this, time.hour, time.minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val requestCode = arguments!!.getInt(KEY_REQUEST_CODE)
        (targetFragment as OnTimeSetListener).onTimeSet(hourOfDay, minute, requestCode)
    }

    interface OnTimeSetListener {
        fun onTimeSet(hourOfDay: Int, minute: Int, requestCode: Int)
    }
}
