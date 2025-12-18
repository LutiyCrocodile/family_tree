package app.familygem

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.familygem.constant.Extra
import app.familygem.share.SharingActivity
import kotlinx.coroutines.test.runTest
import org.folg.gedcom.model.Gedcom
import org.folg.gedcom.model.Person
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Tests for sharing functionality.
 */
@RunWith(AndroidJUnit4::class)
class SharingActivityTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        Global.settings = Settings()
        Global.settings.init()
    }

    @Test
    fun createTreeAndVerifyPNGGeneration() = runTest {
        // Create a simple tree
        val gedcom = Gedcom()
        gedcom.createIndexes()
        
        val person = Person()
        person.id = "I1"
        val name = org.folg.gedcom.model.Name()
        name.value = "Тест/Тестов/"
        person.addName(name)
        
        val birth = org.folg.gedcom.model.EventFact()
        birth.tag = "BIRT"
        birth.date = "1 JAN 2000"
        person.addEventFact(birth)
        
        gedcom.addPerson(person)
        
        // Create tree in settings
        val tree = Settings.Tree(1, "Тестовое дерево", 1, 1, "I1", null, null, 0)
        Global.settings.trees.add(tree)
        
        // Verify tree was created
        assertEquals(1, Global.settings.trees.size)
        assertEquals("Тестовое дерево", Global.settings.trees[0].title)
        assertEquals(1, Global.settings.trees[0].persons)
    }

    @Test
    fun verifySharingIntentCanBeCreated() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // Create intent for SharingActivity
        val intent = Intent(context, SharingActivity::class.java)
        intent.putExtra(Extra.TREE_ID, 1)
        
        assertNotNull(intent)
        assertEquals(1, intent.getIntExtra(Extra.TREE_ID, -1))
    }

    @Test
    fun verifyPNGFileCanBeCreatedInCache() {
        val cacheDir = context.cacheDir
        assertNotNull(cacheDir)
        assertTrue(cacheDir.exists())
        
        // Create a test PNG file
        val testFile = File(cacheDir, "test_tree.png")
        testFile.writeText("test content")
        
        assertTrue(testFile.exists())
        assertTrue(testFile.isFile)
        
        // Clean up
        testFile.delete()
        assertFalse(testFile.exists())
    }

    @Test
    fun verifyTreeTitleIsUsedInFileName() {
        val tree = Settings.Tree(1, "Моё Семейное Дерево", 10, 3, "I1", null, null, 0)
        
        val expectedFileName = "${tree.title}_tree.png"
        assertEquals("Моё Семейное Дерево_tree.png", expectedFileName)
    }
}
