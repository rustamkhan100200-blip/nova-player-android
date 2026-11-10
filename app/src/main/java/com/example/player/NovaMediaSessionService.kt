package com.example.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.R
import com.google.common.collect.ImmutableList

/**
 * Native Android MediaSessionService supporting background media playback,
 * lock-screen controls, and system media notifications for Nova Player.
 */
@OptIn(UnstableApi::class)
class NovaMediaSessionService : MediaSessionService() {

    companion object {
        const val CHANNEL_ID = "nova_player_playback_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val notificationProvider = NovaNotificationProvider(this)
        setMediaNotificationProvider(notificationProvider)

        NovaPlaybackManager.activeMediaSession?.let { session ->
            addSession(session)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Nova Player Playback"
            val descriptionText = "Media playback and lock-screen controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        val session = NovaPlaybackManager.activeMediaSession ?: return null
        if (!sessions.contains(session)) {
            addSession(session)
        }
        return session
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NovaPlaybackManager.activeMediaSession?.let { session ->
            if (!sessions.contains(session)) {
                addSession(session)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = NovaPlaybackManager.activeMediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        NovaPlaybackManager.activeMediaSession?.let { session ->
            removeSession(session)
        }
        super.onDestroy()
    }

    private class NovaNotificationProvider(
        private val context: Context
    ) : DefaultMediaNotificationProvider(
        context,
        NotificationIdProvider { NOTIFICATION_ID },
        CHANNEL_ID,
        R.string.app_name
    ) {
        init {
            setSmallIcon(R.drawable.ic_notification)
        }

        override fun getNotificationContentTitle(metadata: MediaMetadata): CharSequence {
            return metadata.title?.takeIf { it.isNotBlank() }
                ?: metadata.displayTitle?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.app_name)
        }

        override fun getNotificationContentText(metadata: MediaMetadata): CharSequence {
            return metadata.artist?.takeIf { it.isNotBlank() }
                ?: metadata.albumArtist?.takeIf { it.isNotBlank() }
                ?: ""
        }

        override fun addNotificationActions(
            mediaSession: MediaSession,
            mediaButtons: ImmutableList<CommandButton>,
            builder: NotificationCompat.Builder,
            actionFactory: MediaNotification.ActionFactory
        ): IntArray {
            // Ensure notification uses standard system surface and visibility without forcing white card tinting
            builder.setColorized(false)
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            val compactIndices = super.addNotificationActions(mediaSession, mediaButtons, builder, actionFactory)
            // If the provider returned compact indices, use them; otherwise ensure at least up to 3 actions show in compact view
            return if (compactIndices.isNotEmpty()) {
                compactIndices
            } else {
                val count = mediaButtons.size.coerceAtMost(3)
                IntArray(count) { it }
            }
        }
    }
}
