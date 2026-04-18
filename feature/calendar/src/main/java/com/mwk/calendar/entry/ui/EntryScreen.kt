@file:OptIn(ExperimentalMaterial3Api::class)

package com.mwk.calendar.entry.ui

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mwk.calendar.R
import com.mwk.calendar.compose.DateTimePickerDialog
import com.mwk.calendar.compose.LabelWithClickable
import com.mwk.calendar.compose.PicassoImage
import com.mwk.calendar.compose.PicsumImagePickerDialog
import com.mwk.calendar.entry.EntryScreenState
import com.mwk.calendar.entry.EntryUiEvent
import com.mwk.calendar.entry.EntryViewModel
import com.mwk.calendar.entry.EventState
import com.mwk.calendar.entry.ImageCacheManager
import com.mwk.calendar.entry.NotifyState
import com.mwk.calendar.entry.NotifyTimeUtils
import com.mwk.calendar.entry.NotifyType
import com.mwk.calendar.entry.model.FilterType
import com.mwk.kota.DateUtils
import kotlinx.coroutines.launch

@Composable
fun EntryScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    entryId: Long? = null,
    viewModel: EntryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val is24Hour = rememberSaveable { DateFormat.is24HourFormat(context) }
    val coroutineScope = rememberCoroutineScope()
    var showImagePickerDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(entryId) {
        viewModel.loadEntry(entryId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EntryUiEvent.SaveSuccess -> navController.popBackStack()
            }
        }
    }

    EntryScreenContent(
        state = state,
        is24Hour = is24Hour,
        modifier = modifier,
        onNameChange = viewModel::onNameChange,
        onStartDateSelected = viewModel::onStartDateChange,
        onEndDateSelected = viewModel::onEndDateChange,
        onFilterSelected = viewModel::onFilterChange,
        onNotifyOptionSelected = viewModel::onNotifyOptionSelected,
        onSaveButtonClick = viewModel::onSaveClick,
        onDetailsChanged = viewModel::onDescriptionChanged,
        onImagePickClick = { showImagePickerDialog = true },
    )

    if (showImagePickerDialog) {
        PicsumImagePickerDialog(
            onDismiss = { showImagePickerDialog = false },
            onSave = { imageUrl ->
                coroutineScope.launch {
                    val imagePath = ImageCacheManager.saveImageToCache(context, imageUrl)
                    viewModel.onImageChanged(imagePath)
                    showImagePickerDialog = false
                }
            },
        )
    }
}

