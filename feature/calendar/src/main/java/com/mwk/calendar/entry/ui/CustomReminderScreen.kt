package com.mwk.calendar.entry.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.mwk.calendar.R
import com.mwk.calendar.compose.DropDownList
import com.mwk.calendar.entry.model.RepeatFrequency
import com.mwk.kota.DateUtils
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Locale

private val ControlHeight = 48.dp
private val NumberFieldWidth = 64.dp
private val DateFieldWidth = 170.dp


@Composable
fun CustomReminderScreenContent(
    state: RepeatState,
    modifier: Modifier = Modifier,
    minEndDateMillis: Long? = null,
    onDismissClick: () -> Unit = {},
    onSaveButtonClick: () -> Unit = {},
    onChangeInterval: (Int?) -> Unit = {},
    onFrequencySelected: (RepeatFrequency) -> Unit = {},
    onRepeatEndTypeChanged: (RepeatEndType) -> Unit = {},
    onEndDateSelected: (Long?) -> Unit = {},
    onDayOfWeekSelected: (DayOfWeek) -> Unit = {},
    onMaxOccurrencesChanged: (Int?) -> Unit = {}
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {

        RepeatOptions(
            state = state,
            onChangeInterval = onChangeInterval,
            onFrequencySelected = onFrequencySelected,
        )

        DaysOfTheWeek(
            state = state,
            onDayOfWeekSelected = onDayOfWeekSelected
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        EndsAtBlock(
            state = state,
            modifier = modifier,
            minEndDateMillis = minEndDateMillis,
            onRepeatEndTypeChanged = onRepeatEndTypeChanged,
            onEndDateSelected = onEndDateSelected,
            onMaxOccurrencesChanged = onMaxOccurrencesChanged
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(R.string.entry_dialog_cancel))
            }
            Button(
                onClick = onSaveButtonClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(text = stringResource(R.string.entry_dialog_confirm))
            }
        }
    }
}

@Composable
fun RepeatOptions(
    state: RepeatState,
    modifier: Modifier = Modifier,
    onChangeInterval: (Int?) -> Unit = {},
    onFrequencySelected: (RepeatFrequency) -> Unit = {},
) {
    val frequencyItems = listOf(
        stringResource(R.string.entry_repeat_never),
        stringResource(R.string.entry_repeat_every_day),
        stringResource(R.string.entry_repeat_every_week),
        stringResource(R.string.entry_repeat_every_month),
        stringResource(R.string.entry_repeat_every_year),
    )

    val selectedItemIndex = when (state.frequency) {
        RepeatFrequency.NEVER -> 0
        RepeatFrequency.DAILY -> 1
        RepeatFrequency.WEEKLY -> 2
        RepeatFrequency.MONTHLY -> 3
        RepeatFrequency.YEARLY -> 4
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.repeat_at),
            modifier = Modifier.padding(end = 12.dp)
        )
        CompactNumberField(
            modifier = Modifier
                .width(NumberFieldWidth)
                .height(ControlHeight),
            value = state.interval.toString(),
            onValueChange = { value ->
                toIntOrNullFixedRange(value, onChangeInterval)
            },
        )
        Row(
            modifier = Modifier
                .padding(start = 12.dp)
                .height(ControlHeight)
                .wrapContentWidth()
                .border(
                    width = 0.4.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DropDownList(
                label = frequencyItems[selectedItemIndex],
                selectedIndex = selectedItemIndex,
                items = frequencyItems,
                onItemSelected = {
                    onFrequencySelected(
                        when (it) {
                            0 -> RepeatFrequency.NEVER
                            1 -> RepeatFrequency.DAILY
                            2 -> RepeatFrequency.WEEKLY
                            3 -> RepeatFrequency.MONTHLY
                            4 -> RepeatFrequency.YEARLY
                            else -> RepeatFrequency.NEVER
                        }
                    )
                }
            )
        }
    }
}


