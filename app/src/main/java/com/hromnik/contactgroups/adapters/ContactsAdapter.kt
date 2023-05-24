package com.hromnik.contactgroups.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hromnik.contactgroups.models.Contact
import com.hromnik.contactgroups.R
import com.hromnik.contactgroups.services.ContactsService
import com.hromnik.contactgroups.views.ContactsFragmentInterface

class ContactsAdapter(
    private val context: Context,
    private var contacts: MutableList<Contact>,
    private val fragment: ContactsFragmentInterface?,
    private var selectable: Boolean
) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: ViewHolder
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_contacts_item, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        var contact = contacts[position]
        val contactsService = ContactsService(context)

        if (selectable) {
            if (contact.isChecked) {
                holder.photo.setImageResource(R.drawable.ic_checkbox_checked)
            } else {
                setContactPhoto(holder, contact)
            }
        } else {
            setContactPhoto(holder, contact)
        }

        view?.setOnLongClickListener {
            selectable = !selectable
            fragment?.updateSelectable(selectable)
            fragment?.updateSelectedCount(0)
            fragment?.invalidateMenu()
            if (selectable) {
                fragment?.showNotification(context.getString(R.string.contacts_you_can_select_contacts), false)
                fragment?.animateDeleteButton()
            } else {
                val selectedContacts = contacts.filter { it.isChecked }
                selectedContacts.forEach {
                    it.isChecked = false
                }
                fragment?.mergeContacts(selectedContacts)
            }
            true // Return true to indicate that the event has been handled
        }

        view?.setOnClickListener {
            val contactWithAllData = contactsService.getContactById((contact.id))
            contact.emails = contactWithAllData?.emails
            contact.phoneNumbers = contactWithAllData?.phoneNumbers
            setupSelectable(contact, holder, position)
            setupName(holder, contact)
        }

        setupName(holder, contact)
        // view?.findViewById<TextView>(R.id.textPhoneNumber)?.text = contact.phoneNumber

        return view!!
    }

    private fun setupName(holder: ViewHolder, contact: Contact) {
        holder.name.text = if (contact.name.length > 20) {
            contact.name.substring(0, 20) + "..."
        } else {
            contact.name
        }
    }

    private fun setupSelectable(contact: Contact, holder: ViewHolder, position: Int) {
        if (selectable) {
            if (contact.isChecked) {
                setContactPhoto(holder, contact)
                contact.isChecked = false
                fragment?.updateContact(position, contact)
            } else {
                holder.photo.setImageResource(R.drawable.ic_checkbox_checked)
                contact.isChecked = true
                fragment?.updateContact(position, contact)
            }
        } else {
            showContactDetails(contact)
        }
    }

    private fun showContactDetails(contact: Contact) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_contact_detail, null, false)
        val phoneNumbersLayout = dialogView.findViewById<LinearLayout>(R.id.phoneNumbersLayout)
        val emailsLayout = dialogView.findViewById<LinearLayout>(R.id.emailsLayout)
        phoneNumbersLayout.removeAllViews()

        contact.phoneNumbers?.forEachIndexed { index, phoneNumber ->
            val phoneNumberView = LayoutInflater.from(context).inflate(R.layout.layout_phone_number_item, null, false)
            val phoneNumberTextView: TextView = phoneNumberView.findViewById(R.id.phoneNumberTextView)
            val phoneNumberIcon: ImageView = phoneNumberView.findViewById(R.id.phoneIcon)
            val messageIcon: ImageView = phoneNumberView.findViewById(R.id.messageIcon)
            phoneNumberTextView.text = phoneNumber
            phoneNumberTextView.setTextColor(ContextCompat.getColor(context, R.color.black))
            phoneNumberTextView.setPadding(0, 0, 0, context.resources.getDimensionPixelSize(R.dimen.layout_spacing))
            phoneNumberTextView.setOnClickListener {
                // Trigger the call for the clicked phone number
                triggerCall(contact, index)
            }
            phoneNumberIcon.setOnClickListener {
                // Trigger the call for the clicked phone number
                triggerCall(contact, index)
            }
            messageIcon.setOnClickListener {
                triggerMessage(contact, index)
            }
            phoneNumbersLayout.addView(phoneNumberView)
        }

        contact.emails?.forEachIndexed { index, phoneNumber ->
            val emailAddressView = LayoutInflater.from(context).inflate(R.layout.layout_email_address_item, null, false)
            val emailAddressTextView: TextView = emailAddressView.findViewById(R.id.emailAddressTextView)
            emailAddressTextView.text = phoneNumber
            emailAddressTextView.setTextColor(ContextCompat.getColor(context, R.color.black))
            emailAddressTextView.setPadding(0, 0, 0, context.resources.getDimensionPixelSize(R.dimen.layout_spacing))
            emailAddressTextView.setOnClickListener {
                val email = contact.emails!![index]
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$email")
                }
                context.startActivity(intent)
            }
            emailsLayout.addView(emailAddressView)
        }

        dialogView.findViewById<TextView>(R.id.name).text = contact.name

        dialogView.findViewById<ImageView>(R.id.image).apply {
            if (!contact.photo.isNullOrEmpty()) {
                setImageURI(Uri.parse(contact.photo))
            }
        }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun triggerCall(contact: Contact, index: Int) {
        val clickedPhoneNumber = contact.phoneNumbers!![index]
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${clickedPhoneNumber}")
        context.startActivity(intent)
    }

    private fun triggerMessage(contact: Contact, index: Int) {
        val clickedPhoneNumber = contact.phoneNumbers!![index]
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("smsto:${clickedPhoneNumber}")
        context.startActivity(intent)
    }

    private fun setContactPhoto(holder: ViewHolder, contact: Contact) {
        if (contact.photo != null) {
            holder.photo.setImageURI(Uri.parse(contact.photo))
        } else {
            holder.photo.setImageResource(R.drawable.ic_person)
        }
    }

    override fun getCount(): Int {
        return contacts.size
    }

    override fun getItem(position: Int): Any {
        return contacts[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    public fun updateContact(contact: Contact) {
        val index = contacts.indexOfFirst { it.id == contact.id }
        if (index != -1) {
            contacts[index] = contact
        }
    }

    public fun setContacts(_contacts: MutableList<Contact>) {
        contacts = _contacts
    }

    private class ViewHolder(view: View) {
        val photo: ImageView = view.findViewById(R.id.imageView)
        val name: TextView = view.findViewById(R.id.textName)
    }
}
