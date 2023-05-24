package com.hromnik.contactgroups

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hromnik.contactgroups.adapters.GroupsAdapter
import com.hromnik.contactgroups.components.GroupDialog
import com.hromnik.contactgroups.constants.Constants
import com.hromnik.contactgroups.services.GroupsService
import com.hromnik.contactgroups.services.NotificationsService


open class BaseGroupsFragment : Fragment() {

    protected lateinit var groupsAdapter: GroupsAdapter
    protected lateinit var groupsService: GroupsService
    protected lateinit var notificationsService: NotificationsService
    protected lateinit var groupDialog: GroupDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupsAdapter = GroupsAdapter(emptyList(), findNavController())
        groupsService = GroupsService(requireContext())
        notificationsService = NotificationsService(requireContext())
        groupDialog = GroupDialog()
    }

    protected fun setupGroupDialog() {
        groupDialog.show(childFragmentManager, Constants.GROUP_DIALOG_TAG)
    }
}