@Composable
fun EntryScreenContent(
    state: EntryScreenState,
    is24Hour: Boolean,
    modifier: Modifier = Modifier,
    onNameChange: (String) -> Unit = {},
    onStartDateSelected: (Long?) -> Unit = {},
    onEndDateSelected: (Long?) -> Unit = {},
    onFilterSelected: (FilterType) -> Unit = {},
    onNotifyOptionSelected: (NotifyState) -> Unit = {},
    onSaveButtonClick: () -> Unit = {},
    onDetailsChanged: (String) -> Unit = {},
    onImagePickClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showNotifyDialog by rememberSaveable { mutableStateOf(false) }
    var notifyDraft by remember { mutableStateOf(state.notify) }

    LaunchedEffect(state.notify) {
        notifyDraft = state.notify
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(bottom = 96.dp),
        ) {
            EventNameContent(
                state = state,
                onNameChange = onNameChange,
            )

            FilterChips(
                modifier = Modifier.padding(top = 16.dp),
                onFilterSelected = onFilterSelected,
                selectedFilter = state.event.filterType,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            DateTimePicker(
                modifier = Modifier.padding(bottom = 16.dp),
                is24Hour = is24Hour,
                label = stringResource(R.string.entry_start_datetime_label),
                selectedDateTimeMillis = state.event.startTimeMillis,
                minDateTimeMillis = System.currentTimeMillis(),
                onDateTimeSelected = onStartDateSelected,
            )

            if (state.event.filterType == FilterType.Event) {
                DateTimePicker(
                    modifier = Modifier.padding(bottom = 16.dp),
                    is24Hour = is24Hour,
                    label = stringResource(R.string.entry_end_datetime_label),
                    selectedDateTimeMillis = state.event.endTimeMillis,
                    minDateTimeMillis = state.event.startTimeMillis ?: System.currentTimeMillis(),
                    onDateTimeSelected = onEndDateSelected,
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            LabelWithClickable(
                value = notifyText(
                    notifyState = state.notify,
                    is24Hour = is24Hour
                ),
                title = stringResource(R.string.entry_remind_label),
                onClick = {
                    notifyDraft = if (state.notify.type == NotifyType.NONE) {
                        buildNotifyState(
                            type = NotifyType.ONE_HOUR,
                            startTimeMillis = state.event.startTimeMillis,
                            customDateTimeMillis = state.notify.customDateTimeMillis,
                            isEnabled = state.notify.isEnabled,
                        )
                    } else {
                        state.notify
                    }
                    showNotifyDialog = true
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            //todo если успею сделать повторение
            val notImplementetStr = stringResource(R.string.entry_not_implemented_yet)

            LabelWithClickable(
                value = stringResource(R.string.entry_repeat_never),
                title = stringResource(R.string.entry_repeat_label),
                onClick = {
                    Toast.makeText(
                        context,
                        notImplementetStr,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ImageSection(
                imagePath = state.event.image,
                onPickImageClick = onImagePickClick,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.event.description,
                onValueChange = onDetailsChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = { Text(stringResource(R.string.entry_description_placeholder)) },
                minLines = 5,
            )
        }

        if (showNotifyDialog) {
            NotifyDialog(
                state = notifyDraft,
                is24Hour = is24Hour,
                startTimeMillis = state.event.startTimeMillis,
                onDismissClick = {
                    notifyDraft = state.notify
                    showNotifyDialog = false
                },
                onSaveClick = {
                    onNotifyOptionSelected(notifyDraft)
                    showNotifyDialog = false
                },
                onStateChanged = { notifyDraft = it }
            )
        }

        Button(
            enabled = state.canSave,
            onClick = onSaveButtonClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(stringResource(R.string.entry_save_button))
        }
    }
}

@Composable
fun EventNameContent(
    state: EntryScreenState,
    onNameChange: (String) -> Unit,
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.4.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.medium
            ),
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.titleLarge,
        value = state.event.eventName,
        label = {
            Text(
                text = stringResource(R.string.entry_name_label),
                color = MaterialTheme.colorScheme.secondary,
            )
        },
        onValueChange = onNameChange,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
        ),
    )
}

@Composable
fun FilterChips(
    modifier: Modifier = Modifier,
    onFilterSelected: (FilterType) -> Unit = {},
    selectedFilter: FilterType,
) {
    Row(modifier = Modifier) {
        FilterChip(
            selected = selectedFilter == FilterType.Event,
            modifier = modifier.weight(1f),
            onClick = { onFilterSelected(FilterType.Event) },
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp,
                bottomStart = 4.dp
            ),
            label = { Text(stringResource(R.string.entry_filter_event)) }
        )
        FilterChip(
            selected = selectedFilter == FilterType.Task,
            modifier = modifier.weight(1f),
            onClick = { onFilterSelected(FilterType.Task) },
            shape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 4.dp,
                bottomEnd = 4.dp,
                bottomStart = 0.dp
            ),
            label = { Text(stringResource(R.string.entry_filter_task)) }
        )
    }
}

@Composable
fun DateTimePicker(
    modifier: Modifier = Modifier,
    is24Hour: Boolean,
    label: String = stringResource(R.string.entry_start_datetime_label),
    selectedDateTimeMillis: Long? = null,
    minDateTimeMillis: Long? = null,
    onDateTimeSelected: (Long?) -> Unit = {}
) {
    var showDateTimePickerDialog by rememberSaveable { mutableStateOf(false) }

    val displayText = selectedDateTimeMillis?.let { millis ->
        if (is24Hour) {
            DateUtils.formatDateTime24HourMillis(millis)
        } else {
            DateUtils.formatDateTime12HourMillis(millis)
        }
    }.orEmpty()

    OutlinedTextField(
        value = displayText,
        onValueChange = {},
        modifier = modifier.fillMaxWidth(),
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showDateTimePickerDialog = true }) {
                Text(stringResource(R.string.entry_calendar_icon))
            }
        }
    )

    if (showDateTimePickerDialog) {
        DateTimePickerDialog(
            onDismissRequest = { showDateTimePickerDialog = false },
            is24Hour = is24Hour,
            initialDateTimeMillis = selectedDateTimeMillis,
            minDateTimeMillis = minDateTimeMillis,
            onDateTimeSelected = {
                onDateTimeSelected(it)
                showDateTimePickerDialog = false
            },
            modifier = modifier
        )
    }
}

