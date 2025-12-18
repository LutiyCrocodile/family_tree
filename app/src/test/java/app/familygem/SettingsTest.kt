package app.familygem

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Settings class.
 */
class SettingsTest {

    private lateinit var settings: Settings

    @Before
    fun setup() {
        settings = Settings()
        settings.init()
    }

    @Test
    fun `init should set default values`() {
        assertEquals("start", settings.referrer)
        assertTrue(settings.trees.isEmpty())
        assertTrue(settings.autoSave)
        assertEquals("12:00", settings.notifyTime)
        assertTrue(settings.backup)
        assertEquals(BackupViewModel.NO_URI, settings.backupUri)
        assertNotNull(settings.diagram)
    }

    @Test
    fun `max should return highest tree ID`() {
        settings.trees.add(Settings.Tree(1, "Tree 1", 10, 3, "I1", null, null, 0))
        settings.trees.add(Settings.Tree(5, "Tree 5", 20, 4, "I1", null, null, 0))
        settings.trees.add(Settings.Tree(3, "Tree 3", 15, 3, "I1", null, null, 0))
        
        assertEquals(5, settings.max())
    }

    @Test
    fun `max should return 0 when no trees exist`() {
        assertEquals(0, settings.max())
    }

    @Test
    fun `addTree should add tree to list`() {
        val tree = Settings.Tree(1, "Test Tree", 10, 3, "I1", null, null, 0)
        
        settings.addTree(tree)
        
        assertEquals(1, settings.trees.size)
        assertEquals("Test Tree", settings.trees[0].title)
    }

    @Test
    fun `renameTree should update tree title`() {
        val tree = Settings.Tree(1, "Old Name", 10, 3, "I1", null, null, 0)
        settings.trees.add(tree)
        
        settings.renameTree(1, "New Name")
        
        assertEquals("New Name", settings.trees[0].title)
    }

    @Test
    fun `deleteTree should remove tree from list`() {
        val tree1 = Settings.Tree(1, "Tree 1", 10, 3, "I1", null, null, 0)
        val tree2 = Settings.Tree(2, "Tree 2", 20, 4, "I1", null, null, 0)
        settings.trees.add(tree1)
        settings.trees.add(tree2)
        
        settings.deleteTree(1)
        
        assertEquals(1, settings.trees.size)
        assertEquals("Tree 2", settings.trees[0].title)
    }

    @Test
    fun `getTree should return tree by ID`() {
        val tree1 = Settings.Tree(1, "Tree 1", 10, 3, "I1", null, null, 0)
        val tree2 = Settings.Tree(2, "Tree 2", 20, 4, "I1", null, null, 0)
        settings.trees.add(tree1)
        settings.trees.add(tree2)
        
        val result = settings.getTree(2)
        
        assertNotNull(result)
        assertEquals("Tree 2", result.title)
        assertEquals(2, result.id)
    }

    @Test
    fun `Tree should have correct default values`() {
        val tree = Settings.Tree(1, "Test", 10, 3, "I1", null, null, 0)
        
        assertEquals(1, tree.id)
        assertEquals("Test", tree.title)
        assertEquals(10, tree.persons)
        assertEquals(3, tree.generations)
        assertEquals("I1", tree.root)
        assertTrue(tree.backup)
        assertNotNull(tree.settings)
        assertNotNull(tree.birthdays)
    }

    @Test
    fun `Tree addShare should add share to list`() {
        val tree = Settings.Tree(1, "Test", 10, 3, "I1", null, null, 0)
        val share = Settings.Share("20251218120000", "SUB1")
        
        tree.addShare(share)
        
        assertNotNull(tree.shares)
        assertEquals(1, tree.shares!!.size)
        assertEquals("20251218120000", tree.shares!![0].dateId)
        assertEquals("SUB1", tree.shares!![0].submitter)
    }

    @Test
    fun `TreeSettings should have default values`() {
        val treeSettings = Settings.TreeSettings()
        
        assertEquals(110, treeSettings.lifeSpan)
        assertFalse(treeSettings.customDate)
        assertNull(treeSettings.fixedDate)
    }

    @Test
    fun `DiagramSettings should initialize with default values`() {
        val diagram = Settings.DiagramSettings().init()
        
        assertNotNull(diagram)
        assertEquals(3, diagram.ancestors)
        assertEquals(3, diagram.descendants)
        assertEquals(2, diagram.siblings)
        assertTrue(diagram.spouses)
    }
}
