package com.hromnik.contactgroups.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.hromnik.contactgroups.R
import com.hromnik.contactgroups.adapters.ContactsAdapter
import com.hromnik.contactgroups.components.GroupsDialog
import com.hromnik.contactgroups.databinding.FragmentContactsBinding
import com.hromnik.contactgroups.functions.Functions
import com.hromnik.contactgroups.models.Contact
import com.hromnik.contactgroups.services.ContactsService
import com.hromnik.contactgroups.services.GroupsService

class ContactsFragment : Fragment(), ContactsFragmentInterface {

    private lateinit var binding: FragmentContactsBinding
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var gridViewContacts: GridView
    private lateinit var groupsService: GroupsService
    private lateinit var menuHost: MenuHost
    private lateinit var menuProvider: MenuProvider
    private lateinit var view: View
    private var contacts = mutableListOf<Contact>()
    private var selectable: Boolean = false
    private var groupId: String = ""
    private var selectedCount: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentContactsBinding.inflate(inflater, container, false)
        selectable = arguments?.getBoolean("selectable") ?: false
        groupId = arguments?.getString("groupId") ?: ""
        groupsService = GroupsService(requireContext())

        return binding.root
    }

    override fun onViewCreated(_view: View, savedInstanceState: Bundle?) {
        view = _view
        super.onViewCreated(view, savedInstanceState)
        // Set the title of the Toolbar
        updateHeader(getString(R.string.contacts_label))
        setupMenu()

        gridViewContacts = view.findViewById(R.id.gridViewContacts)

        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                loadContacts()
            } else {
                // Handle the case where the permission is not granted
                // You can show a message or perform any other appropriate action
                showNotification(getString(R.string.app_permissions_denied_message), false)
            }
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            // Request the permission
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun loadContacts() {
        val contactsService = ContactsService(requireContext())
        val allContacts = contactsService.getContacts().toMutableList()

        if (groupId.isNotEmpty()) {
            val groupContacts = groupsService.getGroupContacts(groupId)!!.toMutableList()
            allContacts.removeAll(groupContacts)
        }

        contacts.addAll(allContacts)
        contactsAdapter = ContactsAdapter(requireContext(), contacts, this, selectable)
        gridViewContacts.adapter = contactsAdapter
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
                menuInflater.inflate(R.menu.search_menu, menu)
                if (selectable) {
                    menuInflater.inflate(R.menu.add_menu, menu)
                }

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem?.actionView as SearchView

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        // Perform search based on the submitted query
                        return true
                    }

                    override fun onQueryTextChange(text: String?): Boolean {
                        // Perform search based on the changed query text
                        val filteredContacts = contacts.filter {
                            it.name.contains(text ?: "", ignoreCase = true)
                        }.toMutableList()
                        contactsAdapter.setContacts(filteredContacts)
                        contactsAdapter.notifyDataSetChanged()
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.action_add -> {
                        val checkedContacts = contacts.filter { it.isChecked }

                        if (groupId.isEmpty()) {
                            // Show modal with groups
                            val dialogFragment = GroupsDialog(checkedContacts)
                            dialogFragment.show(childFragmentManager, "GroupsDialog")
                        } else {
                            groupsService.addContacts(groupId, checkedContacts)
                            // Remove contacts fragment from the navigation history so
                            // the back button in toolbar does not go back to this fragment
                            findNavController().popBackStack(R.id.fragment_contacts, true)
                        }

                        true
                    }

                    R.id.action_search -> {
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

    override fun updateContact(position: Int, contact: Contact) {
        contacts[position] = contact
        if (contact.isChecked) {
            selectedCount++
        } else {
            if (selectedCount > 0) {
                selectedCount--
            }
        }
        updateHeader("${getString(R.string.contacts_label)} ($selectedCount)")
    }

    override fun updateContacts(contacts: List<Contact>) {
        contactsAdapter.notifyDataSetChanged()
    }

    override fun mergeContacts(contacts: List<Contact>) {
        contactsAdapter.notifyDataSetChanged()
    }

    override fun updateSelectedCount(count: Int) {
        selectedCount = count
        if (selectedCount > 0) {
            updateHeader("${getString(R.string.contacts_to_remove)} ($selectedCount)")
        } else {
            updateHeader(getString(R.string.contacts_label))
        }
    }

    override fun updateSelectable(_selectable: Boolean) {
        selectable = _selectable
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
        // Not yet implemented in ContactsFragment
    }

    private fun updateHeader(text: String) {
        val toolbarTitle = requireActivity().findViewById<TextView>(R.id.toolbar_title)
        toolbarTitle?.text = text
    }
}
