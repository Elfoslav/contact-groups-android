package com.hromnik.contactgroups.models

data class Group(
    val id: String,
    var name: String,
    var contactIds: MutableList<String>
)