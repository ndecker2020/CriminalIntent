package com.example.criminalintent


import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.sql.Time
import java.util.*

private const val ARG_TIME= "time"
class TimePickerFragment: DialogFragment() {
    interface Callbacks{
        fun onTimeSelected(time: Time)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val time = arguments?.getSerializable(ARG_TIME)
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timeListener = TimePickerDialog.OnTimeSetListener { _:TimePicker, hourOfDay, minuteOfDay ->
            val resultTime = Time((hourOfDay*3600000.toLong()+minuteOfDay*60000))

        targetFragment?.let { fragment -> (fragment as Callbacks).onTimeSelected(resultTime) }
        }
       return TimePickerDialog(requireContext(),timeListener,hour,minute,true)
    }
    companion object{
        fun newInstance(time: Time): TimePickerFragment {
            val args= Bundle().apply { putSerializable(ARG_TIME,time) }
            return TimePickerFragment().apply { arguments=args }
        }
    }
}