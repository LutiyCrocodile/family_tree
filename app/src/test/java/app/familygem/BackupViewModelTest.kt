package app.familygem

import android.app.Application
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.familygem.Settings.Tree
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit tests for BackupViewModel.
 */
@ExperimentalCoroutinesApi
class BackupViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockApplication: Application

    private lateinit var viewModel: BackupViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Initialize Global.settings with test data
        Global.settings = Settings()
        Global.settings.init()
        
        viewModel = BackupViewModel(mockApplication)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `displayFolder should show error when no URI is set`() {
        Global.settings.backupUri = BackupViewModel.NO_URI
        
        viewModel.displayFolder()
        
        val state = viewModel.mainState.value
        assertNotNull(state)
        assertTrue(state is MainState.Error)
        assertTrue((state as MainState.Error).exception is InvalidUriException)
    }

    @Test
    fun `displayFolder should show success when valid URI is set`() {
        val testUri = "content://com.android.externalstorage.documents/tree/primary%3ABackup"
        Global.settings.backupUri = testUri
        
        viewModel.displayFolder()
        
        val state = viewModel.mainState.value
        assertNotNull(state)
        assertTrue(state is MainState.Success)
        assertEquals(Uri.parse(testUri), (state as MainState.Success).uri)
    }

    @Test
    fun `setFolder should update settings and display folder`() {
        val testUri = Uri.parse("content://test/folder")
        
        viewModel.setFolder(testUri)
        
        assertEquals(testUri.toString(), Global.settings.backupUri)
        assertEquals(RecoverState.Loading, viewModel.recoverState.value)
    }

    @Test
    fun `listActualTrees should return error when no trees exist`() {
        Global.settings.trees.clear()
        
        viewModel.listActualTrees()
        
        val state = viewModel.saveState.value
        assertNotNull(state)
        assertTrue(state is SaveState.Error)
        assertEquals("No trees.", (state as SaveState.Error).message)
    }

    @Test
    fun `listActualTrees should return success with tree items`() {
        val tree1 = Tree(1, "Test Tree 1", 10, 3, "I1", null, null, 0)
        val tree2 = Tree(2, "Test Tree 2", 20, 4, "I1", null, null, 0)
        Global.settings.trees.add(tree1)
        Global.settings.trees.add(tree2)
        
        viewModel.listActualTrees()
        
        val state = viewModel.saveState.value
        assertNotNull(state)
        assertTrue(state is SaveState.Success)
        assertEquals(2, viewModel.treeItems.size)
        assertEquals("Test Tree 1", viewModel.treeItems[0].tree.title)
        assertEquals("Test Tree 2", viewModel.treeItems[1].tree.title)
    }

    @Test
    fun `selectTree should toggle backup flag`() {
        val tree = Tree(1, "Test Tree", 10, 3, "I1", null, null, 0)
        tree.backup = false
        Global.settings.trees.add(tree)
        viewModel.listActualTrees()
        
        val item = viewModel.treeItems[0]
        assertFalse(item.tree.backup)
        
        viewModel.selectTree(item)
        assertTrue(item.tree.backup)
        
        viewModel.selectTree(item)
        assertFalse(item.tree.backup)
    }

    @Test
    fun `updateState should enable backup when trees are selected`() {
        val tree = Tree(1, "Test Tree", 10, 3, "I1", null, null, 0)
        tree.backup = true
        Global.settings.trees.add(tree)
        viewModel.listActualTrees()
        
        viewModel.updateState()
        
        assertTrue(viewModel.canBackup.value == true)
    }

    @Test
    fun `updateState should disable backup when no trees are selected`() {
        val tree = Tree(1, "Test Tree", 10, 3, "I1", null, null, 0)
        tree.backup = false
        Global.settings.trees.add(tree)
        viewModel.listActualTrees()
        
        viewModel.updateState()
        
        assertFalse(viewModel.canBackup.value == true)
    }

    @Test
    fun `working should update loading state`() {
        viewModel.working(true)
        assertTrue(viewModel.loading.value == true)
        
        viewModel.working(false)
        assertFalse(viewModel.loading.value == true)
    }
}
