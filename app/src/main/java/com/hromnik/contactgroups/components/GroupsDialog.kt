package com.hromnik.contactgroups.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hromnik.contactgroups.adapters.GroupsDialogAdapter
import com.hromnik.contactgroups.databinding.ComponentGroupsDialogBinding
import com.hromnik.contactgroups.models.Contact
import com.hromnik.contactgroups.models.Group
import com.hromnik.contactgroups.services.GroupsService

class GroupsDialog(private val checkedContacts: List<Contact>) : DialogFragment() {

    private var _binding: ComponentGroupsDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupsService: GroupsService
    private lateinit var groupsDialogAdapter: GroupsDialogAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ComponentGroupsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupsService = GroupsService(requireContext())
        groupsDialogAdapter = GroupsDialogAdapter(emptyList(), groupsService, checkedContacts, findNavController())
        recyclerView = binding.groupsRecyclerView
        recyclerView.adapter = groupsDialogAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        binding.closeButton.setOnClickListener {
            dialog?.dismiss()
        }

        val groups: List<Group> = groupsService.getGroups()
        groupsDialogAdapter.setGroups(groups)
    }

    override fun onStart() {
        super.onStart()

        // Retrieve the dialog's window object
        val window = dialog?.window

        // Create WindowManager.LayoutParams and assign it to the window attributes
        val layoutParams = WindowManager.LayoutParams().apply {
            copyFrom(window?.attributes)
            width = (resources.displayMetrics.widthPixels * 0.9).toInt() // Set width to 90% of the screen width
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }

        // Set the modified LayoutParams to the window
        window?.attributes = layoutParams
    }
}