package com.mwk.kota

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mwk.calendar.compose.LabelWithClickable
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LabelWithClickableUiTest {


    @get:Rule
    val composeRule = createComposeRule()


    @Test
    fun labelWithClickable_showsTexts_andCallsClickCallback() {
        var clickCount = 0

        composeRule.setContent {
            MaterialTheme {
                LabelWithClickable(
                    title = "Повторять",
                    value = "не повторять",
                    onClick = {
                        clickCount += 1
                    }
                )
            }
        }

        composeRule.onNodeWithText("Повторять").assertIsDisplayed()

        composeRule.onNodeWithText("не повторять").assertIsDisplayed()

        composeRule.onNodeWithText("Повторять").performClick()

        assertEquals(1, clickCount)
    }


    @Test
    fun labelWithClickable_showsIcon() {
        composeRule.setContent {
            MaterialTheme {
                LabelWithClickable(
                    title = "Уведомлять",
                    value = "Не уведомлять"
                )
            }
        }

        composeRule.onNodeWithContentDescription("Открыть").assertExists()
    }
}
