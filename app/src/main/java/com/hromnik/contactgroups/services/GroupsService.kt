package com.hromnik.contactgroups.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hromnik.contactgroups.models.Contact
import java.io.BufferedReader
import java.io.InputStreamReader
import com.hromnik.contactgroups.models.Group
import java.util.UUID

class GroupsService(private val context: Context) {

    private val gson = Gson()
    private val contactsService = ContactsService(context)

    private fun getGroupsFileName(): String {
        return "groups.json"
    }

    private fun saveGroups(groups: List<Group>) {
        val json = gson.toJson(groups.sortedBy { it.name.lowercase() })
        context.openFileOutput(getGroupsFileName(), Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }

    private fun readGroups(): List<Group> {
        val file = context.getFileStreamPath(getGroupsFileName())
        if (!file.exists()) {
            return emptyList()
        }

        val type = object : TypeToken<List<Group>>() {}.type
        BufferedReader(InputStreamReader(context.openFileInput(getGroupsFileName()))).use {
            return gson.fromJson(it, type)
        }
    }

    /**
     * Saves group into local storage
     * @param name String
     * @param contactIds List<String>
     * @return groupId (String)
     */
    fun saveGroup(name: String, contactIds: MutableList<String>): String {
        val groupId = UUID.randomUUID().toString()
        val group = Group(groupId, name, contactIds)
        val groups = readGroups().toMutableList()
        groups.add(group)
        saveGroups(groups)
        return groupId
    }

    /**
     * Edit and saves the group into local storage
     * @param group Group
     * @return groupId (String)
     */
    fun editGroup(group: Group): Group {
        val groups = readGroups().toMutableList()
        val index = groups.indexOfFirst { it.id == group.id }
        if (index != -1) {
            groups[index] = group
        }

        // Save groups in DB
        saveGroups(groups)
        return group
    }

    fun deleteGroup(id: String) {
        val groups = readGroups().toMutableList()
        val index = groups.indexOfFirst { it.id == id }
        groups.removeAt(index)
        saveGroups(groups)
    }

    fun getGroup(id: String): Group? {
        val groups = readGroups()
        return groups.find { it.id == id }
    }

    fun getGroupByName(name: String): Group? {
        val groups = readGroups()
        return groups.find { it.name.equals(name, ignoreCase = true) }
    }

    fun getGroups(): List<Group> {
        return readGroups().sortedBy { it.name.lowercase() }.toMutableList()
    }

    fun addContacts(groupId: String, contacts: List<Contact>) {
        val group = getGroup(groupId)
        var contactIds = contacts.map { it.id }
        val mergedContactIds = group?.contactIds?.union(contactIds)?.toMutableList()
        group?.contactIds = mergedContactIds ?: mutableListOf()
        if (group != null) {
            editGroup(group)
        }
    }

    fun removeGroupContacts(groupId: String, contactIds: MutableList<String>): Group? {
        val groups = readGroups().toMutableList()
        val group = getGroup(groupId)
        val index = groups.indexOfFirst { it.id == group?.id }
        if (group != null) {
            group.contactIds.removeAll(contactIds)
            groups[index] = group
        }
        saveGroups(groups)
        return group
    }

    fun getGroupContacts(id: String): List<Contact>? {
        val group = getGroup(id)
        return contactsService.getContactsByIds(group?.contactIds?.toList() ?: emptyList())
    }
}
