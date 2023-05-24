package com.hromnik.contactgroups.components

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.hromnik.contactgroups.R
import com.hromnik.contactgroups.listeners.DialogListener
import com.hromnik.contactgroups.models.Group
import com.hromnik.contactgroups.services.GroupsService

class GroupDialog(private val group: Group? = null) : DialogFragment() {

    private lateinit var listener: DialogListener
    private lateinit var textInput: EditText

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = parentFragment as DialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement GroupDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        var dialogTitle = context.getString(R.string.group_dialog_title_add)
        val groupsService = GroupsService(context)
        textInput = EditText(context)
        if (!group?.name.isNullOrEmpty()) {
            textInput.setText(group?.name)
        }

        if (group != null) {
            dialogTitle = context.getString(R.string.group_dialog_title_edit)
        }
        textInput.hint = "Write group name..."
        val layout = LinearLayout(context)
        val layoutPaddingSide = 50
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(layoutPaddingSide, 30, layoutPaddingSide, 0)
        layout.addView(textInput) // displays the user input bar

        return AlertDialog.Builder(context).setTitle(dialogTitle)
            .setView(layout)
            .setPositiveButton("OK") { dialog, _ ->
                val groupName = textInput.text.trim().toString()
                val existingGroupMessage = "Group with name \"$groupName\" already exists, choose a different name."
                if (groupName.isEmpty()) {
                    textInput.error = "Please enter a group name"
                    showAlert("Name cannot be empty")
                } else {
                    val existingGroup = groupsService.getGroupByName(groupName ?: "")
                    val groupExists = existingGroup != null && existingGroup.name.equals(groupName, ignoreCase = true)
                    // if for group list
                    if (group == null && groupExists) {
                        dialog.dismiss()
                        showAlert(existingGroupMessage)
                    // if for group detail
                    } else if (groupExists && existingGroup?.id != group?.id) {
                        dialog.dismiss()
                        showAlert(existingGroupMessage)
                    } else {
                        if (group?.id != null) {
                            group?.name = groupName
                            groupsService.editGroup(group)
                        } else {
                            groupsService.saveGroup(groupName, mutableListOf())
                        }
                        dialog.dismiss()
                        listener.onSaveDialog()
                    }
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Handle negative button click
            }.create()
    }

    private fun showAlert(message: String) {
        if (parentFragmentManager != null && isAdded) {
            AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("OK") { _, _ ->
                    listener.onReOpenDialog()
                }
                .show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
    }
}