@Composable
private fun NotifyDialog(
    state: NotifyState,
    is24Hour: Boolean,
    startTimeMillis: Long?,
    onDismissClick: () -> Unit,
    onSaveClick: () -> Unit,
    onStateChanged: (NotifyState) -> Unit,
) {
    var showCustomDateTimePicker by rememberSaveable { mutableStateOf(false) }

    BasicAlertDialog(
        onDismissRequest = onDismissClick,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.entry_remind_dialog_title),
                    style = MaterialTheme.typography.titleMedium
                )

                NotifyRadioItem(
                    text = stringResource(R.string.entry_remind_one_hour),
                    selected = state.type == NotifyType.ONE_HOUR,
                    onClick = {
                        onStateChanged(
                            buildNotifyState(
                                type = NotifyType.ONE_HOUR,
                                startTimeMillis = startTimeMillis,
                                customDateTimeMillis = state.customDateTimeMillis,
                                isEnabled = state.isEnabled,
                            )
                        )
                    }
                )

                NotifyRadioItem(
                    text = stringResource(R.string.entry_remind_one_day),
                    selected = state.type == NotifyType.ONE_DAY,
                    onClick = {
                        onStateChanged(
                            buildNotifyState(
                                type = NotifyType.ONE_DAY,
                                startTimeMillis = startTimeMillis,
                                customDateTimeMillis = state.customDateTimeMillis,
                                isEnabled = state.isEnabled,
                            )
                        )
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = state.type == NotifyType.CUSTOM,
                            onClick = {
                                onStateChanged(
                                    state.copy(
                                        type = NotifyType.CUSTOM,
                                        notifyTimes = state.customDateTimeMillis?.let(::listOf).orEmpty()
                                    )
                                )
                            }
                        )
                        Text(
                            text = stringResource(R.string.entry_remind_custom),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    CompactDateField(
                        value = state.customDateTimeMillis?.let {
                            if (is24Hour) {
                                DateUtils.formatDateTime24HourMillis(it)
                            } else {
                                DateUtils.formatDateTime12HourMillis(it)
                            }
                        }.orEmpty(),
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = {
                            onStateChanged(
                                state.copy(
                                    type = NotifyType.CUSTOM,
                                    notifyTimes = state.customDateTimeMillis?.let(::listOf).orEmpty()
                                )
                            )
                            showCustomDateTimePicker = true
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissClick) {
                        Text(stringResource(R.string.entry_dialog_cancel))
                    }
                    Button(
                        onClick = onSaveClick,
                        enabled = state.type != NotifyType.CUSTOM || state.customDateTimeMillis != null,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(stringResource(R.string.entry_dialog_confirm))
                    }
                }
            }
        }
    }

    if (showCustomDateTimePicker) {
        DateTimePickerDialog(
            onDismissRequest = { showCustomDateTimePicker = false },
            is24Hour = is24Hour,
            initialDateTimeMillis = state.customDateTimeMillis ?: startTimeMillis,
            onDateTimeSelected = { selectedMillis ->
                onStateChanged(
                    NotifyState(
                        type = NotifyType.CUSTOM,
                        notifyTimes = listOf(selectedMillis),
                        customDateTimeMillis = selectedMillis,
                        isEnabled = state.isEnabled,
                    )
                )
                showCustomDateTimePicker = false
            }
        )
    }
}

@Composable
private fun NotifyRadioItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun buildNotifyState(
    type: NotifyType,
    startTimeMillis: Long?,
    customDateTimeMillis: Long?,
    isEnabled: Boolean,
): NotifyState {
    val notifyMillis = NotifyTimeUtils.getNotifyTimeMillis(
        type = type,
        startTimeMillis = startTimeMillis,
        customDateTimeMillis = customDateTimeMillis,
    )

    return NotifyState(
        type = type,
        notifyTimes = notifyMillis?.let(::listOf).orEmpty(),
        customDateTimeMillis = customDateTimeMillis,
        isEnabled = isEnabled,
    )
}

@Composable
private fun notifyText(
    notifyState: NotifyState,
    is24Hour: Boolean,
): String {
    return when (notifyState.type) {
        NotifyType.ONE_HOUR -> stringResource(R.string.entry_remind_one_hour)
        NotifyType.ONE_DAY -> stringResource(R.string.entry_remind_one_day)
        NotifyType.CUSTOM -> notifyState.customDateTimeMillis?.let {
            if (is24Hour) {
                DateUtils.formatDateTime24HourMillis(it)
            } else {
                DateUtils.formatDateTime12HourMillis(it)
            }
        }.orEmpty()
        NotifyType.NONE -> stringResource(R.string.entry_remind_none)
    }
}

@Composable
private fun CompactDateField(
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .border(
                width = 0.4.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.medium
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1
        )
        IconButton(onClick = onClick) {
            Text(stringResource(R.string.entry_calendar_icon))
        }
    }
}

@Composable
private fun ImageSection(
    imagePath: String?,
    onPickImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        imagePath?.let { path ->
            PicassoImage(
                imagePath = path,
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )
        }

        Button(onClick = onPickImageClick) {
            Text(
                text = stringResource(
                    if (imagePath == null) {
                        R.string.entry_add_image
                    } else {
                        R.string.entry_change_image
                    }
                )
            )
        }
    }
}

@Preview
@Composable
fun EntryScreenContentPreview() {
    EntryScreenContent(
        state = EntryScreenState(event = EventState(filterType = FilterType.Event)),
        is24Hour = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun NotifyDialogPreview() {
    var draft by remember {
        mutableStateOf(
            NotifyState(
                type = NotifyType.ONE_HOUR,
                notifyTimes = emptyList(),
                customDateTimeMillis = System.currentTimeMillis(),
                isEnabled = true,
            )
        )
    }

    NotifyDialog(
        state = draft,
        is24Hour = true,
        startTimeMillis = System.currentTimeMillis() + 3_600_000L,
        onDismissClick = {},
        onSaveClick = {},
        onStateChanged = { draft = it },
    )
}
