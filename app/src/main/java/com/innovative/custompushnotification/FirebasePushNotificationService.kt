package com.innovative.custompushnotification

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebasePushNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("onMessageReceived", "onMessageReceived: " + remoteMessage.data)
        Log.v(
            "new-message>>>>>>",
            "ORIGINAL MESSAGE DATA PAYLOAD NOTIFICATION==>" + remoteMessage.data
        )
        wakeUpScreen()
        takeAction(
            remoteMessage.notification?.title ?: "Notification",
            remoteMessage.notification?.body ?: "Message"
        )
    }

    private fun takeAction(title: String, msg: String) {
        if (isAppIsInBackground(applicationContext)) {
            notification(title, msg)
        } else {
            notification(title, msg)
        }
    }

    private fun playNotificationSound() {
        try {
            // val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val soundUri =
                Uri.parse("android.resource://" + this.packageName + "/" + R.raw.notification_sound)
            val r = RingtoneManager.getRingtone(this, soundUri)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    private fun wakeUpScreen() {
        val pm = this.getSystemService(POWER_SERVICE) as PowerManager
        val isScreenOn = pm.isScreenOn
        Log.e("screen on......", "" + isScreenOn)
        if (!isScreenOn) {
            val wl = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "MyLock"
            )
            wl.acquire(10000)
            val wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock")
            wl_cpu.acquire(10000)
        }
    }

    private fun notification(
        title: String?,
        msg: String?,
    ) {
        try {
            //playNotificationSound()
            var pendingIntent: PendingIntent? = null
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            pendingIntent = PendingIntent.getActivity(
                this,
                j,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val CHANNEL_ID = System.currentTimeMillis().toString() // The id of the channel.
            val name: CharSequence =
                getString(R.string.app_name) // The user-visible name of the channel.
            var importance = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                importance = NotificationManager.IMPORTANCE_HIGH
            }
            val soundUri =
                Uri.parse("android.resource://" + this.packageName + "/" + R.raw.sprrow)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            var mChannel: NotificationChannel? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mChannel = NotificationChannel(CHANNEL_ID, name, importance)
                mChannel.enableLights(true)
                mChannel.lightColor = Color.RED
                mChannel.setSound(soundUri, audioAttributes)
            }
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(
                applicationContext, CHANNEL_ID
            )
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg)
                .setAutoCancel(true)
                //.setSound(defaultSoundUri)
                //.setSound(soundUri,audioAttributes)
                //.setSound("android.resource://com.innovative.custompushnotification/"+R.raw.notification_sound)
                .setSound(Uri.parse("android.resource://com.innovative.custompushnotification/" + R.raw.sprrow), AudioManager.STREAM_NOTIFICATION)
                .setContentIntent(pendingIntent)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert(notificationManager != null)
                notificationManager.createNotificationChannel(mChannel!!)
            }
            notificationManager.notify(j, notificationBuilder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * This method is used to app is background
     */
    private fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo!!.packageName == context.packageName) {
                isInBackground = false
            }
        }
        return isInBackground
    }

    companion object {
        private const val j = 0
    }
}