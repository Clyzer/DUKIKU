package knf.kuma.widgets.emision

import android.content.Intent
import android.widget.RemoteViewsService
import androidx.core.app.NotificationCompat
import knf.kuma.R
import knf.kuma.commons.PrefsUtil
import knf.kuma.download.foreground

class WEmissionService : RemoteViewsService() {

    companion object {
        const val CHANNEL = "widget-service"
    }

    override fun onCreate() {
        foreground(5987, NotificationCompat.Builder(this, CHANNEL).apply {
            setSmallIcon(R.drawable.ic_service)
            priority = NotificationCompat.PRIORITY_MIN
            if (PrefsUtil.collapseDirectoryNotification)
                setSubText("Actualizando widget")
            else
                setContentTitle("Actualizando widget")
        }.build())
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        foreground(5987, NotificationCompat.Builder(this, CHANNEL).apply {
            setSmallIcon(R.drawable.ic_service)
            priority = NotificationCompat.PRIORITY_MIN
            if (PrefsUtil.collapseDirectoryNotification)
                setSubText("Actualizando widget")
            else
                setContentTitle("Actualizando widget")
        }.build())
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return WEListProvider(applicationContext)
    }
}
