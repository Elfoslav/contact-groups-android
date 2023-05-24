package com.hromnik.contactgroups.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import com.hromnik.contactgroups.models.Notification
import java.util.UUID

class NotificationsService(private val context: Context) {

    private val gson = Gson()

    private fun getNotificationsFileName(): String {
        return "notifications.json"
    }

    private fun saveNotifications(notifications: List<Notification>) {
        val json = gson.toJson(notifications)
        context.openFileOutput(getNotificationsFileName(), Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }

    private fun readNotifications(): List<Notification> {
        val file = context.getFileStreamPath(getNotificationsFileName())
        if (!file.exists()) {
            return emptyList()
        }

        val type = object : TypeToken<List<Notification>>() {}.type
        BufferedReader(InputStreamReader(context.openFileInput(getNotificationsFileName()))).use {
            return gson.fromJson(it, type)
        }
    }

    /**
     * Saves notification into local storage
     * @param name String
     * @param isHidden Boolean
     * @return notificationId (String)
     */
    fun saveNotification(name: String, isHidden: Boolean): String {
        val notificationId = UUID.randomUUID().toString()
        val notification = Notification(notificationId, name, isHidden)
        val notifications = readNotifications().toMutableList()
        notifications.add(notification)
        saveNotifications(notifications)
        return notificationId
    }

    /**
     * Edit and saves the notification into local storage
     * @param notification Notification
     * @return notificationId (String)
     */
    fun editNotification(notification: Notification): Notification {
        val notifications = readNotifications().toMutableList()
        val index = notifications.indexOfFirst { it.id == notification.id }
        if (index != -1) {
            notifications[index] = notification
        }

        // Save notifications in DB
        saveNotifications(notifications)
        return notification
    }

    fun deleteNotification(id: String) {
        val notifications = readNotifications().toMutableList()
        val index = notifications.indexOfFirst { it.id == id }
        notifications.removeAt(index)
        saveNotifications(notifications)
    }

    fun getNotification(id: String): Notification? {
        val notifications = readNotifications()
        return notifications.find { it.id == id }
    }

    fun getNotificationByName(name: String): Notification? {
        val notifications = readNotifications()
        return notifications.find { it.name.equals(name, ignoreCase = true) }
    }

    fun getNotifications(): List<Notification> {
        return readNotifications().toMutableList()
    }
}
