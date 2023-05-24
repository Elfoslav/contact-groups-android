package com.hromnik.contactgroups.views

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.GridView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.hromnik.contactgroups.BaseGroupsFragment
import com.hromnik.contactgroups.R
import com.hromnik.contactgroups.adapters.ContactsAdapter
import com.hromnik.contactgroups.components.GroupDialog
import com.hromnik.contactgroups.constants.Constants
import com.hromnik.contactgroups.databinding.FragmentGroupBinding
import com.hromnik.contactgroups.functions.Functions
import com.hromnik.contactgroups.listeners.DialogListener
import com.hromnik.contactgroups.models.Contact
import com.hromnik.contactgroups.models.Group


class GroupFragment : BaseGroupsFragment(), DialogListener, ContactsFragmentInterface {

    private var _binding: FragmentGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var gridViewContacts: GridView
    private lateinit var toolbarTitle: TextView
    private lateinit var menuHost: MenuHost
    private lateinit var menuProvider: MenuProvider
    private lateinit var view: View
    private var contacts = mutableListOf<Contact>()
    private var contactsToDelete = mutableListOf<Contact>()
    private var group: Group? = null
    private var selectable: Boolean = false
    private var selectedCount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupBinding.inflate(inflater, container, false)
        // setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(_view: View, savedInstanceState: Bundle?) {
        view = _view
        super.onViewCreated(view, savedInstanceState)
        setupMenu()

        // Setup group data
        group = getGroup()
        gridViewContacts = view.findViewById(R.id.gridViewContacts)
        toolbarTitle = requireActivity().findViewById(R.id.toolbar_title)
        // Reset selected count
        selectedCount = 0

        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                setupContacts()
            } else {
                // Handle the case where the permission is not granted
                // You can show a message or perform any other appropriate action
                showNotification(getString(R.string.app_permissions_denied_message), false)
            }
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            setupContacts()
        } else {
            // Request the permission
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        // Set the title of the Toolbar
        updateHeader(group?.name ?: "")
        groupDialog = GroupDialog(group)
        setupAddContactsToGroupButton()
        if (contacts.isNotEmpty()) {
            val notification = notificationsService.getNotificationByName(
                Constants.Notifications.Names.GROUP_DELETE_CONTACTS_INFO_MESSAGE
            )

            if (notification == null || !notification?.isHidden!!) {
                val buttons = listOf(Constants.Notifications.NOTIFICATION_BUTTON_DO_NOT_SHOW_AGAIN)
                val callback = fun(buttonClicked: String) {
                    if (buttonClicked == Constants.Notifications.NOTIFICATION_BUTTON_DO_NOT_SHOW_AGAIN) {
                        if (notification == null) {
                            notificationsService.saveNotification(
                                Constants.Notifications.Names.GROUP_DELETE_CONTACTS_INFO_MESSAGE,
                                true
                            )
                        } else {
                            notification.isHidden = true
                            notificationsService.editNotification(notification)
                        }
                    }
                }
                showNotification(
                    getString(R.string.group_delete_contacts_info_message),
                    true,
                    buttons,
                    callback
                )
            }
        }
    }

    override fun invalidateMenu() {
        menuHost.removeMenuProvider(menuProvider)
        setupMenu()
    }

    private fun setupMenu() {
        menuHost = requireActivity()
        menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.edit_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.edit_menu_button -> {
                        onReOpenDialog()
                        true
                    }

                    R.id.delete_menu_button -> {
                        if (selectable) {
                            onContactDelete()
                        } else {
                            onGroupDelete()
                        }
                        true
                    }
                    else -> false
                }
            }
        }
        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun onGroupDelete() {
        AlertDialog.Builder(context)
            .setMessage(R.string.group_confirm_delete_message)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                // Delete group
                groupsService.deleteGroup(group?.id ?: "")
                dialog.dismiss()
                // Remove group fragment from the navigation history so
                // the back button in toolbar does not go back to this group fragment
                findNavController().popBackStack(R.id.fragment_groups, false)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun onContactDelete() {
        AlertDialog.Builder(context)
            .setMessage(R.string.group_confirm_delete_contacts_message)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                val contactIdsToDelete = contactsToDelete.map { it.id }
                group = groupsService.removeGroupContacts(
                    group?.id ?: "",
                    contactIdsToDelete.toMutableList() ?: mutableListOf()
                )
                dialog.dismiss()
                setupContacts()
                selectedCount = 0
                updateSelectable(false)
                updateSelectedCount(selectedCount)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupAddContactsToGroupButton() {
        binding.addContactsToGroupButton.floatingButton.setOnClickListener { _ ->
            findNavController().navigate(
                R.id.action_Group_to_Contacts,
                bundleOf("selectable" to true, "groupId" to group?.id)
            )
        }
    }

    private fun setupContacts() {
        contacts = groupsService.getGroupContacts(group?.id ?: "")?.toMutableList() ?: mutableListOf<Contact>()
        if (contacts.isNotEmpty()) {
            val textNoContacts = requireActivity().findViewById<TextView>(R.id.group_text_no_contacts)
            val textAddContacts = requireActivity().findViewById<TextView>(R.id.group_text_add_contacts)
            textNoContacts.visibility = View.GONE
            textAddContacts.visibility = View.GONE
        }
        contactsAdapter = ContactsAdapter(requireContext(), contacts, this, false)
        gridViewContacts.adapter = contactsAdapter
    }

    private fun getGroup(): Group? {
        return groupsService.getGroup(arguments?.getString("groupId") ?: "")
    }

    override fun onReOpenDialog() {
        groupDialog.show(childFragmentManager, Constants.GROUP_DIALOG_TAG)
    }

    override fun onSaveDialog() {
        group = getGroup()
        toolbarTitle?.text = group?.name
    }

    override fun updateContact(position: Int, contact: Contact) {
        contacts[position] = contact
        if (contact.isChecked) {
            selectedCount++
            contactsToDelete.add(contact)
        } else {
            if (selectedCount > 0) {
                selectedCount--
            }
            contactsToDelete.remove(contact)
        }

        updateSelectedCount(selectedCount)
    }

    override fun updateContacts(contacts: List<Contact>) {
        contactsAdapter.notifyDataSetChanged()
    }

    override fun mergeContacts(_contacts: List<Contact>) {
        contacts = (contacts + _contacts).distinctBy { it.id }.toMutableList()
        contactsAdapter.notifyDataSetChanged()
    }

    override fun updateSelectedCount(count: Int) {
        selectedCount = count
        if (selectedCount >= 0 && selectable) {
            updateHeader("${getString(R.string.contacts_to_remove)} ($selectedCount)")
        } else {
            updateHeader(group?.name ?: "")
        }
    }

    override fun updateSelectable(_selectable: Boolean) {
        selectable = _selectable
        val editMenu = requireActivity().findViewById<TextView>(R.id.edit_menu_button)
        if (selectable) {
            editMenu.visibility = View.GONE
        } else {
            editMenu.visibility = View.VISIBLE
        }
    }

    override fun showNotification(
        message: String,
        showButtons: Boolean,
        buttons: List<String>?,
        callback: ((String) -> Unit)?
    ) {
        Functions.showNotification(view, message, showButtons, buttons, callback)
    }

    override fun animateDeleteButton() {
        val deleteBtn = requireActivity().findViewById<ActionMenuItemView>(R.id.delete_menu_button)
        val shakeAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        deleteBtn.startAnimation(shakeAnimation)
    }

    private fun updateHeader(text: String) {
        toolbarTitle?.text = text
    }
}