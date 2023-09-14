# PushNotification

    fun getDefaultNotificationSoundUri(context: Context): Uri {
        var defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI

        // Check if the default URI is null, and if so, fallback to the system default ringtone
        if (defaultUri == null) {
            defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        return defaultUri
    }