@Composable
fun DaysOfTheWeek(
    state: RepeatState,
    modifier: Modifier = Modifier,
    onDayOfWeekSelected: (DayOfWeek) -> Unit = {},
) {

    val locale = Locale.getDefault()

    val daysNames =
        DayOfWeek.entries.map { day -> Pair(day, day.getDisplayName(TextStyle.NARROW, locale)) }

    if (state.frequency == RepeatFrequency.WEEKLY) {

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            daysNames.forEach { (day, displayName) ->
                FilterChip(
                    selected = state.daysOfWeek.contains(day),
                    modifier = Modifier.padding(horizontal = 2.dp),
                    label = {
                        Text(displayName)
                    },
                    onClick = {
                        onDayOfWeekSelected(day)
                    },
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    }
}


@Composable
fun EndsAtBlock(
    state: RepeatState,
    modifier: Modifier = Modifier,
    minEndDateMillis: Long? = null,
    onRepeatEndTypeChanged: (RepeatEndType) -> Unit = {},
    onEndDateSelected: (Long?) -> Unit = {},
    onMaxOccurrencesChanged: (Int?) -> Unit = {}
) {

    var showDatePickerDialog by remember { mutableStateOf(false) }
    val minSelectableDateMillis = remember(minEndDateMillis) {
        val todayStartMillis = DateUtils.getStartOfDayMillis(LocalDate.now(), ZoneOffset.UTC)
        val minEndStartMillis = minEndDateMillis
            ?.let { DateUtils.getStartOfDayMillis(it, ZoneOffset.UTC) }
            ?: todayStartMillis
        maxOf(todayStartMillis, minEndStartMillis)
    }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= minSelectableDateMillis
            }
        }
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.ends_at),
            modifier = Modifier.padding(start = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = state.endType == RepeatEndType.NEVER,
                onClick = { onRepeatEndTypeChanged(RepeatEndType.NEVER) },
            )
            Text(
                text = stringResource(R.string.never),
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = state.endType == RepeatEndType.UNTIL_DATE,
                onClick = { onRepeatEndTypeChanged(RepeatEndType.UNTIL_DATE) },
            )
            Text(
                text = stringResource(R.string.after),
                modifier = Modifier.width(44.dp)
            )
            CompactReadOnlyField(
                modifier = Modifier
                    .width(DateFieldWidth)
                    .height(ControlHeight),
                value = state.endDateToDisplayDate(),
                trailingIcon = {
                    IconButton(onClick = { showDatePickerDialog = true }) {
                        Text(stringResource(R.string.entry_calendar_icon))
                    }
                }
            )

            if (showDatePickerDialog) {
                DatePickerDialog(
                    onDismissRequest = { showDatePickerDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onEndDateSelected(datePickerState.selectedDateMillis)
                                onRepeatEndTypeChanged(RepeatEndType.UNTIL_DATE)
                                showDatePickerDialog = false
                            }
                        ) {
                            Text(stringResource(R.string.entry_dialog_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePickerDialog = false }) {
                            Text(stringResource(R.string.entry_dialog_cancel))
                        }
                    },
                    modifier = modifier
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(
                selected = state.endType == RepeatEndType.AFTER_COUNT,
                onClick = { onRepeatEndTypeChanged(RepeatEndType.AFTER_COUNT) },
            )
            Text(
                text = stringResource(R.string.after),
                modifier = Modifier.width(44.dp)
            )
            CompactNumberField(
                modifier = Modifier
                    .width(NumberFieldWidth)
                    .height(ControlHeight),
                value = state.maxOccurrences?.toString().orEmpty(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = {
                    toIntOrNullFixedRange(it, onMaxOccurrencesChanged)
                },
            )
            Text(
                text = stringResource(R.string.times),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}


@Preview
@Composable
fun CustomReminderScreenContentPreview() {
    CustomReminderScreenContent(
        state = RepeatState(frequency = RepeatFrequency.WEEKLY)
    )
}

@Composable
private fun CompactNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .border(
                width = 0.4.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.medium
            ),
        singleLine = true,
        textStyle = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                innerTextField()
            }
        }
    )
}

@Composable
private fun CompactReadOnlyField(
    value: String,
    trailingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.border(
            width = 0.4.dp,
            color = MaterialTheme.colorScheme.secondary,
            shape = MaterialTheme.shapes.medium
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1
        )
        trailingIcon()
    }
}


private fun toIntOrNullFixedRange(value: String, call: (Int?) -> Unit) {
    if (value.all(Char::isDigit)) {
        val number = value.toIntOrNull()
        if (number in 1..99) {
            call(number)
        }
    }
}
