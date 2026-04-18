package com.mwk.calendar.main.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mwk.calendar.R
import com.mwk.calendar.main.MainCalendarState
import com.mwk.calendar.main.MainViewModel
import com.mwk.calendar.navigation.DayScreen
import com.mwk.calendar.navigation.EntryScreenDestination
import java.time.LocalDate

@Composable
fun MainScreen(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    MainScreenContent(
        state = state.value,
        modifier = modifier,
        onDayClick = { date ->
            navHostController.navigate(
                DayScreen(
                    year = date.year,
                    month = date.monthValue,
                    day = date.dayOfMonth,
                )
            )
        },
        onVisibleMonthChanged = viewModel::onVisibleMonthChanged,
        onNewEntryButtonClick = {
            navHostController.navigate(EntryScreenDestination())
        },
    )
}


@Composable
fun MainScreenContent(
    state: MainCalendarState,
    onNewEntryButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDayClick: (LocalDate) -> Unit = {},
    onVisibleMonthChanged: (java.time.YearMonth) -> Unit = {},
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        MainCalendarMonthBlock(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onDayClick = onDayClick,
            onVisibleMonthChanged = onVisibleMonthChanged,
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom,
        ) {
            FloatingActionButton(
                onClick = {
                    onNewEntryButtonClick()
                },
                modifier = Modifier
                    .padding(24.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.add_24px),
                    contentDescription = stringResource(R.string.new_entry),
                )
            }
        }
    }
}

@Preview
@Composable
fun MainScreenContentPreview() {
    MainScreenContent(
        state = MainCalendarState(),
        onNewEntryButtonClick = {},
        modifier = Modifier.fillMaxSize(),
        onDayClick = {}
    )
}
