package com.mwk.kota

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mwk.calendar.compose.DropDownList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class DropDownListUiTest {

    @get:Rule
    val composeRule = createComposeRule()


    @Test
    fun dropDownList_opensMenu_andReturnsSelectedIndex() {

        var selectedIndexFromCallback: Int? = null

        val items = listOf("Один", "Два", "Три")

        composeRule.setContent {
            MaterialTheme {
                DropDownList(
                    label = "Один",
                    selectedIndex = 0,
                    items = items,
                    onItemSelected = { newIndex ->
                        selectedIndexFromCallback = newIndex
                    }
                )
            }
        }

        composeRule.onNodeWithText("Один").assertIsDisplayed()

        composeRule.onNodeWithText("Один").performClick()

        composeRule.onNodeWithText("Два").assertIsDisplayed()

        composeRule.onNodeWithText("Два").performClick()

        assertEquals(1, selectedIndexFromCallback)
    }
}
