package com.hromnik.contactgroups.views

import com.hromnik.contactgroups.models.Contact

interface ContactsFragmentInterface {
    fun updateSelectedCount(count: Int)
    fun updateSelectable(selectable: Boolean)
    fun updateContact(position: Int, contact: Contact)
    fun updateContacts(contacts: List<Contact>)
    fun mergeContacts(contacts: List<Contact>)
    fun showNotification(message: String,
                         showButtons: Boolean,
                         buttons: List<String>? = null,
                         callback: ((String) -> Unit)? = null
    )
    fun animateDeleteButton()

    fun invalidateMenu()
}