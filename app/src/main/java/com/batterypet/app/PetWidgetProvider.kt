package com.batterypet.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Home-screen widget showing the current pet in its live mood plus battery %.
 */
class PetWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAll(context)
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = context.getSystemService(AppWidgetManager::class.java)
            val ids = manager.getAppWidgetIds(ComponentName(context, PetWidgetProvider::class.java))
            if (ids.isEmpty()) return

            val prefs = PetKit.prefs(context)
            val views = RemoteViews(context.packageName, R.layout.pet_widget)
            views.setImageViewResource(R.id.widget_pet, PetKit.moodDrawable(context))
            views.setTextViewText(R.id.widget_battery, "${prefs.getInt("last_level", 100)}%")

            val openApp = Intent(context, MainActivity::class.java)
            val pending = PendingIntent.getActivity(
                context,
                0,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pending)

            manager.updateAppWidget(ids, views)
        }
    }
}
