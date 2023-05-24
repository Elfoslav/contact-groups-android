package com.hromnik.contactgroups.models

data class Contact(
    val id: String,
    val name: String,
    val photo: String?,
    var emails: List<String>?,
    var phoneNumbers: List<String>?,
    var isChecked: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Contact) {
            return false
        }
        return this.id == other.id && this.name == other.name
    }

    override fun hashCode(): Int {
        var result = id.toInt()
        result = 31 * result + name.hashCode()
        return result
    }
}