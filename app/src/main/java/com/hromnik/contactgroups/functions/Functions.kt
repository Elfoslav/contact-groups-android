package com.hromnik.contactgroups.functions

import android.view.View
import com.google.android.material.snackbar.Snackbar

object Functions {
    fun showNotification(
        view: View,
        message: String,
        showButtons: Boolean,
        buttons: List<String>?,
        callback: ((String) -> Unit)? = null)
    {
        val notification = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        if (showButtons && buttons != null) {
            buttons.forEach {
                val buttonName = it
                notification.setAction(it, View.OnClickListener {
                    // Do something when the action is clicked
                    callback?.invoke(buttonName)
                })
            }
        }
        notification.show()
    }
}