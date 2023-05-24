package com.hromnik.contactgroups.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hromnik.contactgroups.BaseGroupsFragment
import com.hromnik.contactgroups.R
import com.hromnik.contactgroups.constants.Constants
import com.hromnik.contactgroups.databinding.FragmentGroupsBinding
import com.hromnik.contactgroups.listeners.DialogListener
import com.hromnik.contactgroups.models.Group

class GroupsFragment() : BaseGroupsFragment(), DialogListener {

    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!
    private var groups = listOf<Group>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.groupsRecyclerView
        recyclerView.adapter = groupsAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        // Set the title of the Toolbar
        val toolbarTitle = requireActivity().findViewById<TextView>(R.id.toolbar_title)
        toolbarTitle?.text = getString(R.string.groups_label)

        groups = groupsService.getGroups()
        groupsAdapter.setGroups(groups)

        setupEmptyListLabel(groups)
        setupAddGroupButton()
        setupMenu()
    }

    private fun setupMenu() {
        val menuHost = requireActivity()
        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.search_menu, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem?.actionView as SearchView

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        // Perform search based on the submitted query
                        return true
                    }

                    override fun onQueryTextChange(text: String?): Boolean {
                        // Perform search based on the changed query text
                        val filteredGroups = groups.filter {
                            it.name.contains(text ?: "", ignoreCase = true)
                        }.toMutableList()
                        groupsAdapter.setGroups(filteredGroups)
                        groupsAdapter.notifyDataSetChanged()
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
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

    private fun setupEmptyListLabel(groups: List<Group>) {
        val emptyListLabel = view?.findViewById<TextView>(R.id.groups_list_empty_label)

        if (groups.isEmpty()) {
            emptyListLabel?.visibility = View.VISIBLE
        } else {
            emptyListLabel?.visibility = View.GONE
        }
    }

    private fun setupAddGroupButton() {
        binding.addGroupButton.floatingButton.setOnClickListener { _ ->
            setupGroupDialog()
        }
    }

    override fun onReOpenDialog() {
        groupDialog.show(childFragmentManager, Constants.GROUP_DIALOG_TAG)
    }

    override fun onSaveDialog() {
        groups = groupsService.getGroups()
        setupEmptyListLabel(groups)
        groupsAdapter.setGroups(groups)
        groupsAdapter.notifyDataSetChanged()
    }
}