package com.mwk.calendar.day.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mwk.calendar.R
import com.mwk.calendar.compose.PicassoImage
import com.mwk.calendar.day.DayScreenState
import com.mwk.calendar.day.DayTaskUiState
import com.mwk.calendar.day.DayViewModel
import com.mwk.calendar.day.DayViewModelFactoryEntryPoint
import com.mwk.calendar.navigation.EntryScreenDestination
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DayScreen(
    date: LocalDate,
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dayViewModelFactory = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            DayViewModelFactoryEntryPoint::class.java,
        ).dayViewModelFactory()
    }
    val viewModel: DayViewModel = viewModel(
        factory = remember(date, dayViewModelFactory) {
            dayViewModelFactory.create(date)
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    DayScreenContent(
        state = state,
        modifier = modifier,
        onTaskCheckedChange = viewModel::onTaskCheckedChange,
        onTaskClick = { entryId ->
            navHostController.navigate(EntryScreenDestination(entryId))
        },
    )
}

@Composable
fun DayScreenContent(
    state: DayScreenState,
    modifier: Modifier = Modifier,
    onTaskCheckedChange: (Long, Boolean) -> Unit = { _, _ -> },
    onTaskClick: (Long) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        DayScreenHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            date = state.showingDate,
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.tasks.isEmpty()) {
                    EmptyTasksPlaceholder(
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = state.tasks,
                            key = DayTaskUiState::id,
                        ) { task ->
                            DayTaskCard(
                                task = task,
                                onCheckedChange = { isChecked ->
                                    onTaskCheckedChange(task.id, isChecked)
                                },
                                onClick = { onTaskClick(task.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayScreenHeader(
    date: LocalDate,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val locale = remember(configuration) {
        ConfigurationCompat.getLocales(configuration).get(0) ?: Locale.getDefault()
    }
    val monthName = remember(date, locale) {
        date.month
            .getDisplayName(TextStyle.FULL_STANDALONE, locale)
            .replaceFirstChar { char -> char.titlecase(locale) }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DayHeaderValueBlock(label = date.year.toString())
            DayHeaderValueBlock(label = monthName)
            DayHeaderValueBlock(label = date.dayOfMonth.toString())
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DayHeaderValueBlock(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun DayTaskCard(
    task: DayTaskUiState,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (task.isTask) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    }
    val contentColor = if (task.isTask) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }
    val textDecoration = if (task.isTask && !task.isEnabled) {
        TextDecoration.LineThrough
    } else {
        TextDecoration.None
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (task.isTask) {
                Checkbox(
                    checked = !task.isEnabled,
                    onCheckedChange = onCheckedChange,
                )
            }

            task.imagePath?.let { imagePath ->
                PicassoImage(
                    imagePath = imagePath,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = textDecoration,
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(backgroundColor)
                        .border(
                            width = 1.dp,
                            color = backgroundColor,
                            shape = RoundedCornerShape(6.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${task.startTime} - ${task.endTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        textDecoration = textDecoration,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTasksPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(18.dp),
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.day_screen_empty_tasks),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DayScreenContentPreview() {
    MaterialTheme {
        DayScreenContent(
            state = DayScreenState(
                showingDate = LocalDate.of(2026, 4, 8),
                tasks = listOf(
                    DayTaskUiState(
                        id = 1L,
                        title = "Task",
                        startTime = "08:00",
                        endTime = "09:00",
                        isTask = true,
                        isEnabled = false,
                    )
                ),
            )
        )
    }
}
