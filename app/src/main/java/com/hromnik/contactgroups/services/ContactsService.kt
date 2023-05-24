package com.hromnik.contactgroups.services

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.database.getStringOrNull
import com.hromnik.contactgroups.models.Contact

class ContactsService(private val context: Context) {

    private val sortOrder = "${ContactsContract.Contacts.DISPLAY_NAME} ASC"

    fun getContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val names = HashSet<String>()

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_URI
        )

        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getStringOrNull(it.getColumnIndex(ContactsContract.Contacts._ID))
                val name = it.getStringOrNull(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                val photo = it.getStringOrNull(it.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))

                if (!names.contains(name)) {
                    if (name != null) {
                        names.add(name)
                    }

                    if (id != null && name != null) {
                        contacts.add(Contact(id, name, photo, null, null, false))
                    }
                }
            }
        }

        return contacts
    }

    fun getContactById(id: String): Contact? {
        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.PHOTO_URI
            ),
            "${ContactsContract.Contacts._ID} = ?",
            arrayOf(id),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val name = it.getStringOrNull(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                val photo = it.getStringOrNull(it.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
                val emails = getEmails(id)
                val phoneNumbers = getPhoneNumbers(id)

                if (name != null) {
                    return Contact(id, name, photo, emails, phoneNumbers, false)
                }
            }
        }

        return null
    }

    fun getContactsByIds(ids: List<String>): List<Contact> {
        val contacts = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_URI
        )

        val selection = "${ContactsContract.Contacts._ID} IN (${ids.joinToString(",")})"

        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val photoIndex = it.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)

            while (it.moveToNext()) {
                val id = it.getStringOrNull(idIndex) ?: ""
                val name = it.getStringOrNull(nameIndex)
                val photo = it.getStringOrNull(photoIndex)
                val emails = emptyList<String>()
                val phoneNumbers = emptyList<String>()

                if (name != null && id != null) {
                    contacts.add(Contact(id, name, photo, emails, phoneNumbers, false))
                }
            }
        }

        return contacts
    }

    private fun getEmails(contactId: String): List<String> {
        val emails = mutableListOf<String>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )
        cursor?.use {
            while (it.moveToNext()) {
                val email = it.getStringOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
                email?.let { emailAddress ->
                    emails.add(emailAddress)
                }
            }
        }
        return emails
    }

    private fun getPhoneNumbers(id: String): List<String> {
        val phoneNumbers = LinkedHashSet<String>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(id),
            null
        )
        cursor?.use {
            while (it.moveToNext()) {
                val phoneNumber = it.getStringOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                if (phoneNumber != null) phoneNumbers.add(phoneNumber)
            }
        }
        return filterNumbers(phoneNumbers.toList())
    }

    private fun filterNumbers(phoneNumbers: List<String>): List<String> {
        val uniqueNumbers = mutableSetOf<String>()
        val formattedNumbers = mutableSetOf<String>()

        for (phoneNumber in phoneNumbers) {
            val formattedNumber = phoneNumber.replace("[^+0-9]".toRegex(), "")
            if (formattedNumber !in formattedNumbers) {
                uniqueNumbers.add(phoneNumber)
                formattedNumbers.add(formattedNumber)
            }
        }

        return uniqueNumbers.toList()
    }
}