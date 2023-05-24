package com.hromnik.contactgroups.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.hromnik.contactgroups.R
import com.hromnik.contactgroups.models.Group

class GroupsAdapter(private var groups: List<Group>, private val navController: NavController) : RecyclerView.Adapter<GroupsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_groups_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]
        holder.groupButton.text = group.name
        holder.groupButton.setOnClickListener {
            navController.navigate(R.id.action_Groups_to_Group, bundleOf("groupId" to group.id))
        }
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    fun setGroups(groups: List<Group>) {
        this.groups = groups
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupButton: Button = view.findViewById(R.id.group_button)
    }
}