package com.mwk.calendar.main.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.ConfigurationCompat
import com.mwk.calendar.R
import com.mwk.calendar.compose.DropDownList
import com.mwk.calendar.main.MainCalendarState
import com.mwk.calendar.main.MainDayEntry
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private const val CALENDAR_PAGE_COUNT = 2000
private const val CALENDAR_START_PAGE = CALENDAR_PAGE_COUNT / 2
private const val DAYS_ON_SCREEN = 42
private const val WEEKS_ON_SCREEN = 7
private const val MAX_ENTRIES_IN_A_DAY_CELL = 5
private val YEAR_FIELD_WIDTH = 72.dp

@Composable
fun MainCalendarMonthBlock(
    state: MainCalendarState,
    modifier: Modifier = Modifier,
    onDayClick: (LocalDate) -> Unit,
    onVisibleMonthChanged: (YearMonth) -> Unit = {},
) {
    val pagerState = rememberPagerState(
        initialPage = monthToPage(
            startFromMonth = state.startFromMonth,
            currentOnScreenMonth = state.currentOnScreenMonth,
        ),
        pageCount = { CALENDAR_PAGE_COUNT }
    )
    val coroutineScope = rememberCoroutineScope()

    val currentMonth = remember(pagerState.currentPage, state.startFromMonth) {
        state.startFromMonth.plusMonths((pagerState.currentPage - CALENDAR_START_PAGE).toLong())
    }

    LaunchedEffect(state.currentOnScreenMonth) {
        val targetPage = monthToPage(
            startFromMonth = state.startFromMonth,
            currentOnScreenMonth = state.currentOnScreenMonth,
        )

        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState, state.startFromMonth) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { settledPage ->
                onVisibleMonthChanged(
                    state.startFromMonth.plusMonths((settledPage - CALENDAR_START_PAGE).toLong())
                )
            }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            CalendarMonthSelector(
                selectedMonth = currentMonth,
                onNowClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(CALENDAR_START_PAGE)
                    }
                },
                onMonthSelected = { targetMonth ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            monthToPage(
                                startFromMonth = state.startFromMonth,
                                currentOnScreenMonth = targetMonth,
                            )
                        )
                    }
                }
            )
        }

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
                    .padding(horizontal = 2.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                WeekDaysHeader()

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(0.dp),
                ) { page ->
                    val month =
                        state.startFromMonth.plusMonths((page - CALENDAR_START_PAGE).toLong())

                    MonthGrid(
                        month = month,
                        today = state.today,
                        entriesByDate = state.entriesByDate,
                        onDayClick = onDayClick,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarMonthSelector(
    selectedMonth: YearMonth,
    onNowClick: () -> Unit,
    onMonthSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val locale = remember(configuration) {
        ConfigurationCompat.getLocales(configuration).get(0) ?: Locale.getDefault()
    }
    var yearText by remember(selectedMonth.year) {
        mutableStateOf(selectedMonth.year.toString())
    }
    val monthNames = remember(locale) {
        (1..12).map { month ->
            YearMonth.of(selectedMonth.year, month)
                .month
                .getDisplayName(TextStyle.FULL_STANDALONE, locale)
                .replaceFirstChar { char -> char.titlecase(locale) }
        }
    }
    val nowLabel = stringResource(R.string.calendar_action_now)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = yearText,
                onValueChange = { value ->
                    if (value.length <= 4 && value.all(Char::isDigit)) {
                        yearText = value
                        value.toIntOrNull()?.let { year ->
                            if (year in 1..9999) {
                                onMonthSelected(
                                    YearMonth.of(
                                        year,
                                        selectedMonth.monthValue,
                                    )
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .width(YEAR_FIELD_WIDTH)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        innerTextField()
                    }
                }
            )

            DropDownList(
                label = monthNames[selectedMonth.monthValue - 1],
                items = monthNames,
                selectedIndex = selectedMonth.monthValue - 1,
                onItemSelected = { monthIndex ->
                    onMonthSelected(
                        YearMonth.of(
                            selectedMonth.year,
                            monthIndex + 1,
                        )
                    )
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = nowLabel,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onNowClick)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun WeekDaysHeader(
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val locale = remember(configuration) {
        ConfigurationCompat.getLocales(configuration).get(0) ?: Locale.getDefault()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        DayOfWeek.entries.forEach { dayOfWeek ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    entriesByDate: Map<LocalDate, List<MainDayEntry>>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val weeks = remember(month) { buildMonthWeeks(month) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        weeks.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                week.forEach { date ->
                    DayCell(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        date = date,
                        isCurrentMonth = YearMonth.from(date) == month,
                        isToday = date == today,
                        entries = entriesByDate[date].orEmpty(),
                        onClick = onDayClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    entries: List<MainDayEntry>,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isToday) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isToday) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val resolvedContentColor = if (isCurrentMonth) {
        contentColor
    } else {
        contentColor.copy(alpha = 0.72f)
    }

    val resolvedBorderColor = if (isCurrentMonth) {
        MaterialTheme.colorScheme.outlineVariant
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    }

    val resolvedBackgroundColor = if (isCurrentMonth || isToday) {
        backgroundColor
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(resolvedBackgroundColor)
            .border(
                width = 1.dp,
                color = resolvedBorderColor,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable {
                onClick(date)
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                color = resolvedContentColor,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            entries.take(MAX_ENTRIES_IN_A_DAY_CELL).forEach { entry ->
                DayEntryItem(entry = entry)
            }
        }
    }
}

@Composable
private fun DayEntryItem(
    entry: MainDayEntry,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (entry.isTask) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    }

    val contentColor = if (entry.isTask) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }

    Text(
        text = entry.title,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 3.dp, vertical = 1.dp),
        color = contentColor,
        fontSize = 8.sp,
        lineHeight = 8.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textDecoration = if (entry.isTask && !entry.isEnabled) {
            TextDecoration.LineThrough
        } else {
            TextDecoration.None
        },
    )
}

private fun buildMonthWeeks(month: YearMonth): List<List<LocalDate>> {
    val firstDayOfMonth = month.atDay(1)
    val daysBeforeMonth = firstDayOfMonth.dayOfWeek.value - 1
    val gridStartDate = firstDayOfMonth.minusDays(daysBeforeMonth.toLong())

    return List(DAYS_ON_SCREEN) { index ->
        gridStartDate.plusDays(index.toLong())
    }.chunked(WEEKS_ON_SCREEN)
}


private fun monthToPage(
    startFromMonth: YearMonth,
    currentOnScreenMonth: YearMonth,
): Int {
    val diff = ChronoUnit.MONTHS.between(startFromMonth, currentOnScreenMonth).toInt()
    val inRange = (CALENDAR_START_PAGE + diff).coerceIn(0, CALENDAR_PAGE_COUNT - 1)
    return inRange
}

@Preview(showBackground = true)
@Composable
private fun MainCalendarMonthBlockPreview() {
    MaterialTheme {
        MainCalendarMonthBlock(
            state = MainCalendarState(
                entriesByDate = mapOf(
                    LocalDate.now() to listOf(
                        MainDayEntry(title = "Event title", isTask = false, isEnabled = true),
                        MainDayEntry(title = "Task title", isTask = true, isEnabled = false),
                    )
                )
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            onDayClick = {}
        )
    }
}
