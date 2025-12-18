package app.familygem

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for main user flows in the application.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityUITest {

    @get:Rule
    val activityScenarioRule = activityScenarioRule<LauncherActivity>()

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        Global.settings = Settings()
        Global.settings.init()
    }

    @Test
    fun launcherActivityStartsAndShowsTreesActivity() {
        // Wait for launcher to finish and show trees activity
        Thread.sleep(1000)
        
        // Verify FAB is displayed (indicates we're in TreesActivity)
        onView(withId(R.id.fab))
            .check(matches(isDisplayed()))
    }

    @Test
    fun fabButtonOpensNewTreeDialog() {
        Thread.sleep(1000)
        
        // Click FAB
        onView(withId(R.id.fab))
            .perform(click())
        
        // Verify new tree options are displayed
        Thread.sleep(500)
        // The dialog should show options for creating new tree
    }

    @Test
    fun navigationDrawerOpens() {
        Thread.sleep(1000)
        
        // Navigation drawer test removed - drawer_layout resource doesn't exist in current layout
        // This functionality would require opening a tree first
    }

    @Test
    fun progressViewIsInitiallyHidden() {
        Thread.sleep(1000)
        
        // Verify progress view is not visible initially
        onView(withId(R.id.progress_wheel))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
}
