package com.example

import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessagingService

/**
 * Firebase Cloud Messaging (FCM) service for REVENEX School ERP.
 *
 * Responsibilities:
 *  - Receive push notifications from Firebase Cloud Messaging.
 *  - Handle foreground messages (onMessageReceived).
 *  - Handle background messages (onMessageReceived).
 *  - Refresh device tokens and expose them to the authenticated user.
 *
 * All notification delivery is handled by the system; this service only
 * observes and surfaces the payload. It never stores server credentials.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val tag = "MyFirebaseMessagingService"

    /**
     * Called when the device receives a push message.
     * Runs on the main thread; heavy work should be offloaded.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val from = remoteMessage.from
        val notification = remoteMessage.notification
        val data = remoteMessage.data

        Log.i(
            tag,
            "Received push message. from=$from title=${notification?.title} body=${notification?.body} data=$data"
        )

        // Surface the notification through the system notification tray.
        notification?.let {
            showNotification(
                title = it.title ?: "Revenex School ERP",
                body = it.body ?: "",
                channelId = CHANNEL_DEFAULT
            )
        }
    }

    /**
     * Called when the FCM token is refreshed.
     * The application should upload the new token to its backend
     * and associate it with the currently authenticated user.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(tag, "FCM token refreshed: ${token.take(8)}...")
        // TODO: upload token to backend and associate with authenticated user.
    }

    companion object {
        const val CHANNEL_DEFAULT = "revenex_erp_default"

        /**
         * Displays a system notification. This is a minimal implementation;
         * production code should use NotificationManager with a proper channel.
         */
        fun showNotification(title: String, body: String, channelId: String) {
            Log.d(
                "MyFirebaseMessagingService",
                "showNotification title=$title body=$body channelId=$channelId"
            )
            // Production code should build a NotificationCompat.Builder here.
        }
    }
}