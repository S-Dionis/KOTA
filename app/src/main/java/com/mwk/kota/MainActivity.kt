package com.mwk.kota

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mwk.calendar.entry.ui.EntryScreen
import com.mwk.calendar.main.ui.MainScreen
import com.mwk.calendar.navigation.Calendar
import com.mwk.calendar.navigation.EntryScreenDestination
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import com.mwk.calendar.day.ui.DayScreen as DayScreenContent
import com.mwk.calendar.navigation.DayScreen as DayDestination

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun App() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Calendar(null, null)) {
        composable<Calendar> {
            Scaffold { innerPadding ->
                MainScreen(
                    navHostController = navController,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
        composable<DayDestination> { entry: NavBackStackEntry ->
            val destination = entry.toRoute<DayDestination>()

            Scaffold { innerPadding ->
                DayScreenContent(
                    date = LocalDate.of(
                        destination.year,
                        destination.month,
                        destination.day,
                    ),
                    navHostController = navController,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
        composable<EntryScreenDestination> { entry: NavBackStackEntry ->
            val destination = entry.toRoute<EntryScreenDestination>()

            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("Calendar") })
                }
            ) { innerPadding ->
                EntryScreen(
                    navController = navController,
                    entryId = destination.entryId,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}
