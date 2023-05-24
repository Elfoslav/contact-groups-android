package com.hromnik.contactgroups

import android.content.Context
import com.hromnik.contactgroups.models.Contact
import com.hromnik.contactgroups.models.Group
import com.hromnik.contactgroups.services.GroupsService
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

@RunWith(RobolectricTestRunner::class)
class GroupsServiceUnitTest {

    private lateinit var groupsService: GroupsService
    private lateinit var context: Context
    private lateinit var groupsJsonFile: File

    private val testGroups = listOf(
        Group("1", "Group 1", mutableListOf("1", "2", "3")),
        Group("2", "Group 2", mutableListOf("4", "5"))
    )

    private val testContacts = listOf(
        Contact("1", "John Doe", null,"", emptyList(), false),
        Contact("2", "Jane Doe", null,"", emptyList(), false),
        Contact("3", "Bob Smith", null,"", emptyList(), false),
        Contact("4", "Alice Smith", null,"", emptyList(), false),
        Contact("5", "Joe Johnson", null,"", emptyList(), false)
    )

    private fun getTestGroupsJson(): String {
        // TODO: replace this with the actual JSON data you want to use for the test
        return """
        [
          {
            "id": "1",
            "name": "Group 1",
            "contactIds": ["1", "2", "3"]
          },
          {
            "id": "2",
            "name": "Group 2",
            "contactIds": ["4", "5", "6"]
          }
        ]
    """
    }

    @Before
    fun setup() {
        context = mockk<Context>()
        every { context.cacheDir } returns File("src/test/assets")
        val inputStream: InputStream = ByteArrayInputStream(getTestGroupsJson().toByteArray())
        groupsJsonFile = File.createTempFile("groups", ".json", context.cacheDir)
        val outputStream = FileOutputStream(groupsJsonFile)
        val buffer = ByteArray(1024)
        var length = inputStream.read(buffer)
        while (length > 0) {
            outputStream.write(buffer, 0, length)
            length = inputStream.read(buffer)
        }
        outputStream.close()
        inputStream.close()

        every { context.getFileStreamPath("groups.json") } returns groupsJsonFile
        every { context.openFileInput("groups.json") } returns FileInputStream(groupsJsonFile)
        every { context.openFileOutput("groups.json", Context.MODE_PRIVATE) } returns FileOutputStream(groupsJsonFile)

        groupsService = GroupsService(context)
    }

    @After
    fun cleanup() {
        // Delete the temporary file
        if (groupsJsonFile.exists()) {
            groupsJsonFile.delete()
        }
    }

    @Test
    fun testSaveGroup() {
        val groupId = groupsService.saveGroup("New Group", mutableListOf("1", "2"))
        val newGroup = groupsService.getGroup(groupId)
        assertEquals("New Group", newGroup?.name)
        assertEquals(mutableListOf("1", "2"), newGroup?.contactIds)
    }

    @Test
    fun testEditGroup() {
        val group = testGroups[1].copy(name = "Edited Group", contactIds = mutableListOf("4", "5", "6"))
        groupsService.editGroup(group)
        val editedGroup = groupsService.getGroup("2")
        assertEquals("Edited Group", editedGroup?.name)
        assertEquals(mutableListOf("4", "5", "6"), editedGroup?.contactIds)
    }

    @Test
    fun testDeleteGroup() {
        groupsService.deleteGroup("1")
        val groups = groupsService.getGroups()
        assertEquals(1, groups.size)
        assertEquals("2", groups[0].id)
    }

    @Test
    fun testGetGroup() {
        val group = groupsService.getGroup("1")
        assertEquals("Group 1", group?.name)
        assertEquals(mutableListOf("1", "2", "3"), group?.contactIds)
    }

    @Test
    fun testGetGroupByName() {
        val group = groupsService.getGroupByName("group 2")
        assertEquals("Group 2", group?.name)
        assertEquals(mutableListOf("4", "5"), group?.contactIds)
    }

    @Test
    fun testGetGroups() {
        val groups = groupsService.getGroups()
        assertEquals(2, groups.size)
        assertEquals("1", groups[0].id)
        assertEquals("2", groups[1].id)
    }

    @Test
    fun testAddContacts() {
        groupsService.addContacts("2", testContacts)
        val group = groupsService.getGroup("2")
        assertEquals(mutableListOf("4", "5", "1", "2", "3"), group?.contactIds)
    }
}
