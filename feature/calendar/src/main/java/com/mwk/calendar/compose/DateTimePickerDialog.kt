package com.mwk.calendar.compose

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mwk.calendar.R
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    onDismissRequest: () -> Unit,
    onDateTimeSelected: (Long) -> Unit,
    is24Hour: Boolean,
    modifier: Modifier = Modifier,
    initialDateTimeMillis: Long? = null,
    minDateTimeMillis: Long? = null,
) {
    val effectiveMinDateTimeMillis = remember(minDateTimeMillis) {
        minDateTimeMillis?.coerceAtLeast(System.currentTimeMillis())
    }
    val initialCalendar = remember(initialDateTimeMillis) {
        Calendar.getInstance().apply {
            timeInMillis = initialDateTimeMillis ?: System.currentTimeMillis()
        }
    }
    val minSelectableDateMillis = remember(effectiveMinDateTimeMillis) {
        effectiveMinDateTimeMillis?.let {
            Calendar.getInstance().apply {
                timeInMillis = it
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateTimeMillis ?: effectiveMinDateTimeMillis ?: System.currentTimeMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return minSelectableDateMillis == null || utcTimeMillis >= minSelectableDateMillis
            }
        }
    )
    val timePickerState = rememberTimePickerState(
        initialHour = initialCalendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialCalendar.get(Calendar.MINUTE),
        is24Hour = is24Hour
    )

    var showTimePickerDialog by rememberSaveable { mutableStateOf(false) }
    var selectedDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    val isSelectedTimeValid = remember(
        selectedDateMillis,
        timePickerState.hour,
        timePickerState.minute,
        effectiveMinDateTimeMillis
    ) {
        val dateMillis = selectedDateMillis
        if (dateMillis == null || effectiveMinDateTimeMillis == null) {
            true
        } else {
            Calendar.getInstance().apply {
                timeInMillis = dateMillis
                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                set(Calendar.MINUTE, timePickerState.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis >= effectiveMinDateTimeMillis
        }
    }

    if (!showTimePickerDialog) {
        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        if (selectedDateMillis == null) {
                            onDismissRequest()
                        } else {
                            showTimePickerDialog = true
                        }
                    }
                ) {
                    Text(stringResource(R.string.entry_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.entry_dialog_cancel))
                }
            },
            modifier = modifier
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePickerDialog) {
        TimePickerDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    enabled = isSelectedTimeValid,
                    onClick = {
                        val dateMillis = selectedDateMillis ?: return@TextButton
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = dateMillis
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onDateTimeSelected(calendar.timeInMillis)
                    }
                ) {
                    Text(stringResource(R.string.entry_dialog_confirm))
                }
            },
            title = { Text(stringResource(R.string.entry_select_time_title)) },
            modifier = modifier
        ) {
            TimePicker(state = timePickerState)
        }
    }
}
